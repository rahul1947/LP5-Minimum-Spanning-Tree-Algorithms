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
import java.util.PriorityQueue;
import java.util.Scanner;
import java.util.List;
import java.util.LinkedList;
import java.io.File;

/**
 * Given weighted undirected graph G = (V, E) {assuming it to be connected} 
 * and weights w: E -> Z (Integers)
 * 
 * Output: A minimum spanning tree of graph G, such that
 * wmst = min{w(T)}. 
 * where T is a Spanning Tree of graph G; and  
 * where w(T) = summation of weights of all edges of T.
 */
public class MST extends GraphAlgorithm<MST.MSTVertex> {
	String algorithm;
	public long wmst; // weight of MST
	List<Edge> mst; // stores edges in MST
	
	int count; // for early exit optimization
	int N; // No of vertices in graph

	MST(Graph g) {
		super(g, new MSTVertex((Vertex) null));
		mst = new LinkedList<>();
		wmst = 0;
		count = 0;
		N = g.size();
	}
	
	/** Stores the characteristics of a special vertex in the graph. */
	public static class MSTVertex implements Index, Comparable<MSTVertex>, Factory {
		// Prim's Algorithm - Take 1-3:
		boolean seen; // to see if this MSTVertex has been visited or not.
		Vertex parent; // parent of this MSTVertex 
		
		// Prim's Algorithm - Take 2-3:
		int distance; // distance this MSTVertex from the tree with smallest edge
		Vertex vertex; // reference to the original Vertex
		Edge incidentEdge; // Edge reaching out to this MSTVertex

		// Prim's Algorithm - Take 3:
		int primIndex; // Index of the MSTVertex in the Indexed Heap
		
		// Kruskal's Algorithm: 
		MSTVertex representative; // representative of this MSTVertex 
		int rank; // Only union uses rank. 
		// And only rank of representative changes.
		
		MSTVertex(Vertex u) {
			seen = false;
			parent = null;
			
			distance = Integer.MAX_VALUE;
			vertex = u;
			incidentEdge = null;
			
			primIndex = 0;
			
			representative = null;
			rank = 0;
		}

		MSTVertex(MSTVertex u) { // for prim2
			seen = u.seen;
			parent = u.parent;
			
			distance = u.distance;
			vertex = u.vertex;
			incidentEdge = u.incidentEdge;
			
			primIndex = u.primIndex;
			
			representative = u.representative;
			rank = u.rank;
		}
		
		// for Kruskal's
		public MSTVertex make(Vertex u) {
			representative = this;
			rank = 0;
			return new MSTVertex(u);
		}
		
		// Prims's Algorithm - Take 3:
		public void putIndex(int index) {
			primIndex = index;
		}
		
		// Prims's Algorithm - Take 3:
		public int getIndex() {
			return primIndex;
		}
		
		/**
		 * Ordering MSTVertices on the distance attribute.
		 * Used for: 
		 * Prims's Algorithm Take 2: PriorityQueue<MSTVertex> and 
		 * Prims's Algorithm Take 3: IndexedHeap<MSTVertex> 
		 */
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
		
		/**
		 * Kruskal's Algorithm:
		 * Finds the representative of the component that this vertex is in.
		 * 
		 * @return MSTVertex as a representative for this vertex
		 */
		public MSTVertex find() {
			if (!this.equals(representative)) {
				representative = representative.find();
			}
			return representative;
		}
		
