package gr.demokritos.iit.jinsect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import gr.demokritos.iit.jinsect.encoders.CanonicalCoder;
import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.GenericGraph;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.Pair;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;

/**
 * A utility class accompanying JVertex objects and graphs
 *
 * @author VHarisop
 */
public final class JUtils {
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
	public static int compareCanonicalCodes(
			final UniqueVertexGraph gA,
			final UniqueVertexGraph gB)
	{
		String currA, currB;

		// get iterators on both graphs' canonical codes
		final Iterator<String> caIter =
			CanonicalCoder.iterator(gA);
		final Iterator<String> cbIter =
			CanonicalCoder.iterator(gB);

		while (caIter.hasNext()) {
			currA = caIter.next();

			if (cbIter.hasNext()) {
				currB = cbIter.next();
			} else {
				// if B's code was depleted, A is "greater"
				return 1;
			}

			// if code is unequal at some point, return their difference
			if (!(currA.equals(currB))) {
				return currA.compareTo(currB);
			}
		}

		// if A was depleted but B was not, return -1 (A is "less")
		if (cbIter.hasNext()) {
			return -1;
		}
		else {
			return 0; // equality case
		}
	}


	/**
	 * Renders a JGraph to its DOT representation (See GraphViz for
	 * more info on the format).
	 * @param gTree The input graph.
	 * @param bDirected Indicates whether the graph should be described
	 * as a directed graph or not.
	 * @return The DOT formatted string representation of the graph.
	 */
	public static String
	graphToDot(final UniqueVertexGraph gTree, final boolean bDirected) {
		final StringBuilder sb = new StringBuilder();
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

		for (final Edge e: gTree.edgeSet()) {
			final JVertex vA = gTree.getEdgeSource(e);
			final JVertex vB = gTree.getEdgeTarget(e);

			/* get the labels of the vertices this
			 * edge connects */
			final String sA = vA.toString().replaceAll("\\W", "_");
			final String sB = vB.toString().replaceAll("\\W", "_");
			String sLabel = "";

			/* get weight of edge and add it to the connection's label */
			final double dWeight = gTree.getEdgeWeight(e);
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
	public static boolean eqDouble(final double a, final double b) {
		return (Math.abs(a - b) < 0.000001);
	}

	/**
	 * Computes num ^ exp for integers.
	 * @param num the number to exponentiate
	 * @param exp the exponent (greater than 0)
	 * @return the result of the exponentiation
	 */
	public static final int intPow(final int num, final int exp) {
		int result = 1;
		for (int i = exp; i > 0; --i) {
			result *= num;
		}
		return result;
	}

	/**
	 * Packs a char array (string) into a String of characters formed by
	 * concatenating consecutive groups of bits from the char array. The
	 * char array must only contain the '1' or '0' characters.
	 * @param charArray the {@link String} to pack
	 * @return the packed string
	 * @throws {@link NumberFormatException} if the string does contain
	 * characters other than '1' and '0'
	 */
	public static final String packCharArray(final String charArray)
	throws NumberFormatException {
		final StringBuilder packed = new StringBuilder();
		final int endIndex = charArray.length();
		for (int i = 0; i < (endIndex / 16) + 1; ++i) {
			/* Obtain a 16-bit string */
			final int toIndex = Math.min((i + 1) * 16, endIndex);
			final String charPack = charArray.substring(i * 16, toIndex);
			/* Obtain the character value that matches that int
			 * and append it to the string builder.
			 */
			if (charPack.isEmpty()) {
				/* We reached the end, nothing more to do */
				break;
			}
			final int ordinal = Integer.parseUnsignedInt(charPack, 2);
			packed.append((char) ordinal);
		}
		return packed.toString();
	}

	/**
	 * Changes the label of a vertex in a {@link GenericGraph}.
	 * @param vertex the {@link JVertex} to change
	 * @param newLabel the new label
	 */
	public static void
	changeVertexLabel(final JVertex vertex, final String to) {
		final String[] parts = GenericGraph.getLabelParts(vertex);
		final String newLabel = to + "$" + parts[1];
		vertex.setLabel(newLabel);
	}

	/**
	 * Adds a random {@link Edge} of unitary weight between two
	 * randomly chosen vertices of a {@link GenericGraph}.
	 * @param graph the graph to be edited
	 */
	public static void addRandomEdge(final GenericGraph graph) {
		final Random randGen = new Random();
		final int size = graph.vertexSet().size();
		final int from = randGen.nextInt(size);
		final int to = randGen.nextInt(size);
		/* Get all vertices into a list */
		final List<JVertex> vertices = graph
				.vertexSet()
				.stream()
				.collect(Collectors.toList());
		graph.addEdge(vertices.get(from), vertices.get(to), 1.0);
	}

	/**
	 * Removes a random edge of unitary weight from a {@link GenericGraph}.
	 * @param graph the graph to be edited
	 * @return {@code true} if an edge was succesfully removed
	 */
	public static boolean removeRandomEdge(final GenericGraph graph) {
		final List<Edge> unitaryEdges = graph.edgeSet()
			.stream()
			.filter(e -> e.edgeWeight() == 1.0)
			.collect(Collectors.toList());
		if (unitaryEdges == null || unitaryEdges.size() == 0) {
			return false;
		}
		final Edge toRemove = unitaryEdges.get(
			new Random().nextInt(unitaryEdges.size()));
		if (toRemove != null) {
			return graph.removeEdge(toRemove);
		} else {
			return false;
		}
	}

	/**
	 * Picks a number of random vertices from a {@link GenericGraph}.
	 * @param graph the graph to pick the vertices from
	 * @param number the number of vertices to collect
	 * @return a {@link Collection<JVertex>} of vertices
	 */
	public static Collection<JVertex>
	pickRandomVertices(final GenericGraph graph, final int number) {
		final List<JVertex> vertices = new ArrayList<>(graph
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
	public static JVertex pickRandomVertex(final GenericGraph graph) {
		final List<JVertex> vertices = graph
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
	public static List<JVertex>
	getIsolatedVertices(final GenericGraph graph) {
		return graph.vertexSet()
				.stream()
				.filter(v -> graph.edgesOf(v).size() == 0)
				.collect(Collectors.toList());
	}

	/**
	 * Removes a randomly chosen isolated {@link JVertex} from a
	 * {@link GenericGraph}
	 * @param graph the graph to remove the vertex from
	 * @returns {@code true} if a vertex was successfully removed
	 */
	public static boolean
	removeRandomIsolatedVertex(final GenericGraph graph) {
		final List<JVertex> isolated = getIsolatedVertices(graph);
		final int numIsolated = isolated.size();
		if (numIsolated > 0) {
			final int nextRand = (new Random()).nextInt(numIsolated);
			return graph.removeVertex(isolated.get(nextRand));
		}
		else {
			return false;
		}
	}

	/**
	 * Given a {@link GenericGraph}, performs a number of elementary
	 * edit operations on it and returns a modified version of it.
	 * @param g the graph to edit
	 * @param editCnt the number of edits
	 * @param labels a list of available labels to mutate to
	 * @return the modified {@link GenericGraph} as well as the number of
	 * successful edits, in a {@link Pair<GenericGraph, Integer>} object
	 */
	public static Pair<GenericGraph, Integer> getEditedGraph(
		final GenericGraph g, final int editCnt, final List<String> labels) {
		final GenericGraph gNew = g.clone();
		final Random randGen = new Random();
		final int labelCnt = labels.size();
		/* properly distribute the edit actions */
		final int removals = randGen.nextInt(editCnt + 1);
		final int changes = randGen.nextInt(editCnt + 1 - removals);
		final int additions = randGen.nextInt(
				editCnt + 1 - removals - changes);
		int totalEdits = 0;
		/* Perform the required number of edits per edit action */
		for (int i = 0; i < removals; ++i) {
			if (removeRandomIsolatedVertex(gNew)) {
				totalEdits++;
			}
		}
		for (int i = 0; i < changes; ++i) {
			final String newLabel = labels.get(randGen.nextInt(labelCnt));
			final JVertex changed = pickRandomVertex(gNew);
			if (!newLabel.equals(changed.getLabel())) {
				changed.setLabel(newLabel);
				totalEdits++;
			}
		}
		for (int i = 0; i < additions; ++i) {
			addRandomEdge(gNew);
		}
		totalEdits += additions;
		return new Pair<>(gNew, totalEdits);
	}
}

