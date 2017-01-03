package gr.demokritos.iit.jinsect.comparators;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import gr.demokritos.iit.jinsect.representations.NGramGraph;
import gr.demokritos.iit.jinsect.representations.NGramJGraph;
import gr.demokritos.iit.jinsect.structs.Pair;
import gr.demokritos.iit.jinsect.structs.UniqueVertexGraph;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

public class SparseProjectionTest extends TestCase {

	protected Map<Character, Integer> charIndex;

	public SparseProjectionTest(final String name) {
		super(name);
	}

	public static Test suite() {
		return new TestSuite(SparseProjectionTest.class);
	}

	@Override
	public void setUp() {
		charIndex = new HashMap<>();
		charIndex.put('A', 0);
		charIndex.put('C', 1);
		charIndex.put('G', 2);
		charIndex.put('T', 3);
	}

	/**
	 * Make sure that adjacency vectors are extracted properly.
	 */
	public void testVectors() {
		final NGramGraph ngg = new NGramJGraph("ACTAGTTCGT");
		final UniqueVertexGraph uvg = ngg.getGraphLevel(0);
		final int words = (int) Math.pow(charIndex.size(), 3);
		final SparseProjectionComparator spc =
			new SparseProjectionComparator(
				charIndex, 3, 16,
				SparseProjectionComparator.Projection.SIGN_CONSISTENT);
		final List<Pair<Integer, Double>> adjVec =
			spc.generateAdjacencyVector(uvg);
		assertNotNull(adjVec);
		final double[] projVec = spc.getProjectedVector(uvg);
		assertNotNull(projVec);

		uvg.edgeSet().forEach(e -> {
			final int indexFrom = spc.getNGramIndex(e.getSourceLabel());
			final int indexTo = spc.getNGramIndex(e.getTargetLabel());
			final int totalIndex = indexFrom * words + indexTo;
			assertTrue(adjVec.stream()
				.filter(p -> p.getFirst() == totalIndex)
				.map(p -> p.getSecond())
				.filter(d -> d > 0.0)
				.count() > 0);
		});
	}

	/**
	 * Test if the distance and similarity between NGramGraphs
	 * are computed properly.
	 */
	public void testDistanceAndSimilarity() {
		final NGramGraph nggA = new NGramJGraph("ACTACTAGTCTGA");
		final NGramGraph nggB = new NGramJGraph("ACTACTAGTCTTA");
		final SparseProjectionComparator spc =
			new SparseProjectionComparator(
				charIndex, 3, 16,
				SparseProjectionComparator.Projection.SIGN_CONSISTENT);
		/* Distance */
		final double dist = spc.getDistance(
			nggA.getGraphLevel(0),
			nggB.getGraphLevel(0));
		assertNotNull(dist);
		assertTrue(dist >= 0.0);
		/* Similarity */
		final double sim = spc.getSimilarity(
			nggA.getGraphLevel(0),
			nggB.getGraphLevel(0));
		assertNotNull(sim);
		assertTrue(sim != Double.NaN);
		assertTrue(sim >= 0.0);
	}
}
