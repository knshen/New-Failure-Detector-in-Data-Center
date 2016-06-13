package util;

public class Route {
	public int from;
	public int to;
	
	public Route(int from, int to) {
		this.from = from;
		this.to = to;
	}
	
	public String toString() {
		return from + " -> " + to;
	}
	
	public boolean equals(Object obj) {
		Route other = (Route)obj;
		return other.from == from && other.to == to;
	}
	
	public int hashCode() {
		return new Integer(from).hashCode() + new Integer(to).hashCode();
	}
}
