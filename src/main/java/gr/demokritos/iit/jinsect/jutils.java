package gr.demokritos.iit.jinsect;

import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.GenericGraph;
import gr.demokritos.iit.jinsect.encoders.CanonicalCoder;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * A utility class accompanying JVertex objects and graphs 
 *
 * @author VHarisop
 */
public final class jutils {
	/**
	 * Compares 2 graphs' canonical code representations with respect
	 * to the standard lexicographic order. Returns -1 if the first graph's
	 * canonical code is "less" that the second's, 1 if it is "greater", and
	 * 0 if they are equal.
	 *
	 * @param gA the first graph
	 * @param gB the second graph
	 * @return an integer denoting the result of the canonical codes' comparison
	 */
	public static int compareCanonicalCodes(UniqueVertexGraph gA,
											UniqueVertexGraph gB)
	{
		String currA, currB;

		// get iterators on both graphs' canonical codes
		Iterator<String> caIter = (new CanonicalCoder(gA)).iterator();
		Iterator<String> cbIter = (new CanonicalCoder(gB)).iterator();

		while (caIter.hasNext()) {
			currA = caIter.next();

			if (cbIter.hasNext()) 
				currB = cbIter.next();
			else {
				// if B's code was depleted, A is "greater"
				return 1;
			}

			// if code is unequal at some point, return their difference
			if (!(currA.equals(currB))) {
				return currA.compareTo(currB);
			}
		}

		// if A was depleted but B was not, return -1 (A is "less")
		if (cbIter.hasNext()) 
			return -1;
		else
			return 0; // equality case
	}


	/** 
	 * Renders a JGraph to its DOT representation (See GraphViz for 
	 * more info on the format).
	 * @param gTree The input graph.
	 * @param bDirected Indicates whether the graph should be described 
	 * as a directed graph or not.
	 * @return The DOT formatted string representation of the graph.
	 */
	public static String graphToDot(UniqueVertexGraph gTree, boolean bDirected) {
		StringBuilder sb = new StringBuilder();
		String sConnector;

		// Render graph
		if (!bDirected) {
			sb.append("graph {\n");
			sConnector = "--";
		}
		else {
			sb.append("digraph {\n");
			sConnector = "->";
		}

		for (Edge e: gTree.edgeSet()) {
			JVertex vA = gTree.getEdgeSource(e);
			JVertex vB = gTree.getEdgeTarget(e);

			/* get the labels of the vertices this
			 * edge connects */
			String sA = vA.toString().replaceAll("\\W", "_");
			String sB = vB.toString().replaceAll("\\W", "_");
			String sLabel = "";

			/* get weight of edge and add it to the connection's label */
			double dWeight = gTree.getEdgeWeight(e);
			sLabel += String.format("%4.2f", dWeight);
			sb.append("\t" + sA + " " + sConnector + " " + sB +
					" [label=\"" + sLabel.replaceAll("\\s+", " ") + "\"]\n");
			sb.append("\t" + sA + " [label=\"" + sA + "\"] " + "\n");
		}
		sb.append("}");

		return sb.toString();
	}

	/**
	 * Compares two double values for equality, by checking if their absolute
	 * difference falls below a very small threshold.
	 *
	 * @param a the first number
	 * @param b the second number
	 * @return true if the numbers are equal, otherwise false
	 */
	public static boolean compareDouble(double a, double b) {
		return (Math.abs(a - b) < 0.000001);
	}
	
	/**
	 * Changes the label of a vertex in a {@link GenericGraph}.
	 * @param vertex the {@link JVertex} to change
	 * @param newLabel the new label
	 */
	public static void
	changeVertexLabel(JVertex vertex, String to)
	{
		final String[] parts = GenericGraph.getLabelParts(vertex);
		final String newLabel = to + "$" + parts[1];
		vertex.setLabel(newLabel);
	}
	
