package gr.demokritos.iit.jinsect.structs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import gr.demokritos.iit.jinsect.jutils;
import gr.demokritos.iit.jinsect.encoders.VertexCoder;

import java.lang.Math;

/**
 * Unit test for simple App.
 */
public class SimilarityTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public SimilarityTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( SimilarityTest.class );
    }

	/**
	 * Makes sure that the auto-update of the {@link VertexCoder}'s 
	 * running weight works properly.
	 */
	public void testKeys() {
		VertexCoder vCoder = new VertexCoder()
			.withWeightValue(0.5).withStep(0.01);

		putLabels(vCoder, "AA", "AB", "BA");
	
		assertTrue(eqDouble(vCoder.get("AA"), 0.5));
		assertTrue(eqDouble(vCoder.get("BA"), 0.52));
		assertTrue(eqDouble(vCoder.get("AB"), 0.51));
	}

	/**
	 * Helper function to add many vertices at once, given their labels
	 */
	private void addVertices(UniqueVertexGraph uvg, String ... labels) {
		for (String s: labels) {
			uvg.addVertex(new NGramVertex(s));
		}
	}

	/**
	 * Helper function to add many labels at once to a VertexCoder object
	 */
	private void putLabels(VertexCoder vC, String ... labels) {
		for (String s: labels) {
			vC.putLabel(s);
		}
	}


	public void testGraphs() {
		VertexCoder vCoder = new VertexCoder()
			.withWeightValue(0.5).withStep(0.05);
		UniqueVertexGraph uvgA = new UniqueVertexGraph();
		UniqueVertexGraph uvgB = new UniqueVertexGraph();

		/* populate them with vertices */
		addVertices(uvgA, "A", "B", "C");
		addVertices(uvgB, "A", "B", "D");

		putLabels(vCoder, "A", "B", "C", "D");	
		assertTrue(eqDouble(vCoder.get("A"), 0.5));
		assertTrue(eqDouble(vCoder.get("B"), 0.55));
		assertTrue(eqDouble(vCoder.get("C"), 0.60));
		assertTrue(eqDouble(vCoder.get("D"), 0.65));
	}

	/* Helper function to check doubles for equality */
	private static boolean eqDouble(double a, double b) {
		return (Math.abs(a - b) < 0.000001);
	}
}
