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
		assertTrue(dfsE.getEncoding().equals("TAG->ACT|TAG->CTA|CTA->ACT|"));

		dfsE = new DepthFirstEncoder(ngg.getGraphLevel(0));
		assertTrue(dfsE.getEncoding().equals("CTA->ACT|TAG->ACT|TAG->CTA|"));

		dfsE = new DepthFirstEncoder(ngg.getGraphLevel(0), new NGramVertex("CTA"));
		assertTrue(dfsE.getEncoding().equals("CTA->ACT|TAG->ACT|TAG->CTA|"));

	}

	/* TODO: add testcases for canonical coding 
	 * equality and inequality */
	public void testCanonical() {
		NGramJGraph nggA = new NGramJGraph("ATACA");
		NGramJGraph nggB = new NGramJGraph("AATAC");
		// new graph, equal to nggA
		NGramJGraph nggC = new NGramJGraph("ATACA");

		CanonicalCoder cCoder_A = new CanonicalCoder(nggA.getGraphLevel(0));
		CanonicalCoder cCoder_B = new CanonicalCoder(nggB.getGraphLevel(0));
		CanonicalCoder cCoder_C = new CanonicalCoder(nggC.getGraphLevel(0));

		// make sure that the graphs have different canonical codes
		assertFalse(cCoder_A.getEncoding().equals(cCoder_B.getEncoding()));

		// assert that AAT is lexicographically smaller than ACA
		assertTrue(jutils.compareCanonicalCodes(nggA.getGraphLevel(0), 
												nggB.getGraphLevel(0)) > 0);

		// assert that two identical graphs give indentical canonical codes
		assertTrue(cCoder_A.getEncoding().equals(cCoder_C.getEncoding()));
	}
}
