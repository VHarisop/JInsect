package gr.demokritos.iit.jinsect.structs.calculators;

import gr.demokritos.iit.jinsect.structs.*;

/**
 * A class that is utilized for calculating a {@link UniqueJVertexGraph}'s 
 * vertex in- and out- degree variances, which can be useful in some graph
 * similarity metric.
 *
 * @author VHarisop
 */
public class DegreeVarianceCalculator {
	/**
	 * The graph on which the calculator will operate 
	 */
	protected UniqueJVertexGraph uvg;

	/**
	 * The indegree mean
	 */
	protected double indegreeMean;

	/**
	 * The incoming weight mean
	 */
	protected double inWeightMean;

	/**
	 * The outgoing weight mean
	 */
	protected double outWeightMean;

	/**
	 * The outdegree mean
	 */
	protected double outdegreeMean;

	/**
	 * The indegree variance
	 */
	protected double inDegreeVariance;

	/**
	 * The outdegree variance
	 */
	protected double outDegreeVariance;

	/**
	 * The incoming weight variance
	 */
	protected double inWeightVariance;

	/**
	 * The outgoing weight variance
	 */
	protected double outWeightVariance;

	/**
	 * Flag indicating if values have been calculated, so that
	 * unnecessary new calculations are avoided. 
	 */
	private boolean cached;

	/**
	 * Flag indicating if weight variance values have been calculated
	 * so that unnecessary new calculations are avoided.
	 */
	private boolean weightCached;

	/**
	 * Creates a new DegreeVarianceCalculator object that operates on a 
	 * {@link UniqueJVertexGraph} object.
	 *
	 * @param uvgOn the graph on which to operate 
	 */
	public DegreeVarianceCalculator(UniqueJVertexGraph uvgOn) {
		this.uvg = uvgOn;
		cached = false;
		weightCached = false;
	}

	/**
	 * Utility function that calculates the means of in- and out- degrees.
	 */
	private void calculateMeans() {
		double sumIn = 0;
		double sumOut = 0; 

		for (JVertex vCurr: uvg.vertexSet()) {
			sumOut += uvg.outDegreeOf(vCurr);
			sumIn += uvg.inDegreeOf(vCurr);
		}

		this.indegreeMean = sumIn / (uvg.vertexSet().size());
		this.outdegreeMean = sumOut / (uvg.vertexSet().size());
	}

	private void calculateWeightMeans() {
		double sumIn = 0, sumOut = 0;
		for (JVertex vCurr: uvg.vertexSet()) {
			sumOut += uvg.outgoingWeightSumOf(vCurr);
			sumIn += uvg.incomingWeightSumOf(vCurr);
		}

		inWeightMean = sumIn / (uvg.vertexSet().size());
		outWeightMean = sumOut / (uvg.vertexSet().size());
	}

	/**
	 * Calculates the variances of a vertex's outgoing and incoming
	 * weight sum and returns them in a 2-element double array by this
	 * order.
	 *
	 * @param v the vertex whose variances are computed
	 * @return a double array containing the variances 
	 */
	private double[] calculateWeightVariances(JVertex v) {
		double outDiff = uvg.outgoingWeightSumOf(v) - outWeightMean;
		double inDiff = uvg.incomingWeightSumOf(v) - inWeightMean;

		return new double[] { outDiff * outDiff, inDiff * inDiff };
	}

	/**
	 * Calculates the variances of a vertex's indegree and outdegree
	 * and returns them in a 2-element array which contains the outdegree
	 * variance at index 0 and the indegree variance at index 1.
	 */
	private double[] calculateDegreeVariances(JVertex v) {
		double outDiff = uvg.outDegreeOf(v) - this.outdegreeMean;
		double inDiff = uvg.inDegreeOf(v) - this.indegreeMean;

		return new double[] { outDiff * outDiff, inDiff * inDiff};
	}

	/**
	 * Returns the average variance of the graph's vertex indegrees and
	 * outdegrees. The computation is cached to avoid extra overhead for
	 * successive calls.
	 *
	 * @return the average of the graphs' vertex indegree and outdegree variances
	 */
	public double getAvgDegreeVariance() {
		if (!cached) {
			calculateMeans();
			outDegreeVariance = 0.0;
			inDegreeVariance = 0.0;
			for (JVertex v: uvg.vertexSet()) {
				double[] vars = calculateDegreeVariances(v);
				outDegreeVariance += vars[0];
				inDegreeVariance += vars[1];
			}
			/* set cached flag to avoid future computations */
			cached = true;
		}

		return (outDegreeVariance + inDegreeVariance) / 2;
	}

	public double getTotalWeightVariance() {
		if (!weightCached) {
			calculateWeightMeans();
			outWeightVariance = 0.0;
			inWeightVariance = 0.0;
			for (JVertex v: uvg.vertexSet()) {
				double[] vars = calculateWeightVariances(v);
				outWeightVariance += vars[0];
				inWeightVariance += vars[1];
			}

			/* set weightCached flag to avoid future computations */
			weightCached = true;
		}

		return (outWeightVariance * inWeightVariance) /
			(outWeightVariance + inWeightVariance);
	}

}
