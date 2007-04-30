package korat.exploration;

import junit.framework.Test;
import junit.framework.TestSuite;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 */
public class ExplorationAllTests {
    public static Test suite() {
        TestSuite suite = new TestSuite("Test for korat.exploration");
        // $JUnit-BEGIN$
        suite.addTestSuite(BinaryTreeExplorationTest.class);
        suite.addTestSuite(HeapArrayExplorationTest.class);
        suite.addTestSuite(FibonacciHeapExplorationTest.class);
        suite.addTestSuite(DisjSetExplorationTest.class);
        suite.addTestSuite(BinomialHeapExplorationTest.class);
        suite.addTestSuite(SortedListExplorationTest.class);
        // $JUnit-END$
        // suite.addTestSuite(DagExplorationTest.class);
        // suite.addTestSuite(HashSetExplorationTest.class);
        // suite.addTestSuite(TreeMapExplorationTest.class);
        return suite;
    }
}
