package core;

import java.util.*;
import java.io.*;

import evaluation.Evaluator;
import ml.ArffMaker;
import ml.ClassifyModeller;
import parser.DumpAnalyzer;
import util.Item;
import util.Route;
import util.Util;

public class LinkFailureDetector {
	// parameters
	public static final int check_interval = 1;
	
	public static final List<String> directions = new ArrayList<String>();
	public List<String> class_values = new ArrayList<String>();
	public List<List<Integer>> instances = new ArrayList<List<Integer>>();
	public List<String> classes = new ArrayList<String>();
	
	static {
		for(int i=0; i<6; i++) 
			for(int j=0; j<6; j++)
				directions.add(i + "-->" + j);
	}
	
	public void dataConstructor(String dump_dir, String mark, boolean isTrain) throws IOException {	
		DumpAnalyzer da = null;
		File dir = new File(dump_dir);
		class_values.add("normal");
		for(File file : dir.listFiles()) {
			if(file.isDirectory() && file.getName().startsWith(mark)) {
				//System.out.println("# " + file.getName());
				
				da = new DumpAnalyzer(file.getAbsolutePath());
				List<List<Map<String, Integer>>> res = da.getRSPackets(check_interval, 59);
				
				// form instances	
				String class_value = "";
				if(isTrain)
					class_value = file.getName().replaceAll(" ", "");
				else
					class_value = "normal";
				
				if(!class_value.endsWith("test")) {
					class_values.add(class_value);
				}
				
				int period = 0;
				for(List<Map<String, Integer>> dump_data : res) {
					// an instance == a time period
					List<Integer> ins = new ArrayList<Integer>(36 * 6);
					for(Map<String, Integer> map : dump_data) {
						for(String key : directions) {
							ins.add(map.get(key));
						}
					}
					if(period >= Math.ceil(30.0 / check_interval) - 1)
						if(!class_value.endsWith("test")) 
							classes.add(class_value);
						else
							classes.add("normal");
						
					else
						classes.add("normal");
					instances.add(ins);
					period++;
				}
			}
		}
		//System.out.println();
	}

	public List<Item> detect(String train_dump_dir, String test_dump_dir, String mark) throws Exception {	
		// load train data
		//dataConstructor(train_dump_dir, "link", true);	
		// ArffMaker.make("train.arff", instances, classes, class_values); 
		// load test data
		dataConstructor(test_dump_dir, mark, false);
		ArffMaker.make("test.arff", instances, classes, class_values); 
		
		ClassifyModeller cm = new ClassifyModeller();
		List<List<Item>> res = cm.classify();
		
		List<Item> fs = new ArrayList<Item>();
		
		for(int i=0; i<res.size()-1; i++) {
			for(int j=0; j<res.get(i).size(); j++) {
				Item item = res.get(i).get(j);		
				if(i == 0) {
					fs.add(item);
				}
				else {
					fs.get(j).value += item.value;
				}
			}
		}
		
		Util.normalize(fs);
		Collections.sort(fs);
		
		/*
		for(Item item : fs) {	
			System.out.println(item);
		}*/
		
		/*
		for(int i=0; i<res.size(); i++) {
			System.out.println(i+3);
			for(Item item : res.get(i))
				System.out.println(item.item_name + ": " + item.value);
			System.out.println();
		} 
		*/
		
		/*
		for(int i=1; i<res.size(); i++) {
			System.out.println(Evaluator.discrimination(res.get(i)));
		} */
		
		//System.out.println(Evaluator.discrimination(res.get(i), 21));
		return fs;	
	}
	
	public void try_it() throws IOException {
		Map<String, Integer> dirs = new HashMap<String, Integer>();

		Map<Integer, List<Integer>> sends = Util.getSendRelations();
		for (Map.Entry<Integer, List<Integer>> entry : sends.entrySet()) {
			int from = entry.getKey();
			for (int to : entry.getValue()) {
				String dir = Util.getRackIdByServerId(from) + "-->"
						+ Util.getRackIdByServerId(to);
				if (dirs.containsKey(dir))
					dirs.put(dir, dirs.get(dir) + 1);
				else
					dirs.put(dir, 1);
			}
		}

		for (Map.Entry<String, Integer> entry : dirs.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue());
		}
	}

	public void run() throws Exception {
		List<List<String>> faults = new ArrayList<List<String>>();
		faults.add(Arrays.asList("link2-8", "link4-9", "link5-10", "link0-5"));
		faults.add(Arrays.asList("link3-7", "link5-11", "link0-5", "link4-10"));
		faults.add(Arrays.asList("link2-8", "link1-2", "link0-3", "link4-10"));
		faults.add(Arrays.asList("link0-3", "link0-2", "link1-5", "link4-9"));
		faults.add(Arrays.asList("link2-8", "link0-2", "link0-3", "link3-6"));
		int k = 4;
		
		//////////////////////////////////////////
		List<Double> precisions = new ArrayList<Double>();
		List<Integer> mrrs = new ArrayList<Integer>();
		List<Double> ds = new ArrayList<Double>();
		
		for (int i = 1; i <= 5; i++) {
			LinkFailureDetector lfd = new LinkFailureDetector();
			String mark = "dump-" + k + "-" + i;
			List<Item> res = lfd.detect("z://",
					"z://LinkCrashDump//",
					mark);

			precisions.add(Evaluator.precision_k(res, k, faults.get(i-1)));
			mrrs.add(Evaluator.MRR(res, faults.get(i-1)));
			//System.out.println(Evaluator.DS(res));
			ds.add(Evaluator.DS(res));

		}

		System.out.println("precision@k: " + Util.average(precisions));
		System.out.println("MRR: " + Util.inverseSum(mrrs));
		System.out.println("Avg¡¡DS: " + Util.average(ds));
	}
	
	public static void main(String[] args) throws Exception {
		LinkFailureDetector lfd = new LinkFailureDetector();
		// lfd.try_it();
		
		lfd.run();
		
		System.out.println();
	}

}
