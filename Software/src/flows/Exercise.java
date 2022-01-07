// Lior Wunsch - 206238263
package flows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Scanner;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;

public class Exercise {
	static class Job {
		private String id;
		private int r;
		private int d;

		public Job(String id, int r, int d) {
			this.id = id;
			this.r = r;
			this.d = d;
		}

		@Override
		public String toString() {
			return id + ": " + r + "-" + d;
		}
	}

	private Map<Integer, Integer> capacities; // capacities (value) of the machine in each hour starting at time (key)
	private List<Job> jobs; // list of jobs with release time (r) and deadline time (d)

	public Exercise(Map<Integer, Integer> capacities, List<Job> jobs) {
		this.capacities = capacities;
		this.jobs = jobs;
	}

	public boolean isScheduled() {
		Graph g = new SingleGraph("schedule");

		// build graph g which will be the base of the
		// flow network as instructed
		Node s = g.addNode("s");
		for (Job j : jobs) {
			Node jNode = g.addNode(j.id);
			Edge jEdge = g.addEdge("s-" + jNode.getId(), s, jNode, true);
			jEdge.setAttribute("capacity", 1);
			Edge oEdge = g.addEdge(jNode.getId() + "-s", jNode, s, true);
			oEdge.setAttribute("capacity", 0);
		}

		Node t = g.addNode("t");
		for (Map.Entry<Integer, Integer> entry : capacities.entrySet()) {
			String nodeName = entry.getKey().toString();
			Node iNode = g.addNode(nodeName);
			Edge iEdge = g.addEdge(iNode.getId() + "-t", iNode, t, true);
			iEdge.setAttribute("capacity", entry.getValue());
			Edge oEdge = g.addEdge("t-" + iNode.getId(), t, iNode, true);
			oEdge.setAttribute("capacity", 0);
		}

		for (Job j : jobs) {
			Node jNode = g.getNode(j.id);
			for (Integer i = j.r; i < j.d; i++) {
				Node iNode = g.getNode(i.toString());
				Edge jEdge = g.addEdge(j.id + "-" + iNode.getId(), jNode, iNode, true);
				jEdge.setAttribute("capacity", 1);
				Edge oEdge = g.addEdge(iNode.getId() + "-" + j.id, iNode, jNode, true);
				oEdge.setAttribute("capacity", 0);
			}
		}

		// initialize adjacencies for each node
		// using initAdj method from file Flow.java in the package
		Flow.initAdj(g);
		Flow f = new Flow(g);

		// run ford-fulkerson and bit-scaling ford-fulkerson on the flow network
		// print messages accordingly and check if the schedule can be filled as
		// expected
		System.out.format("The flow graph has %d nodes", g.getNodeCount());
		System.out.format(" and %d edges\n", g.getEdgeCount());
		BitScaling.getMaxFlow(f);
		int flow1 = Flow.getMaxFlow(f);
		System.out.println("Flow = " + flow1);
		if (flow1 == jobs.size())
			return true;
		return false;
	}

	public static void main(String[] args) {
		Map<Integer, Integer> myCapacities = new HashMap<>();
		List<Job> myJobs = new ArrayList<>();

		Scanner scanIn = new Scanner(System.in);
		System.out.print("Please enter number of jobs: ");
		int n = scanIn.nextInt();

		// build a list of jobs and choose start time and deadline time
		// randomally for each one
		Random rand2 = new Random();
		for (Integer j = 0; j < n; j++) {
			int rj = rand2.nextInt(24);
			int dj;
			do {
				dj = rand2.nextInt(25 - rj) + rj;
			} while (dj <= rj);
			Job job = new Job("j" + j.toString(), rj, dj);
			myJobs.add(job);
		}

		// choose how many jobs can be processed each hour randomally
		Random rand = new Random();
		for (int i = 0; i < 24; i++) {
			int c = 0;
			while (c == 0) {
				c = rand.nextInt((int) Math.log(myJobs.size()));
			}
			myCapacities.put(i, c);
		}

		// check if a feasible schedule can be constructed
		Exercise myEx = new Exercise(myCapacities, myJobs);
		System.out.println(myEx.isScheduled());
		scanIn.close();
	}
}
