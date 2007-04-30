package korat.instrumentation;

import junit.framework.Test;
import junit.framework.TestSuite;
import korat.instrumentation.test.TypeArrayGenerator_DynamicTest;
import korat.instrumentation.test.TypeArrayGenerator_StaticTest;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 */
public class InstrumentationAllTests {

    public static Test suite() {

        TestSuite suite = new TestSuite("Test for korat.instrumentation");
        // $JUnit-BEGIN$
        suite.addTestSuite(ArrayInstrumentationTest.class);
        suite.addTestSuite(TypeArrayGenerator_DynamicTest.class);
        suite.addTestSuite(TypeArrayGenerator_StaticTest.class);
        // $JUnit-END$

        return suite;

    }

}