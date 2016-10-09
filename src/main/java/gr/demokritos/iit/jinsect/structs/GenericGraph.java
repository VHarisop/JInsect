package gr.demokritos.iit.jinsect.structs;

import java.util.*;

/* use JGraphT for basic graph operations */
import org.jgrapht.graph.*;

import gr.demokritos.iit.jinsect.structs.calculators.*;

/**
 * GenericGraph is a directed weighted graph with vertices that
 * may or may not be uniquelly labelled.
 * The vertex labels are supposed to follow the convention
 * <label>$<id>, where <label> is the actual label and <id> is the
 * identifier of the vertex in case multiple labels exist.
 *
 * @author VHarisop
 */
public final class GenericGraph
extends DefaultDirectedWeightedGraph<JVertex, Edge>
{
	static final long serialVersionUID = 1L;
	
	/**
	 * Creates a new {@link GenericGraph} object.
	 */
	public GenericGraph() {
		super(Edge.class);
	}

	/**
	 * Converts this graph to a {@link UniqueVertexGraph} by
	 * using the process of compacting - vertex labels are assumed
	 * to follow the convention "<label>$<id>", where <label> is the
	 * label to be used in the collapsing process.
	 *
	 * @return the collapsed {@link UniqueVertexGraph}
	 */
	public UniqueVertexGraph compactToUniqueVertexGraph() {
		UniqueVertexGraph res = new UniqueVertexGraph();
		/* add all vertices to the UniqueVertexGraph */
		this.vertexSet().forEach(v -> res.addVertex(
			new NGramVertex(extractLabel(v))));

		/* 
		 * Add all the edges to the UniqueVertexGraph. First,
		 * check if the edge exists, and if so, increase its
		 * total weight.
		 */
		for (Edge e: this.edgeSet()) {
			final JVertex from = new NGramVertex(
				extractLabel(getEdgeSource(e)));
			final JVertex to = new NGramVertex(
				extractLabel(getEdgeTarget(e)));
			final double w = e.edgeWeight();
			/* Get this edge from the UniqueVertexGraph */
			Edge eCont = res.getEdge(from, to);
			if (null == eCont) {
				/* if no such edge, create it now */
				res.addEdge(from, to, w);
			}
			else {
				/* Otherwise, increment its weight (collapse) */
				res.setEdgeWeight(eCont, eCont.edgeWeight() + w);
			}
		}
		return res;
	}

	/**
	 * Extracts the actual label of a vertex, whose effective label
	 * follows the convention mentioned in the documentation.
	 *
	 * @param v the {@link JVertex} whose label is required
	 * @return the label of the vertex
	 */
	private String extractLabel(JVertex v) {
		return v.getLabel().split("\\$")[0];
	}

	/**
	 * Adds a weighted edge from a source to a target vertex in the graph.
	 *
	 * @param source the source {@link JVertex}
	 * @param target the target {@link JVertex}
	 * @param weight the edge's weight
	 */
	public void addEdge(JVertex source, JVertex target, double weight) {
		// add vertices
		addVertex(source);
		addVertex(target);
		// add edge and set its weight
		Edge eAdded = super.addEdge(source, target);
		setEdgeWeight(eAdded, weight);
	}
    
	@Override
    public Object clone() {
        GenericGraph res = new GenericGraph();
		/* add all edges to the clone graph - all vertices will
		 * eventually be added both to the supergraph's vertex set
		 * and the hashmap, because of calls to the add() method */
        for (Edge eCur: this.edgeSet()) {
            try {
				final JVertex v1 = super.getEdgeSource(eCur);
				final JVertex v2 = super.getEdgeTarget(eCur);
				res.addEdge(v1, v2, super.getEdgeWeight(eCur));
            } catch (Exception ex) {
				ex.printStackTrace();
                return null;
            }
		}
        return res;
    }
    
}
