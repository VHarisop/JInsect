package gr.demokritos.iit.jinsect.encoders;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.structs.Pair;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;

public abstract class BaseGraphEncoder {

	/**
	 * Creates a new BaseGraphEncoder object.
	 */
	public BaseGraphEncoder() {
	}

	/**
	 * Returns the outgoing edges of a JVertex in an unmodifiable
	 * list to provide read-only access.
	 *
	 * @param vSource the node whose edges are returned
	 * @return a list of edge objects
	 */
	public List<Edge> outgoingEdgeList(
		final UniqueVertexGraph uvg,
		final JVertex vSource) {
		return new ArrayList<>(uvg.outgoingEdgesOf(vSource));
	}

	/**
	 * Explores the outgoing edges of a vertex and returns a Pair
	 * containing two lists, one for backward edges (that point to
	 * an already visited vertex) and one for forward edges.
	 *
	 * @param vSource the vertex to explore edges from
	 * @return a Pair of edge lists (forward and backward respectively)
	 */
	public Pair<List<Edge>, List<Edge>> exploreEdges(
		final UniqueVertexGraph uvg, final JVertex vSource) {
		final List<Edge> eFwd = new ArrayList<>();
		final List<Edge> eBwd = new ArrayList<>();
		final Set<String> visited = new HashSet<>();
		/* add to backward edges if target vertex is
		 * already visited, else add to forward edges. */
		uvg.outgoingEdgesOf(vSource)
			.forEach(e -> {
				if (visited.contains(uvg.getEdgeTarget(e))) {
					eBwd.add(e);
				} else {
					eFwd.add(e);
				}
			});

		/* return a tuple containing both lists */
		return new Pair<>(eFwd, eBwd);
	}

	/**
	 * Visits a node by adding it to the set of visited vertices
	 * if it isn't already contained and removing it from the set
	 * of unvisited vertices.
	 *
	 * @param visited the set of visited vertices
	 * @param unvisited the set of unvisited vertices
	 * @param vVisit the vertex to visit
	 * @return a boolean - true if the vertex was not already visited,
	 * else false
	 */
	public boolean visitNode(
		final Set<JVertex> visited, final Set<JVertex> unvisited,
		final JVertex vVisit)
	{
		return (visited.add(vVisit) && unvisited.remove(vVisit));
	}

	// method to choose the starting vertex, if none was provided
	abstract protected JVertex chooseStart(final UniqueVertexGraph uvg);
}
