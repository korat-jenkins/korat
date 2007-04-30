package korat.finitization.impl;

import korat.finitization.IFinitization;
import korat.finitization.IFinitizationFactory;

/**
 * Use this factroy class to create finitizations.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class FinitizationFactory implements IFinitizationFactory {

    private static FinitizationFactory instance;

    public static FinitizationFactory getInstance() {
        if (instance == null) {
            instance = new FinitizationFactory();
        }
        return instance;
    }

    private FinitizationFactory() {
    }
    
    /**
     * @see IFinitizationFactory#createFinitization(Class)
     */
    public IFinitization createFinitization(Class clz) {
        return new Finitization(clz);
    }
    
    /**
     * Helper method. The same as 
     * <code> create(clz)</code>
     * 
     * @param clz class to create finitization for
     * @return finitization for the given class.
     */
    public static IFinitization create(Class clz) {
        return getInstance().createFinitization(clz);
    }

}
