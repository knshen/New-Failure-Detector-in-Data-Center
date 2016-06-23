package crash;

import java.util.*;

public class LinkFailMaker {
	public static final double end_time = 60.0;
	public static Map<Integer, String> linkID2Name = new HashMap<Integer, String>();
	
	static {
		linkID2Name.put(0, "link 0-2");
		linkID2Name.put(1, "link 0-3");
		linkID2Name.put(2, "link 0-4");
		linkID2Name.put(3, "link 0-5");
		linkID2Name.put(4, "link 1-2");
		linkID2Name.put(5, "link 1-3");
		linkID2Name.put(6, "link 1-4");
		linkID2Name.put(7, "link 1-5");
		linkID2Name.put(8, "link 2-6");
		linkID2Name.put(9, "link 2-7");
		linkID2Name.put(10, "link 2-8");
		linkID2Name.put(11, "link 3-6");
		linkID2Name.put(12, "link 3-7");
		linkID2Name.put(13, "link 3-8");
		linkID2Name.put(14, "link 4-9");
		linkID2Name.put(15, "link 4-10");
		linkID2Name.put(16, "link 4-11");
		linkID2Name.put(17, "link 5-9");
		linkID2Name.put(18, "link 5-10");
		linkID2Name.put(19, "link 5-11");
	}
	
	public static void make(int num_link, int num_fail) {
		Set<Integer> have_crash = new HashSet<Integer>();

		for (int i = 0; i < num_fail; i++) {
			int id = (int) (Math.random() * num_link);
			while (have_crash.contains(id)) {
				id = (int) (Math.random() * num_link);
			}
			double time = Math.random() * (end_time - 2) + 2;
			have_crash.add(id);
			System.out.println(linkID2Name.get(id) + "  " + time);
		}
	}

	public static void main(String[] args) {
		for(int i=0; i<10; i++) {
			LinkFailMaker.make(20, 4);
			System.out.println();
		}
			

	}

}
