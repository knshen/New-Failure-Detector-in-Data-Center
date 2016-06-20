package relatedWork.gossip;

import java.io.*;
import java.util.*;

import crash.MessageLossMaker;
import evaluation.Evaluator;
import parser.DumpAnalyzer;
import parser.Packet;
import util.Pair;
import util.TimePeriod;

public class GossipDetector {
	DumpAnalyzer da = null;
	Map<Integer, Double> crashes = new HashMap<Integer, Double>();

	// parameters
	public static final double end_time = 60.0;
	public static final double time_unit = 0.0001;
	public static final double t_gossip = 0.1;
	public static final double t_fail = 0.5; ////////////
	public static final double factor = 0.5; //////////////
	//public static final double t_clean = 2;

	/**
	 * node 12 receive msg: [(time, node), (time, node)...] 
	 * node 13 receive msg: [(time, node), (time, node)...] 
	 * ... 
	 * node 131 receive msg: [(time, node),(time, node)...]
	 */
	public Map<Integer, List<Pair<Double, Integer>>> msgs = new HashMap<Integer, List<Pair<Double, Integer>>>();
	public List<LiveList> heartbeat_status = new ArrayList<LiveList>();

	public GossipDetector(String dump_dir, boolean haveCrash, String crash_path) throws IOException {
		da = new DumpAnalyzer(dump_dir);
		for (int i = 0; i < da.num_nodes; i++)
			heartbeat_status.add(new LiveList(i, da.num_nodes));

		for (int i = 12; i <= 131; i++)
			msgs.put(i, new ArrayList<Pair<Double, Integer>>());
		
		if(haveCrash)
			readCrashFile(crash_path);
	}

	private List<Packet> readDumpFile(String path) throws IOException {
		List<Packet> list = new ArrayList<Packet>();

		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = br.readLine();

		while (line != null) {
			String data[] = line.split(" ");

			Packet pkt = new Packet();
			pkt.time = Double.parseDouble(data[0]);
			pkt.src = data[2].substring(0, data[2].indexOf(".", 7));
			pkt.dest = data[4].substring(0, data[4].indexOf(".", 7));
			pkt.length = Integer.parseInt(data[7]);

			list.add(pkt);
			line = br.readLine();
		}

		br.close();

		return list;
	}

	public void getGossipMsgData(String mark) throws IOException {
		File files[] = new File(da.dir).listFiles();
		for (File file : files) {
			String file_name = file.getName();
			String check[] = file_name.split("-");
			if (check[0].equals(mark) && Integer.parseInt(check[1]) >= 12) {
				int server_id = Integer.parseInt(check[1]);
				List<Packet> list = readDumpFile(file.getAbsolutePath());
				for (Packet pkt : list) {
					if (pkt.src.equals(da.id2ip.get(server_id)))
						// send
						continue;
					if (pkt.dest.equals(da.id2ip.get(server_id))) {
						int src_id = da.ip2id.get(pkt.src);
						msgs.get(server_id).add(new Pair(pkt.time, src_id));
					}
				}
			}
		}

		// message sort by time
		for (Map.Entry<Integer, List<Pair<Double, Integer>>> entry : msgs
				.entrySet()) {
			Collections.sort(entry.getValue());
			/*
			if(entry.getKey() == 12)
				for(Pair<Double, Integer> p : entry.getValue())
					System.out.println(p.first + " " + p.second);
			*/
		}
			

	}

	public void readCrashFile(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = "";
		while ((line = br.readLine()) != null) {
			int id = Integer.parseInt(line.split(" ")[0]);
			double time = Double.parseDouble(line.split(" ")[1]);
			crashes.put(id, time);
		}
		br.close();
	}

