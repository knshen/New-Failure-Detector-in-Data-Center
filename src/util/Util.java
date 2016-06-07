package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Util {
	public static int getRackIdByServerId(int server_id) {
		return (server_id - 12) / 20;
	}
	
	public static Map<Integer, List<Integer>> getSendRelations() throws IOException {
		Map<Integer, List<Integer>> sends = new HashMap<Integer, List<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(new File(
				"rule.txt")));
		String line = "";
		while ((line = br.readLine()) != null) {
			int slave = Integer.parseInt(line.split(":")[0]);
			int m1 = Integer.parseInt(line.split(":")[1].split(" ")[0]);
			int m2 = Integer.parseInt(line.split(":")[1].split(" ")[1]);
			int m3 = Integer.parseInt(line.split(":")[1].split(" ")[2]);
			sends.put(slave, Arrays.asList(m1, m2, m3));
		}
		
		return sends;
	}
	
	public static Map<Integer, List<Integer>> getRule() throws IOException {
		Map<Integer, List<Integer>> rule = new HashMap<Integer, List<Integer>>();
		BufferedReader br = new BufferedReader(new FileReader(new File(
				"rule.txt")));
		String line = "";
		while ((line = br.readLine()) != null) {
			int slave = Integer.parseInt(line.split(":")[0]);
			int m1 = Integer.parseInt(line.split(":")[1].split(" ")[0]);
			int m2 = Integer.parseInt(line.split(":")[1].split(" ")[1]);
			int m3 = Integer.parseInt(line.split(":")[1].split(" ")[2]);

			if (rule.containsKey(m1))
				rule.get(m1).add(slave);
			else {
				List<Integer> list = new ArrayList<Integer>();
				list.add(slave);
				rule.put(m1, list);
			}

			if (rule.containsKey(m2))
				rule.get(m2).add(slave);
			else {
				List<Integer> list = new ArrayList<Integer>();
				list.add(slave);
				rule.put(m2, list);
			}

			if (rule.containsKey(m3))
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


}
