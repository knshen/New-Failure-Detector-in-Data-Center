package evaluation;

import java.util.*;

public class Evaluator {
	public static double avgDetectTime(Map<Integer, Double> crash_time, Map<Integer, Double> report_time) {
		double sum = 0;
		for(Map.Entry<Integer, Double> entry : crash_time.entrySet()) {
			sum += (report_time.get(entry.getKey()) - entry.getValue());
		}
		
		return sum / crash_time.size();
	}
	
	public static int numMsgs(int k, int num_servers) {
		return k * num_servers;
	}
	
	public static double serverPrecision(int num_report, int num_crashes) {
		return num_crashes / (double)num_report;
	}
	
	/*
	public static double linkPrecision(int num_true, int num_fail) {
		
	}
	
	public static double linkRecall() {
	
	*/
	
	public static void main(String[] args) {
		

	}

}
