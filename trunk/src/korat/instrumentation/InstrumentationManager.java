package korat.instrumentation;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.HashSet;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * The place for various constants, utility method and such related
 * to instrumentation. This is also the place for obtaining the
 * currently configured instrumenter, which should be used throughout 
 * the program (by the class loader before loading the class, for example).
 * 
 * @see IInstrumenter
 * @see AbstractInstrumenter
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class InstrumentationManager {
    
    private static AbstractInstrumenter instrumenter = null;

    static {
        CompoundInstrumenter ci = new CompoundInstrumenter();
        ci.add(new SpecialConstructorInstrumenter());
        ci.add(new FieldInstrumenter());
        ci.add(new ArrayFieldInstrumenter());
        //ci.add(new TouchInstrumenter());
        instrumenter = ci;
    }
    
    public static IInstrumenter getInstrumenter() {
        return instrumenter;
    }

    /**
     * global repository of the instrumented classes
     */
    protected static Set<CtClass> alreadyInstrumented = new HashSet<CtClass>();
    
    public static void addToAlreadyInstrumented(CtClass clz) {
        alreadyInstrumented.add(clz);
    }

    /**
     * Instruments the given class if not already instrumented
     * 
     * @param clz
     *            class to instrument
     * @throws CannotCompileException
     * @throws NotFoundException
     * @throws IOException
     */
    protected static void instrumentClassIfNeeded(CtClass clz)
            throws CannotCompileException, NotFoundException, IOException {
        if (clz.isInterface())
            return;
        if (!alreadyInstrumented.contains(clz)) {
            alreadyInstrumented.add(clz);
            instrumenter.instrument(clz);
        }
    }
    
    /**
     * Loads all instrumented classes using class loader cl
     * 
     * @param cl
     */
    public static void loadAllClasses(ClassLoader cl) {
        for (CtClass cls : alreadyInstrumented) 
            try {
                cls.toClass(cl, null);
            } catch (CannotCompileException e) {
                throw new RuntimeException(e);
            }
    }
    
    public static final String KORAT_FIELD_PREFIX = "__korat";

    public static final String TESTER_FIELD_NAME = KORAT_FIELD_PREFIX + "myTester";
    
    public static final String ID_FIELD_PREFIX = KORAT_FIELD_PREFIX + "id_";

    public static final String GETTER_PREFIX = KORAT_FIELD_PREFIX + "_get_";

    public static final String SETTER_THIS_FIELD_NAME = KORAT_FIELD_PREFIX + "_this";

    public static final String SETTER_TEMPLATE = "Korat_{0}_setter";

    public static final String GETSETTER_TEMPLATE = KORAT_FIELD_PREFIX + "_get_{0}_setter";
    
    public static final String _KORAT_ARRAY_FIELD_PREFIX = KORAT_FIELD_PREFIX + "KoratArray_";
    
    /**
     * @param fieldName - field name
     * @return is the field with the given name one of the special fields  
     * introduced by Korat.
     */
    public static boolean isKoratField(String fieldName) {
        // TODO: this is not a good way to determine korat field:
        // non-korat fields may start with the same prefix
        return fieldName.startsWith(KORAT_FIELD_PREFIX);
    }
    
    /**
     * @param methodName - method name
     * @return is the method with the given name one of the special methods  
     * introduced by Korat.
     */
    public static boolean isKoratMethod(String methodName) {
        // TODO: this is not a good way to determine korat method:
        // non-korat methods may start with the same prefix
        return methodName.startsWith(KORAT_FIELD_PREFIX);
    }

    public static String getGetterName(String fieldName) {
        return GETTER_PREFIX + fieldName;
    }

    public static String getIdFieldName(String fieldName) {
        return ID_FIELD_PREFIX + fieldName;
    }

    public static String getGetSetterName(String fieldName) {
        return MessageFormat.format(GETSETTER_TEMPLATE,
                new Object[] { fieldName });
    }

    public static String getSetterClassName(String fieldName) {
        return MessageFormat.format(SETTER_TEMPLATE, new Object[] { fieldName });
    }

    /**
     * For the given array field name returns the name of the corresponding
     * KoratArray field.
     * 
     * @param fName name of the array field
     * @return name of the corresponding KoratArray field
     */
    public static String getKoratArrayFieldName(String fName) {
        return _KORAT_ARRAY_FIELD_PREFIX + fName;
    }

}
