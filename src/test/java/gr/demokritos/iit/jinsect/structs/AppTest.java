package gr.demokritos.iit.jinsect.structs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.Math;

import gr.demokritos.iit.jinsect.representations.NGramJGraph;
import gr.demokritos.iit.jinsect.jutils;


/**
 * Unit test for simple App.
 */
public class AppTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public AppTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( AppTest.class );
    }

	/* Helper function to check doubles for equality */
	private static boolean eqDouble(double a, double b) {
		return (Math.abs(a - b) < 0.000001);
	}

	/**
	 * Test if equality checking has been properly implemented
	 * for NGramVertex objects.
	 */
	public void testNGramVertex() {
		JVertex v1 = new NGramVertex("ACT");
		JVertex v2 = new NGramVertex("AGT");
		JVertex v3 = new NGramVertex("ACT");

		/* check equality */
		assertTrue(v1.equals(v3));
		assertFalse(v2.equals(v3));
		assertFalse(v1.equals(v2));

		/* equal objects should have equal hash codes */
		assertTrue(v1.hashCode() == v3.hashCode());
	}

	/**
	 * Helper function for testNGramJGraph that retrieves an edge based
	 * on the labels of the vertices it connects.
	 */
	private Edge edgeByLabel(UniqueVertexGraph uvg, String from, String to) {
		JVertex vFrom = new NGramVertex(from), vTo = new NGramVertex(to);
		return uvg.getEdge(vFrom, vTo);
	}

	public void testNGramJGraph()
	{
		NGramJGraph dng = new NGramJGraph();
		dng.setDataString("ACTACTA");

		/* get the underlying graph */
		UniqueVertexGraph uvg = dng.getGraphLevel(0);
		
		Edge e; double delta = 0.000001;
		
		e = edgeByLabel(uvg, "CTA", "ACT");
		assertEquals(e.edgeWeight(), 2.0, delta);
		e = edgeByLabel(uvg, "CTA", "TAC");
		assertEquals(e.edgeWeight(), 1.0, delta);
		e = edgeByLabel(uvg, "CTA", "CTA");
		assertEquals(e.edgeWeight(), 1.0, delta);
		e = edgeByLabel(uvg, "TAC", "CTA");
		assertEquals(e.edgeWeight(), 1.0, delta);
		e = edgeByLabel(uvg, "TAC", "ACT");
		assertEquals(e.edgeWeight(), 1.0, delta);
		e = edgeByLabel(uvg, "ACT", "TAC");
		assertEquals(e.edgeWeight(), 1.0, delta);
		e = edgeByLabel(uvg, "ACT", "ACT");
		assertEquals(e.edgeWeight(), 1.0, delta);
		e = edgeByLabel(uvg, "ACT", "CTA");
		assertEquals(e.edgeWeight(), 1.0, delta);

		NGramJGraph d = new NGramJGraph("ACTT");
		dng.setDataString("ACTA");
		dng.mergeGraph(d, 0);

		/* make sure that merging two graphs works properly */
		e = edgeByLabel(dng.getGraphLevel(0), "CTA", "ACT");
		assertEquals(e.edgeWeight(), 1.0, delta);
		e = edgeByLabel(dng.getGraphLevel(0), "CTT", "ACT");
		assertEquals(e.edgeWeight(), 1.0, delta);
	}

	public void testUniqueJGraph()
	{
		/* create a new UVJG graph */
		UniqueVertexGraph uvg = new UniqueVertexGraph();
		JVertex v1 = new NGramVertex("A");
		JVertex v2 = new NGramVertex("C");
		JVertex v3 = new NGramVertex("G");

		/* add vertices to the graph */
		uvg.addVertex(v1);
		uvg.addVertex(v2);
		uvg.addVertex(v3);

		/* add some edges */
		uvg.addEdge(v1, v2, 2.0);
		uvg.addEdge(v3, v2, 2.0);
		uvg.addEdge(v2, v3, 1.0);

		/* assert the validity of Edge.getSourceLabel()
		 * and Edge.getTargetLabel() methods */
		Edge e12 = uvg.getEdge(v1, v2);
		Edge e23 = uvg.getEdge(v2, v3);

		assertTrue(e12.getSourceLabel().equals("A"));
		assertTrue(e23.getSourceLabel().equals("C"));
		assertTrue(e23.getTargetLabel().equals("G"));

		/* assert that the outgoing and incoming weight sums
		 * are computed properly for all vertices */
		assertTrue(eqDouble(uvg.outgoingWeightSumOf(v1), 2.0));
		assertTrue(eqDouble(uvg.incomingWeightSumOf(v1), 0.0));
		assertTrue(eqDouble(uvg.outgoingWeightSumOf(v2), 1.0));
		assertTrue(eqDouble(uvg.incomingWeightSumOf(v2), 4.0));
		assertTrue(eqDouble(uvg.outgoingWeightSumOf(v3), 2.0));
		assertTrue(eqDouble(uvg.incomingWeightSumOf(v3), 1.0));
	}
}
