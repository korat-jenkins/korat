package korat.exploration;

import junit.framework.TestCase;
import korat.Korat;
import korat.testing.impl.TestCradle;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
class TestConfigs {

    private static TestConfigs instance = new TestConfigs();

    public static TestConfigs getInstance() {
        return instance;
    }

    private TestConfigs() {

    }

    private int curr = 0;

    private int numOfConfigs = 1;

    public void reset() {
        curr = 0;
    }

    public boolean hasNext() {
        return curr < numOfConfigs;
    }

    public void next() {
        setConfig(curr);
        curr++;
    }

    private void setConfig(int curr) {
        if (curr == 0) {
            setConfig0();
        }
    }

    private void setConfig0() {
    }

}

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class BaseExplorationTest extends TestCase {

    protected void doTestForAllConfigs(String cmdLine, int newCases, int tested) {
        doTestForAllConfigs(cmdLine.split(" "), newCases, tested);
    }

    private void doTestForAllConfigs(String[] args, int newCases, int tested) {

        TestConfigs it = TestConfigs.getInstance();
        it.reset();
        while (it.hasNext()) {

            it.next(); 
            Korat.main(args);
            assertEquals(newCases, TestCradle.getInstance().getValidCasesGenerated());
            if (tested > 0) {
                assertEquals(tested, TestCradle.getInstance().getTotalExplored());
            }

        }

    }

}
