package gr.demokritos.iit.jinsect.structs.calculators;

import gr.demokritos.iit.jinsect.structs.*;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Unit test for simple App.
 */
public class DegreeRangeTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public DegreeRangeTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( DegreeRangeTest.class );
    }

	/**
	 * Utility function that adds a set of vertices, created from a set
	 * of set of strings, to a unique vertex graph.
	 */
	private void addVertices(UniqueJVertexGraph uvg, String ... labels) {
		for (String s: labels) {
			uvg.add(new NGramVertex(s));
		}
	}

	/**
	 * Utility function that adds an edge with an associated weight to a graph.
	 */
	private void addEdge(UniqueJVertexGraph uvg, 
			String from, String to, double w) {
		uvg.addEdge(new NGramVertex(from), new NGramVertex(to), w);
	}
	
	/**
	 * Makes sure that the degree ranges are calculated correctly
	 * for all vertices.
	 */
	public void testDegrees() {
		UniqueJVertexGraph uvg = new UniqueJVertexGraph();
		addVertices(uvg, "A", "B", "C");
		addEdge(uvg, "A", "B", 2.0);
		addEdge(uvg, "A", "C", 4.0);
		addEdge(uvg, "B", "A", 1.0);
		addEdge(uvg, "C", "B", 3.0);

		// create a new calculator on this object
		DegreeRangeCalculator drc = new DegreeRangeCalculator(uvg);
		double[] degs;

		// degree ranges of A 
		degs = drc.getDegrees("A");
		assertEquals(0.0, degs[0], 0.00001);
		assertEquals(2.0, degs[1], 0.00001);

		// degree ranges of B
		degs = drc.getDegrees("B");
		assertEquals(1.0, degs[0], 0.00001);
		assertEquals(0.0, degs[1], 0.00001);
	}
}
