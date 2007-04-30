package korat.instrumentation;

/**
 * Base interface for instrumenter classes
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public interface IInstrumenter {

    /**
     * Gets instrumented bytecode (to be given to the class loader, for
     * example).
     * 
     * @param className
     *            full class name
     * @return instrumented bytecode
     * @throws ClassNotFoundException
     *             if class with the <code>className</code> name is not found
     */
    public byte[] getBytecode(String className) throws ClassNotFoundException;

}
