package ml;

import java.io.*;
import java.util.*;

public class ArffMaker {
	public static void make(String file_name, List<List<Integer>> data,
			List<String> classes, List<String> class_values) throws IOException {
		clean(data);
		final int data_size = data.size();
		final int num_attr = data.get(0).size();

		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(
				file_name)));
		bw.write("@RELATION rel\n");
		for (int i = 0; i < num_attr; i++) {
			bw.write("@ATTRIBUTE attr" + i + " NUMERIC\n");
		}
		String str = "";
		for (int i = 0; i < class_values.size() - 1; i++) {
			str += class_values.get(i) + ",";
		}
		str += class_values.get(class_values.size() - 1);

		bw.write("@ATTRIBUTE class {" + str + "}\n");
		bw.write("@DATA\n");

		int pos = 0;
		for (List<Integer> instance : data) {
			String line = "";
			for (int val : instance) {
				line += val + ",";
			}
			line += classes.get(pos) + "\n";
			pos++;
			bw.write(line);
		}

		bw.flush();
		bw.close();
	}

	private static void clean(List<List<Integer>> data) {
		for (List<Integer> list : data) {
			for (int i = 0; i < list.size(); i++) {
				if (list.get(i) == null)
					list.set(i, 0);
			}
		}
	}

	public static void randomSplit(List<List<Integer>> data,
			List<String> classes, List<String> class_values, double factor)
			throws IOException {
		int train_size = (int) (data.size() * factor);
		int test_size = data.size() - train_size;

		// shuffle
		for (int k = 0; k < 10000; k++) {
			int i = (int) (Math.random() * data.size());
			int j = (int) (Math.random() * data.size());
			Collections.swap(data, i, j);
			Collections.swap(classes, i, j);
		}

		List<List<Integer>> train_data = new ArrayList<List<Integer>>();
		List<String> train_classes = new ArrayList<String>();
		List<List<Integer>> test_data = new ArrayList<List<Integer>>();
		List<String> test_classes = new ArrayList<String>();

		for (int i = 0; i < train_size; i++) {
			train_data.add(data.get(i));
			train_classes.add(classes.get(i));
		}
		for (int i = train_size; i < data.size(); i++) {
			test_data.add(data.get(i));
			test_classes.add(classes.get(i));
		}

		make("train.arff", train_data, train_classes, class_values);
		make("test.arff", test_data, test_classes, class_values);
	}

}
