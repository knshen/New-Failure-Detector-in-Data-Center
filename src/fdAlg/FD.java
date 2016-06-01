package fdAlg;

import java.util.*;
import java.io.*;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import parser.Analyzer;

public class FD {
	public FD() {
		
	}
	
	public double phiAccrualDetect(List<Double> hb_data, int win_size, double start, double end, double interval, double threshold) {
		List<Double> slide_win = new ArrayList<Double>();	
		double last = -1;
		int pos = 0;
		
		for(double now = start; now<end; now+=0.0001) {
			if(pos < hb_data.size() && hb_data.get(pos) <= now) {
				// receive message
				this.addToSlideWin(slide_win, hb_data.get(pos), win_size);
				last = hb_data.get(pos);
				pos++;
				//System.out.println("seq: " + seq);
			}
			
			double phi_now = -1;
			if(last == -1)
				phi_now = 0;
			else
				phi_now = -1 * Math.log(p_later(now-last, slide_win));
			if(now >= 2.9027) {
				System.out.println();
			}
			System.out.println("time: " + now + "   phi: " + phi_now);
			if(phi_now >= threshold)
				return now;
		}
		
		return -1;
	}
	
	private double p_later(double t, List<Double> slide_win) {
		Object objs[] = slide_win.toArray();
		double data[] = new double[slide_win.size()-1];
		
		
		for(int i=1; i<objs.length; i++) {
			data[i-1] = Double.parseDouble(objs[i].toString()) - Double.parseDouble(objs[i-1].toString());
		}
		
		double mu = new Mean().evaluate(data);
		double sigma = new Variance().evaluate(data);
		if(sigma == 0)
			sigma = 0.1;
		NormalDistribution ndb = new NormalDistribution(mu, sigma);
		double res = 1 - ndb.cumulativeProbability(t);
		return res;
	}
	
	/**
	 * Chen failure detector for one detect pair
	 * @param hb_data : received heart beat data
	 * @param win_size : slide window size
	 * @param start 
	 * @param end
	 * @param interval
	 * @param alpha
	 * @return detected crash time, -1 for no crash
	 */
	public double chenDetect(List<Double> hb_data, int win_size, double start, double end, double interval, double alpha) {			
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
			if(chenDecideIsCrash(slide_win, now, interval, seq, alpha))
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
	
	private boolean chenDecideIsCrash(List<Double> slide_win, double now, double interval, final int seq, double alpha) {
		// seq : seq no. of the most recent received message in slide window
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
		// 20 -> 80
		List<Double> data1 = alr.hb.get(20).get(80);	
		// 40 -> 80
		List<Double> data2 = alr.hb.get(40).get(80);
		// 81 -> 80
		List<Double> data3 = alr.hb.get(81).get(80);
		
		FD fd = new FD();
		
		for(int k=0; k<5; k++) {
			data1.remove(data1.size()-1);
			data2.remove(data2.size()-1);
			data3.remove(data3.size()-1);
		}
			
		
		System.out.println(fd.chenDetect(data1, 10, 2, 12.6, 0.1, 0.1*0.1));
		System.out.println(fd.chenDetect(data2, 10, 2, 12.6, 0.1, 0.2*0.1));
		System.out.println(fd.chenDetect(data3, 10, 2, 12.6, 0.1, 0.3*0.1));
		//System.out.println(fd.phiAccrualDetect(data, 50, 2, 12.6, 0.1, 100));
		System.out.println();
	}

}
