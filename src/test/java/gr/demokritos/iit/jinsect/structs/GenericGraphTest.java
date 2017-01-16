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
    public GenericGraphTest( final String testName )
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
	private void addVertices(
		final GenericGraph gGraph, final String ... labels) {
		for (final String s: labels) {
			gGraph.addVertex(new NGramVertex(s));
		}
	}

	/* Helper function to determine if a graph contains a vertex
	 * that matches a given label */
	private boolean hasVertex(final GenericGraph gGraph, final String label) {
		return gGraph.containsVertex(new NGramVertex(label));
	}

	/* Helper function that returns true if a graph contains an edge
	 * connecting two vertices designated by their labels as well as
	 * having a certain specified weight. */
	private boolean hasEdge(
		final GenericGraph gGraph,
		final String from, final String to, final double w) {
		final JVertex vFrom = new NGramVertex(from);
		final JVertex vTo = new NGramVertex(to);
		final Edge eFound = gGraph.getEdge(vFrom, vTo);
		if (null == eFound) {
			return false;
		}
		else {
			if (JUtils.eqDouble(gGraph.getEdgeWeight(eFound), w)) {
				return true;
			}
			else {
				return false;
			}
		}
	}


	/* Helper function that adds an edge to a graph, given the labels
	 * of its source and target vertices and its desired weight. */
	private void putEdge(
		final GenericGraph gGraph,
		final String from, final String to, final double w) {
		final JVertex vFrom = new NGramVertex(from);
		final JVertex vTo = new NGramVertex(to);
		gGraph.addEdge(vFrom, vTo, w);
	}

	/**
	 * Makes sure that creation works properly for a
	 * {@link GenericGraph} object.
	 */
	public void testCreation() {
		final GenericGraph gGraph = new GenericGraph();
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
		final GenericGraph gGraph = new GenericGraph();
		addVertices(gGraph,
			"a$1", "b$2", "b$3", "a$4", "c$5");
		putEdge(gGraph, "a$1", "b$2", 3.0);
		putEdge(gGraph, "a$1", "b$3", 2.0);
		putEdge(gGraph, "b$2", "b$3", 4.0);
		putEdge(gGraph, "a$2", "c$5", 2.0);
		putEdge(gGraph, "c$5", "b$3", 1.0);
		// apply compacting
		final UniqueVertexGraph uvg = gGraph.compactToUniqueVertexGraph();
		// make sure vertices are there
		final JVertex vA = new NGramVertex("a");
		final JVertex vB = new NGramVertex("b");
		final JVertex vC = new NGramVertex("c");
		assertTrue(uvg.containsVertex(vA));
		assertTrue(uvg.containsVertex(vB));
		assertTrue(uvg.containsVertex(vC));
		assertTrue(uvg.vertexSet().size() == 3);
		// make sure edges are there
		final Edge e1 = uvg.getEdge(vA, vB);
		assertNotNull(e1);
		assertEquals(e1.edgeWeight(), 5.0, 1e-6);
		final Edge e2 = uvg.getEdge(vB, vB);
		assertNotNull(e2);
		assertEquals(e2.edgeWeight(), 4.0, 1e-6);
		final Edge e3 = uvg.getEdge(vC, vB);
		assertNotNull(e3);
		assertEquals(e3.edgeWeight(), 1.0, 1e-6);
		final Edge e4 = uvg.getEdge(vA, vC);
		assertNotNull(e4);
		assertEquals(e4.edgeWeight(), 2.0, 1e-6);
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
			final URI resource = getClass().getResource(fileName).toURI();
			final File fJson = new File(resource);
			final List<GenericGraph> graphs = GenericGraph.fromJsonFile(fJson);
			assertNotNull(graphs);
			assertEquals(3, graphs.size());
		}
		catch (final URISyntaxException ex) {
			/* Print exception and fail instantly */
			ex.printStackTrace();
			assertTrue(false);
		} catch (final FileNotFoundException e) {
			e.printStackTrace();
			assertTrue(false);
		}
	}

	public void testMutations() {
		final List<GenericGraph> graphs = getGraphList();
		final Set<String> availLabels = new HashSet<>();
		graphs.forEach(g -> {
			availLabels.addAll(g.getEffectiveLabelSet());
		});
		/* Create a mutation for every graph */
		graphs.forEach(g -> {
			final Pair<GenericGraph, Integer> gNew = JUtils.getEditedGraph(
				g, 5, availLabels.stream().collect(Collectors.toList()));
			assertNotNull(gNew);
			assertNotNull(gNew.getFirst());
			assertTrue(gNew.getSecond() >= 0);
		});
	}

	/* Get the list of generic graphs from <small.json> */
	private List<GenericGraph> getGraphList() {
		final String fileName = "/small.json";
		try {
			final URI resource = getClass().getResource(fileName).toURI();
			final File jsonFile = new File(resource);
			final List<GenericGraph> graphs = GenericGraph.fromJsonFile(jsonFile);
			return graphs;
		}
		catch (final Exception ex) {
			return null;
		}
	}
}
