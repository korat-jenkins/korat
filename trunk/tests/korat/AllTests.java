package korat;

import junit.framework.Test;
import junit.framework.TestSuite;
import korat.exploration.ExplorationAllTests;
import korat.instrumentation.InstrumentationAllTests;
import korat.utils.ReflectionUtilsTests;
import korat.utils.cv.CVAllTests;
import korat.utils.io.BitInputOutputStreamTest;

public class AllTests {

    public static void main(String[] args) {
        junit.swingui.TestRunner.run(AllTests.class);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite("Tests for korat");
        // $JUnit-BEGIN$
        suite.addTest(ExplorationAllTests.suite());
        suite.addTest(InstrumentationAllTests.suite());
        suite.addTest(CVAllTests.suite());
        suite.addTestSuite(ReflectionUtilsTests.class);
        suite.addTestSuite(BitInputOutputStreamTest.class);
        // $JUnit-END$
        return suite;
    }

}