	public Map<Integer, List<TimePeriod>> detect(boolean haveLoss, double loss_rate) throws IOException {
		Map<Integer, List<TimePeriod>> alerts = new HashMap<Integer, List<TimePeriod>>();
		for(int i = 12; i < da.num_nodes; i++)
			alerts.put(i, new ArrayList<TimePeriod>());
		
		getGossipMsgData("gossip");
		
		if(haveLoss) {
			MessageLossMaker.make(msgs, 2, end_time, loss_rate);
		}
		
		// update heartbeat_status repeatedly
		for (double time = 2.0; time <= end_time; time += time_unit) {
			for (int i = 12; i < da.num_nodes; i++) {
				// i: lcoal_id
				int index = findRecentMsg(i, time);
				if (index == -1 || isCrashNow(i, time))
					// no new message or crashed already
					continue;
								
				// update self
				heartbeat_status.get(i).last_time.set(i, time);
				// have new message
				int from_id = msgs.get(i).get(index).second;
				// merge last_time list
				merge(i, from_id);
				// update status
				for (int j = 12; j < da.num_nodes; j++) {
					if (i == j)
						continue;
					if ((time - heartbeat_status.get(i).last_time.get(j)) >= t_fail)
						heartbeat_status.get(i).isFail.set(j, true);
					else
						heartbeat_status.get(i).isFail.set(j, false);

				}

			} // finish update status at time
			
			// report possible crash
			for (int node = 12; node < da.num_nodes; node++) {
				// node: node in checking
				int num_fails = 0;
				for(int j = 12; j < da.num_nodes; j++) {
					if (!isCrashNow(j, time) && heartbeat_status.get(j).isFail.get(node) == true)
						num_fails++;
				}
				
				if(num_fails >= factor * (da.num_nodes - 12))	{
					//System.out.println("server" + node + " is reported crash at time " + time);
					List<TimePeriod> list = alerts.get(node);
					if(list.size() == 0)
						list.add(new TimePeriod(time, time));
					else {
						TimePeriod tp = list.get(list.size()-1);
						if((time - tp.end) >= 1.1 * time_unit)
							list.add(new TimePeriod(time, time));
						else
							list.get(list.size()-1).end = time;
					}
					
				}
					
			}
		} // end time for

		return alerts;
	}

	private boolean isCrashNow(int id, double now) {
		if (crashes.containsKey(id) && crashes.get(id) <= now)
			return true;
		return false;
	}

	private void merge(int local_id, int from_id) {
		LiveList l1 = heartbeat_status.get(local_id);
		LiveList l2 = heartbeat_status.get(from_id);

		for (int i = 12; i < da.num_nodes; i++) {
			if (i == local_id)
				continue;
			if (l2.last_time.get(i) > l1.last_time.get(i))
				l1.last_time.set(i, l2.last_time.get(i));

		}
	}

	/**
	 * get the index of the most recent message of receiver until now_time
	 * 
	 * @param receiver
	 * @param now_time
	 * @return
	 */
	private int findRecentMsg(int receiver, double now_time) {
		List<Pair<Double, Integer>> list = msgs.get(receiver);
		int index = -1;

		for (int i = 0; i < list.size(); i++) {
			Pair<Double, Integer> pair = list.get(i);

			if (pair.first > now_time)
				break;
			index = i;
		}
		return index;
	}

	public static void main(String[] args) throws IOException {
		//// is there exist server crash
		long a = System.currentTimeMillis();
		GossipDetector gd = new GossipDetector(
				"z://serverCrashDump//gossip-1//", 
				true,
				"z://crashFile//server-crash-1.txt");
		
		//// have message loss? loss rate?
		Map<Integer, List<TimePeriod>> alerts = gd.detect(false, 0.0001);
		for(Map.Entry<Integer, List<TimePeriod>> entry : alerts.entrySet()) {
			if(entry.getValue().size() > 0)
				System.out.println(entry.getKey() + "  " + entry.getValue().size());	
		}
		
		System.out.println("avg query accurate pro: " + Evaluator.avgQueryAccuracyPro(alerts, gd.crashes));
		System.out.println("avg mistake rate: " + Evaluator.faultCrashReportRate(alerts, gd.crashes));
		System.out.println("avg detection time: " + Evaluator.avgDetectionTime(alerts, gd.crashes));

		long b = System.currentTimeMillis();
		System.out.println("runtime: " + (b-a)/1000 + "s");
	}

}
