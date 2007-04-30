package korat.finitization;

public interface IFinitizationFactory {

    /**
     * Creates <code>IFinitization</code> object for the given parameter
     * <code>clz</code>
     * 
     * @param clz class to create finitization for
     * @return finitization for the given class
     */
    IFinitization createFinitization(Class clz);

}
