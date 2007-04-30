package korat.exploration;

/**
 * Test for korat.examples.disjset.DisjSet example
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 */
public class DisjSetExplorationTest extends BaseExplorationTest {

    public void testDisjSet() throws Exception {

        String cmdLine = "-c korat.examples.disjset.DisjSet -a 4";
        doTestForAllConfigs(cmdLine, 914, 7915);

        //Skipped due to long execution time:
        // cmdLine = "-c korat.examples.disjset.DisjSet -a 5";
        // doTestForAllConfigs(cmdLine, 41546, 413855);

    }

}
