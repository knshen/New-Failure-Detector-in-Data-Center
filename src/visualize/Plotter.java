package visualize;

import java.io.*;
import java.util.*;

public class Plotter {

	public Plotter() {

	}

	private Map<String, List<String>> getRule() throws IOException {
		Map<String, List<String>> rule = new HashMap<String, List<String>>();

		BufferedReader br = new BufferedReader(new FileReader(new File(
				"rule.txt")));
		String line = "";
		while ((line = br.readLine()) != null) {
			String slave = line.split(":")[0];
			String masters[] = line.split(":")[1].split(" ");
			rule.put(slave, Arrays.asList(masters));
		}
		return rule;
	}

	public void plotMonitorRule() throws IOException {
		Map<String, List<String>> rule = getRule();

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				"rule.dot")));

		bw.write("digraph G {" + "\n");
		bw.write("subgraph cluster0 {\n");
		// bw.write("style=filled\ncolor=lightgrey\n");
		for (int i = 12; i <= 31; i++) {
			bw.write(String.valueOf(i) + "\n");
		}
		bw.write("}\n");

		bw.write("subgraph cluster1 {\n");
		// bw.write("style=filled\ncolor=lightgrey\n");
		for (int i = 32; i <= 51; i++) {
			bw.write(String.valueOf(i) + "\n");
		}
		bw.write("}\n");

		bw.write("subgraph cluster2 {\n");
		// bw.write("style=filled\ncolor=lightgrey\n");
		for (int i = 52; i <= 71; i++) {
			bw.write(String.valueOf(i) + "\n");
		}
		bw.write("}\n");

		bw.write("subgraph cluster3 {\n");
		// bw.write("style=filled\ncolor=lightgrey\n");
		for (int i = 72; i <= 91; i++) {
			bw.write(String.valueOf(i) + "\n");
		}
		bw.write("}\n");

		bw.write("subgraph cluster4 {\n");
		// bw.write("style=filled\ncolor=lightgrey\n");
		for (int i = 92; i <= 111; i++) {
			bw.write(String.valueOf(i) + "\n");
		}
		bw.write("}\n");

		bw.write("subgraph cluster5 {\n");
		// bw.write("style=filled\ncolor=lightgrey\n");
		for (int i = 112; i <= 131; i++) {
			bw.write(String.valueOf(i) + "\n");
		}
		bw.write("}\n");

		for (Map.Entry<String, List<String>> entry : rule.entrySet()) {
			List<String> list = entry.getValue();
			for (String master : list) {
				String line = master + "->" + entry.getKey() + "\n";
				bw.write(line);
			}
		}

		bw.write("}\n");
		bw.flush();
		bw.close();

	}

	public static void main(String args[]) throws IOException {
		Plotter pl = new Plotter();
		pl.plotMonitorRule();
	}
}
