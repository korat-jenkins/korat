package korat.exploration;

/**
 * Test for korat.examples.binarytree.BinaryTree example.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class BinaryTreeExplorationTest extends BaseExplorationTest {

    public void testBinaryTree() throws Exception {

        String cmdLine = "-c korat.examples.binarytree.BinaryTree -a 6,6,6";
        doTestForAllConfigs(cmdLine, 132, -1);

        cmdLine = "-c korat.examples.binarytree.BinaryTree -a 8,8,8";
        doTestForAllConfigs(cmdLine, 1430, 54418);

    }

}
