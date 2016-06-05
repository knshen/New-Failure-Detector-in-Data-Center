package parser;

import java.io.*;
import java.util.*;

import topo.TopoMaker;

public class DumpAnalyzer {
	public static String dir;

	int num_nodes = 132;
	List<Integer> servers = new ArrayList<Integer>();
	List<Integer> router_switch = new ArrayList<Integer>();
	int topo[][] = new int[num_nodes][num_nodes];
	Map<Integer, String> id2ip = new HashMap<Integer, String>();
	Map<String, Integer> ip2id = new HashMap<String, Integer>();

	// out parameters
	/**
	 * key -> key1 [msg1, msg2, ...] -> key2 [msg1, msg2, ...] -> key3 [msg1,
	 * msg2, ...]
	 */
	public Map<Integer, Map<Integer, List<Double>>> hb = new HashMap<Integer, Map<Integer, List<Double>>>();

	public DumpAnalyzer(String dir) throws IOException {
		this.dir = dir;
		for (int i = 12; i <= 131; i++)
			servers.add(i);
		for (int i = 0; i < 12; i++)
			router_switch.add(i);

		topo = new TopoMaker("topo.txt").make(num_nodes);
		getIP();

		for (int server_id : servers) {
			hb.put(server_id, new HashMap<Integer, List<Double>>());
		}
	}

	private void getIP() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(
				new File("ip.txt")));
		String line = br.readLine();
		while (line != null) {
			String tmp[] = line.split(":");
			int id = Integer.parseInt(tmp[0].trim());
			String ip = tmp[1].trim();

			ip2id.put(ip, id);
			id2ip.put(id, ip);

			line = br.readLine();
		}
		br.close();
	}

	private List<Packet> readDumpFile(String path) throws IOException {
		List<Packet> list = new ArrayList<Packet>();

		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = br.readLine();

		while (line != null) {
			String data[] = line.split(" ");

			Packet pkt = new Packet();
			pkt.time = Double.parseDouble(data[0]);
			pkt.src = data[2].substring(0, data[2].indexOf(".", 7));
			pkt.dest = data[4].substring(0, data[4].indexOf(".", 7));
			pkt.length = Integer.parseInt(data[7]);

			list.add(pkt);
			line = br.readLine();
		}

		br.close();

		return list;
	}

	/**
	 * get heart beat data with timestamp
	 * 
	 * @throws IOException
	 */
	public void formalize() throws IOException {
		File files[] = new File(dir).listFiles();
		for (File file : files) {
			String file_name = file.getName();
			String check[] = file_name.split("-");
			if (check[0].equals("topo") && Integer.parseInt(check[1]) >= 12) {
				int server_id = Integer.parseInt(check[1]);
				List<Packet> list = readDumpFile(file.getAbsolutePath());
				// deal with packets
				for (Packet pkt : list) {
					// send packets
					if (pkt.src.equals(id2ip.get(server_id)))
						continue;
					if (pkt.dest.equals(id2ip.get(server_id))) {
						int slave_id = ip2id.get(pkt.src);
						Map<Integer, List<Double>> map = hb.get(server_id);
						if (!map.containsKey(slave_id))
							map.put(slave_id, new ArrayList<Double>());
						map.get(slave_id).add(pkt.time);
					}

				}

			}
		}
	}

	public static void main(String[] args) throws IOException {
		DumpAnalyzer alr = new DumpAnalyzer("z://dump3//");
		/*
		 * List<Packet> list = alr.readDumpFile(dir + "topo-131-1"); for(Packet
		 * pkt : list) System.out.println(pkt);
		 */
		alr.formalize();
		Map<Integer, List<Double>> data = alr.hb.get(20);
		for (Map.Entry<Integer, List<Double>> entry : data.entrySet()) {
			System.out.println("server #" + entry.getKey() + "   "
					+ "message number: " + entry.getValue().size());
			List<Double> sw = entry.getValue();
			for (Double msg : sw) {
				System.out.print(msg + " ");
			}
			System.out.println();
		}
		System.out.println();
	}

}
