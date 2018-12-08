package rsn170330.lp5;

/**
 * CS 5V81.001: Implementation of Data Structures and Algorithms 
 * Long Project LP5: Minimum Spanning Tree Algorithms
 * Team: LP101
 * @author Rahul Nalawade (rsn170330)
 */


import java.util.NoSuchElementException;

public class BinaryHeap<T extends Comparable<? super T>> {
	Comparable[] pq; // Priority Queue
	int capacity; // maximum size of Priority Queue
	int size; // current size of Priority Queue
	
	// Constructor for building an empty priority queue 
	// using natural ordering of T
	public BinaryHeap(int maxCapacity) {
		capacity = maxCapacity;
		pq = new Comparable[capacity];
		size = 0;
	}

	/**
	 * Add element to the priority queue, throw exception if it is full
	 * @param x the element to be added
	 * @throws Exception when pq is full
	 */
	public boolean add(T x) throws Exception {
		boolean result = offer(x);
		
		if (!result) {
			throw new Exception("Queue is full.\n");
		}
		return true;
	}
	
	/**
	 * Offer(add) a number x, return false if pq is full
	 * @param x the number to be offered
	 * @return isOffered? true when added, else false
	 */
	public boolean offer(T x) {
		if (size == pq.length) {
			return false;
		} 
		// Adding to the leaf
		pq[size] = x; 
		// Moving to the appropriate place
		percolateUp(size); 
		size++;
		return true;
	}

	/**
	 * Removes an element from the pq.
	 * @return the element that was removed
	 * @throws NoSuchElementException when pq is empty
	 */
	public T remove() throws NoSuchElementException {
		T result = poll();
		
		if (result == null) {
			throw new NoSuchElementException("Priority queue is empty.\n");
		} else {
			return result;
		}
	}

	/**
	 * Polls (or removes) the element.
	 * @return T the element that was removed, null when PQ is empty
	 */
	public T poll() {
		if (size == 0) {
			return null;
		} 
		// The first element which is to be removed
		Comparable<? super T> temp = pq[0]; 
		pq[0] = pq[size - 1];
		size--;
		
		// Moving newly added element to appropriate place
		percolateDown(0); 
		
		return (T) temp;
	}
	
	// The top element of the heap.
	public T min() {
		return peek();
	}

	/**
	 * Sees the top element of the heap (or head of the queue).
	 * @return the first element of queue, null if PQ is empty
	 */
	public T peek() {
		if (0 < size) {
			return (T) pq[0];
		}
		return null;
	}

	int parent(int i) {
		return (i - 1) / 2;
	}

	int leftChild(int i) {
		return 2 * i + 1;
	}

	
	/**
	 * Move the element[index] up in the heap, at it's appropriate place.
	 * 
	 * @param index the index to be moved up in the heap.
	 */
	void percolateUp(int index) {
		Comparable<? super T> x = pq[index];
		
		//pq[index] may violate heap order with parent***
		while (index > 0 && (compare(pq[parent(index)], x) > 0)) {
			move(index, pq[parent(index)]); // pq[index] = pq[parent(index)];
			index = parent(index);
		}
		move(index, x); // pq[index] = x;
	}

	/**
	 * Move the element[index] down in the heap, at it's appropriate place. 
	 * 
	 * @param index the index to be moved down in the heap.
	 */
	void percolateDown(int index) {
		Comparable<? super T> x = pq[index];
		int c = leftChild(index); // (2 * index) + 1;
		
		// pq[i] may violate heap order with children***
		while (c <= size - 1) {
			
			if (c < (size - 1) && (compare(pq[c], pq[c + 1]) > 0)) { c++; }
			
			if (compare(x, pq[c]) <= 0) { break; }
			
			move(index, pq[c]); // pq[index] = pq[c];
			index = c;
			c = leftChild(index); // 2 * index + 1;
		}
		move(index, x); // pq[index] = x;
	}
	
	/**
	 * Assigns x to pq[dest]. Indexed Heap will override this method.
	 * @param dest the index where x is to be assigned.
	 * @param x the element to be assigned.
	 */
	void move(int dest, Comparable x) {
		pq[dest] = x;
	}
	
	// 
	int compare(Comparable a, Comparable b) {
		return ((T) a).compareTo((T) b);
	}

	/** 
	 * Create a heap. Bottom-up [RT: O(n)]
	 * Precondition: none. 
	 */
	void buildHeap() {
		for (int i = parent(size - 1); i >= 0; i--) {
			percolateDown(i);
		}
	}

	public boolean isEmpty() {
		return size() == 0;
	}
	
	// no if elements in the heap.
	public int size() {
		return size;
	}

	/**
	 * Resize array to double the current size
	 * NOTE: Might fail when contiguous memory available < memory used for PQ
	 */
	void resize() {
		Comparable[] newPQ = new Comparable[capacity * 2];
		// copying existing elements
		for (int i = 0; i < size; i++) {
			newPQ[i] = pq[i];
		}
		capacity = capacity * 2;
		pq = newPQ;
	}

	public interface Index {
		
		public void putIndex(int index);

		public int getIndex();
	}
	
	/**
	 * For Prim3 and Dijkstra's Algorithms. 
	 * Specialization of Binary Heaps applied on only Comparable objects.
	 */
	public static class IndexedHeap<T extends Index & Comparable<? super T>> extends BinaryHeap<T> {
		/** Build a priority queue with a given array */
		IndexedHeap(int capacity) {
			super(capacity);
		}

		/** restore heap order property after the priority of x has decreased */
		void decreaseKey(T x) {
			int thisIndex = x.getIndex();
			percolateUp(thisIndex);
		}

		@Override
		void move(int i, Comparable x) {
			super.move(i, x);
			
			// updating the index after moving
			T xImage = (T) x; 
			xImage.putIndex(i);
		}
	}

	public static void main(String[] args) {
		Integer[] arr = { 0, 9, 7, 5, 3, 1, 8, 6, 4, 2 };
		BinaryHeap<Integer> h = new BinaryHeap<Integer>(arr.length);

		System.out.print("Before:");
		for (Integer x : arr) {
			h.offer(x);
			System.out.print(" " + x);
		}
		System.out.println();

		for (int i = 0; i < arr.length; i++) {
			arr[i] = h.poll();
		}

		System.out.print("After :");
		for (Integer x : arr) {
			System.out.print(" " + x);
		}
		System.out.println();
	}
}
