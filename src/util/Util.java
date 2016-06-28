package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;
import java.util.*;

public class Util {
	
	public static void normalize(List<Item> list) {
		double total = 0;
		for(Item item : list) 
			if(!item.item_name.equals("normal"))
				total += item.value;
		
		for(Item item : list) 
			if(item.item_name.equals("normal"))
				item.value = 0;
			else
				item.value /= total;	
	}
	
	public static double residual(List<Item> list1, List<Item> list2) {
		double res = 0;
		for(Item i1 : list1) {
			Item other = null;
			for(int i=0; i<list2.size(); i++) {
				Item i2 = list2.get(i);
				if(i2.item_name.equals(i1.item_name)) {
					other = i2;
					break;
				}	
			}
			res += (i1.value - other.value) * (i1.value - other.value);
		}
		return res;
	}
	
	public static int C_N_M(int n, int m) {
		return factorial(n) / (factorial(m) * factorial(n - m));
	}
	
	public static int factorial(int n) {
		int res = 1;
		for(int i=1; i<=n; i++)
			res *= i;
		return res;
	}
	
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
	
	public static int howManyDistinctRack(List<Integer> servers) {
		Set<Integer> set = new HashSet<Integer>();
		for(int server : servers)
			set.add(getRackIdByServerId(server));
		return set.size();
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
	
	public static Map<Integer, List<Integer>> getDetectRule(List<List<Integer>> send_rule) {
		// key -> [...]
		Map<Integer, List<Integer>> rule = new HashMap<Integer, List<Integer>>();
		for(int i=0; i<send_rule.size(); i++) {
			List<Integer> masters = send_rule.get(i);
			if(masters == null)
				continue;
			for(int master : masters) {
				if(rule.containsKey(master))
					rule.get(master).add(i);
				else {
					List<Integer> slaves = new ArrayList<Integer>();
					slaves.add(i);
					rule.put(master, slaves);
				}
			}
		}
		
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

	public static void main(String args[]) {
		double p = 0.001;
		
		for(int k=2; k<=6; k++) {
			int m = 1 + k - (k+1) / 2;
			double pro = 0;
			for(; m<=k; m++) {
				pro += Util.C_N_M(k, m) * Math.pow(p, m) * Math.pow(1-p, k-m);
			}
			System.out.println("k = " + k + ": " + pro);
		}
		System.out.println();
	}
}
