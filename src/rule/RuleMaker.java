package rule;

import java.util.*;
import java.io.*;

public class RuleMaker {
	public static final String path = "rule.txt";
	
	
	public RuleMaker() {
		
	}
	
	public Map<Integer, List<Integer>> make() {
		return simpleRule();
	}
	
	
	public Map<Integer, List<Integer>> simpleRule() {
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
	
	public void clean() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File("rule.txt")));
		String line = br.readLine();
		List<String> list = new ArrayList<String>();
		
		while(line != null) {
			String tmp[] = line.split(":");
			String tmp1[] = tmp[1].trim().split("\t");
			list.add(tmp[0].trim() + ":" + tmp1[0] + " " + tmp1[1] + " " + tmp1[2]);
			line = br.readLine();
		}
		br.close();
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("rule.txt")));
		for(String str : list) {
			bw.write(str + "\n");
		}
		
		bw.flush();	
		bw.close();
	}
	
	public void check() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File("rule.txt")));
		String line = br.readLine();
		Map<Integer, List<Integer>> detect = new HashMap<Integer, List<Integer>>();
		while(line != null) {
			int slave = Integer.parseInt(line.split(":")[0]);
			int master1 = Integer.parseInt(line.split(":")[1].split(" ")[0]);
			int master2 = Integer.parseInt(line.split(":")[1].split(" ")[1]);
			int master3 = Integer.parseInt(line.split(":")[1].split(" ")[2]);
			
			if(detect.containsKey(master1))
				detect.get(master1).add(slave);
			else {
				List<Integer> list = new ArrayList<Integer>();
				list.add(slave);
				detect.put(master1, list);
			}
				
			if(detect.containsKey(master2))
				detect.get(master2).add(slave);
			else {
				List<Integer> list = new ArrayList<Integer>();
				list.add(slave);
				detect.put(master2, list);
			}
			
			if(detect.containsKey(master3))
				detect.get(master3).add(slave);
			else {
				List<Integer> list = new ArrayList<Integer>();
				list.add(slave);
				detect.put(master3, list);
			}
			line = br.readLine();
		}
		
		for(Map.Entry<Integer, List<Integer>> entry : detect.entrySet()) {
			if(entry.getValue().size() != 3) {
				System.err.println("errorrrrrr");
			}
			System.out.println(entry.getKey() + "  " + entry.getValue());
		}
		
		br.close();
	}
	
	public static void main(String[] args) throws IOException {
		RuleMaker rm = new RuleMaker();
		//rm.save(rm.make());
		rm.check();
	}

}
