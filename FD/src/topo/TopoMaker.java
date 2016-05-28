package topo;

import java.io.*;
import java.util.*;

public class TopoMaker {
	String filePath = "";
	
	public TopoMaker(String fp) {
		this.filePath = fp;
	}
	
	public int[][] make(int n) {
		// ÉÏÈı½Ç¾ØÕó
		int matrix[][] = new int[n][n];
		int links = 0;
		
		for(int tor = 6; tor<=11; tor++) {
			int offset = tor - 6;
			for(int i=0; i<20; i++) {
				matrix[tor][offset*20+i+12] = 1;
				links++;
			}
		}
		
		matrix[0][2] = 1;
		matrix[0][3] = 1;
		matrix[0][4] = 1;
		matrix[0][5] = 1;
		
		matrix[1][2] = 1;
		matrix[1][3] = 1;
		matrix[1][4] = 1;
		matrix[1][5] = 1;

		matrix[2][6] = 1;
		matrix[2][7] = 1;
		matrix[2][8] = 1;
		matrix[3][6] = 1;
		matrix[3][7] = 1;
		matrix[3][8] = 1;

		matrix[4][9] = 1;
		matrix[4][10] = 1;
		matrix[4][11] = 1;
		matrix[5][9] = 1;
		matrix[5][10] = 1;
		matrix[5][11] = 1;

		links += 20;
		
		//System.out.println("# of links: " + links);
		return matrix;
	}
	
	public void writeTopo(int a[][], int n) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File(filePath)));
		for(int i=0; i<n; i++) {
			String line = "";
			for(int j=0; j<n; j++) {
				line += a[i][j] + "\t";
			}
			bw.write(line + "\n");
		}
		bw.flush();
		bw.close();
	}
	
	public void plot(int a[][], int n) throws IOException {
		BufferedWriter bw = new BufferedWriter(new FileWriter(new File("topo.dot")));
		bw.write("graph G {" + "\n");
		for(int i=0; i<12; i++) {
			bw.write(i+"[shape=box];" + "\n");
		}
		
		for(int i=0; i<n; i++) {
			for(int j=0; j<n; j++) {
				if(a[i][j] == 1) {
					bw.write(i + " -- " + j + "\n");
				}
			}
		}
		
		bw.write("}\n");
		bw.flush();
		bw.close();
		
		/*
		Runtime rt = Runtime.getRuntime();
		String[] args = {"dot", "-T"+"jpg", "topo.dot", "-o", "topo.jpg"};
        Process p = rt.exec(args);
		*/
	}
	
	public static void main(String[] args) throws IOException {
		TopoMaker tm = new TopoMaker("topo.txt");
		int n = 132;
		
		int a[][] = tm.make(n);
		tm.writeTopo(a, n);
		tm.plot(a, n);
	}

}
