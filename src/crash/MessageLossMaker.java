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
	 *            : global message send-receive relations : pair(A, B) means: A
	 *            -> B && A lose some messages of B
	 */
	public static void make(DumpAnalyzer alr, double start, double end,
			List<Pair<Integer, Integer>> list, double loss_rate) {

		if (alr == null) {
			System.err.println("Analyzer no initialization!");
			return;
		}

		double total_num_msg = 0;

		for (Map.Entry<Integer, Map<Integer, List<Double>>> entry : alr.hb
				.entrySet()) {
			for (Map.Entry<Integer, List<Double>> en : entry.getValue()
					.entrySet()) {
				total_num_msg += en.getValue().size();
			}
		}

		final int num_loss = (int) (total_num_msg * loss_rate);
		//System.out.println("total losed msg number: " + num_loss);

		for (int i = 0; i < num_loss; i++) {
			int relation_id = (int) (Math.random() * list.size());
			double start_to_lose_time = Math.random() * (end - start - 1)
					+ start;
			int master = list.get(relation_id).first;
			int slave = list.get(relation_id).second;

			List<Double> msgs = alr.hb.get(master).get(slave);
			int j = 0;
			for (; j < msgs.size(); j++)
				if (msgs.get(j) >= start_to_lose_time)
					break;

			//System.err.println("#server" + master
			//		+ " lose a messages of #server" + slave + " from time "
			//		+ start_to_lose_time);
			if(j >= msgs.size())
				j--;
			msgs.remove(j);
		}
	}

	/**
	 * this method is invoked when the message send-receive relations are
	 * uncertain (e.g. Gossip)
	 * 
	 * @param msgs
	 * @param start
	 * @param end
	 * @param loss_rate
	 */
	public static void make(Map<Integer, List<Pair<Double, Integer>>> msgs,
			double start, double end, double loss_rate) {
		double total_num_msg = 0;
		int node_num = 0;
		int node_base = -1;
		for (Map.Entry<Integer, List<Pair<Double, Integer>>> entry : msgs
				.entrySet()) {
			total_num_msg += entry.getValue().size();
			node_num++;
			if (node_base == -1 || entry.getKey() < node_base) {
				node_base = entry.getKey();
			}
		}

		final int num_loss = (int) (total_num_msg * loss_rate);
		System.out.println("total losed msg number: " + num_loss);

		for (int i = 0; i < num_loss; i++) {
			// randomly select a receiver
			int node_id = (int) (Math.random() * node_num) + node_base;
			// randomly select a msg seq number
			int msg_id = (int) (Math.random() * msgs.get(node_id).size());
			System.err.println("#server" + node_id + " lose a msg from #server"
					+ msgs.get(node_id).get(msg_id).second + " at time "
					+ msgs.get(node_id).get(msg_id).first);
			msgs.get(node_id).remove(msg_id);
		}
	}
}
