package rule;

import java.util.*;
import java.io.*;

import util.Util;

public class RuleMaker {

	public Map<Integer, List<Integer>> make(int start_server_id,
			List<List<Integer>> racks, int K) {
		// key: is monitored by(send msg to) [...]
		Map<Integer, List<Integer>> rule = new HashMap<Integer, List<Integer>>();
		final int num_rack = racks.size();
		final int num_server_each_rack = racks.get(0).size();
		final int num_server = num_rack * num_server_each_rack;

		final int IN_RACK = K - 1;
		
		Map<Integer, Integer> remain_slaves = new HashMap<Integer, Integer>();
		for(int i = start_server_id; i < start_server_id + num_server; i++)
			remain_slaves.put(i, K);
		
		for (int i = 0; i < num_rack; i++) {
			int first_id = i * num_server_each_rack + start_server_id;
			int last_id = i * num_server_each_rack + start_server_id + num_server_each_rack - 1;
			
			for(int j = 0; j < num_server_each_rack; j++) {
				List<Integer> list = new ArrayList<Integer>(K);
				int node_id = first_id + j;
				int x = IN_RACK;
				for(int k=first_id; k<=last_id; k++) {
					if(x <= 0)
						break;
					if(node_id != k && remain_slaves.get(k) > 0) {
						x --;
						remain_slaves.put(k, remain_slaves.get(k)-1);
						list.add(k);
					}
				}
				rule.put(node_id, list);
			}

		}
		
		int z = (K - IN_RACK) * num_server_each_rack / (num_rack - 1);
		List<List<Integer>> rs_rack = new ArrayList<List<Integer>>();
		for(int i=0; i<num_rack; i++) {
			List<Integer> list = new ArrayList<Integer>();
			for(int j=0; j<num_rack; j++) {
				if(i == j) 
					list.add(0);
				else
					list.add(z);		
			}
			rs_rack.add(list);
		}
		
		for(int i = 0; i < num_rack; i++) {
			int first_id = i * num_server_each_rack + start_server_id;
			int last_id = i * num_server_each_rack + start_server_id + num_server_each_rack - 1;

			for(int j = 0; j < num_server_each_rack; j++) {
				// rack id: i
				int node_id = first_id + j;
				int x = K - IN_RACK; // cross rack
		
				for(int k=0; k<num_rack; k++) {				
					if(x == 0)
						break;
					if(rs_rack.get(k).get(i) <= 0) 
						continue;
								
					int first_id_rack_k = k * num_server_each_rack + start_server_id;
					int last_id_rack_k = k * num_server_each_rack + start_server_id + num_server_each_rack - 1;
				
					for(int y=first_id_rack_k; y<=last_id_rack_k; y++) {
						if(remain_slaves.get(y) > 0) {
							remain_slaves.put(y, remain_slaves.get(y)-1);
							rule.get(node_id).add(y);
							x--;
							rs_rack.get(k).set(i, rs_rack.get(k).get(i) - 1);
							
							if(x == 0 || rs_rack.get(k).get(i) <= 0)
								break;
						}
					}
				}
			}
		}
		
		return rule;
	}

	/**
	 * write monitor rule to rule.txt
	 * 
	 * @param rule
	 * @throws IOException
	 */
	public void save(Map<Integer, List<Integer>> rule, String path) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));
		for (Map.Entry<Integer, List<Integer>> entry : rule.entrySet()) {
			int key = entry.getKey();
			List<Integer> list = entry.getValue();
			String line = key + ":";
			for (int master : list) {
				line += master + " ";
			}
			line = line.trim();
			bw.write(line + "\n");
		}

		bw.flush();
		bw.close();
	}

	/**
	 * reverse the send rule : detection rule
	 * @param send_rule
	 * @return
	 */
	public Map<Integer, List<Integer>> detectionRule(Map<Integer, List<Integer>> send_rule) {
		// key -> [...]
		Map<Integer, List<Integer>> rule = new HashMap<Integer, List<Integer>>();
		for(Map.Entry<Integer, List<Integer>> entry : send_rule.entrySet()) {
			int slave = entry.getKey();
			for(int master : entry.getValue()) {
				if(rule.containsKey(master))
					rule.get(master).add(slave);
				else {
					 List<Integer> list = new ArrayList<Integer>();
					 list.add(slave);
					 rule.put(master, list);
				}
			}
		}
		
		return rule;
	}
	
	private boolean constraintCheck() {
		return true;
	}

	public static void main(String[] args) throws IOException {
		RuleMaker rm = new RuleMaker();

		List<List<Integer>> racks = new ArrayList<List<Integer>>();
		int server_id = 12;
		for (int i = 0; i < 6; i++) {
			List<Integer> list = new ArrayList<Integer>();
			for (int j = 0; j < 20; j++) {
				list.add(server_id++);
			}
			racks.add(list);
		}
		
		Map<Integer, List<Integer>> send_rule = rm.make(12, racks, 6);
		rm.save(send_rule, "z://ruleFile//rule-6.txt");
		
		Map<Integer, List<Integer>> re_rule = rm.detectionRule(send_rule);
		
		
		for(Map.Entry<Integer, List<Integer>> entry : send_rule.entrySet()) {
			System.out.println(entry.getKey() + "  " + entry.getValue());
		}
		
		System.out.println();
	}

}
