package ml;

import java.util.*;
import java.io.*;

import util.Item;
import weka.classifiers.Classifier;
import weka.classifiers.bayes.BayesNet;
import weka.classifiers.functions.MultilayerPerceptron;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ArffLoader;

public class ClassifyModeller {
	public Instances ins_train = null;
	public Instances ins_test = null;
	public static List<String> class_mapping = new ArrayList<String>();

	static {
		class_mapping = Arrays.asList("normal", "link0-2", "link0-3",
				"link0-4", "link0-5", "link1-2", "link1-3", "link1-4",
				"link1-5", "link2-6", "link2-7", "link2-8", "link3-6",
				"link3-7", "link3-8", "link4-10", "link4-11", "link4-9",
				"link5-10", "link5-11", "link5-9");
	}

	private void load() throws IOException {
		File file = new File("train.arff");
		ArffLoader atf = new ArffLoader();
		atf.setFile(file);
		ins_train = atf.getDataSet();
		file = new File("test.arff");
		atf.setFile(file);
		ins_test = atf.getDataSet();
	}

	public List<List<Item>> classify() throws Exception {
		List<List<Item>> ress = new ArrayList<List<Item>>();
		load();
		ins_train.setClassIndex(216);
		ins_test.setClassIndex(216);

		File model = new File("ann.model");
		
		// Serialize
		if(!model.exists()) {
			Classifier cr = new MultilayerPerceptron();
			cr.buildClassifier(ins_train);
			SerializationHelper.write("ann.model", cr);
		}
			
		Classifier cr = (Classifier)SerializationHelper.read("ann.model");
				
		for (int i = 0; i < ins_test.numInstances(); i++) {
			// one instance:
			List<Item> list = new ArrayList<Item>();
			
			double res = cr.classifyInstance(ins_test.instance(i));
			//double should = ins_test.instance(i).classValue();
			double pro[] = cr.distributionForInstance(ins_test.instance(i));

			for(int j=0; j<pro.length; j++) {
				list.add(new Item(class_mapping.get(j), pro[j]));
				//System.out.print(class_mapping.get(j) + ": " + pro[j] + "	");
			}
			
			Collections.sort(list);
			ress.add(list);
		} // end for

		return ress;
	}

	public static void main(String[] args) throws Exception {
		ClassifyModeller cm = new ClassifyModeller();
		cm.classify();
	}

}
