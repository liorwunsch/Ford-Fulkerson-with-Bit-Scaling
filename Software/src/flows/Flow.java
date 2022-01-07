// Lior Wunsch - 206238263
package flows;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.Graphs;

public class Flow {
	private Graph graph;

	// generate a flow with n+5 nodes
	public Flow(int n) {
		this.graph = EGraph.EGraph1(n);
	}

	// generate residual flow with edges having a capacity
	public Flow(Flow f) {
		Graph gf = Graphs.clone(f.graph);
		for (Node n : gf) {
			Iterator<Edge> it = n.getEdgeIterator();
			while (it.hasNext()) {
				if ((int) it.next().getAttribute("capacity") <= 0)
					it.remove();
			}
		}
		graph = gf;
	}

	// generate a flow from example graph
	public Flow(Graph g) {
		this.graph = g;
	}

	// implementation of Edmonds-Karp algorithm using Ford-Fulkerson method -
	// O(min{nm^2, mF})
	// calculates max flow value in flow network r using bfs
	public static int getMaxFlow(Flow r) {
		Graph g = r.graph;

		final long startTime = System.nanoTime();
		for (Edge e : g.getEachEdge())
			e.setAttribute("flow", 0);

		// generate a residual flow with a similar graph gf
		// with same capacities ignoring edges with 0 capacity
		Flow rf = new Flow(r);
		Graph gf = rf.graph;

		for (Edge ef : gf.getEachEdge()) {
			Edge e = g.getEdge(ef.getId());
			// calculate new edge capacity in gf
			int cf = (int) e.getAttribute("capacity") - (int) e.getAttribute("flow");
			ef.setAttribute("capacity", cf);
		}

		// remove all edges in gf with no capacity
		for (Node n : gf) {
			Iterator<Edge> it = n.getEdgeIterator();
			while (it.hasNext()) {
				if ((int) it.next().getAttribute("capacity") <= 0)
					it.remove();
			}
		}

		// find a route from s to t in gf, while there is one update flows
		initAdj(gf);
		BFS(gf);
		while ((int) gf.getNode("t").getAttribute("d-s") != -1) {
			List<Edge> p = getPath(gf, "t");

			// find minimal residual capacity rc of edges on path p in gf
			int cfp = p.get(0).getAttribute("capacity");
			for (Edge ef : p)
				if ((int) ef.getAttribute("capacity") < cfp)
					cfp = ef.getAttribute("capacity");

			// raise the flow of g along path p by rc
			for (Edge ef : p) {
				// for each edge in gf get the same edge in g
				Edge e = g.getEdge(ef.getId());
				int f = (int) e.getAttribute("flow") + cfp;
				e.setAttribute("flow", f);

				// determine the flow on edges opposite to path in g
				Node u = e.getSourceNode();
				Node v = e.getTargetNode();
				Edge eo = v.getEdgeToward(u);
				eo.setAttribute("flow", f * (-1));
			}

			// update the residual graph along the path
			for (Edge ef : p) {
				Edge e = g.getEdge(ef.getId());
				// calculate new edge capacity in gf
				int cf = (int) e.getAttribute("capacity") - (int) e.getAttribute("flow");
				ef.setAttribute("capacity", cf);

				// find opposite edge in g
				Node u = e.getSourceNode();
				Node v = e.getTargetNode();
				Edge eo = v.getEdgeToward(u);

				// find opposite edge in gf
				// if it doesn't exist, add it to gf
				Node uf = ef.getSourceNode();
				Node vf = ef.getTargetNode();
				if (!vf.hasEdgeToward(uf))
					gf.addEdge(vf.getId() + "-" + uf.getId(), vf, uf, true);
				Edge efo = vf.getEdgeToward(uf);

				// calculate new opposite edge capacity in gf
				int cfo = (int) eo.getAttribute("capacity") - (int) eo.getAttribute("flow");
				efo.setAttribute("capacity", cfo);
			}

			// remove all edges in gf with no capacity
			for (Node n : gf) {
				Iterator<Edge> it = n.getEdgeIterator();
				while (it.hasNext()) {
					if ((int) it.next().getAttribute("capacity") <= 0)
						it.remove();
				}
			}

			// find new route from s to t in gf
			initAdj(gf);
			BFS(gf);
		}

		int F = 0;
		Node s = g.getNode("s");
		List<Node> l = s.getAttribute("adj");
		for (Node n : l) {
			Edge e = s.getEdgeToward(n);
			F += (int) e.getAttribute("flow");
		}

		final long duration = System.nanoTime() - startTime;
		System.out.format("Ford-Fulkerson running time : " + duration / Math.pow(10, 9) + " seconds.\n");
		return F;
	}

	// add adjacency list for each node
	// adjacent of a node has an edge from the node to it
	public static void initAdj(Graph g) {
		for (Node u : g.getEachNode()) {
			List<Node> adj = new ArrayList<>();
			for (Node v : g.getEachNode())
				if (u.hasEdgeToward(v))
					adj.add(v);
			u.setAttribute("adj", adj);
		}
	}

	// build BFS traversal from a given source s
	public static void BFS(Graph g) {
		// a list for all visited nodes
		List<Node> visited = new ArrayList<>();

		// a queue for BFS
		List<Node> queue = new ArrayList<>();

		Node s = g.getNode("s");
		// initialize parameters for u in V\{s}
		for (Node u : g.getEachNode()) {
			if (!u.equals(s)) {
				u.setAttribute("d-s", -1); // distance from s to node
				u.setAttribute("pi-s", "0"); // predecessor of node in the path from s to node
			}
		}

		// mark source node as visited and add it to the queue
		// distance to itself is 0 and it has no predecessor
		visited.add(s);
		s.setAttribute("d-s", 0);
		s.setAttribute("pi-s", 0);
		queue.add(s);

		while (queue.size() != 0) {
			// starting from u = s
			Node u = queue.remove(0);
			int dsu = u.getAttribute("d-s");
			List<Node> adj = u.getAttribute("adj");

			// for each adjacent node v of u, if not visited, mark as visited
			// set distance and predecessor in path from s and add to queue
			for (Node v : adj) {
				if (!visited.contains(v)) {
					visited.add(v);
					v.setAttribute("d-s", dsu + 1);
					v.setAttribute("pi-s", u);
					queue.add(v);
				}
			}
		}
	}

	// get path (edges) from graph g's source to node t recursively
	public static List<Edge> getPath(Graph g, String tid) {
		List<Edge> path = new ArrayList<>();
		Node s = g.getNode("s");
		Node t = g.getNode(tid);
		if (t.equals(s))
			return path;

		if (t.getAttribute("pi-s").equals("0"))
			return path;

		Node p = t.getAttribute("pi-s");
		path.addAll(getPath(g, p.getId()));
		if (p.getDegree() != 0)
			path.add(p.getEdgeBetween(t));

		return path;
	}

	public Graph getGraph() {
		return graph;
	}

	public static void main(String args[]) {
		Scanner scanIn = new Scanner(System.in);
		System.out.print("Bit-Scaling ford-fulkerson VS ford-fulkerson\nPlease enter number of nodes: ");
		int n = scanIn.nextInt();
		Flow f = new Flow(n-5);
		System.out.format("\nThe flow graph has %d nodes", f.graph.getNodeCount());
		System.out.format(" and %d edges\n", f.graph.getEdgeCount());
		BitScaling.getMaxFlow(f);
		Flow.getMaxFlow(f);
		scanIn.close();
	}
}