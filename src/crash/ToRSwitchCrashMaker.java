package crash;

import java.util.*;

public class ToRSwitchCrashMaker {

	public static void make(int num_tor, int num_crash) {
		int base = 6;
		Set<Integer> have_crash = new HashSet<Integer>();

		for (int i = 0; i < num_crash; i++) {
			int id = (int) (Math.random() * num_tor) + base;
			while (have_crash.contains(id)) {
				id = (int) (Math.random() * num_tor) + base;
			}
			System.out.println(id);
			have_crash.add(id);
		}
	}

	public static void main(String[] args) {
		ToRSwitchCrashMaker.make(6, 1);

	}

}
