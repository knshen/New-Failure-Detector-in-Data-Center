package relatedWork.hierarchy;

import java.util.*;
import java.io.*;

import core.ServerFailureDetector;
import parser.DumpAnalyzer;

public class HierarchyServerDetector extends ServerFailureDetector {

	public HierarchyServerDetector(String dump_dir) throws IOException {
		super(dump_dir);
		alr.getServerPackets("hie");
		/*
		for(Map.Entry<Integer, Map<Integer, List<Double>>> entry : alr.hb.entrySet()) {
			System.out.println(entry.getKey() + ": " + entry.getValue().keySet());
		}*/
	}
	
	public void detect() throws IOException {
		this.detect(false, null);
	}
	
	public static void main(String[] args) throws IOException {
		HierarchyServerDetector hsd = new HierarchyServerDetector("z://hie1//");
		hsd.detect();
		System.out.println();
	}

}
