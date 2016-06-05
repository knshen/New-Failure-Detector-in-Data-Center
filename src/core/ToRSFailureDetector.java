package core;

import java.util.*;
import java.io.*;

import parser.DumpAnalyzer;

public class ToRSFailureDetector {
	List<Integer> crash_tor = new ArrayList<Integer>();
	
	public void defineCrash() {
		crash_tor.add(6);
	}
	
	private Map<Integer, List<Integer>> getRule() throws IOException {
		 Map<Integer, List<Integer>> rule = new HashMap<Integer, List<Integer>>();
		 BufferedReader br = new BufferedReader(new FileReader(new File("rule.txt")));
		 String line = "";
		 while((line = br.readLine()) != null) {
			 int slave = Integer.parseInt(line.split(":")[0]);
			 int m1 = Integer.parseInt(line.split(":")[1].split(" ")[0]);
			 int m2 = Integer.parseInt(line.split(":")[1].split(" ")[1]);
			 int m3 = Integer.parseInt(line.split(":")[1].split(" ")[2]);
			 
			 if(rule.containsKey(m1))
				 rule.get(m1).add(slave);
			 else {
				 List<Integer> list = new ArrayList<Integer>();
				 list.add(slave);
				 rule.put(m1, list);
			 }
			 
			 if(rule.containsKey(m2))
				 rule.get(m2).add(slave);
			 else {
				 List<Integer> list = new ArrayList<Integer>();
				 list.add(slave);
				 rule.put(m2, list);
			 }

			 if(rule.containsKey(m3))
				 rule.get(m3).add(slave);
			 else {
				 List<Integer> list = new ArrayList<Integer>();
				 list.add(slave);
				 rule.put(m3, list);
			 }

		 }
		 
		 br.close();
		 return rule;
	}
	
	public Map<Integer, Double> detect() throws IOException {
		/*
		 *  key : crashed ToR Switch ID
		 *  value : reported time 
		 */
		Map<Integer, Double> final_alert = new HashMap<Integer, Double>();
		////////////
		DumpAnalyzer alr = new DumpAnalyzer("z://tor-6//");
		Map<Integer, List<Integer>> rule = getRule();
		return final_alert;
	}
	
	public static void main(String[] args) throws IOException {
		ToRSFailureDetector tfd = new ToRSFailureDetector();
		tfd.detect();

	}

}
