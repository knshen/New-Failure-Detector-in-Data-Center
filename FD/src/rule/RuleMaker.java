package rule;

import java.util.*;
import java.io.*;

public class RuleMaker {
	public static final String path = "rule.txt";
	
	
	public RuleMaker() {
		
	}
	
	public Map<Integer, List<Integer>> make() {
		// send relation: key is monitored by each element in value
		Map<Integer, List<Integer>> rule = new HashMap<Integer, List<Integer>>();
		for(int i=12; i<=131; i++) {
			List<Integer> masters = new ArrayList<Integer>();
			if(i == 31 || i == 51 || i == 71 || i == 91 || i == 111 || i == 131)
				masters.add(i-19);
			else
				masters.add(i+1);
			
			if(i >= 12 && i <= 31) {
				masters.add(20+i);
				masters.add(40+i);
				masters.add(60+i);
				masters.add(80+i);
				masters.add(100+i);
			}
			else if(i >= 32 && i <= 51) {
				masters.add(20+i);
				masters.add(40+i);
				masters.add(60+i);
				masters.add(80+i);
				masters.add(i-20);
			}
			else if(i >= 52 && i <= 71) {
				masters.add(20+i);
				masters.add(40+i);
				masters.add(60+i);
				masters.add(i-20);
				masters.add(i-40);
			}
			else if(i >= 72 && i <= 91) {
				masters.add(20+i);
				masters.add(40+i);
				masters.add(i-60);
				masters.add(i-20);
				masters.add(i-40);

			}
			else if(i >= 92 && i <= 111) {
				masters.add(20+i);
				masters.add(i-80);
				masters.add(i-60);
				masters.add(i-20);
				masters.add(i-40);
			}
			else if(i >= 112 && i <= 131) {
				masters.add(i-100);
				masters.add(i-80);
				masters.add(i-60);
				masters.add(i-20);
				masters.add(i-40);
			}

			rule.put(i, masters);
		} // end for
		
		return rule;
	}
	
	public void save(Map<Integer, List<Integer>> rule) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));
		for(Map.Entry<Integer, List<Integer>> entry : rule.entrySet()) {
			int key = entry.getKey();
			List<Integer> list = entry.getValue();
			String line = key + ":";
			for(int master : list) {
				line += master + " ";
			}
			line = line.trim();
			bw.write(line+"\n");
		}
		
		bw.flush();
		bw.close();
	}
	
	public static void main(String[] args) throws IOException {
		RuleMaker rm = new RuleMaker();
		rm.save(rm.make());
	}

}
