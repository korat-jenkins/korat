package korat.utils.cv;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 */
public class CVAllTests {

    public static Test suite() {

        TestSuite suite = new TestSuite("Test for korat.exploration");
        // $JUnit-BEGIN$
        suite.addTestSuite(CVFinderDeltaTest.class);
        suite.addTestSuite(CVDeltaTest.class);
        suite.addTestSuite(CVDeltaReadersTest.class);
        suite.addTestSuite(CVReadersTest.class);
        // $JUnit-END$

        return suite;

    }

}