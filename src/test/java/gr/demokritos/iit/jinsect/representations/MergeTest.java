package gr.demokritos.iit.jinsect.representations;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import gr.demokritos.iit.jinsect.structs.*;
import java.util.*;

/**
 * Unit test for simple App.
 */
public class MergeTest 
	extends TestCase {
	/**
	 * Create the test case
	 *
	 * @param testName name of the test case
	 */
	public MergeTest( String testName )
	{
		super( testName );
	}

	/**
	 * @return the suite of tests being tested
	 */
	public static Test suite()
	{
		return new TestSuite( MergeTest.class );
	}

	public static void testGraphMerging() {
		UniqueVertexGraph uvg;
		Edge eFound;
		/* This N-Gram Graph should contain edges:
		 * [CTA] -> [ACT] (1.0)
		 * [TAG] -> [ACT] (1.0)
		 * [AGT] -> [ACT] (1.0)
		 * [TAG] -> [CTA] (1.0)
		 * [AGT] -> [CTA] (1.0)
		 * [AGT] -> [TAG] (1.0)
		 */
		NGramGraph ngA = new NGramJGraph("ACTAGT");
		
		/* This N-Gram Graph should contain edges:
		 * [CTA] -> [ACT] (2.0)
		 * [TAC] -> [ACT] (1.0)
		 * [ACT] -> [ACT] (1.0)
		 * [TAC] -> [CTA] (1.0)
		 * [ACT] -> [CTA] (1.0)
		 * [ACT] -> [TAC] (1.0)
		 * [CTA] -> [TAC] (1.0)
		 * [CTA] -> [CTA] (1.0)
		 */
		NGramGraph ngB = new NGramJGraph("ACTACTA");

		/* Average the two graphs and see what happens */
		ngA.merge(ngB, 0.5);
		uvg = ngA.getGraphLevel(0);

		/* Get [CTA] -> [ACT] - existed in both graphs with weights 1 and 2,
		 * so the new weight should be 1.5. */
		eFound = uvg.getEdge(new NGramVertex("CTA"), new NGramVertex("ACT"));
		assertEquals(1.5, eFound.edgeWeight(), 0.00001);

		/* Get [ACT] -> [TAC] - only existed in the second graph, so
		 * its weight should be 1.0 (new addition) */
		eFound = uvg.getEdge(new NGramVertex("ACT"), new NGramVertex("TAC"));
		assertEquals(1.0, eFound.edgeWeight(), 0.00001);

		/* Get [AGT -> TAG] - only existed in the first graph, so its
		 * weight should be equal to 1.0 */
		eFound = uvg.getEdge(new NGramVertex("AGT"), new NGramVertex("TAG"));
		assertEquals(1.0, eFound.edgeWeight(), 0.00001);
	}

	public static void testInverseIntersection() {
		/* This N-Gram Graph should contain edges:
		 * [CTA] -> [ACT] (1.0)
		 * [TAG] -> [ACT] (1.0)
		 * [AGT] -> [ACT] (1.0)
		 * [TAG] -> [CTA] (1.0)
		 * [AGT] -> [CTA] (1.0)
		 * [AGT] -> [TAG] (1.0)
		 */
		NGramGraph ngA = new NGramJGraph("ACTAGT");

		/* This N-Gram Graph should contain edges:
		 * [CTA] -> [ACT] (2.0)
		 * [TAC] -> [ACT] (1.0)
		 * [ACT] -> [ACT] (1.0)
		 * [TAC] -> [CTA] (1.0)
		 * [ACT] -> [CTA] (1.0)
		 * [ACT] -> [TAC] (1.0)
		 * [CTA] -> [TAC] (1.0)
		 * [CTA] -> [CTA] (1.0)
		 */
		NGramGraph ngB = new NGramJGraph("ACTACTA");

		/* keep the original edges in a set */
		Set<Edge> originalEdges =
			new HashSet<Edge>(ngA.getGraphLevel(0).edgeSet());

		/*
		 * The intersection should contain edges:
		 * [CTA] -> [ACT]
		 *
		 * Therefore, the inverse intersection should contain
		 * all edges in ngA except [CTA -> ACT].
		 */
		NGramGraph ngRes = ngA.inverseIntersectGraph(ngB);

		UniqueVertexGraph uvg = ngRes.getGraphLevel(0);

		/* make sure the common edge is not found in the inverse inters. */
		Edge eFound = uvg.getEdge(
				new NGramVertex("CTA"), new NGramVertex("ACT")
				);
		assertEquals(null, eFound);

		/* make sure all the other edges are still there */
		for (Edge e: originalEdges) {
			JVertex source = uvg.getEdgeSource(e);
			JVertex target = uvg.getEdgeTarget(e);
			if (source.getLabel().equals("CTA") &&
				target.getLabel().equals("ACT"))
			{
				continue;
			}

			/* otherwise, continue looking */
			eFound = uvg.getEdge(source, target);
			assertNotNull(eFound);
		}
	}
}
