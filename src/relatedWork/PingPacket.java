package relatedWork;

public class PingPacket {
	double time;
	String src; // ip
	String dest;
	int seq;
	//String type;
	
	public String toString() {
		return time + ": " + src + " -> " + dest + " " + seq;
	}
}
