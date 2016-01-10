package gr.demokritos.iit.jinsect.encoders;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import org.jgrapht.traverse.*;
import org.jgrapht.*;
import org.jgrapht.graph.*;

import gr.demokritos.iit.jinsect.documentModel.representations.NGramJGraph;
import gr.demokritos.iit.jinsect.documentModel.representations.NGramSymJGraph;
import gr.demokritos.iit.jinsect.structs.*;
import gr.demokritos.iit.jinsect.jutils;
import gr.demokritos.iit.jinsect.utils;


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
    public EncodersTest( String testName )
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

	/* TODO: add testcases for Dfs encoding */
	public void testDfs() {
		NGramJGraph ngg = new NGramJGraph("ACTAG");
		DepthFirstEncoder dfsE = 
			new DepthFirstEncoder(ngg.getGraphLevel(0), new NGramVertex("TAG"));

		System.out.println(dfsE.getEncoding());

		dfsE = new DepthFirstEncoder(ngg.getGraphLevel(0));
		System.out.println(dfsE.getEncoding());
	}

	/* TODO: add testcases for canonical coding 
	 * equality and inequality */
	public void testCanonical() {
		NGramJGraph nggA = new NGramJGraph("ATACA");
		NGramJGraph nggB = new NGramJGraph("AATAC");

		CanonicalCoder cCoder_A = new CanonicalCoder(nggA.getGraphLevel(0));
		CanonicalCoder cCoder_B = new CanonicalCoder(nggB.getGraphLevel(0));

		// make sure that the graphs have different canonical codes
		assertFalse(cCoder_A.getEncoding().equals(cCoder_B.getEncoding()));

		// assert that AAT is lexicographically smaller than ACA
		assertTrue(jutils.compareCanonicalCodes(nggA.getGraphLevel(0), 
												nggB.getGraphLevel(0)) > 0);
	}
}
