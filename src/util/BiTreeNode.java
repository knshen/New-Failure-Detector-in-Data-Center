package util;

import java.util.*;

public class BiTreeNode {
	public int id;
	public List<BiTreeNode> children = new ArrayList<BiTreeNode>();
	public BiTreeNode parent = null;
	public double pro;
	
	public BiTreeNode(int id, BiTreeNode parent) {
		this.id = id;
		this.pro = pro;
		this.parent = parent;
	}
}

