// Lior Wunsch - 206238263
package flows;

import java.util.Random;

import org.graphstream.algorithm.generator.BaseGenerator;
import org.graphstream.algorithm.generator.DorogovtsevMendesGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class EGraph {

	public static Graph EGraph1(int n) {
		// keep generating graphs and fit them to a flow network
		// until there is a path from source to sink
		while (true) {
			BaseGenerator gen = new DorogovtsevMendesGenerator();
			Graph g = new SingleGraph("Random " + (n + 5));

			gen.addSink(g);
			gen.setDirectedEdges(true, false);
			gen.begin();
			for (int i = 0; i < n; i++)
				gen.nextEvents();
			gen.end();

			// add source and connect it to the graph
			Node s = g.addNode("s");
			int count = 0;
			while (count < n / 3) {
				Node v = g.getNode(count);
				g.addEdge("s-" + v.getId(), s, v, true);
				count++;
			}

			// add target and connect the graph to it
			Node t = g.addNode("t");
			count = n - 1;
			while (count >= (2 * n) / 3) {
				Node v = g.getNode(count);
				g.addEdge(v.getId() + "-t", v, t, true);
				count--;
			}

			// add weights to all edges randomally
			Random rand = new Random();
			for (Edge e : g.getEachEdge()) {
				int a = 0;
				while (a == 0) {
					// a = rand.nextInt(g.getEdgeCount());
					// a = rand.nextInt((int) Math.log(g.getEdgeCount()));
					a = rand.nextInt((int) Math.log10(g.getEdgeCount()) + 1);
					// a = rand.nextInt((int) Math.pow(2, 5));
				}
				e.setAttribute("capacity", a);
			}

			Flow.initAdj(g);
			Flow.BFS(g);

			// if there is a path from source to sink, generate all opposite edges that does
			// not yet exist, to be fit as a flow network
			if ((int) t.getAttribute("d-s") != -1) {
				for (Edge e : g.getEachEdge()) {
					Node u = e.getSourceNode();
					Node v = e.getTargetNode();
					if (!v.hasEdgeToward(u)) {
						Edge a = g.addEdge(v.getId() + "-" + u.getId(), v, u, true);
						a.setAttribute("capacity", 0);
					}
				}
				return g;
			}
		}
	}
}
