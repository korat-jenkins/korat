package korat.exploration;

/**
 * Test for korat.examples.binheap.BinomialHeap example
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 */
public class BinomialHeapExplorationTest extends BaseExplorationTest {

    public void testBinomialHeap() throws Exception {

        String cmdLine = "-c korat.examples.binheap.BinomialHeap -a 7";
        doTestForAllConfigs(cmdLine, 107416, -1);

        //Skipped due to long execution time:
        // cmdLine = "-c korat.examples.binheap.BinomialHeap -a 8";
        // doTestForAllConfigs(cmdLine, 603744, -1);

    }

}
