package gr.demokritos.iit.jinsect;

import gr.demokritos.iit.jinsect.structs.UniqueJVertexGraph;
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
	 * Gets the structural similarity of a graph over another, which is
	 * defined as the sum of the edge structural similarities for all 
	 * edges connecting vertices in the union of the 2 graphs' edge sets.
	 *
	 *
	 * @param gA the first graph
	 * @param gB the second graph
	 * @return the graph structural similarity of gA over gB
	 */
	public static double graphStructuralSimilarity(UniqueJVertexGraph gA,
												   UniqueJVertexGraph gB)
	{
		return gA.totalNormWeight() - gB.totalNormWeight();
	}

	/**
	 * Gets the structural similarity of an edge in two graphs.
	 * The structural similarity is defined as the difference of the 
	 * normalized edge weights in the two graphs. It is non commutative.
	 *
	 * @param gA the first graph
	 * @param gB the second graph
	 * @param vI the source vertex of the edge
	 * @param vJ the target vertex of the edge
	 * @return the structural similarity of the edge in graph gA over gB
	 */
	public static double edgeStructuralSimilarity(UniqueJVertexGraph gA, 
												  UniqueJVertexGraph gB, 
												  JVertex vI, 
												  JVertex vJ)
	{
		// get both weights (0 if edge is not present) and
		// return their difference
		double qA = gA.getNormalizedEdgeWeight(vI, vJ);
		double qB = gB.getNormalizedEdgeWeight(vI, vJ);
		return qA - qB;
	}

	
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
	public static int compareCanonicalCodes(UniqueJVertexGraph gA,
											UniqueJVertexGraph gB)
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
	public static String graphToDot(UniqueJVertexGraph gTree, boolean bDirected) {
		StringBuffer sb = new StringBuffer();
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

			String sA = "_" + vA.toString().replaceAll("\\W", "_");
			String sB = "_" + vB.toString().replaceAll("\\W", "_");
			String sLabel = "";
			if (e instanceof Edge) {
				double dWeight = gTree.getEdgeWeight(e);
				sLabel += String.format("%4.2f", dWeight);
			}
			if (e instanceof Edge)
				sb.append("\t" + sA + " " + sConnector + " " + sB + 
						" [label=\"" + sLabel.replaceAll("\\s+", " ") + "\"]\n");
			else
				sb.append("\t" + sA + " " + sConnector + " " + sB + "\n");
			sb.append("\t" + sA + " [label=\"" + sA + "\"] " + "\n");
		}
		sb.append("}");

		return sb.toString();
	}
}

