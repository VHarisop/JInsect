package gr.demokritos.iit.jinsect.io;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.io.File;

/**
 * Unit test for simple App.
 */
public class IoTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public IoTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( IoTest.class );
    }
	
	/**
	 * Test the correctness of the line reader
	 */
	public void testLineReader() {
		String fileName = "/testFile01.txt";
		assertNotNull("Test file missing", getClass().getResource(fileName));

		try {
			File res = new File(getClass().getResource(fileName).toURI());
			LineReader lnRdr = new LineReader();
			String[] lines = lnRdr.getLines(res);

			// make sure lines exist and are 10
			assertNotNull(lines);
			assertTrue(lines.length == 10);
		} catch (Exception ex) {
			ex.printStackTrace();
			assertTrue(false); // fail
		}
	}
	
}
