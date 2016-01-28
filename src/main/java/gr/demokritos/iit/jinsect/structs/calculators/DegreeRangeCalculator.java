package gr.demokritos.iit.jinsect.structs.calculators;

import gr.demokritos.iit.jinsect.structs.*;
import java.util.HashMap;
import java.util.Set;

public class DegreeRangeCalculator {
	/**
	 * A mapping of vertex labels to in-degree ranges.
	 */
	protected HashMap<String, Double> inWeightMap;

	/**
	 * A mapping of vertex labels to out-degree ranges.
	 */
	protected HashMap<String, Double> outWeightMap;

	/**
	 * The underlying graph on which this object operates. 
	 */
	protected UniqueJVertexGraph uvGraph;

	/**
	 * Creates a new DegreeRangeCalculator object operating on 
	 * a given {@link UniqueJVertexGraph}.
	 *
	 * @param uvg the graph to operate on
	 */
	public DegreeRangeCalculator(UniqueJVertexGraph uvg) {
		this.uvGraph = uvg;

		/* initialize new mappings with initial capacity equal
		 * to the vertex set size of the underlying graph */
		inWeightMap = new HashMap<String, Double>(uvg.vertexSet().size());
		outWeightMap = new HashMap<String, Double>(uvg.vertexSet().size());

		/* populate the maps */
		populateMaps();
	}

	/**
	 * Utility function that calculates and returns the range
	 * of the incoming and outgoing edges. The return value 
	 * is double containing [inRange, outRange]
	 */
	private double[] calculateRange(JVertex vCurr) {
		// get both edge sets
		Set<Edge> outEdges = uvGraph.outgoingEdgesOf(vCurr);
		Set<Edge> inEdges = uvGraph.incomingEdgesOf(vCurr);

		double minOut, minIn, maxOut, maxIn;

		/* find max, min weight for outgoing edges */
		if (outEdges.size() == 0) {
			minOut = maxOut = 0.0;
		}
		else {
			maxOut = minOut = outEdges.iterator().next().edgeWeight();
			for (Edge e: outEdges) {
				if (minOut > e.edgeWeight()) {
					minOut = e.edgeWeight();
					continue;
				}
				if (maxOut < e.edgeWeight()) {
					maxOut = e.edgeWeight();
				}
			}
		}

		if (inEdges.size() == 0) {
			maxIn = minIn = 0.0;
		}
		else {
			maxIn = minIn = inEdges.iterator().next().edgeWeight();
			for (Edge e: inEdges) {
				if (minIn > e.edgeWeight()) {
					minIn = e.edgeWeight();
					continue;
				}

				if (maxIn < e.edgeWeight()) {
					maxIn = e.edgeWeight();
				}
			}
		}

		return new double[] { maxIn - minIn, maxOut - minOut };
	}

	/**
	 * Calculates the in and out degree ranges for all vertices
	 * and stores them in their respective hash maps to enable 
	 * efficient retrieval. 
	 */
	private void populateMaps() {
		double[] ranges;
		/* get ranges for every vertex and populate
		 * the label - range hash maps */
		for (JVertex vCurr: uvGraph.vertexSet()) {
			ranges = calculateRange(vCurr);
			inWeightMap.put(vCurr.getLabel(), ranges[0]);
			outWeightMap.put(vCurr.getLabel(), ranges[1]);
		}
	}

	/**
	 * Returns the in and out degree ranges for a vertex matching
	 * a given label. 
	 *
	 * @param vLabel the vertex label
	 * @return a 2-element array containing the in and out degree ranges
	 */
	public double[] getDegrees(String vLabel) {
		return new double[] 
		{ inWeightMap.get(vLabel), outWeightMap.get(vLabel) };
	}

	/**
	 * Clears the mappings of the degree range calculator
	 */
	public void clear() {
		inWeightMap.clear();
		outWeightMap.clear(); 
	}

	/**
	 * Replaces the graph this calculator operates on with a newly
	 * provided {@link UniqueJVertexGraph}. The maps are cleared and
	 * repopulated based on the new graph.
	 *
	 * @param newGraph the new graph to operate on
	 */
	public void setGraph(UniqueJVertexGraph newGraph) {
		clear();
		this.uvGraph = newGraph;
		populateMaps();
	}
}