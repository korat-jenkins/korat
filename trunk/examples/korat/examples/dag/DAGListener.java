package korat.examples.dag;

import java.io.File;
import java.io.FileOutputStream;
import java.io.PrintStream;

import korat.testing.ITestCaseListener;
import korat.utils.io.FileUtils;

/**
 * <p>
 * This class is called every time a new valid DAG is generated. It saves given
 * DAG to file in the following form: first line is the number of nodes (n,
 * nodes are enumerated from 0 to n-1), and the following lines represent edges
 * between nodes (they contain two integers, each of which represents a node).
 * </p>
 * <p> 
 * To use this listener during Korat search, use the <code>--listeners</code>
 * command line switch. For example:
 * <pre>
 *     --class korat.examples.dag.DAG --args 3 --listeners korat.examples.dag.DAGListener
 * </pre>
 * </p>
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class DAGListener implements ITestCaseListener {

    public static final int MAX_FILES_PER_DIR = 100000;

    //COMPAT1.4
    public static final int NUM_DIGITS = (int) Math.ceil(Math.log(MAX_FILES_PER_DIR)/Math.log(10));

    private static final int START_COUNTER = 1;

    public static final String GRAPHS_DIR = "graphs";

    private int counter = 0;

    private int dirCounter = START_COUNTER;

    private int fileCounter = START_COUNTER;

    public DAGListener() {
    }

    public void notifyNewTestCase(Object testCase) {
        DAG dag = (DAG) testCase;
        String graph = getTextRepresentation(dag);
        saveGraph(graph);
    }

    protected String getTextRepresentation(DAG dag) {
        StringBuffer sb = new StringBuffer();
        for (int k = 0; k < dag.getNodes().size(); k++) {
            DAGNode node = dag.getNodes().get(k);
            for (int i = 0; i < node.children.length; i++) {
                DAGNode child = node.children[i];
                if (child == null)
                    continue;
                String edge = node.id + " " + child.id;
                sb.append(edge + "\n");
            }
        }
        String graph = sb.toString();
        return graph;
    }
    
    public void notifyTestFinished(long numOfExplored, long numOfGenerated) {
        for (int dirNo = START_COUNTER; dirNo <= dirCounter; dirNo++) {
            File dir = new File(GRAPHS_DIR + File.separatorChar + GRAPHS_DIR + dirNo);
            FileUtils.appendSuffix(dir, NUM_DIGITS);
        }
    }
    
    private void emptyOldGraphs() {
        File dir = new File(GRAPHS_DIR);
        if (dir.isDirectory())
            FileUtils.deleteFolder(dir, false);
    }

    private void incFileCounter() {
        fileCounter++;
        if (fileCounter >= MAX_FILES_PER_DIR) {
            fileCounter = START_COUNTER;
            dirCounter++;
        }
    }

    protected void saveGraph(String graph) {
        if (counter == 0) {
            emptyOldGraphs();
        }
        counter++;
        String fileCnt = Integer.toString(fileCounter);
        String dirCnt = Integer.toString(dirCounter);
        incFileCounter();

        int prependZeros = NUM_DIGITS - fileCnt.length();
        for (int i = 0; i < prependZeros; i++) {
            fileCnt = "0" + fileCnt;
        }
        
        String dirName = GRAPHS_DIR + File.separatorChar + GRAPHS_DIR + dirCnt;
        File dir = new File(dirName);
        if (!dir.exists() || !dir.isDirectory()) {
            if (!dir.mkdirs()) {
                System.err.println("Cannot create directory " + dir.getAbsolutePath());
                return;
            }
        }
        
        String fName = dirName + File.separatorChar + "graph-" + fileCnt + "-of-";
        saveToFile(graph, fName);
    }

    protected void saveToFile(String graph, String fName) {
        PrintStream ps = null;
        try {
            File f = new File(fName);
            if (!f.exists())
                if (!f.createNewFile()) {
                    System.err.println("Cannot create new file.");
                    return;
                }
            ps = new PrintStream(new FileOutputStream(f));
            ps.print(graph + "\n");
            System.out.println("Graph saved as " + f.getAbsolutePath());
        } catch (Exception e) {
            System.err.println("Cannot save graph file to disk. Reason: "
                    + e.getMessage());
        } finally {
            if (ps != null) {
                ps.flush();
                ps.close();
            }
        }
    }

}
