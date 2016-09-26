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
public final class UniqueVertexGraph
extends DefaultDirectedWeightedGraph<JVertex, Edge>
{
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
		super.outgoingEdgesOf(vFrom).stream()
			.forEach(e -> {
				adjacentVertices.add(super.getEdgeTarget(e));
			});

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
		return eList.stream()
			.mapToDouble(e -> super.getEdgeWeight(e))
			.sum();
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
