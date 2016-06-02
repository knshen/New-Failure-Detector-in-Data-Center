package crash;

import java.io.*;
import java.util.*;

class Fault {
	int server_id;
	double time;
	
	public Fault(int si, double time) {
		this.server_id = si;
		this.time = time;
	}
}

public class ServerCrashMaker {
	public ServerCrashMaker() {
		
	}
	
	public static void make(double start, double end, int num_server, int num_fault) throws IOException {
		int base = 12;
		List<Fault> events = new ArrayList<Fault>();
		Set<Integer> have_crash = new HashSet<Integer>();
		
		for(int i=0; i<num_fault; i++) {
			int si = (int)(Math.random() * 120) + base;
			while(have_crash.contains(si)) {
				si = (int)(Math.random() * 120) + base;
			}
			have_crash.add(si);
			double time = Math.random() * (end - start) + start;
			events.add(new Fault(si, time));
		}
		write(events);
	}
	
	private static void write(List<Fault> events) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("server-crash.txt")));
		for(Fault fault : events) {
			String line = fault.server_id + " " + fault.time + "\n";
			bw.write(line);
		}
		bw.flush();
		bw.close();
	}
	
	public static void main(String[] args) throws IOException {
		ServerCrashMaker.make(2, 3600, 120, 12);

	}

}
