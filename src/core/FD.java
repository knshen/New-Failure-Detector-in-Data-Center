package core;

import java.util.*;
import java.io.*;

import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.PoissonDistribution;
import org.apache.commons.math3.stat.descriptive.moment.Mean;
import org.apache.commons.math3.stat.descriptive.moment.Variance;

import parser.DumpAnalyzer;

public class FD {
	// parameters
	public static final double time_unit = 0.0001;
	
	public FD() {

	}

	public double phiAccrualDetect(List<Double> hb_data, int win_size,
			double start, double end, double interval, double threshold) {
		List<Double> slide_win = new ArrayList<Double>();
		double last = -1;
		int pos = 0;

		// time unit : 0.1 ms = e-4 s
		for (double now = start; now < end; now += time_unit) {
			if (pos < hb_data.size() && hb_data.get(pos) <= now) {
				// receive message
				this.addToSlideWin(slide_win, hb_data.get(pos), win_size);
				last = hb_data.get(pos);
				pos++;
				// System.out.println("seq: " + seq);
			}

			if (slide_win.size() >= win_size)
				System.out.println();

			double phi_now = -1;
			if (slide_win.size() < win_size)
				phi_now = 0;
			else
				phi_now = -1 * Math.log(p_later(now - last, slide_win));

			// System.out.println("time: " + now + "   phi: " + phi_now);
			if (phi_now >= threshold) {
				System.out.println(phi_now);
				return now;
			}

		}

		return -1;
	}

	private double p_later(double t, List<Double> slide_win) {
		Object objs[] = slide_win.toArray();
		double data[] = new double[slide_win.size() - 1];

		for (int i = 1; i < objs.length; i++) {
			data[i - 1] = Double.parseDouble(objs[i].toString())
					- Double.parseDouble(objs[i - 1].toString());
		}

		double mu = new Mean().evaluate(data) * 10000;

		PoissonDistribution dist = new PoissonDistribution(mu);
		double res = 1 - dist.cumulativeProbability((int) (t * 10000));
		return res;
	}

	/**
	 * Chen failure detector for one detect pair
	 * 
	 * @param hb_data
	 *            : received heart beat data
	 * @param win_size
	 *            : slide window size
	 * @param start
	 * @param end
	 * @param interval
	 * @param alpha
	 * @return detected crash time, -1 for no crash
	 */
	public double chenDetect(List<Double> hb_data, int win_size, double start,
			double end, double interval, double threshold) {
		List<Double> slide_win = new ArrayList<Double>();

		int seq = 0;
		int pos = 0;
		// time unit : e-4s = 0.1ms
		for (double now = start; now < end; now += time_unit) {
			if (pos < hb_data.size() && hb_data.get(pos) <= now) {
				addToSlideWin(slide_win, hb_data.get(pos), win_size);
				pos++;
				seq++;
			}
			double fail_pro = chenDecideIsCrash(slide_win, now, interval, seq);

			if (fail_pro >= threshold) {
				// System.out.println("fail probability: " + fail_pro);
				return now;
			}

		}
		return -1;
	}

	private void addToSlideWin(List<Double> slide_win, double data, int win_size) {
		if (slide_win.size() < win_size)
			slide_win.add(data);
		else {
			slide_win.remove(0);
			slide_win.add(data);
		}
	}

	private double chenDecideIsCrash(List<Double> slide_win, double now,
			double interval, final int seq) {
		// seq : seq no. of the most recent received message in slide window
		int size = slide_win.size();
		if (size == 0)
			return 0;

		double tmp = 0;
		for (double hb : slide_win)
			tmp += hb;
		for (int i = 0; i <= size - 1; i++)
			tmp -= (seq - i) * interval;

		double EA = tmp / size + (seq + 1) * interval;

		return Math.tanh(now - EA);
	}

	public static void main(String[] args) throws IOException {
		DumpAnalyzer alr = new DumpAnalyzer("z://dump3//");
		alr.getServerPackets("topo");
		// 64 -> 100
		List<Double> data1 = alr.hb.get(64).get(100);
		// 65 -> 100
		List<Double> data2 = alr.hb.get(65).get(100);
		// 101 -> 100
		List<Double> data3 = alr.hb.get(101).get(100);

		FD fd = new FD();
		System.out.println("--------------------------------------------");
		System.out.println(fd.chenDetect(data1, 100, 2, 61, 0.1, 0.01));
		System.out.println(fd.chenDetect(data2, 100, 2, 61, 0.1, 0.01));
		System.out.println(fd.chenDetect(data3, 100, 2, 61, 0.1, 0.01));

		// System.out.println(fd.phiAccrualDetect(data1, 1000, 2, 601, 0.1,
		// 100));
		System.out.println();
	}

}
