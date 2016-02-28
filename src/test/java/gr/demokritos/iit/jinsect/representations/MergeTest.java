package gr.demokritos.iit.jinsect.representations;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import gr.demokritos.iit.jinsect.structs.*;

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
	}
}