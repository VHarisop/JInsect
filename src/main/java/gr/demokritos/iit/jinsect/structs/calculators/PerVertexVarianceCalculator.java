package gr.demokritos.iit.jinsect.structs.calculators;

import gr.demokritos.iit.jinsect.structs.*;

/**
 * A class that is utilized for calculating a {@link UniqueJVertexGraph}'s 
 * vertex in- and out- degree variances, which can be useful in some graph
 * similarity metric.
 *
 * @author VHarisop
 */
public class PerVertexVarianceCalculator {
	/**
	 * The graph on which the calculator will operate 
	 */
	protected UniqueJVertexGraph uvg;
	
	/**
	 * The sum of weight variance ratios
	 */
	protected double totalVarRatio;

	/**
	 * Flag indicating if values have been calculated, so that
	 * unnecessary new calculations are avoided. 
	 */
	private boolean cached;

	/**
	 * Creates a new PerVertexVarianceCalculator object that operates on a 
	 * {@link UniqueJVertexGraph} object.
	 *
	 * @param uvgOn the graph on which to operate 
	 */
	public PerVertexVarianceCalculator(UniqueJVertexGraph uvgOn) {
		this.uvg = uvgOn;
		cached = false;
	}

	/**
	 * Utility function that calculates the variances of a vertex's outgoing
	 * and incoming edge weights and returns their ratio.
	 *
	 * @param v the vertex on which to operate
	 * @return the deviation between the vertex's incoming / outgoing variances
	 */
	protected double getVarianceRatioOf(JVertex v) {
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

		sumIn = (sumIn > 0.0) ? sumIn : 0.1;
		return (sumOut / sumIn);
	}

	/**
	 * Calculates and returns the sum of the graph's vertex incoming and outgoing
	 * weight variance ratios.
	 *
	 * @return the sum of variance ratios
	 */
	public double getTotalVarianceRatios() {
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
