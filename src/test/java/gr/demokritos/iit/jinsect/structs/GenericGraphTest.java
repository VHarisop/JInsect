package gr.demokritos.iit.jinsect.structs;

import java.io.File;
import java.io.FileNotFoundException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import gr.demokritos.iit.jinsect.JUtils;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

/**
 * Unit test for {@link GenericGraph} and its conversion via collapsing
 * to {@link UniqueVertexGraph}.
 */
public class GenericGraphTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public GenericGraphTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( GenericGraphTest.class );
    }

	/* Helper function to add an undefined number of vertices
	 * to a GenericGraph, given their desired labels. */
	private void addVertices(GenericGraph gGraph, String ... labels) {
		for (String s: labels) {
			gGraph.addVertex(new NGramVertex(s));
		}
	}

	/* Helper function to determine if a graph contains a vertex
	 * that matches a given label */
	private boolean hasVertex(GenericGraph gGraph, String label) {
		return gGraph.containsVertex(new NGramVertex(label));
	}

	/* Helper function that returns true if a graph contains an edge
	 * connecting two vertices designated by their labels as well as
	 * having a certain specified weight. */
	private boolean
	hasEdge(GenericGraph gGraph, String from, String to, double w) {
		JVertex vFrom = new NGramVertex(from);
		JVertex vTo = new NGramVertex(to);
		Edge eFound = gGraph.getEdge(vFrom, vTo);
		if (null == eFound) {
			return false;
		}
		else {
			if (eqDouble(gGraph.getEdgeWeight(eFound), w)) {
				return true;
			}
			else {
				return false;
			}
		}
	}


	/* Helper function that adds an edge to a graph, given the labels
	 * of its source and target vertices and its desired weight. */
	private void
	putEdge(GenericGraph gGraph, String from, String to, double w) {
		JVertex vFrom = new NGramVertex(from);
		JVertex vTo = new NGramVertex(to);
		gGraph.addEdge(vFrom, vTo, w);
	}

	/**
	 * Makes sure that creation works properly for a
	 * {@link GenericGraph} object.
	 */
	public void testCreation() {
		GenericGraph gGraph = new GenericGraph();
		addVertices(gGraph, "C$1", "C$2", "O$3", "C$4");
		// make sure every vertex was added
		assertTrue(hasVertex(gGraph, "C$1"));
		assertTrue(hasVertex(gGraph, "C$2"));
		assertTrue(hasVertex(gGraph, "O$3"));
		assertTrue(hasVertex(gGraph, "C$4"));
		// make sure they are the only vertices added
		assertTrue(gGraph.vertexSet().size() == 4);
		// add a set of edges
		putEdge(gGraph, "C$1", "C$4", 1.0);
		putEdge(gGraph, "C$1", "C$2", 2.5);
		putEdge(gGraph, "O$3", "C$2", 0.5);
		putEdge(gGraph, "C$2", "C$4", 1.5);
		// make sure they exist
		assertTrue(hasEdge(gGraph, "C$1", "C$4", 1.0));
		assertTrue(hasEdge(gGraph, "C$1", "C$2", 2.5));
		assertTrue(hasEdge(gGraph, "O$3", "C$2", 0.5));
		assertTrue(hasEdge(gGraph, "C$2", "C$4", 1.5));
		// make sure they are the only ones
		assertTrue(gGraph.edgeSet().size() == 4);
	}

	/**
	 * Test the process of compacting a {@link GenericGraph} to a
	 * {@link UniqueVertexGraph}.
	 */
	public void testCompacting() {
		GenericGraph gGraph = new GenericGraph();
		addVertices(gGraph,
			"a$1", "b$2", "b$3", "a$4", "c$5");
		putEdge(gGraph, "a$1", "b$2", 3.0);
		putEdge(gGraph, "a$1", "b$3", 2.0);
		putEdge(gGraph, "b$2", "b$3", 4.0);
		putEdge(gGraph, "a$2", "c$5", 2.0);
		putEdge(gGraph, "c$5", "b$3", 1.0);
		// apply compacting
		UniqueVertexGraph uvg = gGraph.compactToUniqueVertexGraph();
		// make sure vertices are there
		JVertex vA = new NGramVertex("a");
		JVertex vB = new NGramVertex("b");
		JVertex vC = new NGramVertex("c");
		assertTrue(uvg.containsVertex(vA));
		assertTrue(uvg.containsVertex(vB));
		assertTrue(uvg.containsVertex(vC));
		assertTrue(uvg.vertexSet().size() == 3);
		// make sure edges are there
		final Edge e1 = uvg.getEdge(vA, vB);
		assertNotNull(e1);
		assertTrue(eqDouble(e1.edgeWeight(), 5.0));
		final Edge e2 = uvg.getEdge(vB, vB);
		assertNotNull(e2);
		assertTrue(eqDouble(e2.edgeWeight(), 4.0));
		final Edge e3 = uvg.getEdge(vC, vB);
		assertNotNull(e3);
		assertTrue(eqDouble(e3.edgeWeight(), 1.0));
		final Edge e4 = uvg.getEdge(vA, vC);
		assertNotNull(e4);
		assertTrue(eqDouble(e4.edgeWeight(), 2.0));
		// make sure they are the only edges there
		assertTrue(uvg.edgeSet().size() == 4);
	}

	/**
	 * Test that the list of generic graphs in the file small.json
	 * is read properly.
	 */
	public void testGsonInput() {
		final String fileName = "/small.json";
		try {
			assertNotNull("Missing file!", getClass().getResource(fileName));
			URI resource = getClass().getResource(fileName).toURI();
			File fJson = new File(resource);
			List<GenericGraph> graphs = GenericGraph.fromJsonFile(fJson);
			assertNotNull(graphs);
			assertEquals(3, graphs.size());
		}
		catch (URISyntaxException ex) {
			/* Print exception and fail instantly */
			ex.printStackTrace();
			assertTrue(false);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}
	
	public void testMutations() {
		List<GenericGraph> graphs = getGraphList();
		Set<String> availLabels = new HashSet<>();
		graphs.forEach(g -> {
			availLabels.addAll(g.getEffectiveLabelSet());
		});
		/* Create a mutation for every graph */
		graphs.forEach(g -> {
			GenericGraph gNew = JUtils.getEditedGraph(
				g, 3, availLabels.stream().collect(Collectors.toList()));
			assertNotNull(gNew);
		});
	}
	
	/* Get the list of generic graphs from <small.json> */
	private List<GenericGraph> getGraphList() {
		final String fileName = "/small.json";
		try {
			URI resource = getClass().getResource(fileName).toURI();
			File jsonFile = new File(resource);
			List<GenericGraph> graphs = GenericGraph.fromJsonFile(jsonFile);
			return graphs;
		}
		catch (Exception ex) {
			return null;
		}
	}

	/* Helper function to check doubles for equality */
	private static boolean eqDouble(double a, double b) {
		return (Math.abs(a - b) < 0.000001);
	}
}
