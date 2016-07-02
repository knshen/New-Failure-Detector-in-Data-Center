package evaluation;

import java.util.*;

import util.Item;
import util.TimePeriod;

public class Evaluator {
	public static final double time_unit = 0.0001;
	public static final double end_time = 60.0;
	
	public static double avgDetectionTime(Map<Integer, List<TimePeriod>> alerts, Map<Integer, Double> crashes) {
		double res = 0.0;
		int num_crash = crashes.size();
		
		for(Map.Entry<Integer, Double> entry : crashes.entrySet()) {
			int crash_id = entry.getKey();
			double real_time = entry.getValue();
			
			double suspect_time = -1;
			for(TimePeriod tp : alerts.get(crash_id)) {
				if(tp.end >= real_time) {
					suspect_time = tp.begin;
					break;
				}
			}
			if(suspect_time == -1) {
				num_crash--;
				System.err.println("ignore a real crash!");
			}
			else if(suspect_time - real_time >= 0)
				res += (suspect_time - real_time);
		}
		
		return res / num_crash;
	}
	
	public static double avgQueryAccuracyPro(Map<Integer, List<TimePeriod>> alerts, Map<Integer, Double> crashes) {
		List<Integer> corr_freq_all = new ArrayList<Integer>();
		
		int total_time_units = (int)((end_time - 2) / time_unit);
		/////////////
		for(int i=12; i<=131; i++) {
			int correct_frequent = 0;
			if(!crashes.containsKey(i)) {
				// no crash
				int wrong = 0;
				for(TimePeriod tp : alerts.get(i)) 
					wrong += (int)((tp.end - tp.begin) / time_unit);
				correct_frequent = total_time_units - wrong;
			}
			else {
				// crash at sometime
				double crash_time = crashes.get(i);
				for(TimePeriod tp : alerts.get(i)) {
					if(tp.begin <= crash_time && crash_time <= tp.end)
						correct_frequent += (int)((tp.end - crash_time) / time_unit);
					else if(crash_time <= tp.begin)
						correct_frequent += (int)((tp.end - tp.begin) / time_unit);
				}
			}
			
			// for the node i:
			corr_freq_all.add(correct_frequent);
		}
		
		double res = 0;
		for(int freq : corr_freq_all) {
			res += freq / (double)total_time_units;
		}
		
		return res / corr_freq_all.size();
	}
	
	public static double avgQueryAccuracyPro4ToR(Map<Integer, List<TimePeriod>> alerts, Map<Integer, Double> crashes) {
		List<Integer> corr_freq_all = new ArrayList<Integer>();
		
		int total_time_units = (int)((end_time - 2) / time_unit);
		/////////////
		for(int i=0; i<=5; i++) {
			int correct_frequent = 0;
			if(!crashes.containsKey(i)) {
				// no crash
				int wrong = 0;
				if(alerts.containsKey(i))
					for(TimePeriod tp : alerts.get(i)) 
						wrong += (int)((tp.end - tp.begin) / time_unit);
				correct_frequent = total_time_units - wrong;
			}
			else {
				// crash at sometime
				double crash_time = crashes.get(i);
				if(alerts.containsKey(i)) {
					for(TimePeriod tp : alerts.get(i)) {
						if(tp.begin <= crash_time && crash_time <= tp.end)
							correct_frequent += (int)((tp.end - crash_time) / time_unit);
						else if(crash_time <= tp.begin)
							correct_frequent += (int)((tp.end - tp.begin) / time_unit);
					}
				}
			}
			
			// for the node i:
			corr_freq_all.add(correct_frequent);
		}
		
		double res = 0;
		for(int freq : corr_freq_all) {
			res += freq / (double)total_time_units;
		}
		
		return res / corr_freq_all.size();
	}

	public static double faultCrashReportRate(Map<Integer, List<TimePeriod>> alerts, Map<Integer, Double> crashes) {
		// number of faulty crash reports per second for all monitorable objects
		double mistakes = 0;
		
		///////////
		for(int i=12; i<=131; i++) {
			for(TimePeriod tp : alerts.get(i)) {
				if(!isCrashNow(i, tp.begin, crashes)) {
					mistakes++;
				}
			}
		}
		
		return mistakes / (end_time - 2);
	}

	public static double faultCrashReportRate4ToR(Map<Integer, List<TimePeriod>> alerts, Map<Integer, Double> crashes) {
		double mistakes = 0;
		
		for(int i=0; i<=5; i++) {
			if(!alerts.containsKey(i))
				continue;
			for(TimePeriod tp : alerts.get(i)) {
				if(!isCrashNow(i, tp.begin, crashes)) {
					mistakes++;
				}
			}
		}
		
		return mistakes / (end_time - 2);
	}
	
	private static boolean isCrashNow(int id, double now, Map<Integer, Double> crashes) {
		if (crashes.containsKey(id) && crashes.get(id) <= now)
			return true;

		return false;
	}
	
	/**
	 * smaller, better
	 * @param suspects : normalized, only contain link failures
	 * @param size
	 * @return
	 */
	public static double DS(List<Item> suspects) {
		double res = 0;
		for(int i=0; i<suspects.size(); i++) {
			double pro = suspects.get(i).value;
			if(pro == 0)
				continue;
			res += pro * ((Math.log(pro)) / (Math.log(2)));		
		}
		return res * -1;
	}
	
	/**
	 * precision@k for one instance
	 * @param suspect
	 * @param k
	 * @param faults
	 * @return
	 */
	public static double precision_k(List<Item> suspect, int k, List<String> faults) {
		double hits = 0;
		for(int i=0; i<k; i++) {
			Item item = suspect.get(i);
			if(faults.contains(item.item_name))
				hits++;
		}
		hits /= (double)k;
		
		System.out.println("k: " + k + "  hits: " + hits);
		return hits;
	}
	
	/**
	 * MRR for one instance
	 * @param suspect
	 * @param faults
	 * @return
	 */
	public static int MRR(List<Item> suspect, List<String> faults) {
		int rank = 0;
		for(int i=0; i<suspect.size(); i++) {
			if(faults.contains(suspect.get(i).item_name)) {
				rank = i + 1;
				break;
			}
		}
		//System.out.println("rank: " + rank);
		return rank;
	}
	
	public static void main(String[] args) {
		
		System.out.println();
	} 

}
