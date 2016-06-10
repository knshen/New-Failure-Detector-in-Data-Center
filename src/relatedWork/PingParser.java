package relatedWork;

import java.util.*;
import java.io.*;

public class PingParser {
	
	public static final double end = 10;
	String dir;
	List<List<Integer>> success_pings = new ArrayList<List<Integer>>();
	
	public PingParser(String dir) {
		this.dir = dir;
	}
	
	public void getPingData() throws IOException {
		File f_dir = new File(dir);
		for(File file : f_dir.listFiles()) {
			if(file.getName().startsWith("ping")) {
				int node_id = Integer.parseInt(file.getName().split("-")[1]);
				
				
			}
		}
	}
	
	private List<PingPacket> readPingDumpFile(String path) throws IOException {
		List<PingPacket> res = new ArrayList<PingPacket>();
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		
		
		br.close();
		return res;
	}
	
	public static void main(String args[]) throws IOException {
		
	}
}
