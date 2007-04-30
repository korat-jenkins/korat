package korat.loading;

import korat.instrumentation.IInstrumenter;
import korat.instrumentation.InstrumentationManager;
import korat.loading.filter.FilterManager;
import korat.loading.filter.IComparingFilter;

/**
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 *
 */
public class InstrumentingClassLoader extends ClassLoader {

    public static InstrumentingClassLoader cl;

    private IInstrumenter instrumenter;

    private IComparingFilter comparingFilter;
    
    private static final boolean DEBUG = false;

    public InstrumentingClassLoader() {
        super(InstrumentingClassLoader.class.getClassLoader());
        initialize();
    }

    private void initialize() {
        comparingFilter = FilterManager.getFilter();
        instrumenter = InstrumentationManager.getInstrumenter();
    }

    @Override
    protected Class<?> loadClass(String className, boolean resolve)
            throws ClassNotFoundException {

        // find the class if it was already loaded
        Class c = findLoadedClass(className);

        if (c == null) { // class was not found in local table

            // check class name against custom filters
            boolean pass = comparingFilter.allowProcessing(className);

            if (pass) {

                // instrument and load class
                byte data[] = instrumenter.getBytecode(className);
                c = defineClass(className, data, 0, data.length);
                
            } else {
                
                // load class using default loader
                c = getParent().loadClass(className);
                if (DEBUG)
                    System.out.println("X " + className);
                return c;
                
            }

            if (c == null)
                throw new ClassNotFoundException(className);
             
            if (DEBUG)
                System.out.println((pass? "* " : "  ") + "Loaded class: " + c.getCanonicalName());
        }

        // if resolution is required, do it
        if (resolve)
            resolveClass(c);

        return c;
        
    }

    public IComparingFilter getComparingFilter() {
        return comparingFilter;
    }

    public void setComparingFilter(IComparingFilter comparingFilter) {
        this.comparingFilter = comparingFilter;
    }

    public IInstrumenter getInstrumenter() {
        return instrumenter;
    }

    public void setInstrumenter(IInstrumenter instrumenter) {
        this.instrumenter = instrumenter;
    }

}
