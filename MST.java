package rsn170330.lp5;

import rsn170330.lp5.BinaryHeap.Index;
import rsn170330.lp5.BinaryHeap.IndexedHeap;

import rbk.Graph;
import rbk.Graph.Vertex;
import rbk.Graph.Edge;
import rbk.Graph.GraphAlgorithm;
import rbk.Graph.Factory;
import rbk.Graph.Timer;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Scanner;
import java.util.Set;
import java.util.List;
import java.util.Map;
import java.util.LinkedList;
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
		int name; // prim2: name of the edge reaching to this MSTVertex
		
		int thisIndex; // prim3: index of the node in the Priority Queue
		
		MSTVertex parentMST;
		int rank;
		
		
		MSTVertex(Vertex u) {
			seen = false;
			parent = null;
			parentMST = null;
			distance = Integer.MAX_VALUE;
			vertex = u;
		}

		MSTVertex(MSTVertex u) { // for prim2
			
		}

		public MSTVertex make(Vertex u) {
			parentMST = this;
			rank = 0;
			return new MSTVertex(u);
		}
		
		// prim3
		public void putIndex(int index) {
			thisIndex = index;
		}
		
		// prim3
		public int getIndex() {
			return thisIndex;
		}
		
		// prim2: more the distance -> lesser the priority 
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
		
		// --------------------------- Kruskal -------------------------------
		public MSTVertex find() {
			if (!this.equals(parentMST)) {
				parentMST = parentMST.find();
			}
			return parentMST;
		}
		
		public void union(MSTVertex rv) {
			if (rv.rank < this.rank) {
				rv.parentMST = this;
			}
			else if (this.rank < rv.rank) {
				this.parentMST = rv;
			}
			else {
				this.rank++;
				rv.parentMST = this;
			}
		}
	}
	
	/**
	 * Kruskal's MST Algorithm using disjoint set data structure 
	 * with union()-and-find() operations.
	 * 
	 * @return the total weight of the Minimum Spanning Tree.
	 */
	public long kruskal() {
		algorithm = "Kruskal";
		Edge[] edgeArray = g.getEdgeArray();
		mst = new LinkedList<>();
		wmst = 0;
		
		for (Vertex u : g) {
			get(u).make(u);
		}
		
		Arrays.sort(edgeArray);
		
		for (Edge e : edgeArray) {
			MSTVertex ru = get(e.fromVertex()).find();
			MSTVertex rv = get(e.toVertex()).find();
			
			if (!ru.equals(rv)) {
				mst.add(e);
				wmst += e.getWeight();
				ru.union(rv);
			}
		}
		
		return wmst;
	}
	
	/**
	 * Prim's MST Algorithm - Take 3: using Indexed heap
	 * NOTE: best Prim Algorithm for dense graphs.
	 * 
	 * @param s source vertex
	 * @return the total weight of the Minimum Spanning Tree.
	 * @throws Exception
	 */
	public long prim3(Vertex s) throws Exception {
		algorithm = "indexed heaps";
		mst = new LinkedList<>();
		wmst = 0;
		IndexedHeap<MSTVertex> q = new IndexedHeap<>(g.size());
		
		for (Vertex u : g) {
			get(u).seen = false;
			get(u).parent = null;
			get(u).distance = Integer.MAX_VALUE;
			get(u).vertex = u;
			get(u).putIndex(u.getIndex());
		}
		get(s).distance = 0;
		
		for (Vertex u : g) {
			q.add(get(u));
		}
		while (!q.isEmpty()) {
			MSTVertex u = q.remove();
			u.seen =  true;
			wmst += u.distance; // u.distance = weight of the smallest edge 
			// that connects mst to this edge
			Vertex uImage = u.vertex;
			
			for (Edge e : g.incident(uImage)) {
				Vertex v = e.otherEnd(uImage);
				
				if (!get(v).seen && (e.getWeight() < get(v).distance)) {
					get(v).distance = e.getWeight();
					get(v).parent = uImage; // or u? as MSTVertex
					q.decreaseKey(get(v)); 
					// Need to call percolateUp(index of v in q). 
					// How do we find it? No idea! :(
				}
			}
		}
		
		return wmst;
	}
	
	/**
	 * Prim's MST Algorithm - Take 2: using Priority Queue of Vertices.
	 * Duplicates are allowed.
	 * 
	 * @param s the source vertex
	 * @return the total weight of the Minimum Spanning Tree.
	 */
	public long prim2(Vertex s) {
		algorithm = "PriorityQueue<Vertex>";
		mst = new LinkedList<>();
		wmst = 0;
		PriorityQueue<MSTVertex> q = new PriorityQueue<>();
		Map<Integer, Edge> edgeMap = new HashMap<>(); 
		
		
		for (Vertex u : g) {
			get(u).seen = false;
			get(u).parent = null;
			get(u).distance = Integer.MAX_VALUE;
		}
		get(s).distance = 0;
		q.add(get(s));
		
		while (!q.isEmpty()) {
/**
 * Every Vertex u is the main reference point for all MSTVertex copies.
 * As every copy of MSTVertex made out of Vertex u, stores reference to u in 
 * it's attribute vertex.
 * 
 * So, whenever we add a MSTVertex copy of v, we create a new MSTVertex(v),
 * update it's distance and parent (which differs for its similar copies made
 * from Vertex v). This avoids concurrent modification of similar MSTVertex.
 * 
 * In short, for each Vertex v, we have it's main MSTVertex copy as get(v),
 * and might have new MSTVertex(v) copies stored in the Priority Queue q, 
 * with different distance and parent values. 
 */
			MSTVertex u = q.remove();
			Vertex uOriginal = u.vertex; 
			
			// NOTE: get(uImage) != u***
			if (!get(uOriginal).seen) {
				//u.seen = true;
				// Only get(u) is responsible for 'seen'ness
				get(uOriginal).seen = true; 
				wmst += u.distance;
				
				// add the edge to MST if it's parent exists
				if (u.parent != null) { mst.add(edgeMap.get(u.name)); }
				
				for (Edge e : g.incident(uOriginal)) {
					Vertex v = e.otherEnd(uOriginal);
					
					if (!get(v).seen && e.getWeight() < get(v).distance) {
						MSTVertex vImage = new MSTVertex(v);
						vImage.distance = e.getWeight();
						vImage.parent = uOriginal;
						
						// Storing unique name of the edge in the vImage
						vImage.name = e.getName();
						edgeMap.put(vImage.name, e); // each new Copy of 
						// MSTVertex(v) knows about the edge reaching to it.
						q.add(vImage);
					}
				}
			}
		}
		return wmst;
	}
	
	/**
	 * Prim's MST Algorithm - Take 1: using Priority Queue of Edges.
	 * NOTE: For sparse graphs, prefer take 1 over take 3.
	 * 
	 * @param s the source vertex
	 * @return the total weight of the Minimum Spanning Tree.
	 */
	public long prim1(Vertex s) {
		algorithm = "PriorityQueue<Edge>";
		mst = new LinkedList<>();
		wmst = 0;
		
		PriorityQueue<Edge> q = new PriorityQueue<>(); // PQ of Edges
		
		// Starting from the source, adding all edges incident to source
		for (Edge e : g.incident(s)) {
			q.add(e);
		}
		
		// Until there is an edge which needs to be processed
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
			mst.add(e); // updating MST edges
			
			for (Edge e2 : g.incident(v)) {
				Vertex w = e2.otherEnd(v);
				if (!get(w).seen) {
					q.add(e2);
				}
			}
		}
		
		return wmst;
	}

	public static MST mst(Graph g, Vertex s, int choice) throws Exception {
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

	public static void main(String[] args) throws Exception {
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