		/**
		 * Kruskal's Algorithm:
		 * Merge two components into one. 
		 * Precondition: Only called by representatives.
		 * 
		 * @param rv the other representative
		 */
		public void union(MSTVertex rv) {
			if (rv.rank < this.rank) {
				rv.representative = this;
			}
			else if (this.rank < rv.rank) {
				this.representative = rv;
			}
			else {
				this.rank++;
				rv.representative = this;
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
		
		// make singleton component for each vertex having it in its component.
		for (Vertex u : g) { get(u).make(u); }
		
		Arrays.sort(edgeArray); // sort edges by weight
		
		for (Edge e : edgeArray) {
			// Get out of the loop, once you've added (N-1) Edges 
			if (count == (N-1)) { break; } // DO NOT CHANGE

			MSTVertex u = get(e.fromVertex());
			MSTVertex v = get(e.toVertex());
			
			MSTVertex ru = u.find(); // representative of u
			MSTVertex rv = v.find(); // representative of v
			
			// When different representatives, unify two components by edge e:
			if (!ru.equals(rv)) {
				wmst += e.getWeight();
				ru.union(rv);
				mst.add(e); // adding to the MST*
				count++; 
			}
		}
		return wmst;
	}
	
	/**
	 * Prim's MST Algorithm - Take 3: using Indexed Binary Heap
	 * NOTE: best Prim's Algorithm for dense graphs.
	 * 
	 * @param s source vertex
	 * @return the total weight of the Minimum Spanning Tree.
	 * @throws Exception Full/Empty IndexedHeap exceptions
	 */
	public long prim3(Vertex s) throws Exception {
		algorithm = "indexed heaps";
		IndexedHeap<MSTVertex> q = new IndexedHeap<>(g.size());
		
		// Initialization
		for (Vertex u : g) {
			get(u).seen = false;
			get(u).parent = null;
			get(u).distance = Integer.MAX_VALUE;
			get(u).vertex = u;
			get(u).putIndex(u.getIndex());
		}
		get(s).distance = 0;
		
		// Indexed Heap q will always have all the vertices almost all the time.
		for (Vertex u : g) { q.add(get(u)); }
		
		while (!q.isEmpty()) {
			// Do we really need this optimization here? NO 
			// Get out of the loop, once you've added (N-1) Edges 
			if (count == (N-1)) { break; } // DO NOT CHANGE

			MSTVertex u = q.remove(); // MSTVertex
			Vertex uOriginal = u.vertex; // normal Vertex
			
			u.seen = true;
			wmst += u.distance; 
			
			// Adding edge to the MST, incrementing the count
			if (u.parent != null) { mst.add(u.incidentEdge); count++; }
			
			for (Edge e : g.incident(uOriginal)) {
				Vertex v = e.otherEnd(uOriginal);
				
				if (!get(v).seen && (e.getWeight() < get(v).distance)) {
					get(v).distance = e.getWeight();
					get(v).parent = uOriginal; 
					get(v).incidentEdge = e; // edge reaching out to MSTVertex get(v) 
					q.decreaseKey(get(v)); // percolateUp(MSTVertex v), if needed
				}
			}
		}
		return wmst;
	}
	
	/**
	 * Prim's MST Algorithm - Take 2: using Priority Queue of Vertices.
	 * Duplicates are allowed. Uses space proportional to the Edge set. 
	 * Not good for dense graphs.
	 * 
	 * @param s the source vertex
	 * @return the total weight of the Minimum Spanning Tree.
	 */
	public long prim2(Vertex s) {
		algorithm = "PriorityQueue<Vertex>";
		PriorityQueue<MSTVertex> q = new PriorityQueue<>();
		
		// Initialization:
		for (Vertex u : g) {
			get(u).seen = false;
			get(u).parent = null;
			get(u).distance = Integer.MAX_VALUE;
		}
		get(s).distance = 0;
		q.add(get(s)); // adding the source to the PriorityQueue q
		
		while (!q.isEmpty()) {
			// Get out of the loop, once you've added (N-1) Edges
			if (count == (N-1)) { break; } // DO NOT CHANGE
			
			MSTVertex u = q.remove(); // MSTVertex copy 
			Vertex uOriginal = u.vertex; // normal Vertex 
			
			// NOTE: get(uOriginal) != u ***
			if (!get(uOriginal).seen) {
				// Only get(u) is responsible for 'seen'ness
				get(uOriginal).seen = true; 
				wmst += u.distance;
				
				// add the edge to MST if it's parent exists
				if (u.parent != null) { mst.add(u.incidentEdge); count++; }
				
				for (Edge e : g.incident(uOriginal)) {
					Vertex v = e.otherEnd(uOriginal);
					
					if (!get(v).seen && e.getWeight() < get(v).distance) {
						// vImage: copy of Vertex v to be added in Priority Queue
						MSTVertex vImage = new MSTVertex(get(v)); 
						// or MSTVertex vImage = new MSTVertex(v); // makes no difference
						vImage.distance = e.getWeight();
						vImage.parent = uOriginal;
						
						vImage.incidentEdge = e; // edge reaching out to MSTVertex get(v) 
						q.add(vImage);
					}
				}	
			}
		}
		return wmst;
	/**
	 * Every Vertex u is the main reference point for all MSTVertex copies of u.
	 * As every copy of MSTVertex made out of Vertex u, stores reference to u in 
	 * it's attribute vertex.
	 * 
	 * So, whenever we add a MSTVertex copy of Vertex v, we create a new MSTVertex(v),
	 * update it's distance and parent (which differs for its similar copies made
	 * from Vertex v). This avoids concurrent modification of similar MSTVertex.
	 * 
	 * In short, for each original Vertex v, we've it's main MSTVertex copy as get(v),
	 * and might have new MSTVertex(v) copies stored in the Priority Queue q, 
	 * with different distance and parent values. 
	 */
	}
	
	/**
	 * Prim's MST Algorithm - Take 1: using Priority Queue of Edges.
	 * NOTE: For sparse graphs, prefer take 1 over take 3. 
	 * Uses space proportional to the Edge set. 
	 * 
	 * @param s the source vertex
	 * @return the total weight of the Minimum Spanning Tree.
	 */
	public long prim1(Vertex s) {
		algorithm = "PriorityQueue<Edge>";
		PriorityQueue<Edge> q = new PriorityQueue<>(); // PQ of Edges
		
		for(Vertex u : g) {
			get(u).seen = false;
			get(u).parent = null;
		}
		get(s).seen = true;
		
		// Starting from the source, adding all edges incident to source
		for (Edge e : g.incident(s)) { q.add(e); }
		
		// Until there is an edge which needs to be processed
		while (!q.isEmpty()) {
			// Get out of the loop, once you've added (N-1) Edges 
			if (count == (N-1)) { break; } // DO NOT CHANGE
			
			Edge e = q.remove();
			Vertex u = e.fromVertex();
			// u needs to be always seen
			Vertex v = (get(u).seen)? e.otherEnd(u) : u; 
			
			// When v is also seen, keep removing the edges
			if (get(v).seen) { continue; }

			get(v).seen = true;
			get(v).parent = u;
			
			wmst += e.getWeight();
			mst.add(e); // updating MST edges
			count++;
			
			for (Edge e2 : g.incident(v)) {
				Vertex w = e2.otherEnd(v); // subgraph: (u)---(v)---(w)
				
				if (!get(w).seen) {
					q.add(e2); // When edge (v)---(w) is not visited
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
		
		//System.out.println("Count: " + m.count);
		System.out.println("Minimum Spanning Tree: " + m.algorithm);
		for (Edge e : m.mst) {
			System.out.println(e.toString());
		}
		
	}
}
