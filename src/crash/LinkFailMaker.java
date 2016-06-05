package crash;

import java.util.*;

public class LinkFailMaker {
	public static void make(int num_link, int num_fail) {
		Set<Integer> have_crash = new HashSet<Integer>();

		for (int i = 0; i < num_fail; i++) {
			int id = (int) (Math.random() * num_link);
			while (have_crash.contains(id)) {
				id = (int) (Math.random() * num_link);
			}
			have_crash.add(id);
			System.out.println(id);
		}
	}

	public static void main(String[] args) {
		LinkFailMaker.make(20, 1);

	}

}
