package gr.demokritos.iit.jinsect.structs;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;

/* use JGraphT for basic graph operations */
import org.jgrapht.graph.*;

import gr.demokritos.iit.jinsect.structs.calculators.*;

/**
 * UniqueJVertexGraph is an extension to a weighted graph from
 * jgrapht that is used in JInsect. It is a directed graph with
 * weighted edges.
 *
 * @author VHarisop
 */
public class UniqueVertexGraph 
extends DefaultDirectedWeightedGraph<JVertex, Edge>
{

	/**
	 * A {@link EntropyCalculator} object to encode the vertices of the graph.
	 */
	public EntropyCalculator entropyCalc;

	/**
	 * A {@link DegreeVarianceCalculator} that operates on the vertices
	 * of this graph.
	 */
	public DegreeVarianceCalculator degreeVarCalc;

	/**
	 * A {@link PerVertexVarianceCalculator} that operates on this graph's
	 * vertices.
	 */
	public PerVertexVarianceCalculator perVertexVarCalc;

	/**
	 * A {@link WeightRangeCalculator} that operates on this graph's vertices.
	 */
	public WeightRangeCalculator degreeRangeCalc;

	/**
	 * A {@link OrderedWeightCalculator} that operates on this graph's vertices.
	 */
	public OrderedWeightCalculator ordWeightCalc;

	static final long serialVersionUID = 1L;

	/**
	 * A map of label to vertex correspondences for this graph.
	 */
    public HashMap<String, JVertex> UniqueVertices;

	/**
	 * flag that indicates if the sum of the normalized
	 * weights has been cached or not 
	 */
	protected boolean cached = false;

	/**
	 * flag that indicates if the sum of the graph's 
	 * edge weights has been cached or not
	 */
	protected boolean totalCached = false;

	/**
	 * cached sum of normalized weights for efficient
	 * similarity calculation
	 */
	protected double normSum = 0;

	/**
	 * cached sum of the graph's edge weights
	 */
	protected double edgeSum;

	/**
	 * Returns a <tt>UniqueJVertexJGraph</tt> object. 
	 */
	public UniqueVertexGraph() {
		super(Edge.class);
		UniqueVertices = new HashMap<String, JVertex>();
	}
    
	/** 
	 * Checks whether a given vertex exists in this graph.
     * @param v The vertex, the label of which will be used for the lookup.
     * @return True if the vertex is contained in this graph. Otherwise false.
     */
    public boolean contains(JVertex v) {
        return UniqueVertices.containsKey(v.getLabel());
    }
    
	/** 
	 * Looks up a given vertex in this graph.
     * @param v The vertex, the label of which will be used for the lookup.
     * @return The vertex if it is contained in this graph. Otherwise null.
     */
    public synchronized JVertex locateVertex(JVertex v) {
        return UniqueVertices.get(v.getLabel());
    }

    /** 
	 * Looks up a given vertex label in this graph.
     * @param sJVertexLabel  The label which will be used for the lookup.
     * @return The vertex if it is contained in this graph. Otherwise null.
     */
    public synchronized JVertex locateVertex(String sJVertexLabel) {
        return UniqueVertices.get(sJVertexLabel);
    }

	/**
	 * Get the total number of edges contained in the graph.
	 * @return the number of the graph's edges
	 */
	public synchronized int getEdgeCount() {
		return super.edgeSet().size();
	}

	/**
	 * Add a <tt>JVertex</tt> to the graph, if it is not already present.
	 * @param v the <tt>JVertex</tt> to be added 
	 */
	public synchronized void add(JVertex v) {
		/* if already existing, return immediately */
		if (this.contains(v))
			return;
		
		/* otherwise, add to vertices and update label map */
		super.addVertex(v);
		UniqueVertices.put(v.getLabel(), v);
	}

	/**
	 * Adds a weighted edge to the graph that connects a pair of
	 * vertices. If the edge was already present, the weight is not
	 * updated, since <tt>addEdge</tt> will return <tt>null</tt>.
	 * @param v1 the first vertex to be connected
	 * @param v2 the second vertex to be connected
	 * @param weight the weight of the connection
	 * @return the newly created edge if addition was a success, else 
	 * 		   <tt>null</tt>
	 */
	public synchronized Edge addEdge(JVertex v1, JVertex v2, double weight)
	{
		/* implicitly add missing vertices from the supergraph */
		if (!(this.contains(v1))) 
			add(v1);

		if (!(this.contains(v2))) 
			add(v2);

		/* add edge to the graph, and update the weight 
		 * if the addition was successful. Also make the
		 * cached variable false */
		Edge e = super.addEdge(v1, v2);
		if (e != null) {
			super.setEdgeWeight(e, weight);
			cached = false;
		}

		return e;
	}

	/**
	 * Gets all vertices adjacent to a given vertex in 
	 * an unmodifiable list.
	 * @param vFrom the vertex whose adjacent vertices are needed
	 * @return an unmodifiable list of adjacent vertices
	 */
	public List<JVertex> getAdjacentVertices(JVertex vFrom) {
		List<JVertex> adjacentVertices = new ArrayList<JVertex>();
		for (Edge e: super.outgoingEdgesOf(vFrom)) {
			adjacentVertices.add(super.getEdgeTarget(e));
		}

		return Collections.unmodifiableList(adjacentVertices);
	}

	/**
	 * Gets the total weight of all the graph's edges. 
	 *
	 * @return the total edge weight of the graph
	 */
	public double totalEdgeWeight() {
		if (!totalCached) {
			edgeSum = sumWeights(super.edgeSet());
			totalCached = true;
		}

		return edgeSum;
	}

	/**
	 * Gets the total normalized weight of all the graph's edges.
	 * @return a double containing the sum of normalized weights
	 */
	public double totalNormWeight() {
		// if result is not cached, calculate from the beginning
		if (!cached) { 	
			normSum = 0; 

			// calculate sum of normalized weights
			for (Edge e: super.edgeSet()) {
				normSum += getNormalizedEdgeWeight(e);
			}

			// set caching variable
			cached = true;
		}

		return normSum;
	}
	
	/**
	 * Gets the normalized edge weight for a directed edge connecting
	 * two vertices in the graph. The normalized edge weight is defined
	 * as the geometric mean of the ratios of edge weight over the sum of
	 * incoming and outgoing edge weights respectively. 
	 *
	 * @param e a {@link Edge} that belongs to the graph
	 * @return a double value with the normalized edge weight
	 */
	public double getNormalizedEdgeWeight(Edge e) {
		double w = super.getEdgeWeight(e);
		JVertex vFrom = super.getEdgeSource(e);
		JVertex vTo = super.getEdgeTarget(e);

		// get ratios of weights
		double kOut = w / outgoingWeightSumOf(vFrom);
		double kIn = w / incomingWeightSumOf(vTo);

		// return the geometric mean of the 2 ratios
		return (kOut * kIn / (kOut + kIn));
	}

	/**
	 * Gets the normalized edge weight for a directed edge connecting
	 * two vertices in the graph. The normalized edge weight is defined
	 * as the geometric mean of the ratios of edge weight over the sum of
	 * incoming and outgoing edge weights respectively. If no edge that
	 * connects the two vertices exists, the normalized weight is assumed
	 * to be 0 by definition. 
	 *
	 * @param v1 the first connected vertex
	 * @param v2 the second connected vertex
	 * @return a double value with the normalized edge weight
	 */
	public double getNormalizedEdgeWeight(JVertex v1, JVertex v2) {
		/* get edge connecting v1 and v2
		 * and return 0 if no such edge exists */
		Edge e = super.getEdge(v1, v2);
		if (e == null) 
			return 0;

		double wi = super.getEdgeWeight(e);
		/* Get sum of incoming weights for v2 and 
		 * sum of outgoing weights for v1 */
		double kOut = wi / outgoingWeightSumOf(v1);
		double kIn = wi / incomingWeightSumOf(v2);

		// return the geometric mean of the two ratios 
		return ((kOut * kIn) / (kOut + kIn));
	}

	/**
	 * Gets the sum of all the graph's vertex degree ratios, which are 
	 * calculated using {@link #getDegreeRatio}.
	 *
	 * @return the sum of all the graph's vertex degree ratios.
	 */
	public double getDegreeRatioSum() {
		double total = 0.0;
		for (JVertex v: this.vertexSet()) {
			total += getDegreeRatio(v);
		}
		return total;
	}

	/**
	 * Gets the degree ratio of a vertex, which is the ratio of its indegree
	 * over its outdegree. If the vertex's outdegree is zero, it is replaced 
	 * by 0.1 to avoid division by zero.
	 *
	 * @param v the vertex for which the degree ratio is required
	 * @return the vertex's ratio of indegree over outdegree
	 */
	public double getDegreeRatio(JVertex v) {
		/* if outSum ~ 0, replace it with 0.1 */
		double outSum = outgoingWeightSumOf(v);
		outSum = (outSum > 0.000001) ? outSum : 0.1;

		return (incomingWeightSumOf(v) / outSum);
	}

	/**
	 * Returns the quantization value of a vertex, which is defined as the product
	 * of |indegree - outdegree| * vertex_quantization_value, where the 
	 * quantization value is provided in a hash map that matches vertex labels to
	 * double values. The aforementioned hashmap is provided by a database that
	 * builds the graph index. 
	 *
	 * @param v the JVertex for which the value is computed
	 * @param vW the hash map of Label - Weight entries
	 * @return the vertex's quant value 
	 */
	public double getQuantValue(JVertex v, VertexCoder vW) {
		String vLabel = v.getLabel();

		double factor = (incomingWeightSumOf(v) * outgoingWeightSumOf(v)) /
			(incomingWeightSumOf(v) + outgoingWeightSumOf(v));

		/* if the key was not already there, add it now */
		if ( !(vW.containsKey(vLabel)) ) {
			double newVal = vW.putLabel(v.getLabel());
			// System.out.printf("Unseen: %s - new val:%3.3f\n", vLabel, newVal);
			return newVal * factor;
		}
		else {
			double labelVal = vW.get(vLabel); 
			return labelVal * factor;
		}

	}

	/**
	 * Computes the sum of edge weights for edges incident to a vertex.
	 * @param v the <tt>JVertex</tt> for which the sum is requested
	 * @return the sum of incident edge weights, or 0 if no edges are incident.
	 */
	public synchronized double weightSumOf(JVertex v) {
		return sumWeights(super.edgesOf(v));
	}
	
	/**
	 * Computes the sum of edge weights for a vertex's incoming edges.
	 * @param v the <tt>JVertex</tt> for which the sum is calculated
	 * @return the sum of incoming edge weights, or 0 if no such edges exist.
	 */
	public synchronized double incomingWeightSumOf(JVertex v) {
		return sumWeights(super.incomingEdgesOf(v));
	}

	/**
	 * Computes the sum of edge weights for a vertex's outgoing edges.
	 * @param v the <tt>JVertex</tt> for which the sum is calculated
	 * @return the sum of outgoing edge weights, or 0 if no such edges exist.
	 */
	public synchronized double outgoingWeightSumOf(JVertex v) {
		return sumWeights(super.outgoingEdgesOf(v));
	}

	/**
	 * Private helper function that sums the weights of a set of edges.
	 * @param eList a set of weighted edges 
	 * @return the sum of the edge set or 0 if the set is empty 
	 */
	private double sumWeights(Set<Edge> eList) {
		double sum = 0;
		
		/* sum all the edge weights */
		for (Edge e: eList) { sum += super.getEdgeWeight(e); }

		return sum;
	}

	/**
	 * Calculates the total weight variance of the graph's vertices
	 * as computed by {@link #degreeVarCalc}.
	 *
	 * @return the total weight variance
	 */
	public double getWeightVariance() {
		if (degreeVarCalc == null) {
			degreeVarCalc = new DegreeVarianceCalculator(this);
		}

		/* get the total variance */
		return degreeVarCalc.getWeightVariance();
	}
	
	/**
	 * Calculates the degree variance of the graph's vertices
	 * as computed by {@link #degreeVarCalc}.
	 *
	 * @return the degree variance of the graph
	 */
	public double getDegreeVariance() {
		if (degreeVarCalc == null) {
			degreeVarCalc = new DegreeVarianceCalculator(this);
		}

		/* get the total variance */
		return degreeVarCalc.getDegreeVariance();
	}

	/**
	 * Calculates the vertex weight range code, which is defined as the sum 
	 * of the graph's vertex code + weight ranges. These are the quantities
	 * <tt>(v_code + max(v_inweight) - min(v_inweight)) * 
	 * (v_code + max(v_outweight) - min(v_outweight)) </tt> where the 
	 * <tt>v_code</tt> quantities are supplied by a {@link VertexCoder} 
	 * and a {@link VertexEntropy}.
	 *
	 * @param vWt the vertex coder to use
	 * @return the sum of weight range codes
	 */
	public double getWeightRangeCode(VertexCoder vwMap) {
		if (degreeRangeCalc == null || entropyCalc == null) {
			degreeRangeCalc = new WeightRangeCalculator(this);
			entropyCalc = new EntropyCalculator(this);
		}
		
		double sum = 0; double[] ranges; double vW;
		for (JVertex v: this.vertexSet()) {
			ranges = degreeRangeCalc.getDegrees(v.getLabel());
			vW = vwMap.getLabel(v.getLabel());
			double weight = entropyCalc.getEntropy(v.getLabel()) + vW;

			sum += (weight + ranges[0]) * (weight + ranges[1]);
		}

		return sum;
	}


	/**
	 * Gets the sum of the vertex entropy for the vertices
	 * of this graph. 
	 *
	 * @return the sum of vertex entropies
	 */
	public double getTotalVertexEntropy() {
		if (entropyCalc == null) {
			entropyCalc = new EntropyCalculator(this);
		}
		return entropyCalc.getTotalVertexEntropy();
	}

	/**
	 * Calculates the sum of this graph's vertex weight variance 
	 * ratios via {@link #perVertexVarCalc}.
	 *
	 * @return the sum of vertex weight variance ratios
	 */
	public double getTotalVarRatios() {
		if (perVertexVarCalc == null) {
			perVertexVarCalc = new PerVertexVarianceCalculator(this);
		}
		/* get the total var diff */
		return perVertexVarCalc.getTotalVarianceRatios();
	}

	/**
	 * Calculates this graph's ordered (weight, vertex) pairs via
	 * {@link #ordWeightCalc} and returns them to the caller.
	 *
	 * @return the list of ordered weight pairs of this graph
	 */
	public List<Pair<JVertex, Double>> getOrderedWeightPairs() {
		if (null == ordWeightCalc) {
			ordWeightCalc = new OrderedWeightCalculator(this);
		}
		return ordWeightCalc.getOrderedPairs();
	}
	  
	@Override
    public Object clone() {
		JVertex v1, v2;
        UniqueVertexGraph res = new UniqueVertexGraph();
		
		/* add all edges to the clone graph - all vertices will
		 * eventually be added both to the supergraph's vertex set
		 * and the hashmap, because of calls to the add() method */
        for (Edge eCur: this.edgeSet()) {
            try {
				v1 = super.getEdgeSource(eCur);
				v2 = super.getEdgeTarget(eCur);

                res.addEdge(v1, v2, super.getEdgeWeight(eCur));
            } catch (Exception ex) {
				ex.printStackTrace();
                return null;
            }
		}
        return res;
    }
    
}
