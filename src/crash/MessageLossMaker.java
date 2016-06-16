package crash;

import java.util.*;

import parser.DumpAnalyzer;
import util.Pair;

public class MessageLossMaker {

	/**
	 * message loss during a very short time (caused by explosion of flow)
	 * 
	 * @param alr
	 * @param start
	 * @param end
	 * @param list
	 *            : pair(A, B) means: A -> B && A lose some messages of B
	 */
	public static void make(DumpAnalyzer alr, double start, double end,
			List<Pair<Integer, Integer>> list) {
		////
		final int num_loss = 2;

		if (alr == null) {
			System.err.println("Analyzer no initialization!");
			return;
		}

		for (Pair<Integer, Integer> pair : list) {
			int master = pair.first;
			int slave = pair.second;
			double start_to_lose_time = Math.random() * (end - start - 1)
					+ start;

			List<Double> msgs = alr.hb.get(master).get(slave);
			int i = 0;
			for (; i < msgs.size(); i++)
				if (msgs.get(i) >= start_to_lose_time)
					break;

			System.err.println("#server" + master + " lose " + num_loss
					+ " messages of #server" + slave + " from time "
					+ start_to_lose_time);
			for (int j = 0; j < num_loss; j++)
				msgs.remove(i);
		}

	}

	public static void main(String[] args) {

	}

}
