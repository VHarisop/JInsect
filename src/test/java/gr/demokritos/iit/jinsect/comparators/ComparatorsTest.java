package gr.demokritos.iit.jinsect.comparators;

import gr.demokritos.iit.jinsect.representations.NGramGraph;
import gr.demokritos.iit.jinsect.representations.NGramJGraph;
import gr.demokritos.iit.jinsect.structs.Edge;
import gr.demokritos.iit.jinsect.structs.GraphSimilarity;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

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
    public ComparatorsTest( final String testName )
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
		final NGramGraphComparator nggComp = new NGramGraphComparator();
		final NGramJGraph nggA = new NGramJGraph("ACTAGC");
		final NGramJGraph nggB = new NGramJGraph("ACTAGC");

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

	/**
	 * Test GraphSimilarity for graphs of multiple levels.
	 */
	public void testSimilarity() {
		final NGramGraphComparator nggComp = new NGramGraphComparator();
		final NGramGraph nggA =
			new NGramJGraph("hello hello", 2, 3, 3);
		final NGramGraph nggB =
			new NGramJGraph("hello hey", 2, 3, 3);
		/* get similarity between the two graphs */
		final GraphSimilarity gSim = nggComp.getSimilarityBetween(nggA, nggB);
		/* get separate edge counts */
		final int bigramEdgeCountA = nggA.getGraphLevel(0).edgeSet().size();
		final int bigramEdgeCountB = nggB.getGraphLevel(0).edgeSet().size();
		final int trigramEdgeCountA = nggA.getGraphLevel(1).edgeSet().size();
		final int trigramEdgeCountB = nggB.getGraphLevel(1).edgeSet().size();
		/* manually calculate size similarity */
		final int bigramMinEdges = Math.min(bigramEdgeCountA, bigramEdgeCountB);
		final int bigramMaxEdges = Math.max(bigramEdgeCountA, bigramEdgeCountB);
		final double bigramSizeSim =
			bigramMinEdges / Math.max(bigramMaxEdges, 1.0);
		final int trigramMinEdges = Math.min(trigramEdgeCountA, trigramEdgeCountB);
		final int trigramMaxEdges = Math.max(trigramEdgeCountA, trigramEdgeCountB);
		final double trigramSizeSim =
			trigramMinEdges / Math.max(trigramMaxEdges, 1.0);

		/* 2 is level 0 importance, 2 + 3 = 5 is level 1 importance,
		 * and finally 2 + 5 = 7 is overall importance */
		final double sizeSim = (2 * bigramSizeSim / 7) + (5 * trigramSizeSim / 7);
		assertEquals(sizeSim, gSim.SizeSimilarity, 0.0001);

		/* Compare containment similarity.
		 * Here, degradation degree is 0.
		 * Common edges were found by enumerating all edges
		 * of the two edge sets. */
		final int commonBigramEdges = 15;
		final int commonTrigramEdges = 12;
		final double bigramContSim = commonBigramEdges / (1.0 * bigramMinEdges);
		final double trigramContSim = commonTrigramEdges / (1.0 * trigramMinEdges);
		final double contSim = (2 * bigramContSim / 7) + (5 * trigramContSim / 7);
		assertEquals(contSim, gSim.ContainmentSimilarity, 0.0001);

		/* Compare value similarity */
		double bigramValueSim = 0.0;
		final UniqueVertexGraph bigramsA = nggA.getGraphLevel(0);
		final UniqueVertexGraph bigramsB = nggB.getGraphLevel(0);
		for (final Edge e: bigramsA.edgeSet()) {
			final Edge eFound = bigramsB.getEdge(
				bigramsA.getEdgeSource(e),
				bigramsA.getEdgeTarget(e));
			if (eFound == null) {
				continue;
			}
			final double wa = e.edgeWeight();
			final double wb = eFound.edgeWeight();
			bigramValueSim += Math.min(wa, wb) / Math.max(wa, wb);
		}
		bigramValueSim /= Math.max(bigramMaxEdges, 1.0);

		double trigramValueSim = 0.0;
		final UniqueVertexGraph trigramsA = nggA.getGraphLevel(1);
		final UniqueVertexGraph trigramsB = nggB.getGraphLevel(1);
		for (final Edge e: trigramsA.edgeSet()) {
			final Edge eFound = trigramsB.getEdge(
					trigramsA.getEdgeSource(e),
					trigramsA.getEdgeTarget(e));
			if (eFound == null) {
				continue;
			}
			final double wa = e.edgeWeight();
			final double wb = eFound.edgeWeight();
			trigramValueSim += Math.min(wa, wb) / Math.max(wa, wb);
		}
		trigramValueSim /= Math.max(trigramMaxEdges, 1.0);
		final double valueSim = 2 * bigramValueSim / 7 + 5 * trigramValueSim / 7;
		assertEquals(valueSim, gSim.ValueSimilarity, 0.0001);
	}
}
