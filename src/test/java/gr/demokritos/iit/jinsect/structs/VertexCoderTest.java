package gr.demokritos.iit.jinsect.structs;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;

import java.lang.Math;

/**
 * Unit test for simple App.
 */
public class VertexCoderTest 
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public VertexCoderTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( VertexCoderTest.class );
    }

	/**
	 * Makes sure that the auto-update of the {@link VertexCoder}'s 
	 * running weight works properly.
	 */
	public void testKeys() {
		VertexCoder vCoder = new VertexCoder()
			.withWeightValue(0.5).withStep(0.01);

		vCoder.putLabel("AA");
		vCoder.putLabel("AB");
		vCoder.putLabel("BA");
	
		assertTrue(eqDouble(vCoder.get("AA"), 0.5));
		assertTrue(eqDouble(vCoder.get("BA"), 0.52));
		assertTrue(eqDouble(vCoder.get("AB"), 0.51));
	}

	/* Helper function to check doubles for equality */
	private static boolean eqDouble(double a, double b) {
		return (Math.abs(a - b) < 0.000001);
	}
}
