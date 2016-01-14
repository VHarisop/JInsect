package gr.demokritos.iit.jinsect.documentModel.representations;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;

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
}
