package korat.instrumentation;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import junit.framework.TestCase;
import korat.instrumentation.examples.IntArrayTestClass;
import korat.instrumentation.examples.ObjArrayTestClass;
import korat.loading.InstrumentingClassLoader;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 */
public class ArrayInstrumentationTest extends TestCase {

    public void testIntArray() throws Exception {

        final Class[] ctorArgTypes = new Class[] {};
        final Object[] ctorArgValues = new Object[] {};

        final Class[] method1ArgTypes = new Class[] { int.class };
        final Object[] method1ArgValues = new Object[] { 5 };

        final Class[] method2ArgTypes = new Class[] {};
        final Object[] method2ArgValues = new Object[] {};

        doEqualTest(IntArrayTestClass.class, ctorArgTypes, ctorArgValues,
                "sum", method1ArgTypes, method1ArgValues);

        doEqualTest(IntArrayTestClass.class, ctorArgTypes, ctorArgValues, "f",
                method2ArgTypes, method2ArgValues);

    }

    public void testObjArray() throws Exception {

        final Class[] ctorArgTypes = new Class[] {};
        final Object[] ctorArgValues = new Object[] {};

        final Class[] method1ArgTypes = new Class[] {};
        final Object[] method1ArgValues = new Object[] {};

        final Class[] method2ArgTypes = new Class[] { String.class };
        final Object[] method2ArgValues = new Object[] { "aleksandar" };

        doEqualTest(ObjArrayTestClass.class, ctorArgTypes, ctorArgValues,
                "getLength", method1ArgTypes, method1ArgValues);
        doEqualTest(ObjArrayTestClass.class, ctorArgTypes, ctorArgValues,
                "reverse", method2ArgTypes, method2ArgValues);

    }

    private void doEqualTest(Class<?> clz, final Class[] ctorArgTypes,
            final Object[] ctorArgValues, String methodName,
            final Class[] methodArgTypes, final Object[] methodArgValues)
            throws NoSuchMethodException, InstantiationException,
            IllegalAccessException, InvocationTargetException,
            ClassNotFoundException {

        Constructor<?> ctor = clz.getConstructor(ctorArgTypes);
        Object obj = ctor.newInstance(ctorArgValues);
        Method m = clz.getMethod(methodName, methodArgTypes);
        Object res1 = m.invoke(obj, methodArgValues);

        InstrumentingClassLoader cl = new InstrumentingClassLoader();

        clz = cl.loadClass(clz.getName());
        ctor = clz.getConstructor(ctorArgTypes);
        obj = ctor.newInstance(ctorArgValues);
        m = clz.getMethod(methodName, methodArgTypes);
        Object res2 = m.invoke(obj, methodArgValues);

        assertEquals(res1, res2);

    }

}
