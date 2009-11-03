package korat.testing.impl;

import java.io.IOException;
import java.io.PrintStream;
import java.util.LinkedList;
import java.util.List;

import korat.testing.ITestCaseListener;

/**
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public abstract class AbstractTestCaseGenerator {

    private List<ITestCaseListener> clients = new LinkedList<ITestCaseListener>();
    
    //TODO: think more elegant way
    private List<ITestCaseListener> specialClients = new LinkedList<ITestCaseListener>();
    
    protected PrintStream out;

    private static boolean PRINT_CASES = false;

    public AbstractTestCaseGenerator() {
        this(System.out);
    }

    public AbstractTestCaseGenerator(PrintStream out) {
        this.out = out;
    }

    public void attachClient(ITestCaseListener client) {
        clients.add(client);
    }
    
    public void attachSpecialClient(ITestCaseListener special) {
        specialClients.add(special);
    }

    public void detachClient(ITestCaseListener client) {
        clients.remove(client);     
        specialClients.remove(client);
    }

    protected void notifyClients(Object testCase) {
        if (interrupted)
            return;
        //TODO: this is not nice way of doing things
        if (TestCradle.getInstance().isPredicateOK()) {
            for (ITestCaseListener client : clients)
                client.notifyNewTestCase(testCase);
        }
        for (ITestCaseListener special : specialClients)
            special.notifyNewTestCase(testCase);
    }

    protected void notifyTestFinished(long numOfExplored, long numOfGenerated) {
//        if (interrupted)
//            return;
        for (ITestCaseListener client : clients)
            client.notifyTestFinished(numOfExplored, numOfGenerated);
        for (ITestCaseListener special : specialClients)
            special.notifyTestFinished(numOfExplored, numOfGenerated);
    }
    
    protected void warning(String string, IOException e) {
        System.err.println("WARNING: " + string);
        e.printStackTrace();
    }
    
    protected void log(Object obj) {
        if (PRINT_CASES == false)
            return;
        out.print(obj);
    }

    @SuppressWarnings("unused")
    private void log(Object obj, boolean carriageReturn) {
        if (PRINT_CASES == false)
            return;
        out.println(obj);
    }

    protected void log(int[] vector) {
        if (PRINT_CASES == false)
            return;
        for (int i = 0; i < vector.length; i++)
            out.print(vector[i] + " ");
    }

    protected void log(String str) {
        if (PRINT_CASES == false)
            return;
        out.print(str);
    }

    protected boolean interrupted;

    public void interrupt() {
        interrupted = true;
    }

    public boolean isInterrupted() {
        return interrupted;
    }

}
