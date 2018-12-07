package rsn170330.lp5;

import rbk.Graph.Vertex;
import rbk.Graph;
import rbk.Graph.Edge;
import rbk.Graph.GraphAlgorithm;
import rbk.Graph.Factory;
import rbk.Graph.Timer;

import rsn170330.lp5.BinaryHeap.Index;
import rsn170330.lp5.BinaryHeap.IndexedHeap;

import java.util.Arrays;
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.Comparator;
import java.util.List;
import java.util.LinkedList;
import java.io.FileNotFoundException;
import java.io.File;

public class MST extends GraphAlgorithm<MST.MSTVertex> {
	String algorithm;
	public long wmst;
	List<Edge> mst;

	MST(Graph g) {
		super(g, new MSTVertex((Vertex) null));
	}

	public static class MSTVertex implements Index, Comparable<MSTVertex>, Factory {
		boolean seen;
		Vertex parent;
		int distance;
		Vertex vertex;
		
		MSTVertex(Vertex u) {
			seen = false;
			parent = null;
			distance = Integer.MAX_VALUE;
			vertex = u;
		}

		MSTVertex(MSTVertex u) { // for prim2
			
		}

		public MSTVertex make(Vertex u) {
			return new MSTVertex(u);
		}

		public void putIndex(int index) {
		}

		public int getIndex() {
			return 0;
		}

		public int compareTo(MSTVertex other) {
			if (other == null || this.distance > other.distance) {
				return 1;
			}
			else if (this.distance == other.distance) {
				return 0;
			}
			else {
				return -1;
			}
		}
	}

	public long kruskal() {
		algorithm = "Kruskal";
		Edge[] edgeArray = g.getEdgeArray();
		mst = new LinkedList<>();
		wmst = 0;
		return wmst;
	}

	public long prim3(Vertex s) {
		algorithm = "indexed heaps";
		mst = new LinkedList<>();
		wmst = 0;
		IndexedHeap<MSTVertex> q = new IndexedHeap<>(g.size());
		return wmst;
	}

	public long prim2(Vertex s) {
		algorithm = "PriorityQueue<Vertex>";
		mst = new LinkedList<>();
		wmst = 0;
		PriorityQueue<MSTVertex> q = new PriorityQueue<>();
		
		for (Vertex u : g) {
			get(u).seen = false;
			get(u).parent = null;
			get(u).distance = Integer.MAX_VALUE;
		}
		get(s).distance = 0;
		q.add(get(s));
		
		while (!q.isEmpty()) {
			MSTVertex u = q.remove();
			if (!u.seen) {
				u.seen = true;
				wmst += u.distance;
				Vertex uImage = u.vertex;
				
				for (Edge e : g.incident(uImage)) {
					Vertex v = e.otherEnd(uImage);
					if (!get(v).seen && e.getWeight() < get(v).distance) {
						get(v).distance = e.getWeight();
						get(v).parent = uImage;
						q.add(get(v));
					}
				}
			}
		}
		
		return wmst;
	}

	public long prim1(Vertex s) {
		algorithm = "PriorityQueue<Edge>";
		mst = new LinkedList<>();
		wmst = 0;
		
		PriorityQueue<Edge> q = new PriorityQueue<>();
		
		for (Edge e : g.incident(s)) {
			q.add(e);
		}
		
		while (!q.isEmpty()) {
			Edge e = q.remove();
			Vertex u = e.fromVertex();
			Vertex v = (get(u).seen)? e.otherEnd(u) : u; 
			
			if (get(v).seen) {
				continue;
			}
			get(v).seen = true;
			get(v).parent = u;
			wmst += e.getWeight();
			mst.add(e);
			
			for (Edge e2 : g.incident(v)) {
				Vertex w = e2.otherEnd(v);
				if (!get(w).seen) {
					q.add(e2);
				}
			}
		}
		
		return wmst;
	}

	public static MST mst(Graph g, Vertex s, int choice) {
		MST m = new MST(g);
		switch (choice) {
		case 0:
			m.kruskal();
			break;
		case 1:
			m.prim1(s);
			break;
		case 2:
			m.prim2(s);
			break;
		default:
			m.prim3(s);
			break;
		}
		return m;
	}

	public static void main(String[] args) throws FileNotFoundException {
		Scanner in;
		int choice = 0; // Kruskal
		if (args.length == 0 || args[0].equals("-")) {
			in = new Scanner(System.in);
		} else {
			File inputFile = new File(args[0]);
			in = new Scanner(inputFile);
		}

		if (args.length > 1) {
			choice = Integer.parseInt(args[1]);
		}

		Graph g = Graph.readGraph(in);
		Vertex s = g.getVertex(1);

		Timer timer = new Timer();
		MST m = mst(g, s, choice);
		System.out.println(m.algorithm + "\n" + m.wmst);
		System.out.println(timer.end());
	}
}
