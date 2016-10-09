package gr.demokritos.iit.jinsect.structs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.Math;

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
	 * to a GenericGraph, given their wanted labels. */
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

	/* Helper function to check doubles for equality */
	private static boolean eqDouble(double a, double b) {
		return (Math.abs(a - b) < 0.000001);
	}
}
