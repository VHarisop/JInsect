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

		nggB.setDataString("AGTACG");
		gSim = nggComp.getSimilarityBetween(nggA, nggB);
		assertEquals(1.0, gSim.SizeSimilarity, 0.0001);
		assertTrue(gSim.ValueSimilarity < 1.0);
		assertTrue(gSim.ContainmentSimilarity < 1.0);
	}
}
