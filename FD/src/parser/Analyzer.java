package parser;

import java.io.*;
import java.util.*;

import topo.TopoMaker;

public class Analyzer {
	public static String dir = "dump//";
	
	int num_nodes = 132;
	List<Integer> servers = new ArrayList<Integer>();
	List<Integer> router_switch = new ArrayList<Integer>();
	int topo[][] = new int[num_nodes][num_nodes];
	Map<Integer, String> id2ip = new HashMap<Integer, String>();
	Map<String, Integer> ip2id = new HashMap<String, Integer>();
	
	public Analyzer() throws IOException {
		for(int i=12; i<=131; i++)
			servers.add(i);
		for(int i=0; i<12; i++)
			router_switch.add(i);
		
		topo = new TopoMaker("topo.txt").make(num_nodes);
		getIP();
	}
	
	private void getIP() throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File("ip.txt")));
		String line = br.readLine();
		while(line != null) {
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
		
		while(line != null) {
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
	
	public static void main(String[] args) throws IOException {
		Analyzer alr = new Analyzer();
		List<Packet> list = alr.readDumpFile(dir + "topo-131-1");
		for(Packet pkt : list)
			System.out.println(pkt);
	}

}
