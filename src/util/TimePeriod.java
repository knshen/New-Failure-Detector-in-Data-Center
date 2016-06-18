package util;

public class TimePeriod {
	public double begin;
	public double end;
	
	public TimePeriod(double b, double e) {
		this.begin = b;
		this.end = e;
	}
	
	public String toString() {
		return "<" + begin + " " + end + ">";
	}
}
