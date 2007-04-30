package korat.exploration;

/**
 * Test for korat.examples.fibheap.FibonacciHeap example
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class FibonacciHeapExplorationTest extends BaseExplorationTest {

    public void testFibonacciHeap() throws Exception {

        String cmdLine = "-c korat.examples.fibheap.FibonacciHeap -a 5";
        doTestForAllConfigs(cmdLine, 52281, -1);

    }

}
