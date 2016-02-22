package gr.demokritos.iit.jinsect.encoders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.List;

import gr.demokritos.iit.jinsect.structs.*;

public abstract class BaseGraphEncoder {

	UniqueVertexGraph nGraph;
	JVertex vStart = null;

	// sets of visited / unvisited vertices
	Set<JVertex> visited = new HashSet<JVertex>();
	Set<JVertex> unvisited = new HashSet<JVertex>();


	/**
	 * Initializes the visited / unvisited sets. Creates 2 empty
	 * {@link java.util.HashSet} instances, one for visited and 
	 * one for unvisited nodes.
	 *
	 * This method can be overriden in subclasses that require
	 * different set types.
	 */
	protected void initSets() {
		unvisited = new HashSet<JVertex>();
		visited = new HashSet<JVertex>();
	}

	/**
	 * Creates a new BaseGraphEncoder object to operate on
	 * a given UniqueJVertexGraph.
	 *
	 * @param uvg the graph to be encoded
	 * @return a new BaseGraphEncoder object
	 */
	public BaseGraphEncoder(UniqueVertexGraph uvg) {
		nGraph = uvg;

		// initialize sets
		initSets();

		unvisited.addAll(uvg.vertexSet());
	}

	/**
	 * Creates a new BaseGraphEncoder object to operate on
	 * a given UniqueJVertexGraph starting on a given JVertex.
	 *
	 * @param uvg the graph to be encoded
	 * @param vFrom the starting vertex
	 * @return a new BaseGraphEncoder object
	 */
	public BaseGraphEncoder(UniqueVertexGraph uvg, JVertex vFrom) {
		nGraph = uvg;

		// initialize sets
		initSets();

		unvisited.addAll(uvg.vertexSet());
		vStart = vFrom;
	}

	/**
	 * Returns the outgoing edges of a JVertex in an unmodifiable
	 * list to provide read-only access.
	 *
	 * @param vSource the node whose edges are returned
	 * @return a list of edge objects
	 */
	public List<Edge> outgoingEdgeList(JVertex vSource) {
		List<Edge> eList = new ArrayList<Edge>();

		for (Edge e: nGraph.outgoingEdgesOf(vSource)) {
			eList.add(e);
		}

		return eList;
	}

	/**
	 * Explores the outgoing edges of a vertex and returns a Pair
	 * containing two lists, one for backward edges (that point to
	 * an already visited vertex) and one for forward edges. 
	 *
	 * @param vSource the vertex to explore edges from
	 * @return a Pair of edge lists (forward and backward respectively)
	 */
	public Pair<List<Edge>, List<Edge>> exploreEdges(JVertex vSource) {
		List<Edge> eFwd = new ArrayList<Edge>();
		List<Edge> eBwd = new ArrayList<Edge>(); 
		
		/* add to backward edges if target vertex is 
		 * already visited, else add to forward edges. */
		for (Edge e: nGraph.outgoingEdgesOf(vSource)) {
			if (visited.contains(nGraph.getEdgeTarget(e))) {
				eBwd.add(e);
			}
			else {
				eFwd.add(e);
			}
		}

		/* return a tuple containing both lists */
		return new Pair<List<Edge>, List<Edge>>(eFwd, eBwd);
	}

	/**
	 * Visits a node by adding it to the set of visited vertices 
	 * if it isn't already contained and removing it from the set
	 * of unvisited vertices.
	 *
	 * @param vVisit the vertex to visit
	 * @return a boolean - true if the vertex was not already visited, 
	 * else false
	 */
	public boolean visitNode(JVertex vVisit) {
		return (visited.add(vVisit) && unvisited.remove(vVisit));
	}

	// method to choose the starting vertex, if none was provided
	abstract protected JVertex chooseStart();
}
