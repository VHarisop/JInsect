package gr.demokritos.iit.jinsect.structs;

import java.util.*;

/**
 * A class that encodes vertices by assigning them a weight inversely
 * proportional to the vertex's entropy. The value returned is 
 * (1 - vertex_entropy) * weight_factor.
 *
 * @author VHarisop
 */
public class VertexEntropy extends HashMap<String, Double> {

	static final long serialVersionUID = 1L;

	/**
	 * The value used for weighing returned values.
	 */
	protected double weight = 1.0;

	/**
	 * The {@link UniqueJVertexGraph} this object operates on.
	 */
	protected UniqueJVertexGraph uvg;

	/**
	 * @see java.util.HashMap#HashMap()
	 */
	public VertexEntropy() {
		super();
	}

	/**
	 * @see java.util.HashMap#HashMap(int)
	 */
	public VertexEntropy(int initialCapacity) {
		super(initialCapacity);
	}

	/**
	 * @see java.util.HashMap#HashMap(int, float)
	 */
	public VertexEntropy(int initialCapacity, float loadFactor) {
		super(initialCapacity, loadFactor);
	}

	/**
	 * @see java.util.HashMap#HashMap(Map)
	 */
	public VertexEntropy(Map<? extends String, ? extends Double> m) {
		super(m);
	}

	/**
	 * Sets the underlying {@link UniqueJVertexGraph} object that the
	 * encoder operates on, and returns the modified VertexEntropy object.
	 *
	 * @param _uvg the graph to operate on
	 * @return the modified object
	 */
	public VertexEntropy withGraph(UniqueJVertexGraph _uvg) {
		this.uvg = _uvg;
		return this;
	}

	/**
	 * Sets {@link #weight} to a new provided value, returning the modified
	 * object.
	 *
	 * @param weight the new weight to be used
	 * @return the modified VertexEntropy object
	 */
	public VertexEntropy withWeight(double weight) {
		this.weight = weight;
		return this;
	}

	/**
	 * Adds a new vertex label to the entropy coder and 
	 * associates it with its vertex's entropy.
	 *
	 * @param v the vertex to add
	 * @return the value associated after adding the vertex
	 */
	public Double putLabel(JVertex v) {
		Double incident =
			uvg.incomingWeightSumOf(v) +
			uvg.outgoingWeightSumOf(v);
		Double total = uvg.totalEdgeWeight();
		return super.put(v.getLabel(), (incident / total));
	}

	/**
	 * Adds a new vertex label to the entropy coder and associates
	 * it with the vertex's entropy.
	 *
	 * @param vLabel the label of the vertex
	 * @return the value associated with the added label
	 */
	public Double putLabel(String vLabel) {
		JVertex vCurr = uvg.locateVertex(vLabel);
		return this.putLabel(vCurr);
	}


	/**
	 * @see #getEntropy(String)
	 *
	 * @param vGet the vertex 
	 * @return the associated weight
	 */
	public Double getEntropy(JVertex vGet) {
		String vLabel = vGet.getLabel();
		return getEntropy(vLabel);
	}

	/**
	 * Gets the entropy associated with a given vertex. If the vertex's label
	 * is not found in the backing map, it is automatically added and its new
	 * associated weight is returned. 
	 *
	 * @param vLabel the vertex's label 
	 * @return the associated weight
	 */
	public Double getEntropy(String vLabel) {
		if (this.get(vLabel) == null) {
			putLabel(vLabel);
			return getWeight(uvg.locateVertex(vLabel));
		}
		else {
			return getWeight(vLabel);
		}
	}

	/**
	 * Returns the weight associated with the label of a vertex.
	 *
	 * @param v the vertex for which the weight is requested
	 * @return the associated weight
	 */
	public Double getWeight(JVertex v) {
		return (1 - this.get(v.getLabel())) * this.weight;
	}

	/**
	 * Returns the weight associated with a label.
	 *
	 * @param vLabel the vertex label
	 * @return the associated weight
	 */
	public Double getWeight(String vLabel) {
		return (1 - this.get(vLabel)) * this.weight;
	}
}
