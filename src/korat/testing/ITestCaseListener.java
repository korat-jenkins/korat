package korat.testing;

/**
 * Client that should handle test structures
 * 
 * @author korat.team *
 * 
 */
public interface ITestCaseListener {
    /**
     * Called by ITestCaseGenerator for which this object is registered as
     * listener.
     * 
     * @param testCase -
     *            generated test structure that satisfies requirements (class
     *            invariant and preconditions)
     * 
     */
    void notifyNewTestCase(Object testCase);
    
    /**
     * Called by ITestCaseGenerator when test generation is finished.
     * 
     * @param numOfExplored
     * @param numOfGenerated
     */
    void notifyTestFinished(long numOfExplored, long numOfGenerated);
}
