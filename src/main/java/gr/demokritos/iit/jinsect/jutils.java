package gr.demokritos.iit.jinsect;

import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.encoders.CanonicalCoder;

import java.util.Iterator;

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
}

