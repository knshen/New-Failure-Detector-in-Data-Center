package relatedWork.hierarchy;

import java.util.*;
import java.io.*;

import core.FD;
import core.ServerFailureDetector;
import crash.MessageLossMaker;
import evaluation.Evaluator;
import parser.DumpAnalyzer;
import util.Pair;
import util.TimePeriod;
import util.Util;

public class HierarchyServerDetector extends ServerFailureDetector {
	public static final double end_time = 60.0;
	
	List<Integer> leaders = new ArrayList<Integer>();
	List<List<Integer>> send_rule_hie = new ArrayList<List<Integer>>();
	
	public HierarchyServerDetector(String dump_dir, boolean haveCrash, String crash_path) throws IOException {
		super(dump_dir, haveCrash, crash_path);
		alr.getServerPackets("hie");
		/*
		for(Map.Entry<Integer, Map<Integer, List<Double>>> entry : alr.hb.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue().keySet());
		}*/
		for(int i=0; i<6; i++) {
			leaders.add(12 + i * 20);
		}
		
		getRule();
	}
	
	private void getRule() {
		for(int i=0; i<12; i++) 
			send_rule_hie.add(null);
		
		for(int i=12; i<=131; i++) {
			List<Integer> masters = new ArrayList<Integer>();
			if(leaders.contains(i)) {
				for(int node : leaders)
					if(node != i)
						masters.add(node);
			}
			else {
				masters.add(leaders.get((i - 12) / 20));
			}
			send_rule_hie.add(masters);
		}
	}
	
	public Map<Integer, List<TimePeriod>> detectInHierarchy(boolean isLoss, double loss_rate) throws IOException {
		Map<Integer, List<TimePeriod>> alert_reports = new HashMap<Integer, List<TimePeriod>>();
		List<Pair<Integer, Integer>> monitor_relas = new ArrayList<Pair<Integer, Integer>>();
		
		// generate monitor relationships
		for(int i=12; i<=131; i++) {
			// i monitor who?
			if(leaders.contains(i)) {
				// node i is a team leader
				for(int node : leaders) {
					if(node != i)
						monitor_relas.add(new Pair(i, node));
				}
				for(int j=1; j<20; j++) {
					monitor_relas.add(new Pair(i, i + j));
				}
			}
		}
		
		if(isLoss) {
			MessageLossMaker.make(alr, 2, end_time, monitor_relas, loss_rate);
		}
		
		FD fd = new FD();
		
		for(int i=12; i<=131; i++) {
			// i: slave
			// crash report for the server i
			List<TimePeriod> crash_report = new ArrayList<TimePeriod>();
						
			List<List<TimePeriod>> ress = new ArrayList<List<TimePeriod>>();
			for(int master : send_rule_hie.get(i)) {
				List<TimePeriod> res = fd.chenDetect(alr.hb.get(master).get(i), win_size, 2, end_time, interval, fail_threshold);
				ress.add(res);
			}
						
			for(double time = 2; time <= end_time; time += time_unit) {
				int alerts = 0;
				for(int k = 0; k < ress.size(); k++) {
					// k = 5 || 1
					int master_id = send_rule_hie.get(i).get(k);
					if(!isCrashNow(master_id, time) && Util.isInPeriod(ress.get(k), time)) {
						//System.out.println(master_id + " report crash of " + i + " at time: " + time);
						alerts++;
					}			
				}
				
				int K = 0;
				if(leaders.contains(i)) 
					// I am a team leader
					K = leaders.size() - 1;
				else 
					K = 1;
				
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

			} // end time
			alert_reports.put(i, crash_report);
			//System.out.println(i + "  " + crash_report);
			
		}// end out for
		return alert_reports;
	}
	
	private boolean isCrashNow(int id, double now) {
		if (crashes.containsKey(id) && crashes.get(id) <= now)
			return true;
		return false;
	}

	public static void main(String[] args) throws IOException {
		//// is there exist server crash?
		HierarchyServerDetector hsd = new HierarchyServerDetector(
				"z://serverCrashDump//hie-50//", 
				true,
				"z://crashFile//server-crash-50.txt");
		//// enable message loss? loss rate?
		Map<Integer, List<TimePeriod>> alerts = hsd.detectInHierarchy(false, 0.2);
		
		for(Map.Entry<Integer, List<TimePeriod>> entry : alerts.entrySet()) {
			if(entry.getValue().size() > 0)
				System.out.println(entry.getKey() + "  " + entry.getValue().size());
		}
		
		System.out.println("avg query accurate pro: " + Evaluator.avgQueryAccuracyPro(alerts, hsd.crashes));
		System.out.println("avg mistake rate: " + Evaluator.faultCrashReportRate(alerts, hsd.crashes));
		System.out.println("avg detection time: " + Evaluator.avgDetectionTime(alerts, hsd.crashes));

		System.out.println();
	}

}
