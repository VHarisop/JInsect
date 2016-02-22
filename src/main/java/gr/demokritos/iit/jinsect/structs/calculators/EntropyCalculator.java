package gr.demokritos.iit.jinsect.structs.calculators;

import gr.demokritos.iit.jinsect.structs.*;

/**
 * A calculator for entropy-related quantities.
 */
public class EntropyCalculator {
	
	/**
	 * The backing {@link VertexEntropy} object.
	 */
	protected VertexEntropy vEnt;

	/**
	 * The UniqueJVertexGraph this object operates on.
	 */
	protected UniqueVertexGraph opGraph;

	/**
	 * A cached value of total entropy.
	 */
	protected double totalEntropy;
	protected boolean cached;

	/**
	 * Creates a new EntropyCalculator object operating on a specified graph.
	 *
	 * @param uvg the graph to operate on
	 */
	public EntropyCalculator(UniqueVertexGraph uvg) {
		vEnt = new VertexEntropy().withGraph(uvg);
		opGraph = uvg;
		cached = false;
	}

	/**
	 * Simple getter for the graph this calculator operates on.
	 *
	 * @return the underlying graph object
	 */
	public UniqueVertexGraph getGraph() {
		return opGraph;
	}

	/**
	 * @see VertexEntropy#getEntropy(JVertex) getEntropy
	 */
	public double getEntropy(JVertex vCurr) {
		return vEnt.getEntropy(vCurr);
	}

	/**
	 * @see VertexEntropy#getEntropy(String) getEntropy
	 */
	public double getEntropy(String vLabel) {
		return vEnt.getEntropy(vLabel);
	}

	/**
	 * Gets the total vertex entropy of the underlying graph, which is simply
	 * the sum of entropies for each vertex in the graph.
	 *
	 * @return the sum of vertex entropies in the graph
	 */
	public double getTotalVertexEntropy() {
		if (cached) {
			return this.totalEntropy;
		}
		/* first, populate the entropy object */
		for (JVertex vCurr: opGraph.vertexSet()) {
			vEnt.putLabel(vCurr);
		}
		
		/* sum all the vertex entropies */
		double sum = 0;
		for (JVertex vCurr: opGraph.vertexSet()) {
			sum += vEnt.getEntropy(vCurr);
		}
		/* set the cached variable and flag */
		cached = true; this.totalEntropy = sum;

		return this.totalEntropy;
	}
}
