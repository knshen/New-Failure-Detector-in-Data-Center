package core;

import java.util.*;
import java.io.*;

import parser.DumpAnalyzer;
import util.Util;

public class ToRSFailureDetector {
	List<Integer> crash_tor = new ArrayList<Integer>();

	public void defineCrash() {
		crash_tor.add(6);
	}


	public Map<Integer, Double> detect() throws IOException {
		/*
		 * key : crashed ToR Switch ID value : reported time
		 */
		Map<Integer, Double> final_alert = new HashMap<Integer, Double>();
		// //////////
		DumpAnalyzer alr = new DumpAnalyzer("z://tor-6//");
		alr.getServerPackets();
		Map<Integer, List<Integer>> rule = Util.getRule();
		FD fd = new FD();

		Map<Integer, List<Double>> crash_reports = new HashMap<Integer, List<Double>>();

		for (int i = 12; i <= 131; i++) {
			// i : master id
			Map<Integer, List<Double>> map = alr.hb.get(i);
			for (Map.Entry<Integer, List<Double>> entry : map.entrySet()) {
				int slave_id = entry.getKey();
				// ///////////////
				double res = fd.chenDetect(entry.getValue(), 100, 2, 60, 0.1,
						0.001);

				if (res != -1)
					if (crash_reports.containsKey(i))
						crash_reports.get(i).add(res);
					else {
						List<Double> list = new ArrayList<Double>();
						list.add(res);
						crash_reports.put(i, list);
					}

			}
		}
		
		// analyze
		for (Map.Entry<Integer, List<Double>> entry : crash_reports.entrySet()) {
			if (entry.getValue().size() == 2) {
				Collections.sort(entry.getValue());
				int tor_id = Util.getRackIdByServerId(rule.get(entry.getKey())
						.get(0));
				System.out.println("#server " + entry.getKey()
						+ " report crash of tor " + tor_id + " at time: "
						+ entry.getValue().get(1));
			}
		}

		return final_alert;
	}

	public static void main(String[] args) throws IOException {
		ToRSFailureDetector tfd = new ToRSFailureDetector();
		tfd.detect();

	}

}
