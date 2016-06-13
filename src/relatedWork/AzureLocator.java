package relatedWork;

import java.util.*;
import java.io.*;

import util.BiTreeNode;
import util.Route;
import util.Node;
import util.Util;

class Edge {
	int v1;
	int v2;

	public Edge(int v1, int v2) {
		this.v1 = v1;
		this.v2 = v2;
	}

	public String toString() {
		return v1 + " -- " + v2;
	}
	
	public boolean equals(Object obj) {
		Edge other = (Edge) obj;
		return (v1 == other.v1 && v2 == other.v2)
				|| (v1 == other.v2 && v2 == other.v1);
	}

	public int hashCode() {
		return new Integer(v1).hashCode() + new Integer(v2).hashCode();
	}
}

public class AzureLocator {
	PingParser pp = null;
	int dis[][];
	List<Edge> edges = new ArrayList<Edge>();
	Map<Edge, Integer> find = new HashMap<Edge, Integer>();

	public Map<Route, BiTreeNode> paths = new HashMap<Route, BiTreeNode>();
	public double pro_i_j_e[][][];

	public AzureLocator(String dir) throws IOException {
		pp = new PingParser(dir);
		dis = new int[pp.num_nodes][pp.num_nodes];
		for (int i = 0; i < pp.num_nodes; i++) {
			for (int j = i; j < pp.num_nodes; j++) {
				if (pp.topo[i][j] == 1) {
					Edge edge = new Edge(i, j);
					edges.add(edge);
					find.put(edge, edges.size() - 1);
					dis[i][j] = 1;
					dis[j][i] = 1;
				} else if (i != j) {
					dis[i][j] = 999;
					dis[j][i] = 999;
				}
			}
		}
		pro_i_j_e = new double[pp.num_nodes][pp.num_nodes][edges.size()];
	}

	public List<Double> locate() throws IOException {
		pp.getPingData();
		List<Double> x_e = new ArrayList<Double>();
		final int total_pings = (int)(pp.end - 2);
		
		for(Edge edge : edges) {
			int index = find.get(edge);
			double numerator = 0; 
			double denominator = 0;
			
			for(int i=0; i<pp.num_nodes; i++) {
				for(int j=0; j<pp.num_nodes; j++) {
					double pr = pro_i_j_e[i][j][find.get(edge)];
					numerator += (total_pings - pp.success_pings.get(i).get(j)) * pr;
					denominator += total_pings * pr * pr;
				}
			}
			
			x_e.add(numerator / denominator);
		}
		
		return x_e;
	}

	public void computeEdgePro() {
		// System.out.println(edges.size());
		for (Map.Entry<Route, BiTreeNode> entry : paths.entrySet()) {
			Route rou = entry.getKey();
			BiTreeNode root = entry.getValue();
			Queue<BiTreeNode> queue = new LinkedList<BiTreeNode>();
			queue.offer(root);

			while (!queue.isEmpty()) {
				BiTreeNode node = queue.poll();
				if (node.parent != null) {
					this.pro_i_j_e[rou.from][rou.to][find.get(new Edge(
							node.parent.id, node.id))] += node.pro;
				}
				for (BiTreeNode bn : node.children)
					queue.offer(bn);
			}
		}
	}

	private void adjustPathPro(BiTreeNode root) {
		if (root.parent == null)
			root.pro = 1;
		else
			root.pro = root.parent.pro / root.parent.children.size();
		for (BiTreeNode node : root.children) {
			adjustPathPro(node);
		}
	}

	public void getAllPath() {
		int[][] hops = Util.floyd(dis);
		for (int i = 0; i < pp.num_nodes; i++) {
			for (int j = 0; j < pp.num_nodes; j++) {
				if (i == j)
					continue;

				List<Node> list = Util.bfs(i, j, dis, hops[i][j]);
				Route edge = new Route(i, j);
				for (Node node : list) {
					Stack<Integer> path = new Stack<Integer>();
					Node n = node;
					while (n != null) {
						path.push(n.id);
						n = n.pre;
					}

					if (!paths.containsKey(edge)) {
						paths.put(edge, new BiTreeNode(i, null));
					}
					BiTreeNode root = paths.get(edge);
					BiTreeNode bnode = root;
					path.pop();
					while (!path.isEmpty()) {
						int id = path.pop();
						boolean contains = false;
						BiTreeNode nnode = null;
						for (BiTreeNode bin : bnode.children)
							if (bin.id == id) {
								nnode = bin;
								contains = true;
							}

						if (!contains) {
							nnode = new BiTreeNode(id, bnode);
							bnode.children.add(nnode);

						}
						bnode = nnode;
					}

				}
			}
		}

		for (Map.Entry<Route, BiTreeNode> entry : paths.entrySet())
			adjustPathPro(entry.getValue());

	}


	public static void main(String[] args) throws IOException {
		AzureLocator al = new AzureLocator("z://ping2//");
		al.getAllPath();
		al.computeEdgePro();
		// Util.printBiTree(al.paths.get(new Route(12, 72)));
	
		List<Double> failure = al.locate();
		int i = 0;
		for(Edge edge : al.edges) {
			System.out.println(edge + ": " + failure.get(i++));
		}
		
		System.out.println();

	}

}
