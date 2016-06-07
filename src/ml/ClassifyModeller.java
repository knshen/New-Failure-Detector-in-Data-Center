package ml;

import java.util.*;
import java.io.*;

import weka.classifiers.Classifier;
import weka.classifiers.trees.J48;
import weka.core.Instances;
import weka.core.converters.ArffLoader;

public class ClassifyModeller {
	public Instances ins_train = null;
	public Instances ins_test = null;

	public void load() throws IOException {
		File file = new File("train.arff");
		ArffLoader atf = new ArffLoader();
		atf.setFile(file);
		ins_train = atf.getDataSet();
		file = new File("test.arff");
		atf.setFile(file);
		ins_test = atf.getDataSet();
	}

	public void classify() throws Exception {
		load();
		ins_train.setClassIndex(216);
		ins_test.setClassIndex(216);
		
		Classifier cr = new J48();
		cr.buildClassifier(ins_train);
		
		int correct = 0;
		for(int i=0; i<ins_test.numInstances(); i++) {
			double res = cr.classifyInstance(ins_test.instance(i));
			double should = ins_test.instance(i).classValue();
			double pro[] = cr.distributionForInstance(ins_test.instance(i));
			
			if(res == should) {		
				correct++;
			}
			else
				System.out.println("pro: " + pro[(int)res]);
				
		}
		
		System.out.println("correct ratio: " + (double)correct / ins_test.numInstances());
	}

	public static void main(String[] args) throws Exception {
		ClassifyModeller cm = new ClassifyModeller();
		cm.classify();
	}

}
