package parser;

public class Packet {
	double time;
	String src; // ip
	String dest; // ip
	int length; // byte

	public String toString() {
		return time + ": " + src + " -> " + dest;
	}
}
