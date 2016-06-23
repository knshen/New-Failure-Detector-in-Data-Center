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

public class DeviceCrashMaker {
	public DeviceCrashMaker() {

	}

	public static void make(int base, int total_num, double start, double end,
			int num_fault) throws IOException {
		List<Fault> events = new ArrayList<Fault>();
		Set<Integer> have_crash = new HashSet<Integer>();

		for (int i = 0; i < num_fault; i++) {
			int si = (int) (Math.random() * total_num) + base;
			while (have_crash.contains(si)) {
				si = (int) (Math.random() * total_num) + base;
			}
			have_crash.add(si);
			double time = Math.random() * (end - start) + start;
			events.add(new Fault(si, time));
		}
		write(events);
	}

	private static void write(List<Fault> events) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				"tor-crash-3.txt")));
		for (Fault fault : events) {
			String line = fault.server_id + " " + fault.time + "\n";
			bw.write(line);
		}
		bw.flush();
		bw.close();
	}

	public static void main(String[] args) throws IOException {
		final double end_time = 60.0;
		int crash_number = 3;
		DeviceCrashMaker.make(0, 6, 2, end_time, crash_number);

	}

}
