package korat.testing;

import korat.utils.IIntList;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public interface IKoratSearchStrategy {

    /**
     * Searches for next test case. If state space has been exausted, returns null. 
     * 
     * @return next test case
     */
    Object nextTestCase();

    /**
     * Gets candidate vector that corresponds to the test case returned last.
     * 
     * @return candidate vector
     */
    int[] getCandidateVector();

    /**
     * Gets the list of accessed fields during the last search.
     * 
     * @return accessed fields
     */
    IIntList getAccessedFields();
    
    void setStartCandidateVector(int[] startCV);
    
    void setEndCandidateVector(int[] endCV);
    
    /**
     * Reports the current test case as valid structure
     * The current test case is the last one returned with <code>nextTestCase</code>. 
     * If this method is executed before first <code>nextTestCase</code>, the behaviour 
     * is undefined 
     *
     */
    void reportCurrentAsValid();

}