	/**
	 * Adds a random {@link Edge} of unitary weight between two
	 * randomly chosen vertices of a {@link GenericGraph}.
	 * @param graph the graph to be edited
	 */
	public static void addRandomEdge(GenericGraph graph) {
		Random randGen = new Random();
		final int size = graph.vertexSet().size();
		int from = randGen.nextInt(size);
		int to = randGen.nextInt(size);
		/* Get all vertices into a list */
		List<JVertex> vertices = graph
				.vertexSet()
				.stream()
				.collect(Collectors.toList());
		graph.addEdge(vertices.get(from), vertices.get(to), 1.0);
	}
	
	/**
	 * Picks a number of random vertices from a {@link GenericGraph}.
	 * @param graph the graph to pick the vertices from 
	 * @param number the number of vertices to collect
	 * @return a {@link Collection<JVertex>} of vertices
	 */
	public static Collection<JVertex>
	pickRandomVertices(GenericGraph graph, int number) {
		List<JVertex> vertices = new ArrayList<>(graph
				.vertexSet()
				.stream()
				.collect(Collectors.toList()));
		/* Shuffle the vertices, pick the first [number] elems */
		Collections.shuffle(vertices);
		return vertices.subList(0, Math.min(number, vertices.size()));
	}
	
	/**
	 * Picks a random vertex from a graph.
	 * @param graph the {@link GenericGraph} to pick from
	 * @return a random {@link JVertex}
	 */
	public static JVertex pickRandomVertex(GenericGraph graph) {
		List<JVertex> vertices = graph
				.vertexSet()
				.stream()
				.collect(Collectors.toList());
		return vertices.get((new Random()).nextInt(vertices.size()));
	}
	
	/**
	 * Gets all the isolated (i.e. disconnected) vertices
	 * of a {@link GenericGraph}.
	 * @param graph the graph from which to get the vertices
	 * @return a {@link List} of {@link JVertex} objects
	 */
	public static List<JVertex> getIsolatedVertices(GenericGraph graph) {
		return graph.vertexSet()
				.stream()
				.filter(v -> graph.edgesOf(v).size() == 0)
				.collect(Collectors.toList());
	}
	
	/**
	 * Removes a randomly chosen isolated {@link JVertex} from a
	 * {@link GenericGraph}
	 * @param graph the graph to remove the vertex from
	 */
	public static void
	removeRandomIsolatedVertex(GenericGraph graph) {
		List<JVertex> isolated = getIsolatedVertices(graph);
		final int numIsolated = isolated.size();
		if (numIsolated > 0) {
			int nextRand = (new Random()).nextInt(numIsolated);
			graph.removeVertex(isolated.get(nextRand));
		}
	}
	
	/**
	 * Given a {@link GenericGraph}, performs a number of elementary
	 * edit operations on it and returns a modified version of it.
	 * @param g the graph to edit
	 * @param editCount the number of edits
	 * @param labels a list of available labels to mutate to
	 * @return the modified {@link GenericGraph}
	 */
	public static GenericGraph
	getEditedGraph(GenericGraph g, int editCount, List<String> labels) {
		GenericGraph gNew = g.clone();
		Random randGen = new Random();
		final int labelCnt = labels.size();
		/* properly distribute the edit actions */ 
		final int removals = randGen.nextInt(editCount + 1);
		final int changes = randGen.nextInt(editCount + 1 - removals);
		final int additions = randGen.nextInt(
				editCount + 1 - removals - changes);
		/* Perform the required number of edits per edit action */
		for (int i = 0; i < removals; ++i) {
			removeRandomIsolatedVertex(gNew);
		}
		for (int i = 0; i < changes; ++i) {
			String newLabel = labels.get(randGen.nextInt(labelCnt));
			JVertex changed = pickRandomVertex(gNew);
			changed.setLabel(newLabel);
		}
		for (int i = 0; i < additions; ++i) {
			addRandomEdge(gNew);
		}
		return gNew;
	}
}

