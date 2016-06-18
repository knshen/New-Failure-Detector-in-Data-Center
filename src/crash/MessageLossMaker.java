package crash;

import java.util.*;

import parser.DumpAnalyzer;
import util.Pair;

public class MessageLossMaker {
	public static final double send_interval = 0.1;
	
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
			List<Pair<Integer, Integer>> list, double loss_rate) {
		
		if (alr == null) {
			System.err.println("Analyzer no initialization!");
			return;
		}
		
		double total_num_msg = 0;
		
		for(Map.Entry<Integer, Map<Integer, List<Double>>> entry : alr.hb.entrySet()) {
			for(Map.Entry<Integer, List<Double>> en : entry.getValue().entrySet()) {
				total_num_msg += en.getValue().size();
			}
		}
		
		final int num_loss = (int)(total_num_msg * loss_rate);

		for(int i=0; i<num_loss; i++) {
			int relation_id = (int)(Math.random() * list.size());
			double start_to_lose_time = Math.random() * (end - start - 1)
					+ start;
			int master = list.get(relation_id).first;
			int slave = list.get(relation_id).second;
			
			List<Double> msgs = alr.hb.get(master).get(slave);
			int j = 0;
			for (; j < msgs.size(); j++)
				if (msgs.get(j) >= start_to_lose_time)
					break;

			System.err.println("#server" + master + " lose a messages of #server" + slave + " from time "
					+ start_to_lose_time);
			msgs.remove(j);
		}
	}

}
