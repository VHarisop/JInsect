package gr.demokritos.iit.jinsect.structs.calculators;

import gr.demokritos.iit.jinsect.structs.*;

import java.util.HashMap;

/**
 * A class that is utilized for calculating a {@link UniqueVertexGraph}'s 
 * vertex in- and out- degree variances, which can be useful in some graph
 * similarity metric.
 *
 * @author VHarisop
 */
public class PerVertexVarianceCalculator {
	/**
	 * The graph on which the calculator will operate 
	 */
	protected UniqueVertexGraph uvg;
	
	/**
	 * The sum of weight variance ratios
	 */
	protected double totalVarRatio;

	/**
	 * A hashmap to store already computed vertex - variance pairs
	 */
	protected HashMap<JVertex, Double> vertexVariances;

	/**
	 * Flag indicating if values have been calculated, so that
	 * unnecessary new calculations are avoided. 
	 */
	private boolean cached;

	/**
	 * Creates a new PerVertexVarianceCalculator object that operates on a 
	 * {@link UniqueVertexGraph} object.
	 *
	 * @param uvgOn the graph on which to operate 
	 */
	public PerVertexVarianceCalculator(UniqueVertexGraph uvgOn) {
		this.uvg = uvgOn;
		cached = false;
		vertexVariances = new HashMap<JVertex, Double>();
	}

	/**
	 * Calculates the variances of a vertex's outgoing and incoming edge 
	 * weights and returns the ratio of minimum to maximum among the two.
	 *
	 * @param v the vertex on which to operate
	 * @return the ratio of the smaller to higher weight variance
	 */
	protected double getVarianceRatioOf(JVertex v) {
		Double val = vertexVariances.get(v);
		if (val != null) {
			return val;
		} 
		else {
			double outMean = uvg.outgoingWeightSumOf(v) / uvg.outDegreeOf(v);
			double inMean = uvg.incomingWeightSumOf(v) / uvg.inDegreeOf(v);
			double sumOut = 0, sumIn = 0, diff;

			for (Edge e: uvg.outgoingEdgesOf(v)) {
				diff = e.edgeWeight() - outMean;
				sumOut += diff * diff;
			}

			for (Edge e: uvg.incomingEdgesOf(v)) {
				diff = e.edgeWeight() - inMean;
				sumIn += diff * diff;
			}

			/* normalize to a small value if either of them is zero */
			sumIn = (sumIn > 0.0) ? sumIn : 0.1;
			sumOut = (sumOut > 0.0) ? sumOut : 0.1;

			/* store new value to the hashmap before returning it */
			val = sumIn / sumOut;
			// val  = (sumIn < sumOut) ? sumIn / sumOut : sumOut / sumIn;
			vertexVariances.put(v, val);
			return val;
		}
	}

	/**
	 * Calculates and returns the sum of the graph's vertex incoming and outgoing
	 * weight variance ratios. This sum is guaranteed to be bounded between
	 * 0 and |V(G)|, since the variance ratio of each vertex is between 0 and 1.
	 *
	 * @return the sum of variance ratios
	 */
	public double getTotalVarianceRatios() {
		/* if result is cached, return the cached value */
		if (cached)
			return totalVarRatio;

		totalVarRatio = 0;
		for (JVertex vCurr: uvg.vertexSet()) {
			totalVarRatio += this.getVarianceRatioOf(vCurr);
		}
		cached = true;
		return totalVarRatio;
	}
}
