package gr.demokritos.iit.jinsect.encoders;

import java.util.HashMap;
import java.util.Map;

import gr.demokritos.iit.jinsect.JUtils;
import gr.demokritos.iit.jinsect.representations.NGramGraph;
import gr.demokritos.iit.jinsect.representations.NGramJGraph;
import gr.demokritos.iit.jinsect.structs.NGramVertex;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;


/**
 * Unit test for simple App.
 */
public class EncodersTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public EncodersTest( final String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( EncodersTest.class );
    }

	/**
	 * Test is DFS encoding works properly
	 */
	public void testDfs() {
		NGramJGraph ngg = new NGramJGraph("ACTAG");
		DepthFirstEncoder dfsE = new DepthFirstEncoder();
		assertTrue(dfsE.getEncoding(
			ngg.getGraphLevel(0),
			new NGramVertex("TAG")).equals("TAG->ACT|TAG->CTA|CTA->ACT|"));
		assertTrue(dfsE.getEncoding(
			ngg.getGraphLevel(0)).equals("CTA->ACT|TAG->ACT|TAG->CTA|"));
		assertTrue(dfsE.getEncoding(
			ngg.getGraphLevel(0),
			new NGramVertex("CTA")).equals("CTA->ACT|TAG->ACT|TAG->CTA|"));

	}

	/**
	 * Test if encoding using adjacency vectors works properly.
	 */
	public void testAdjacencyEncoding() {
		NGramGraph nggA = new NGramJGraph("ACTAGTAG");
		NGramGraph nggB = new NGramJGraph("ACTAGTA");
		NGramGraph nggC = new NGramJGraph("AGTAGTA");
		NGramGraph nggD = new NGramJGraph("AGTAGTAG");
		Map<Character, Integer> charIndex = new HashMap<>();
		charIndex.put('A', 0);
		charIndex.put('C', 1);
		charIndex.put('G', 2);
		charIndex.put('T', 3);
		AdjacencyEncoder adjE = new AdjacencyEncoder(charIndex, 3);
		final String encA = adjE.getEncoding(nggA.getGraphLevel(0));
		final String encB = adjE.getEncoding(nggB.getGraphLevel(0));
		assertFalse(encA.equals(encB));
		final String encC = adjE.getEncoding(nggC.getGraphLevel(0));
		final String encD = adjE.getEncoding(nggD.getGraphLevel(0));
		assertFalse(encC.equals(encD));
		// Also test the packed encodings
		final String packEncA = adjE.getPackedEncoding(nggA.getGraphLevel(0));
		final String packEncB = adjE.getPackedEncoding(nggB.getGraphLevel(0));
		assertFalse(packEncA.equals(packEncB));
		final String packEncC = adjE.getPackedEncoding(nggC.getGraphLevel(0));
		final String packEncD = adjE.getPackedEncoding(nggD.getGraphLevel(0));
		assertFalse(packEncC.equals(packEncD));
	}

	/*
	 * Test if Canonical Coding works properly.
	 */
	public void testCanonical() {
		NGramJGraph nggA = new NGramJGraph("ATACA");
		NGramJGraph nggB = new NGramJGraph("AATAC");
		// new graph, equal to nggA
		NGramJGraph nggC = new NGramJGraph("ATACA");

		CanonicalCoder cCoder = new CanonicalCoder();
		final String encA = cCoder.getEncoding(nggA.getGraphLevel(0));
		final String encB = cCoder.getEncoding(nggB.getGraphLevel(0));
		final String encC = cCoder.getEncoding(nggC.getGraphLevel(0));

		// make sure that the graphs have different canonical codes
		assertFalse(encA.equals(encB));
		// assert that AAT is lexicographically smaller than ACA
		assertTrue(JUtils.compareCanonicalCodes(nggA.getGraphLevel(0),
												nggB.getGraphLevel(0)) > 0);
		// assert that two identical graphs give indentical canonical codes
		assertTrue(encA.equals(encC));
	}
}
