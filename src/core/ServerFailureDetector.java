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
	public Map<Integer, Double> tor_crashes = new HashMap<Integer, Double>();
	
	public DumpAnalyzer alr = null;
	public List<Pair<Integer, Integer>> send_recv_relation = new ArrayList<Pair<Integer, Integer>>();
	public ServerFailureDetector(String dump_dir, boolean haveCrash, String crash_path,
			boolean have_tor_crash, String tor_crash_path) throws IOException {
		alr = new DumpAnalyzer(dump_dir);
		if(haveCrash)
			readCrashFile(crash_path);
		if(have_tor_crash) 
			readToRCrashFile(tor_crash_path);
		
		
		for(int i=0; i<alr.send_rule.size(); i++) {
			List<Integer> pair = alr.send_rule.get(i);
			if(pair != null)
				for(int master : pair) {
					send_recv_relation.add(new Pair(master, i));
				}		
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

	public void readToRCrashFile(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = "";
		while ((line = br.readLine()) != null) {
			int id = Integer.parseInt(line.split(" ")[0]);
			double time = Double.parseDouble(line.split(" ")[1]);
			tor_crashes.put(id, time);
		}
		br.close();
	}
	
	Map<Integer, List<TimePeriod>> raw_reports = new HashMap<Integer, List<TimePeriod>>();
	
	public Map<Integer, List<TimePeriod>> detect(int K, boolean haveLoss, double loss_rate) throws IOException {
		/**
		 * i: time periods that report crash of node i 
		 */
		Map<Integer, List<TimePeriod>> final_report = new HashMap<Integer, List<TimePeriod>>();
		FD fd = new FD();	
		alr.getServerPackets("topo");
		
		///////////// enable message loss?
		if(haveLoss) {
			MessageLossMaker.make(alr, 2, end_time, send_recv_relation, loss_rate);
		}

		for (int i = 12; i <= 131; i++) {
			// i: slave (monitorable object)
			// crash report for the server i
			List<TimePeriod> crash_report = new ArrayList<TimePeriod>();
			List<TimePeriod> raw_report = new ArrayList<TimePeriod>();
			
			List<List<TimePeriod>> ress = new ArrayList<List<TimePeriod>>();
			for(int master : alr.send_rule.get(i)) {
				List<TimePeriod> res = fd.chenDetect(alr.hb.get(master).get(i), win_size, 2, end_time, interval, fail_threshold);
				ress.add(res);
			}
				
			for(double time = 2; time <= end_time; time += time_unit) {			
				int alerts = 0;
				int detector_id = -1;
				for(int k = 0; k < ress.size(); k++) {
					// k = 0,1,2
					int master_id = alr.send_rule.get(i).get(k);
					int master_rack_id = Util.getRackIdByServerId(master_id);
					if(!isToRCrashNow(master_rack_id, time) && 
							!isCrashNow(master_id, time) && 
							Util.isInPeriod(ress.get(k), time)) {
						//System.out.println(master_id + " report crash of " + i + " at time: " + time);
						alerts++;
						detector_id = master_id;
					}
						
				}
				if(alerts == 1 && Util.getRackIdByServerId(detector_id) != Util.getRackIdByServerId(i)) {
					if(raw_report.size() == 0)
						raw_report.add(new TimePeriod(time, time));
					else {
						if((time - raw_report.get(raw_report.size()-1).end) > 1.1 * time_unit) {
							raw_report.add(new TimePeriod(time, time));
						}
						else {
							raw_report.get(raw_report.size()-1).end = time;
						}
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
			raw_reports.put(i, raw_report);
			final_report.put(i, crash_report);
			//System.out.println(i + "  " + crash_report);
		}
		
		return final_report;
	}

	public Map<Integer, List<TimePeriod>> detectToRCrash(Map<Integer, List<TimePeriod>> alerts) {
		final double alpha = 0.4;
		
		Map<Integer, List<TimePeriod>> tor_alerts = new HashMap<Integer, List<TimePeriod>>();
		
		for(double time = 2; time <= end_time; time += time_unit) {
			List<Integer> rack_alerts = new ArrayList<Integer>();
			for(int i=0; i<6; i++)
				rack_alerts.add(0);
			
			for(Map.Entry<Integer, List<TimePeriod>> entry : alerts.entrySet()) {
				int rack = Util.getRackIdByServerId(entry.getKey());
				if(Util.isInPeriod(entry.getValue(), time)) 
					rack_alerts.set(rack, rack_alerts.get(rack) + 1);	
			}

			for(int i=0; i<rack_alerts.size(); i++) {
				if(rack_alerts.get(i) >= (int)(20 * alpha)) {
					if(tor_alerts.containsKey(i)) {
						List<TimePeriod> list = tor_alerts.get(i);
						if((time - list.get(list.size()-1).end) >= 1.1 * time_unit) {
							list.add(new TimePeriod(time, time));
						}
						else {
							list.get(list.size()-1).end = time;
						}
					}
					else {
						List<TimePeriod> tp = new ArrayList<TimePeriod>();
						tp.add(new TimePeriod(time, time));
						tor_alerts.put(i, tp);
					}
				}
			}
		}
		
		return tor_alerts;
	}
	
	private boolean isCrashNow(int id, double now) {
		if (crashes.containsKey(id) && crashes.get(id) <= now)
			return true;

		return false;
	}

	private boolean isToRCrashNow(int tor_id, double now) {
		if (tor_crashes.containsKey(tor_id) && tor_crashes.get(tor_id) <= now)
			return true;

		return false;
	}
	
	public void run() throws IOException {
		double rates[] = new double[]{0.012, 0.014, 0.016, 0.018, 0.02, 0.025 ,0.03 ,0.035 ,0.04};
		
		for(int i = 0; i < rates.length; i++) {
			ServerFailureDetector sfd = new ServerFailureDetector(
					"z://serverCrashDump//k-5//dump-0", 
					false, 
					"z://crashFile//server-crash-" + /*num_crash[i] +*/ ".txt",
					false,
					null);
			
			Map<Integer, List<TimePeriod>> alerts = sfd.detect(5, true, rates[i]);	
			
			System.out.println("loss rate: " + rates[i]);
			//System.out.println("crashes: " + num_crash[i]);
			System.out.println("avg query accurate pro: " + Evaluator.avgQueryAccuracyPro(alerts, sfd.crashes));
			System.out.println("avg mistake rate: " + Evaluator.faultCrashReportRate(alerts, sfd.crashes));
			System.out.println("avg detection time: " + Evaluator.avgDetectionTime(alerts, sfd.crashes));
			System.out.println();

		}
	}
	
	// run for ToR detection
	public void run1() throws IOException {
		final int num_tor_crash = 3;
		int server_crash[] = new int[]{0};
		
		for(int i : server_crash) {
			ServerFailureDetector sfd = new ServerFailureDetector(
					"z://torCrashDump//tor-" + num_tor_crash + "-" + i +"//", 
					false, 
					"z://crashFile//server-crash-" + i +".txt",
					true,
					"z://crashFile//tor-crash-" + num_tor_crash + ".txt");
			
			Map<Integer, List<TimePeriod>> alerts = sfd.detect(3, true, 0.001);	
			
			/*
			for(Map.Entry<Integer, List<TimePeriod>> entry : sfd.raw_reports.entrySet()) {
				if(entry.getValue().size() > 0)
					System.out.println(entry.getKey() + "  " + entry.getValue());
			}*/
			
			Map<Integer, List<TimePeriod>> tor_alerts = sfd.detectToRCrash(sfd.raw_reports);
			/*
			for(Map.Entry<Integer, List<TimePeriod>> entry : tor_alerts.entrySet()) {
				if(entry.getValue().size() > 0)
					System.out.println(entry.getKey() + "  " + entry.getValue());
			}*/
			
			System.out.println("# server crash: " + i + "# tor crash " + num_tor_crash);
			System.out.println("average detection time: " + Evaluator.avgDetectionTime(tor_alerts, sfd.tor_crashes));
			System.out.println("mistake rate: " + Evaluator.faultCrashReportRate4ToR(tor_alerts, sfd.tor_crashes));
			System.out.println("average query accuracy: " + Evaluator.avgQueryAccuracyPro4ToR(tor_alerts, sfd.tor_crashes));
			System.out.println();
		}
	}
	
	public static void main(String[] args) throws IOException {
		ServerFailureDetector sfd = new ServerFailureDetector(
				"z://torCrashDump//tor-1-1//", 
				false, 
				"z://crashFile//server-crash-1.txt",
				true,
				"z://crashFile//tor-crash-1.txt");
		
		/*
		Map<Integer, List<TimePeriod>> alerts = sfd.detect(3, true, 0.001);	
		
		/*
		for(Map.Entry<Integer, List<TimePeriod>> entry : sfd.raw_reports.entrySet()) {
			if(entry.getValue().size() > 0)
				System.out.println(entry.getKey() + "  " + entry.getValue());
		}*/
		
		/*
		Map<Integer, List<TimePeriod>> tor_alerts = sfd.detectToRCrash(sfd.raw_reports);
		
		for(Map.Entry<Integer, List<TimePeriod>> entry : tor_alerts.entrySet()) {
			if(entry.getValue().size() > 0)
				System.out.println(entry.getKey() + "  " + entry.getValue());
		} 
		*/
		
		sfd.run1();
		System.out.println();
	}

}
