# Long Project LP5: Minimum Spanning Tree Algorithms

# Team: LP101 
 * Rahul Nalawade (https://github.com/rahul1947) 
   rsn170330@utdallas.edu 
 * Prateek Sarna (https://github.com/prateek5795)  
   pxs180012@utdallas.edu 
 * Bhavish Khanna Narayanan (https://github.com/bhavish14) 
   bxn170002@utdallas.edu 
 
# End Date: 
 * Sunday, December 09, 2018
_______________________________________________________________________________

BinaryHeap.java: 
   Implemented BinaryHeap and its nested class IndexedHeap.

MST.java: 
   Implemented the three versions of Prim's algorithm discussed in class, 
   and Kruskal's algorithm. 

_______________________________________________________________________________

# OBSERVATIONS:

1. In BinaryHeap - add(x), and remove() throws exception when they 
   encounter full and empty queue conditions respectively. But, offer(x), 
   and poll() silently returns false for the above conditions. 
   
2. In BinaryHeap - we could have used resize() as we try to add an element 
   to fully occupied queue. But, as we are dealing with fixed graph, the 
   resize() is never being called.
   
3. In MST Algorithms - the early exit optimization is applied but, when 
   tested it appears that Prim3 doesn't need such optimization. 
   Note that this optimization only helps in saving computations after you 
   add the last (n-1)th edge to the MST. 

4. For Prim's Algorithm - Take 2:
   We may not need to use the constructor MSTVertex(MSTVertex u).
   As we could store the original vertex information for each MSTVertex,
   we can make copies of new MSTVertices out of the original Vertex, 
   instead of it's MSTVertex copy.
   For e.g. if we have Vertex v, we can use -
   MSTVertex vCopy = new MSTVertex(v) instead of
   MSTVertex vCopy = new MSTVertex(get(v)).

   These copies are used to store different distance and parent values 
   whereas, get(v) has the information about 'seen'ness of all MSTVertices 
   made out of v. 
   
5. For Prim2 and Prim3, we are storing the information about the edge 
   which reaches out to this MSTVertex. It is used to form mst<Edge>.
_______________________________________________________________________________

# RESULTS:

+---------------------------------------------------------------------------------------------------------------------+
|             |               |                       Prim's Algorithm                          |      Kruskal's      |
|             |  Graph Size   |-----------------------------------------------------------------|      Algorithm      |
| Test        |               |        Take 1       |        Take 2       |        Take 3       |                     |
| Files       |    |V| |E|    |---------------------|---------------------|---------------------|---------------------|
|             |               |  Time |   Memory    |  Time |   Memory    |  Time |   Memory    |  Time |   Memory    |
|-------------|---------------|-------|-------------|-------|-------------|-------|-------------|-------|-------------|
| lp5-t01.txt |           5 6 |     2 |     1 / 117 |     2 |     1 / 117 |     2 |     1 / 117 |     1 |     1 / 117 |
|-------------|---------------|-------|-------------|-------|-------------|-------|-------------|-------|-------------|
| lp5-t02.txt |        50 140 |     3 |     1 / 117 |     3 |     1 / 117 |     3 |     1 / 117 |     3 |     1 / 117 |
|-------------|---------------|-------|-------------|-------|-------------|-------|-------------|-------|-------------|
| lp5-t03.txt |       100 284 |     5 |     2 / 117 |     4 |     2 / 117 |     5 |     2 / 117 |     4 |     2 / 117 |
|-------------|---------------|-------|-------------|-------|-------------|-------|-------------|-------|-------------|
| lp5-t04.txt |   10000 30000 |    29 |     9 / 147 |    31 |    10 / 147 |    37 |    10 / 147 |    43 |    14 / 147 |
|-------------|---------------|-------|-------------|-------|-------------|-------|-------------|-------|-------------|
| lp5-t05.txt |    1000 19808 |    20 |    15 / 117 |    19 |    17 / 117 |    12 |    16 / 117 |    26 |    14 / 117 |
|-------------|---------------|-------|-------------|-------|-------------|-------|-------------|-------|-------------|
| lp5-t06.txt |  10000 199794 |   133 |   115 / 208 |   129 |   124 / 208 |    69 |   111 / 208 |   133 |   117 / 208 |
|-------------|---------------|-------|-------------|-------|-------------|-------|-------------|-------|-------------|
| lp5-t07.txt | 50000 9980016 |  7157 |  976 / 2009 | 14847 | 1284 / 2008 |  1697 |  835 / 2009 |  5439 |  929 / 2009 |
+---------------------------------------------------------------------------------------------------------------------+
Graph Size:
  |V| - number of vertices in the graph
  |E| - number of edges in the graph 
Time: in milliSeconds
Memory: Used / Available in MiBs 

NOTE: 
- The Files are ordered on Graph 'Density' = |E| / |V|, ratio of # edges to 
  the # vertices in a graph.

- Refer lp5-script-results.txt as obtained from 
  $ ./lp5-script.sh > lp5-script-results.txt

- All the Graphs runs perfectly producing correct results. To allocate 
  sufficient memory for the lp5-t07.txt test file, you can use - 
  $ java -Xss512m -Xms2g rsn170330.MST lp5-test/lp5-t07.txt 1 

- Time and Memory might change, as you run the test the program on a 
  different system, but they could be comparable to the above values.
  Existing Processor: Intel® Core™ i5-8250U CPU @ 1.60GHz × 8
  Memory: 7.5 GiB

- First four files (t01 to t04) are not good examples for determining 
  efficiency of Prim3 over Prim1. 
  For sparse graphs like these, Prim1 could beat Prim3, but 
  when graph grows dense, in case of t07, Prim3 could easily beat Prim1.
_______________________________________________________________________________

# How to Run: 

1. Extract the rsn170330.zip 

2. Compile: 
   $javac rsn170330/*.java

3. Run: 
   $java rsn170330.MST lp5-test/lp5-t04.txt <value>
   where value can be an integer = {1, 2, 3, 4} corresponding to the
   Prim1, Prim2, Prim3, Kruskal's Algorithm respectively.

NOTE: the current directory must contain rbk directory with rbk/Graph.java
_______________________________________________________________________________