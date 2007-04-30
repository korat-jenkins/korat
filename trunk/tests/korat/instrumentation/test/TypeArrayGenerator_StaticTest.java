package korat.instrumentation.test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import junit.framework.TestCase;
import korat.instrumentation.ArrayGenerator;
import korat.instrumentation.Setter;
import korat.loading.InstrumentingClassLoader;

public class TypeArrayGenerator_StaticTest extends TestCase {

    public static class TestC {
        int i;
    }
    
    static InstrumentingClassLoader KoratCL;
    
    static {
        //IncludingPackageFilter f = new IncludingPackageFilter();
        //f.addPackage("korat.instrumentation");
        //KoratCL = (InstrumentingClassLoader) TestCradle.getInstance().getClassLoader();
        //KoratCL.setComparingFilter(f);
    }

    ArrayGenerator gen;

    Class arrayClass;

    Class componentClass;
    
    public TypeArrayGenerator_StaticTest() throws Exception {
        //this(Class.forName(TestC.class.getName(), true, KoratCL));
        this(TestC[].class);
        
    }

    public TypeArrayGenerator_StaticTest(Class cls) throws Exception {
        assertTrue(cls.isArray());
        gen = new ArrayGenerator(cls);
        arrayClass = cls;
        componentClass = cls.getComponentType();

    }

    public void testArrayGenerator_CompilationException() throws Exception {

        Class array = gen.getArrayClass();
        assertNotNull(array);

    }

    public void testGenerateClassObject() throws Exception {
        try {
            gen.getArrayClass();
        } catch (Exception e) {
            assertTrue("The following exception occured:\n" + e, false);
        }

    }

    public void testFieldsCreated() throws Exception {
        Class<?> array = gen.getArrayClass();

        Field values = array.getDeclaredField("values");
        assertEquals(arrayClass, values.getType());

        Field values_ids = array.getDeclaredField("values_ids");
        assertEquals(int[].class, values_ids.getType());

        Field length = array.getDeclaredField("length");
        assertEquals(int.class, length.getType());

        Field length_id = array.getDeclaredField("length_id");
        assertEquals(int.class, length_id.getType());

    }

    public void testMethodsCreated() throws Exception {
        Class<?> array = gen.getArrayClass();

        // -- Element access ---
        Method getElementSetter = array.getDeclaredMethod("get_element_setter",
                new Class[] { int.class, int.class });
        assertEquals(Setter.class, getElementSetter.getReturnType());

        Method getElement = array.getDeclaredMethod("get",
                new Class[] { int.class });
        assertEquals(componentClass, getElement.getReturnType());

        Method setElement = array.getDeclaredMethod("set", new Class[] {
                int.class, componentClass });
        assertEquals(void.class, setElement.getReturnType());

        Method getLengthSetter = array.getDeclaredMethod("get_length_setter",
                new Class[] { int.class });
        assertEquals(Setter.class, getLengthSetter.getReturnType());

        Method getLength = array.getDeclaredMethod("getLength", new Class[] {});
        assertEquals(int.class, getLength.getReturnType());

        Method setLength = array.getDeclaredMethod("setLength",
                new Class[] { int.class });
        assertEquals(void.class, setLength.getReturnType());

    }

    private Map<String, Class> getInnerClasses(Class clz) {
        Map<String, Class> innerClasses = new HashMap<String, Class>();
        Class[] clsArr = clz.getClasses();
        for (Class c : clsArr)
            innerClasses.put(c.getName(), c);

        return innerClasses;
    }

    public void testInnerClassesCreated() throws Exception {
        Class array = gen.getArrayClass();
        Map<String, Class> innerClasses = getInnerClasses(array);

        Set<String> classNames = innerClasses.keySet();
        assertEquals(2, classNames.size());
        assertTrue(classNames.contains(array.getName() + "$_Element_Setter"));
        assertTrue(classNames.contains(array.getName() + "$_Length_Setter"));

    }

    public void testInnerClassesFieldsAndMethods() throws Exception {

        Class array = gen.getArrayClass();
        Map<String, Class> innerClasses = getInnerClasses(array);

        // --- Element Setter ---
        Class<?> elementSetter = innerClasses.get(array.getName()
                + "$_Element_Setter");
        assertNotNull(elementSetter);

        Class ELsuper = elementSetter.getSuperclass();
        assertEquals(Setter.class, ELsuper);

        Field EL_this = elementSetter.getDeclaredField("_this");
        assertEquals(array, EL_this.getType());

        Field ELposition = elementSetter.getDeclaredField("position");
        assertEquals(int.class, ELposition.getType());

        Constructor ELconstr = elementSetter.getConstructor(new Class[] {
                array, int.class });
        assertNotNull(ELconstr);

        Class[] params = new Class[] { (componentClass.isPrimitive() ? componentClass
                : Object.class) };
        Method ELset = elementSetter.getDeclaredMethod("set", params);
        assertEquals(void.class, ELset.getReturnType());

        // --- Length Setter ---
        Class<?> lengthSetter = innerClasses.get(array.getName()
                + "$_Length_Setter");
        assertNotNull(lengthSetter);

        Class Lsuper = lengthSetter.getSuperclass();
        assertEquals(Setter.class, Lsuper);

        Field L_this = lengthSetter.getDeclaredField("_this");
        assertEquals(array, L_this.getType());

        Constructor Lconstr = lengthSetter.getConstructor(new Class[] { array });
        assertNotNull(Lconstr);

        Method Lset = lengthSetter.getDeclaredMethod("set",
                new Class[] { int.class });
        assertEquals(void.class, Lset.getReturnType());

    }

}

