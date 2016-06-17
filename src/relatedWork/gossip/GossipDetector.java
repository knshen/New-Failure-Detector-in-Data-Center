package relatedWork.gossip;

import java.io.*;
import java.util.*;

import parser.DumpAnalyzer;
import parser.Packet;
import util.Pair;

public class GossipDetector {
	DumpAnalyzer da = null;
	Map<Integer, Double> crashes = new HashMap<Integer, Double>();

	// parameters
	public static final double end = 60.0;
	public static final double time_unit = 0.0001;
	public static final double t_gossip = 0.1;
	public static final double t_fail = 1;
	public static final double t_clean = 2;

	/**
	 * node 12 receive msg: [(time, node), (time, node)...] node 13 receive msg:
	 * [(time, node), (time, node)...] ... node 131 receive msg: [(time, node),
	 * (time, node)...]
	 */
	public Map<Integer, List<Pair<Double, Integer>>> msgs = new HashMap<Integer, List<Pair<Double, Integer>>>();
	public List<LiveList> heartbeat_status = new ArrayList<LiveList>();

	public GossipDetector(String dump_dir) throws IOException {
		da = new DumpAnalyzer(dump_dir);
		for (int i = 0; i < da.num_nodes; i++)
			heartbeat_status.add(new LiveList(i, da.num_nodes));

		for (int i = 12; i <= 131; i++)
			msgs.put(i, new ArrayList<Pair<Double, Integer>>());
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

	public void detect() {
		// update heartbeat_status repeatedly
		for (double time = 2.0; time <= end; time += time_unit) {
			for (int i = 12; i < da.num_nodes; i++) {
				// i: lcoal_id
				int index = findRecentMsg(i, time);
				if (index == -1
						|| (crashes.containsKey(i) && crashes.get(i) <= time))
					// no new message or crashed already
					continue;
				
				//if(i == 12)
				//	System.out.println();
				
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

					if ((time - heartbeat_status.get(i).last_time.get(j)) >= t_clean)
						heartbeat_status.get(i).isClean.set(j, true);
				}

				// report possible crash
				for (int j = 12; j < da.num_nodes; j++) {
					if (i == j)
						continue;
					
					//if (heartbeat_status.get(i).isFail.get(j) == true)
					//	System.out.println("#node " + i
					//			+ " report crash of #node " + j + " at time: "
					//			+ time);
					if(heartbeat_status.get(i).isClean.get(j) == true)
						System.err.println("#node " + i
								+ " clean up #node " + j + " at time: "
								+ time);
					
				}
			}

		}

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
		GossipDetector gd = new GossipDetector("z://gossip1//");
		gd.readCrashFile("server-crash-1min.txt");

		gd.getGossipMsgData("gossip");
		gd.detect();
		
		for(LiveList ll : gd.heartbeat_status) {
			System.out.println(ll.last_time);
		}
		System.out.println();
	}

}
