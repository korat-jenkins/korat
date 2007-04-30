package korat.exploration;

/**
 * Test for korat.examples.sortedlist.SortedList example
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class SortedListExplorationTest extends BaseExplorationTest {

    public void testSortedList() throws Exception {

        String cmdLine = "-c korat.examples.sortedlist.SortedList -a 11,11,12,11";
        doTestForAllConfigs(cmdLine, 352716, 3880154);

    }

}
