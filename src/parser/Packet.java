package parser;

public class Packet {
	public double time;
	public String src; // ip
	public String dest; // ip
	public int length; // byte

	public String toString() {
		return time + ": " + src + " -> " + dest;
	}
}
