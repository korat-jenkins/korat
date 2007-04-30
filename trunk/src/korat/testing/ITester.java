package korat.testing;

/**
 * Interface for a class that should communicate with the debugger
 * 
 * @author korat.team
 * 
 */
public interface ITester {

    /**
     * Notifies the beginning of field trace
     * 
     */
    void startFieldTrace();

    /**
     * Notifies continuation of a stopped field trace
     * 
     */
    void continueFieldTrace();

    /**
     * Notifies the end of field trace
     * 
     */
    void stopFieldTrace();

    /**
     * Notifies that particular field has be accessed
     * 
     * @param obj -
     *            object the field of which is accessed
     * @param field -
     *            field that is accessed
     */
    void notifyFieldAccess(Object obj, String field);

    /**
     * Notifies that particular field has been accessed
     * 
     * @param accessedFieldIndex -
     *            index of field in candidate vector
     */
    void notifyFieldAccess(int accessedFieldIndex);

}
