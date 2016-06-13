package relatedWork;

import java.util.*;
import java.io.*;

import parser.DumpAnalyzer;
import util.Util;

public class PingParser extends DumpAnalyzer {

	public static final double end = 10;
	public static final double delay = 0.1; // single hop delay

	Map<Integer, Set<String>> special_ip = new HashMap<Integer, Set<String>>();
	String dir;

	/*
	 * node 0: [0->0, 0->1, ... 0->131] node 1: [1->0, 1->1, ... 1->131] ...
	 * node 131: [131->0, 131->1, ... 131->130]
	 */
	public List<List<Integer>> success_pings = null;

	public PingParser(String dir) throws IOException {
		super(dir);
		this.dir = dir;
		success_pings = new ArrayList<List<Integer>>();
		for (int i = 0; i < num_nodes; i++) {
			List<Integer> list = new ArrayList<Integer>();
			for (int j = 0; j < num_nodes; j++)
				list.add(0);

			success_pings.add(list);
		}

		readSpecial();
	}

	public void readSpecial() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(
				"special.txt")));
		String line = "";

		while ((line = br.readLine()) != null) {
			int id = Integer.parseInt(line.split(":")[0].trim());
			String ip = line.split(":")[1].trim();
			ip2id.put(ip, id);
			if (!special_ip.containsKey(id)) {
				Set<String> set = new HashSet<String>();
				set.add(ip);
				special_ip.put(id, set);
			} else {
				Set<String> set = special_ip.get(id);
				set.add(ip);
				special_ip.put(id, set);
			}
		}

		br.close();
	}

	public void getPingData() throws IOException {
		int dis[][] = new int[num_nodes][num_nodes];
		for (int i = 0; i < num_nodes; i++) {
			for (int j = i; j < num_nodes; j++) {
				if (topo[i][j] == 1) {
					dis[i][j] = 1;
					dis[j][i] = 1;
				} else if (i != j) {
					dis[i][j] = 999;
					dis[j][i] = 999;
				}
			}
		}
		int hops[][] = Util.floyd(dis);

		File f_dir = new File(dir);
		for (File file : f_dir.listFiles()) {
			if (file.getName().startsWith("ping")) {
				int node_id = Integer.parseInt(file.getName().split("-")[1]); // ping
																				// source
																				// id
				List<PingPacket> replys = readPingDumpFile(file
						.getAbsolutePath());
				for (PingPacket ppkt : replys) {
					if ((node_id < 12 && special_ip.get(node_id).contains(
							ppkt.dest))
							|| ppkt.dest.equals(id2ip.get(node_id))) {
						int ping_dest_id = ip2id.get(ppkt.src);
						double arrive_time = ppkt.time;
						if ((arrive_time - (int) arrive_time) < (2 * hops[node_id][ping_dest_id] + 0.5)
								* delay) {
							int pings = success_pings.get(node_id).get(
									ping_dest_id);
							success_pings.get(node_id).set(ping_dest_id,
									pings + 1);
						}

					}
				}
			}
		}

	}

	private void print() {
		for (List<Integer> list : success_pings) {
			for (int num : list) {
				System.out.print(num + " ");
			}
			System.out.println();
		}
	}

	private List<PingPacket> readPingDumpFile(String path) throws IOException {
		List<PingPacket> res = new ArrayList<PingPacket>();
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = "";

		while ((line = br.readLine()) != null) {
			String tokens[] = line.split(" ");
			if (!clean(tokens[1]).equals("IP"))
				continue;
			String type = clean(tokens[7]);
			if (!type.equals("reply"))
				continue;

			double time = Double.parseDouble(clean(tokens[0]));
			String src_ip = clean(tokens[2]);
			String dest_ip = clean(tokens[4]);
			int seq = Integer.parseInt(clean(tokens[11]));
			PingPacket ppkt = new PingPacket();
			ppkt.time = time;
			ppkt.src = src_ip;
			ppkt.dest = dest_ip;
			ppkt.seq = seq;

			res.add(ppkt);
		}

		br.close();
		return res;
	}

	private String clean(String str) {
		str = str.trim();
		str = str.replaceAll(" ", "");
		str = str.replaceAll(",", "");
		str = str.replaceAll(":", "");
		return str;
	}

	public static void main(String args[]) throws IOException {
		PingParser pp = new PingParser("z://ping2//");
		pp.getPingData();
		pp.print();
	}
}
