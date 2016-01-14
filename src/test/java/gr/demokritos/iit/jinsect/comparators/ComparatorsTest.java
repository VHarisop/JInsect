package gr.demokritos.iit.jinsect.comparators;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jgrapht.traverse.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;

import gr.demokritos.iit.jinsect.documentModel.representations.NGramJGraph;
import gr.demokritos.iit.jinsect.documentModel.representations.NGramSymJGraph;
import gr.demokritos.iit.jinsect.structs.UniqueJVertexGraph;
import gr.demokritos.iit.jinsect.structs.NGramVertex;
import gr.demokritos.iit.jinsect.structs.JVertex;
import gr.demokritos.iit.jinsect.jutils;
import gr.demokritos.iit.jinsect.utils;


/**
 * Unit test for simple App.
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
	 * Test the correctness of the similarity comparator 
	 */
	public void testSimilarityComparator() {
		// verify that the similarity comparator works properly
		SimilarityComparator simComp = new SimilarityComparator();

		UniqueJVertexGraph uvgA = new UniqueJVertexGraph();
		UniqueJVertexGraph uvgB = new UniqueJVertexGraph();

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
