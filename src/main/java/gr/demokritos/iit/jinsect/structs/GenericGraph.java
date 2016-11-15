package gr.demokritos.iit.jinsect.structs;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/* use JGraphT for basic graph operations */
import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

/**
 * GenericGraph is a directed weighted graph with vertices that
 * may or may not be uniquely labeled.
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

	private final String label;

	/**
	 * Creates a new {@link GenericGraph} object.
	 */
	public GenericGraph() {
		super(Edge.class);
		label = "";
	}

	public GenericGraph(String label) {
		super(Edge.class);
		this.label = label;
	}

	/**
	 * Gets the label of this {@link GenericGraph}.
	 * @return the graph label
	 */
	public String getLabel() {
		return label;
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
	 * Given a {@link JVertex} whose name follows the convention
	 * <label>$<id>, splits it into a two-part String array.
	 * @param v the vertex whose label will be split
	 * @return a {@link String[]} containing the parts
	 */
	public static String[] getLabelParts(JVertex v) {
		return v.getLabel().split("\\$");
	}
	
	/**
	 * Get the set of all effective labels in this graph.
	 * @return a {@link Set} containing all the labels
	 */
	public Set<String> getEffectiveLabelSet() {
		return vertexSet().stream()
			.map(v -> extractLabel(v))
			.collect(Collectors.toSet());
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

	/**
	 * Queries the graph for an edge connecting two vertices
	 * designated by their labels. Returns <tt>null</tt> if no
	 * such edge is found.
	 * @param lblFrom the label of the source vertex
	 * @param lblTo the label of the target vertex
	 * @return the {@link Edge} found, if any, otherwise null
	 */
	public Edge getEdge(String lblFrom, String lblTo) {
		return super.getEdge(
				new NGramVertex(lblFrom),
				new NGramVertex(lblTo));
	}

	@Override
	public GenericGraph clone() {
		GenericGraph res = new GenericGraph();
		/* add all edges to the clone graph - all vertices will
		 * eventually be added both to the supergraph's vertex set
		 * and the hashmap, because of calls to the add() method */
		this.edgeSet().forEach(e ->
		res.addEdge(
				getEdgeSource(e),
				getEdgeTarget(e),
				getEdgeWeight(e))
				);
		return res;
	}

	/* An inner class that will be used for parsing
	 * by the JsonParser */
	public static class GraphTemplate {
		Map<String, String> nodes;
		Map<String, Map<String, Double>> edges;
		String id;

		private static final Type G_TYPE =
				new TypeToken<List<GraphTemplate>>(){}.getType();

		/**
		 * Reads a list of {@link GenericGraph.GraphTemplate} objects
		 * from a .json file. If the file does not exists, throws an
		 * appropriate exception
		 * @param file the file to read from
		 * @return a list of {@link GraphTemplate} objects
		 * @throws {@link FileNotFoundException}
		 */
		public static List<GraphTemplate> fromJsonFile(File file)
		throws FileNotFoundException
		{
			Gson gson = new Gson();
			List<GraphTemplate> parsed = gson.fromJson(
				new JsonReader(new FileReader(file)), G_TYPE);
			return parsed;
		}

		/**
		 * @see #fromJsonFile(File)
		 * @param fileName the name of the file
		 * @return a list of {@link GraphTemplate} objects
		 * @throws {@link FileNotFoundException}
		 */
		public static List<GraphTemplate> fromJsonFile(String fileName)
		throws FileNotFoundException {
			File f = new File(fileName);
			return fromJsonFile(f);
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("Id: " + id);
			sb.append(" Nodes: " + nodes.toString());
			sb.append(" Edges: " + edges.toString());
			return sb.toString();
		}

		/**
		 * Creates a {@link GenericGraph} object out of this
		 * {@link GraphTemplate}.
		 * @return a new generic graph object
		 */
		public GenericGraph toGenericGraph() {
			GenericGraph gGen = new GenericGraph(id);
			addNodes(gGen); addEdges(gGen);
			return gGen;
		}

		/**
		 * Add all the nodes to a {@link GenericGraph}.
		 * @param g the {@link GenericGraph} to populate
		 */
		private void addNodes(GenericGraph g) {
			nodes.forEach((id, label) -> g.addVertex(new NGramVertex(label)));
		}

		/**
		 * Add all the edges to a {@link GenericGraph}
		 * @param g the {@link GenericGraph} to add the edges to
		 */
		private void addEdges(GenericGraph g) {
			/* Iterate over all source ids */
			edges.forEach((sourceId, edgeMap) -> {
				final String sourceLabel = nodes.get(sourceId);
				final JVertex source = new NGramVertex(sourceLabel);
				/* Iterate over all targets of the source vertex */
				edges.get(sourceId).forEach((targetId, targetWeight) -> {
					final String targetLabel = nodes.get(targetId);
					final JVertex target = new NGramVertex(targetLabel);
					g.addEdge(source, target, targetWeight);
				});
			});
		}
	}

	/**
	 * Reads a list of {@link GenericGraph} objects which are contained
	 * in a .json file.
	 * @param file the file to read from
	 * @return the list of graphs
	 * @throws {@link FileNotFoundException} if no such file exists
	 */
	public static List<GenericGraph> fromJsonFile(final File file)
	throws FileNotFoundException {
		List<GraphTemplate> gTmps = GraphTemplate.fromJsonFile(file);
		/* Map every read GraphTemplate to a GenericGraph */
		return gTmps.stream()
			.map(g -> g.toGenericGraph())
			.collect(Collectors.toList());
	}

	/**
	 * @see #fromJsonFile(File)
	 * @param fileName the name of the file
	 * @return the list of graphs
	 * @throws {@link FileNotFoundException} if no such file exists
	 */
	public static List<GenericGraph> fromJsonFile(final String fileName)
	throws FileNotFoundException {
		File f = new File(fileName);
		return fromJsonFile(f);
	}
	/**
	 * Main method for quick GenericGraph test.
	 * @param args program arguments
	 * @throws FileNotFoundException
	 */
	public static void main(String[] args) throws FileNotFoundException {
		String fileName = args[0];
		// List<GenericGraph> parsed = GenericGraph.fromJsonFile(fileName);
		List<GraphTemplate> parsed = GraphTemplate.fromJsonFile(fileName);
		parsed.forEach(g -> {
			final GenericGraph gGen = g.toGenericGraph();
			UniqueVertexGraph uvg = gGen.compactToUniqueVertexGraph();
			System.out.println("Vertices: ");
			uvg.vertexSet().forEach(v -> {
				System.out.println("\t" + v.getLabel());
			});
			System.out.println("Edges: ");
			uvg.edgeSet().forEach(e -> {
				System.out.println("\t" + e.getLabels());
			});
		});
	}
}
