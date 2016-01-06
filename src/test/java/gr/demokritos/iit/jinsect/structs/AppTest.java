package gr.demokritos.iit.jinsect.structs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import salvo.jesus.graph.*;

import org.jgrapht.traverse.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;

import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramGraph;
import gr.demokritos.iit.jinsect.documentModel.representations.DocumentNGramSymWinGraph;
import gr.demokritos.iit.jinsect.documentModel.representations.NGramJGraph;
import gr.demokritos.iit.jinsect.documentModel.representations.NGramSymJGraph;
import gr.demokritos.iit.jinsect.jutils;
import gr.demokritos.iit.jinsect.utils;

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
	 * TODO: modify traversal so that it returns all edges
	 * like dfs coding
	 */
	public void testIteration() {
		NGramJGraph ngg = new NGramJGraph("ACTACT");
		DepthFirstIterator<JVertex, Edge> iDfs =
			new DepthFirstIterator<JVertex, Edge>(ngg.getGraphLevel(0));

		while (iDfs.hasNext()) {
			JVertex nextV = iDfs.next();
			System.out.println(nextV.toString());
		}
	}

	/* TODO: add testcases for Dfs encoding */
	public void testDfs() {
		NGramJGraph ngg = new NGramJGraph("ACTAG");
		DepthFirstEncoder dfsE = 
			new DepthFirstEncoder(ngg.getGraphLevel(0), new NGramVertex("TAG"));

		System.out.println(dfsE.getEncoding());

		dfsE = new DepthFirstEncoder(ngg.getGraphLevel(0));
		System.out.println(dfsE.getEncoding());
	}

	public void testSimilarities() 
	{
		UniqueJVertexGraph uvgA = new UniqueJVertexGraph();
		UniqueJVertexGraph uvgB = new UniqueJVertexGraph();

		JVertex v1 = new NGramVertex("A");
		JVertex v2 = new NGramVertex("C");
		JVertex v3 = new NGramVertex("G");

		/* add vertices to the graphs */
		uvgA.addVertex(v1); uvgB.addVertex(v1);
		uvgA.addVertex(v2); uvgB.addVertex(v2);
		uvgA.addVertex(v3); uvgB.addVertex(v3);

		/* add some edges */
		uvgA.addEdge(v1, v2, 2.0);
		uvgA.addEdge(v3, v2, 2.0);
		uvgB.addEdge(v2, v3, 1.0);

		double dSim = jutils.graphStructuralSimilarity(uvgA, uvgB);
		System.out.println("Structural Similarity: " + String.valueOf(dSim));
	}

	public void testNGramJGraph()
	{
		NGramJGraph dng = new NGramJGraph();
		dng.setDataString("ACTACTA");
		System.out.println(jutils.graphToDot(dng.getGraphLevel(0), false));

		/*
		for (salvo.jesus.graph.Edge e: dg.getGraphLevel(0).UniqueEdges.keySet()) {
			System.out.println(e.toString());
		}
		*/

		NGramJGraph d = new NGramJGraph();
		d.setDataString("ACTTAC");
		
		System.out.println(jutils.graphToDot(d.getGraphLevel(0), true));
		System.out.println(jutils.graphToDot(dng.getGraphLevel(0), false));
		dng = dng.intersectGraph(d);
		System.out.println(jutils.graphToDot(dng.getGraphLevel(0), true));


		d.setDataString("ACTT");
		dng.setDataString("ACTA");
		dng.mergeGraph(d, 0);
		System.out.println(jutils.graphToDot(dng.getGraphLevel(0), true));
		/* TODO: make sure that the two representations come up with the
		 * same result */
		assertTrue( true );
	}

	public void testNGramSymJGraph() {
		NGramSymJGraph nsg = new NGramSymJGraph("abcdef", 3, 3, 2);
		DocumentNGramSymWinGraph dng = new DocumentNGramSymWinGraph();
		dng.setDataString("abcdef");

		System.out.println(jutils.graphToDot(nsg.getGraphLevel(0), true));
		System.out.println(utils.graphToDot(dng.getGraphLevel(0), true));
		assertTrue( true );
	}

	public void testUniqueJGraph()
	{
		/* create a new UVJG graph */
		UniqueJVertexGraph uvg = new UniqueJVertexGraph();
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

		System.out.printf("V1 Out: %.1f In: %.1f\n",
						   uvg.outgoingWeightSumOf(v1),
						   uvg.incomingWeightSumOf(v1));
		System.out.printf("V2 Out: %.1f In: %.1f\n",
						   uvg.outgoingWeightSumOf(v2),
						   uvg.incomingWeightSumOf(v2));
		System.out.printf("V3 Out: %.1f In: %.1f\n",
						   uvg.outgoingWeightSumOf(v3),
						   uvg.incomingWeightSumOf(v3));

		System.out.printf("V1 - V2 Norm: %.2f\n",
						  uvg.getNormalizedEdgeWeight(v1, v2));
		System.out.printf("V2 - V3 Norm: %.2f\n",
						  uvg.getNormalizedEdgeWeight(v2, v3));
		System.out.printf("V3 - V2 Norm: %.2f\n",
						  uvg.getNormalizedEdgeWeight(v3, v2));

		assertTrue( true );
	}



    /**
     * Rigourous Test :-)
     */
    public void testApp()
    {
        assertTrue( true );
    }
}