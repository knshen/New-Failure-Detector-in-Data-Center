package core;

import java.util.*;
import java.io.*;

import crash.MessageLossMaker;
import evaluation.Evaluator;
import parser.DumpAnalyzer;
import util.Pair;
import util.TimePeriod;
import util.Util;

public class ServerFailureDetector {

	// parameters
	public static final int win_size = 100;
	public static final double end_time = 60.0;
	public static final double fail_threshold = 0.001;
	public static final double interval = 0.1;
	public static final double time_unit = 0.0001;

	public Map<Integer, Double> crashes = new HashMap<Integer, Double>();
	public DumpAnalyzer alr = null;

	public ServerFailureDetector(String dump_dir, boolean haveCrash, String crash_path) throws IOException {
		alr = new DumpAnalyzer(dump_dir);
		if(haveCrash)
			////////
			readCrashFile(crash_path);
	}

	private int distance(int id1, int id2) {
		int rackId1 = (id1 - 12) / 20;
		int rackId2 = (id2 - 12) / 20;
		if (rackId1 == rackId2)
			return 0;
		if ((rackId1 >= 0 && rackId1 <= 2 && rackId2 >= 0 && rackId2 <= 2)
				|| (rackId1 >= 3 && rackId1 <= 5 && rackId2 >= 3 && rackId2 <= 5))
			return 1;
		return 2;
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

	public Map<Integer, List<TimePeriod>> detect(int K, boolean haveLoss, List<Pair<Integer, Integer>> relations, double loss_rate) throws IOException {
		/**
		 * i: time periods that report crash of node i 
		 */
		Map<Integer, List<TimePeriod>> final_report = new HashMap<Integer, List<TimePeriod>>();
		FD fd = new FD();	
		alr.getServerPackets("topo");
		
		///////////// enable message loss?
		if(haveLoss) {
			MessageLossMaker.make(alr, 2, end_time, relations, loss_rate);
		}

		for (int i = 12; i <= 131; i++) {
			// i: slave (monitorable object)
			// crash report for the server i
			List<TimePeriod> crash_report = new ArrayList<TimePeriod>();
			
			List<List<TimePeriod>> ress = new ArrayList<List<TimePeriod>>();
			for(int master : alr.send_rule.get(i)) {
				List<TimePeriod> res = fd.chenDetect(alr.hb.get(master).get(i), win_size, 2, end_time, interval, fail_threshold);
				ress.add(res);
			}
			
			for(double time = 2; time <= end_time; time += time_unit) {
				int alerts = 0;
				for(int k = 0; k < ress.size(); k++) {
					// k = 0,1,2
					int master_id = alr.send_rule.get(i).get(k);
					if(!isCrashNow(master_id, time) && Util.isInPeriod(ress.get(k), time)) {
						//System.out.println(master_id + " report crash of " + i + " at time: " + time);
						alerts++;
					}
						
				}
				
				if(alerts >= (K+1) / 2) {
					// server i is decided to be crash !!!
					if(crash_report.size() == 0)
						crash_report.add(new TimePeriod(time, time));
					else {
						if((time - crash_report.get(crash_report.size()-1).end) > 1.1 * time_unit) {
							crash_report.add(new TimePeriod(time, time));
						}
						else {
							crash_report.get(crash_report.size()-1).end = time;
						}
					}
				}
			}
			
			final_report.put(i, crash_report);
			//System.out.println(i + "  " + crash_report);
		}
		
		return final_report;
	}

	private boolean isCrashNow(int id, double now) {
		if (crashes.containsKey(id) && crashes.get(id) <= now)
			return true;

		return false;
	}

	public static void main(String[] args) throws IOException {
		///// is there exist server crash ? crash file path?
		ServerFailureDetector sfd = new ServerFailureDetector(
				"z://serverCrashDump//dump-60//", 
				true, 
				"z://crashFile//server-crash-60.txt");
		
		List<Pair<Integer, Integer>> list = new ArrayList<Pair<Integer, Integer>>();
		for(int i=0; i<sfd.alr.send_rule.size(); i++) {
			List<Integer> pair = sfd.alr.send_rule.get(i);
			if(pair != null)
				for(int master : pair) {
					list.add(new Pair(master, i));
				}		
		}
		///// enable message loss? loss rate?
		Map<Integer, List<TimePeriod>> alerts = sfd.detect(3, false, list, 0.1);

		
		for(Map.Entry<Integer, List<TimePeriod>> entry : alerts.entrySet()) {
			if(entry.getValue().size() > 0)
				System.out.println(entry.getKey() + "  " + entry.getValue().size());
		}
		
		System.out.println("avg query accurate pro: " + Evaluator.avgQueryAccuracyPro(alerts, sfd.crashes));
		System.out.println("avg mistake rate: " + Evaluator.faultCrashReportRate(alerts, sfd.crashes));
		System.out.println("avg detection time: " + Evaluator.avgDetectionTime(alerts, sfd.crashes));
	}

}
