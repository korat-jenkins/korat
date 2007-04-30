package korat.instrumentation;

import static javassist.Modifier.isAbstract;
import static javassist.Modifier.isNative;
import static javassist.Modifier.isStatic;

import java.io.IOException;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import korat.config.ConfigManager;
import korat.loading.filter.FilterManager;

/**
 * <p>Base instrumenter class.</p>
 * 
 * <p>Implements <code>getByteCode(String)</code> method (from 
 * <code>IInstrumenter</code> interface) using template method design
 * pattern. Subclasses should only override <code>instrument(CtClass)</code>
 * method which does the instrumentation in the terms of the javassist, i.e.
 * instruments given javassist <code>CtClass</code> object.</p>
 *
 * @see FieldInstrumenter
 * @see ArrayFieldInstrumenter
 * @see SpecialConstructorInstrumenter
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
abstract class AbstractInstrumenter implements IInstrumenter {

    protected ClassPool cp;
    
    protected AbstractInstrumenter() {
        cp = ClassPool.getDefault();
    }
    
    /**
     * @see korat.instrumentation.IInstrumenter#getBytecode(java.lang.String)
     */
    public byte[] getBytecode(String className) throws ClassNotFoundException {
        byte[] data = null;
        try {

            CtClass clz = cp.get(className);
            clz.stopPruning(true); // for JUnit regression tests

            if (clz.isFrozen()) {
                clz.defrost();
            } else {
                //this method calls instrument(CtClass) for the 
                //currently configured instrumenter
                InstrumentationManager.instrumentClassIfNeeded(clz);
            }

            data = clz.toBytecode();

            if (ConfigManager.getInstance().dumpBytecodes) {
                korat.utils.BytecodeDumper.getInstance().dumpAndEatExceptions(data,
                    clz.getName(), true);
            }
            
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (NotFoundException e) {
            throw new ClassNotFoundException("class " + className
                    + " doesn't exist!", e);
        }
        return data;
    }

    protected abstract void instrument(CtClass clz) 
            throws CannotCompileException, NotFoundException, IOException;
    
    /**
     * Helper: returns if the given field should be processed i.e. instrumented. 
     * For example, Korat should not instrument field accesses for static fields, nor
     * it should add special getters and setters for them. Also, <code>FilterManager</code>
     * instance should be consulted about user configured excluded-from-instrumentation
     * packages. Subclasses may override this method if different policy is needed. 
     * 
     * @param f - field to instrument or not
     * @return whether this field should be instrumented or not
     */
    protected boolean shouldProcessField(CtField f) {
        int modifiers = f.getModifiers();
        if (isStatic(modifiers) || InstrumentationManager.isKoratField(f.getName()))
            return false;
        return FilterManager.getFilter().allowProcessing(f.getDeclaringClass().getName());
    }
    
    /**
     * Helper: generally, abstract and native methods should not be processed.
     * 
     * @param m - method to check
     * @return if the method should be processes or not
     */
    protected boolean shouldProcessMethod(CtMethod m) {
        if (isAbstract(m.getModifiers()))
            return false;
        if (isNative(m.getModifiers()))
            return false;
        return true;
    }
    
}
