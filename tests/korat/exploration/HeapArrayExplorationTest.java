package korat.exploration;

/**
 * Test for korat.examples.heaparray.HeapArray example.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class HeapArrayExplorationTest extends BaseExplorationTest {

    public void testHeapArray() throws Exception {

        String cmdLine = "-c korat.examples.heaparray.HeapArray -a 6,6,6";
        doTestForAllConfigs(cmdLine, 13139, 64533);

        cmdLine = "-c korat.examples.heaparray.HeapArray -a 7,7,7";
        doTestForAllConfigs(cmdLine, 117562, 519968);

    }

}
