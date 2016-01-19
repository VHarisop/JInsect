package gr.demokritos.iit.jinsect.structs;

import java.util.Collections;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Set;

/* use JGraphT for basic graph operations */
import org.jgrapht.graph.*;

/**
 * UniqueJVertexGraph is an extension to a weighted graph from
 * jgrapht that is used in JInsect. It is an directed graph with
 * weighted edges 
 *
 * @author VHarisop
 */
public class UniqueJVertexGraph 
extends DefaultDirectedWeightedGraph<JVertex, Edge>
{
	static final long serialVersionUID = 1L;
    public HashMap<String, JVertex> UniqueVertices;

	/**
	 * flag that indicates if the sum of the normalized
	 * weights has been cached or not 
	 */
	protected boolean cached = false;
	
	/**
	 * cached sum of normalized weights for efficient
	 * similarity calculation
	 */
	protected double normSum = 0;

	/**
	 * Returns a <tt>UniqueJVertexJGraph</tt> object. 
	 */
	public UniqueJVertexGraph() {
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
        return (JVertex)UniqueVertices.get(v.getLabel());
    }

    /** 
	 * Looks up a given vertex label in this graph.
     * @param sJVertexLabel  The label which will be used for the lookup.
     * @return The vertex if it is contained in this graph. Otherwise null.
     */
    public synchronized JVertex locateVertex(String sJVertexLabel) {
        return (JVertex)UniqueVertices.get(sJVertexLabel);
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
	 */
	public double totalEdgeWeight() {
		return sumWeights(super.edgeSet());
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
	 * Returns the quantization value of a vertex, which is defined as the product
	 * of (indegree / outdegree) * vertex_quantization_value, where the 
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

		/* If outSum ~ 0, replace it with unity. */
		double outSum = outgoingWeightSumOf(v);
		outSum = (outSum > 0.000001) ? outSum : 1.0;

		/* if the key was not already there, add it now */
		if ( !(vW.containsKey(vLabel)) ) {
			System.out.printf("Label not found! - [%s]\n", vLabel);
			double newVal = vW.putLabel(v.getLabel());
			return (newVal * incomingWeightSumOf(v)) / outSum;
		}
		else {
			double labelVal = vW.get(vLabel); 
			return (labelVal * incomingWeightSumOf(v)) / outSum;
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
	  
	@Override
    public Object clone() {
		JVertex v1, v2;
        UniqueJVertexGraph res = new UniqueJVertexGraph();
        res.UniqueVertices = (HashMap<String, JVertex>) this.UniqueVertices.clone();
        
        for (Edge eCur: this.edgeSet()) {
            try {
				v1 = super.getEdgeSource(eCur);
				v2 = super.getEdgeTarget(eCur);

                res.addEdge(v1, v2, super.getEdgeWeight(eCur));
            } catch (Exception ex) {
                return null;
            }
		}
        return res;
    }
    
}
