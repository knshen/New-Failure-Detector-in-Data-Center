package evaluation;

import java.util.*;

public class Evaluator {
	public double avgDetectTime(List<Double> crash_time, List<Double> report_time) {
		double sum = 0;
		for(int i=0; i<crash_time.size(); i++) {
			sum += (report_time.get(i) - crash_time.get(i));
		}
		return sum / (double)crash_time.size();
	}
	
	public int numMsgs(int k, int num_servers) {
		return k * num_servers;
	}
	
	public double serverPrecision(int num_report, int num_crashes) {
		return num_crashes / (double)num_report;
	}
	
	/*
	public double linkPrecision(int num_true, int num_fail) {
		
	}
	
	public double linkRecall() {
	
	*/
	
	public static void main(String[] args) {
		

	}

}
