package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.*;

public class Util {
	
	public static boolean isInPeriod(List<TimePeriod> list, double time) {
		for(TimePeriod tp : list) {
			if(time >= tp.begin && time <= tp.end)
				return true;
		}
		return false;
	}
	
	public static int getRackIdByServerId(int server_id) {
		return (server_id - 12) / 20;
	}
	
	public static int[][] floyd(int dis[][]) {
		final int n = dis.length;
		int hops[][] = new int[n][n];

		for (int q = 0; q < n; q++) {
			for (int w = 0; w < n; w++) {
				hops[q][w] = dis[q][w];
			}
		}

		for (int k = 0; k < n; k++) {
			for (int i = 0; i < n; i++) {
				for (int j = 0; j < n; j++) {
					if (hops[i][j] >= hops[i][k] + hops[k][j]) {
						hops[i][j] = hops[i][k] + hops[k][j];

					}

				}
			}
		}

		return hops;
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

	public static void printBiTree(BiTreeNode root) {
		Queue<BiTreeNode> queue = new LinkedList<BiTreeNode>();
		Queue<Integer> level = new LinkedList<Integer>();
		queue.offer(root);
		level.offer(1);
		
		int now = 0;
		while(!queue.isEmpty()) {
			BiTreeNode node = queue.poll();
			int this_level = level.poll();
			
			if(this_level > now) {
				System.out.println();
				now = this_level;
			}
			
			System.out.print(node.id + "[" + node.pro + "] "); 
			
			for(BiTreeNode bn : node.children) {
				queue.offer(bn);
				level.offer(this_level + 1);
			}
				
		}
	}
	
	public static List<Node> bfs(int source, int dest, int graph[][], int hop) {
		List<Node> res = new ArrayList<Node>();
		final int n = graph.length;
		/*
		boolean isVisit[] = new boolean[n];
		for(int i=0; i<n; i++)
			isVisit[i] = false;
		*/
		
		Queue<Node> q = new LinkedList<Node>();
		q.offer(new Node(source, null, 0));
		//isVisit[source] = true;
		/*
		if(source == 12 && dest == 72)
			System.out.println();
		*/
		while(!q.isEmpty()) {
			Node node = q.poll();
			if(node.id == dest) {
				res.add(node);
			}
				
			if(node.step >= hop) {
				continue;
			}
			
			for(int i=0; i<n; i++) {
				if((node.pre == null || node.pre.id != i) && node.id != i && graph[node.id][i] == 1) {
					Node next = new Node(i, node, node.step + 1);
					//isVisit[i] = true;
					q.offer(next);
				}
			}
		}
		return res;
	}

}
