package core;

import java.util.*;
import java.io.*;

import ml.ArffMaker;
import ml.ClassifyModeller;
import parser.DumpAnalyzer;
import util.Route;
import util.Util;

public class LinkFailureDetector {
	// parameters
	public static final int check_interval = 2;
	
	public static final List<String> directions = new ArrayList<String>();
	public List<String> class_values = new ArrayList<String>();
	public List<List<Integer>> instances = new ArrayList<List<Integer>>();
	public List<String> classes = new ArrayList<String>();
	
	static {
		for(int i=0; i<6; i++) 
			for(int j=0; j<6; j++)
				directions.add(i + "-->" + j);
	}
	
	public void dataConstructor() throws IOException {	
		DumpAnalyzer da = null;
		File dir = new File("z://");
		class_values.add("normal");
		for(File file : dir.listFiles()) {
			if(file.isDirectory() && file.getName().startsWith("link")) {
				//System.out.println("# " + file.getName());
				
				da = new DumpAnalyzer(file.getAbsolutePath());
				List<List<Map<String, Integer>>> res = da.getRSPackets(check_interval, 59);
				
				// form instances	
				String class_value = file.getName().replaceAll(" ", "");
				
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
		System.out.println();
	}

	public void detect() throws IOException {
		ClassifyModeller cm = new ClassifyModeller();
		dataConstructor();
		//////
		ArffMaker.randomSplit(instances, classes, class_values, 0.9524, false);
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

	public static void main(String[] args) throws IOException {
		LinkFailureDetector lfd = new LinkFailureDetector();
		// lfd.try_it();
		lfd.detect();
		
	}

}
