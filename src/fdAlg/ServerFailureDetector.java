package fdAlg;

import java.util.*;
import java.io.*;

import evaluation.Evaluator;
import parser.Analyzer;

public class ServerFailureDetector {
	Map<Integer, Double> crashes = new HashMap<Integer, Double>();

	private int distance(int id1, int id2) {
		int rackId1 = (id1 - 12) / 20;
		int rackId2 = (id2 - 12) / 20;
		if (rackId1 == rackId2)
			return 0;
		if ((rackId1 >= 0 && rackId1 <= 2 && rackId2 >= 0 && rackId2 <= 2)
				|| (rackId1 >= 3 && rackId1 <= 5 && rackId2 >= 3 && rackId2 <= 5))
			return 1;
		return 2;
	}

	public void readCrashFile(String path) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(new File(path)));
		String line = "";
		while ((line = br.readLine()) != null) {
			int id = Integer.parseInt(line.split(" ")[0]);
			double time = Double.parseDouble(line.split(" ")[1]);
			crashes.put(id, time);
		}
		br.close();
	}

	public Map<Integer, Double> detect() throws IOException {
		// key is reported at time [...]
		Map<Integer, Double> final_alerts = new HashMap<Integer, Double>();
		Map<Integer, List<Double>> alerts = new HashMap<Integer, List<Double>>();
		FD fd = new FD();

		Analyzer alr = new Analyzer();
		alr.formalize();
		for (int i = 12; i <= 131; i++) {
			// i: master
			Map<Integer, List<Double>> map = alr.hb.get(i);
			for (Map.Entry<Integer, List<Double>> entry : map.entrySet()) {
				double res = fd.chenDetect(entry.getValue(), 10, 2, 60, 0.1,
						0.001);
				if (res != -1
						&& (!crashes.containsKey(i) || crashes.get(i) > res)) {
					if (alerts.containsKey(entry.getKey()))
						alerts.get(entry.getKey()).add(res);

					else {
						List<Double> list = new ArrayList<Double>();
						list.add(res);
						alerts.put(entry.getKey(), list);
					}

					// System.out.println(i + " report crash of " +
					// entry.getKey() + " at time " + res);
				}

			}

		}
		// analyze
		for (Map.Entry<Integer, List<Double>> entry : alerts.entrySet()) {
			Collections.sort(entry.getValue());
			if (entry.getValue().size() >= 2) {
				final_alerts.put(entry.getKey(), entry.getValue().get(0));
				System.out.println("server " + entry.getKey()
						+ " crashed at time: " + entry.getValue().get(0));
			}

		}
		return final_alerts;
	}

	public static void main(String[] args) throws IOException {
		ServerFailureDetector sfd = new ServerFailureDetector();
		sfd.readCrashFile("server-crash-1min.txt");
		Map<Integer, Double> alerts = sfd.detect();

		System.out.println(Evaluator.avgDetectTime(sfd.crashes, alerts));
	}

}
