package fdAlg;

import java.util.*;
import java.io.*;
import parser.Analyzer;

public class FD {

	public FD() {
		
	}
	

	public double chenDetect(List<Double> hb_data, int win_size, double start, double end, double interval) {			
		List<Double> slide_win = new ArrayList<Double>();
		
		int seq = 0;
		int pos = 0;
		// time unit : e-6s = us
		for(double now = start; now<end; now+=0.000001) {	
			if(pos < hb_data.size() && hb_data.get(pos) <= now) {
				addToSlideWin(slide_win, hb_data.get(pos), win_size);
				pos++;
				seq++;
			}
			if(decideIsCrash(slide_win, now, interval, seq))
				return now;
			
		}
		return -1;
	}
	
	private void addToSlideWin(List<Double> slide_win, double data, int win_size) {
		if(slide_win.size() < win_size)
			slide_win.add(data);
		else {
			slide_win.remove(0);
			slide_win.add(data);
		}
	}
	
	private boolean decideIsCrash(List<Double> slide_win, double now, double interval, final int seq) {
		// seq : seq no. of the most recent received message in slide window
		double alpha = interval;
		int size = slide_win.size();
		if(size == 0)
			return false;
			
		double tmp = 0;
		for(double hb : slide_win) 
			tmp += hb;
		for(int i=0; i<=size-1; i++) 
			tmp -= (seq-i) * interval;
		
		double EA = tmp / size + (seq+1) * interval;
			
		return (EA + alpha) <= now;  
	}
	
	public static void main(String[] args) throws IOException {
		Analyzer alr = new Analyzer();
		alr.formalize();
		List<Double> data = alr.hb.get(20).get(80);
		
		FD fd = new FD();
		for(int k=0; k<5; k++)
			data.remove(data.size()-1);
		System.out.println(fd.chenDetect(data, 10, 2, 22, 0.1));
		System.out.println();
	}

}
