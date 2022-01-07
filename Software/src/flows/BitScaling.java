// Lior Wunsch - 206238263
package flows;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

public class BitScaling {

	// implementation of Ford-Fulkerson algorithm
	// using bit-scaling to find the maximum flow value in a flow network graph
	public static int getMaxFlow(Flow r) {
		final long startTime = System.nanoTime();
		Graph g = r.getGraph();
		int F = 0;

		// first, saving all edges with their capacities
		Map<Edge, Integer> eList = new HashMap<>();
		for (Edge e : g.getEachEdge())
			eList.put(e, e.getAttribute("capacity"));

		// initializing flows and new capacities with 0
		for (Edge e : g.getEachEdge()) {
			e.setAttribute("flow", 0);
			e.setAttribute("capacity", 0);
		}

		int numOfBits = getRequiredBits(eList.values());
		for (int k = numOfBits - 1; k >= 0; k--) {
			// starting from the most significant bit and 'revealing'
			// more and more bits from the left, each 'reveal' causes the flow to double
			// and the capacity to double and add 1 to the capacity if the next bit
			// in the saved capacity is one, for each edge
			for (Map.Entry<Edge, Integer> edge : eList.entrySet()) {
				Edge e = edge.getKey();
				int Ck = e.getAttribute("capacity");

				int msb = (edge.getValue() & (1 << k)) >> k;
				// example :
				// .a = .value = ...1101010010101
				// .b = 1 << i = ...1000000000000
				// ..c = a & b = ...1000000000000
				// d = c >>> i = ...0000000000001 = 1

				int newC = 2 * Ck + msb;
				e.setAttribute("capacity", newC);

				int Fk = e.getAttribute("flow");
				e.setAttribute("flow", 2 * Fk);
			}

			F = FordFulkerson(r);
		}

		final long duration = System.nanoTime() - startTime;
		System.out.println("Number of Bits = " + numOfBits + "\n");
		System.out.format("Bit-Scaling running time : " + duration / Math.pow(10, 9) + " seconds.\n");
		return F;
	}

	// calculate the minimum number of bits required
	// for representing all capacities in binary
	public static int getRequiredBits(Collection<Integer> cList) {
		// calculate for each capacity, how many bits are required
		// without leading zeros and return the maximum

		int maxBits = 0;
		int bits;
		int maxc = 0;
		for (int c : cList) {
			// using the fact the an integer is 32 bits
			// the required number of bits to represent an integer
			// is 32 subtracted by the number of leading zeros in
			// the binary representation returned from java's method
			bits = (int) (Math.floor(Math.log(c) / Math.log(2))) + 1;
			if (bits > maxBits)
				maxBits = bits;
			if (c > maxc)
				maxc = c;
		}
		System.out.print("Max Capacity = " + maxc + ", ");
		return maxBits;
	}

	// implementation of Edmonds-Karp algorithm using Ford-Fulkerson method
	// calculates max flow value in flow network r using bfs
	// some changes to the algorithm to fit bit-scaling method :
	// the residual graph in the beginning does not equal the original graph
	// the flows are not zeroed at the beginning so information is not lost from
	// last bit iteration
	public static int FordFulkerson(Flow r) {
		Graph g = r.getGraph();

		// generate a residual flow with a graph gf
		// with capacities ignoring edges with 0 capacity
		Flow rf = new Flow(r);
		Graph gf = rf.getGraph();

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
		Flow.initAdj(gf);
		Flow.BFS(gf);
		while ((int) gf.getNode("t").getAttribute("d-s") != -1) {
			List<Edge> p = Flow.getPath(gf, "t");

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
			Flow.initAdj(gf);
			Flow.BFS(gf);
		}

		int F = 0;
		Node s = g.getNode("s");
		List<Node> l = s.getAttribute("adj");
		for (Node n : l) {
			Edge e = s.getEdgeToward(n);
			F += (int) e.getAttribute("flow");
		}

		return F;
	}
}
