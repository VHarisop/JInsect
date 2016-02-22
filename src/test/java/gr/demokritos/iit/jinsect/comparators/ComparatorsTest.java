package gr.demokritos.iit.jinsect.comparators;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


import gr.demokritos.iit.jinsect.representations.NGramJGraph;
import gr.demokritos.iit.jinsect.structs.*;

/**
 * Unit test for the comparators/ submodule of jinsect.
 */
public class ComparatorsTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ComparatorsTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( ComparatorsTest.class );
    }

	/**
	 * Test the correctness of {@link NGramGraphComparator}
	 */
	public void testNGramGraphComparator() {
		NGramGraphComparator nggComp = new NGramGraphComparator();
		NGramJGraph nggA = new NGramJGraph("ACTAGC");
		NGramJGraph nggB = new NGramJGraph("ACTAGC");

		GraphSimilarity gSim = nggComp.getSimilarityBetween(nggA, nggB);
		assertEquals(1.0, gSim.SizeSimilarity, 0.001);
		assertEquals(1.0, gSim.ValueSimilarity, 0.001);
		assertEquals(1.0, gSim.ContainmentSimilarity, 0.001);
		assertEquals(1.0, gSim.getOverallSimilarity(), 0.0001);
		assertEquals(0.0, gSim.StructuralSimilarity, 0.00001);

		nggB.setDataString("AGTACG");
		gSim = nggComp.getSimilarityBetween(nggA, nggB);
		assertEquals(1.0, gSim.SizeSimilarity, 0.0001);
		assertTrue(gSim.ValueSimilarity < 1.0);
		assertTrue(gSim.ContainmentSimilarity < 1.0);
		assertEquals(0.0, gSim.StructuralSimilarity, 0.00001);
	}

	/**
	 * Test the correctness of the similarity comparator 
	 */
	public void testSimilarityComparator() {
		// verify that the similarity comparator works properly
		SimilarityComparator simComp = new SimilarityComparator();

		UniqueVertexGraph uvgA = new UniqueVertexGraph();
		UniqueVertexGraph uvgB = new UniqueVertexGraph();

		JVertex v1 = new NGramVertex("A");
		JVertex v2 = new NGramVertex("B");
		JVertex v3 = new NGramVertex("C");

		/* A: v1 ->[1.0] v2 [4.0]<- ->[2.0] v3 */
		uvgA.addEdge(v1, v2, 1.0);
		uvgA.addEdge(v2, v3, 2.0);
		uvgA.addEdge(v3, v2, 4.0);

		/* B: v2 ->[1.0] v3 [4.0]<- -> [2.0] v1 */
		uvgB.addEdge(v2, v3, 1.0);
		uvgB.addEdge(v3, v1, 2.0);
		uvgB.addEdge(v1, v3, 4.0);	
		
		/* the two graphs have the same structure */
		assertTrue(simComp.compare(uvgA, uvgB) == 0);
	}
}
