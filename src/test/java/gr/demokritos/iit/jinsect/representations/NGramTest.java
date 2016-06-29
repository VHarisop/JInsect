package gr.demokritos.iit.jinsect.representations;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;

import java.util.*;
import gr.demokritos.iit.jinsect.structs.*;

/**
 * Unit test for simple App.
 */
public class NGramTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public NGramTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( NGramTest.class );
    }

	/**
	 * Test if the static methods that call the
	 * {@link gr.demokritos.iit.jinsect.io.LineReader} class actually work.
	 */
	public void testLines() {
		String fPath = "/testFile01.txt";
		assertNotNull("Test file missing", getClass().getResource(fPath));

		try {
			File res = new File(getClass().getResource(fPath).toURI());
			NGramJGraph[] ngrams = NGramJGraph.fromFileLines(res);
			assertNotNull(ngrams);
			assertTrue(ngrams.length == 10);
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false); // fail
		}
	}
	
	/**
	 * Make sure the array version of {@link GraphUtils}'s methods does not
	 * return nulls.
	 */
	public void testUtils() {
		String fPath = "/testFile01.txt";
		assertNotNull("Test file missing", getClass().getResource(fPath));
		try {
			File res = new File(getClass().getResource(fPath).toURI());
			NGramJGraph[] ngrams = NGramJGraph.fromFileLines(res);
			assertNotNull(ngrams);
			assertTrue(GraphUtils.removeNoise(ngrams).length == 10);
			assertNotNull(GraphUtils.mergeGraphs(ngrams));
		}
		catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false);
		}
	}

	/**
	 * Verify that graph construction works properly for
	 * multilevel graphs.
	 */
	public void testNGramJGraph() {
		/*
		 * Create a NGramGraph that uses both bigrams and trigrams
		 */
		NGramGraph dng = new NGramJGraph("hello hello", 2, 3, 3);
		UniqueVertexGraph uvgBigram = dng.getGraphLevel(0);
		UniqueVertexGraph uvgTrigram = dng.getGraphLevel(1);

		/*
		 * Ensure that the two graphs contain the proper sets
		 * of edges and vertices.
		 */
		String[] bigrams = {
			"he", "el", "ll", "lo", "o ", " h"
		};
		Set<JVertex> bigramVertices = uvgBigram.vertexSet();
		for (String s: bigrams) {
			assertTrue(bigramVertices.contains(new NGramVertex(s)));
		}
		EdgePair[] edges = {
			new EdgePair("el", "he", 2.0),
			new EdgePair("ll", "he", 2.0),
			new EdgePair("lo", "he", 2.0),
			new EdgePair("ll", "el", 2.0),
			new EdgePair("lo", "el", 2.0),
			new EdgePair("o ", "el", 1.0),
			new EdgePair("lo", "ll", 2.0),
			new EdgePair("o ", "ll", 1.0),
			new EdgePair(" h", "ll", 1.0),
			new EdgePair("o ", "lo", 1.0),
			new EdgePair(" h", "lo", 1.0),
			new EdgePair("he", "lo", 1.0),
			new EdgePair(" h", "o ", 1.0),
			new EdgePair("he", "o ", 1.0),
			new EdgePair("el", "o ", 1.0)
		};

		/*
		 * Make sure all edges are contained in the graph
		 */
		for (EdgePair ep: edges) {
			assertTrue(containedIn(
				new String[] {ep.source, ep.target},
				uvgBigram));
			assertTrue(hasWeightOf(
				new String[] {ep.source, ep.target},
				uvgBigram,
				ep.weight));
		}

		/*
		 * Check trigram graph
		 */
		String[] trigrams = {
			"hel", "ell", "llo", "lo ", "o h", " he"
		};
		Set<JVertex> trigramVertices = uvgTrigram.vertexSet();
		for (String s: trigrams) {
			assertTrue(trigramVertices.contains(new NGramVertex(s)));
		}
		EdgePair[] triEdges = {
			new EdgePair("ell", "hel", 2.0),
			new EdgePair("llo", "hel", 2.0),
			new EdgePair("lo ", "hel", 1.0),
			new EdgePair("llo", "ell", 2.0),
			new EdgePair("lo ", "ell", 1.0),
			new EdgePair("o h", "ell", 1.0),
			new EdgePair("lo ", "llo", 1.0),
			new EdgePair("o h", "llo", 1.0),
			new EdgePair(" he", "llo", 1.0),
			new EdgePair("o h", "lo ", 1.0),
			new EdgePair(" he", "lo ", 1.0),
			new EdgePair("hel", "lo ", 1.0),
			new EdgePair(" he", "o h", 1.0),
			new EdgePair("hel", "o h", 1.0),
			new EdgePair("ell", "o h", 1.0),
			new EdgePair("hel", " he", 1.0),
			new EdgePair("ell", " he", 1.0),
			new EdgePair("llo", " he", 1.0)
		};
		/*
		 * Make sure all edges are contained in the graph
		 */
		for (EdgePair ep: triEdges) {
			assertTrue(containedIn(
				new String[] {ep.source, ep.target},
				uvgTrigram));
			assertTrue(hasWeightOf(
				new String[] {ep.source, ep.target},
				uvgTrigram,
				ep.weight));
		}
	}

	/*
	 * Checks if an edge, represented by the labels of its source
	 * as well as its target, is contained in a graph.
	 */
	static boolean
	containedIn(String[] edgeLabels, UniqueVertexGraph uvg) {
		JVertex source = new NGramVertex(edgeLabels[0]);
		JVertex target = new NGramVertex(edgeLabels[1]);
		return (null != uvg.getEdge(source, target));
	}

	/*
	 * Checks if an edge, represented by the labels of its source
	 * as well as its target, is contained in a graph with a certain
	 * weight.
	 */
	static boolean
	hasWeightOf(String[] edgeLabels, UniqueVertexGraph uvg, double w) {
		JVertex source = new NGramVertex(edgeLabels[0]);
		JVertex target = new NGramVertex(edgeLabels[1]);
		Edge e = uvg.getEdge(source ,target);
		return (Math.abs(uvg.getEdgeWeight(e) - w) < 0.0001);
	}

	/*
	 * Utility class for graph-related tests
	 */
	static class EdgePair {
		public String source, target;
		public double weight;
		public EdgePair(String from, String to, double w) {
			source = from;
			target = to;
			weight = w;
		}
	}
}
