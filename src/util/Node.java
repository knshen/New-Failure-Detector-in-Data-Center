package util;

public class Node {
	public int id;
	public Node pre;
	public int step;
	
	public Node(int id, Node pre, int step) {
		this.id = id;
		this.pre = pre;
		this.step = step;
	}
}