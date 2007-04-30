package korat.instrumentation.test;

import static korat.utils.ReflectionUtils.getFieldWithAccess;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;

import junit.framework.TestCase;
import korat.instrumentation.ArrayGenerator;
import korat.instrumentation.IKoratArray;
import korat.instrumentation.Setter;
import korat.testing.ITester;
public class TypeArrayGenerator_DynamicTest extends TestCase {
    
    private static class DtTester implements ITester {

        LinkedList<Integer> accessedFields = new LinkedList<Integer>();
        
        public void notifyFieldAccess(int accessedFieldIndex) {
            accessedFields.add(accessedFieldIndex);
        }
        
        public int [] getAccessedList() {
            int [] ret = new int[accessedFields.size()];
            for (int i = 0; i < ret.length; i++) {
                ret[i] = accessedFields.get(i);
            }
            return ret;
        }
        
        public void startFieldTrace() {
            accessedFields.clear();
        }

        //unused
        public void stopFieldTrace() {
        }

        public void continueFieldTrace() {
        }

        public void notifyFieldAccess(Object obj, String field) {
        }

        
    }

    IKoratArray array;

    int maxArraySize;

    Constructor arrayConstr;

    Method get;

    Method set;

    Method getLength;

    Method setLength;

    Method get_element_setter;

    Method get_length_setter;
    
    Method initialize;
    
    Field tester;
    
    Field values;

    Field values_ids;

    Field length;

    Field length_id;

    Class arrayClass;

    Class componentClass;
    
    DtTester myTester;

    public TypeArrayGenerator_DynamicTest() throws Exception {
        //this(int[].class, 3);
        this(Character[].class, 3);
    }

    public TypeArrayGenerator_DynamicTest(Class cls, int maxSize)
            throws Exception {
        assertTrue("The tested class must be array", cls.isArray());
        ArrayGenerator gen = new ArrayGenerator(cls);
        maxArraySize = maxSize;
        arrayClass = cls;
        componentClass = cls.getComponentType();

        Class<?> ka = gen.getArrayClass();

        arrayConstr = ka.getConstructor(new Class[] { int.class });
        initialize = ka.getDeclaredMethod("initialize", new Class[]{ITester.class});        
        
        get = ka.getDeclaredMethod("get", new Class[] { int.class });
        set = ka.getDeclaredMethod("set", new Class[] { int.class,
                componentClass });
        getLength = ka.getDeclaredMethod("getLength", new Class[0]);
        setLength = ka.getDeclaredMethod("setLength", new Class[] { int.class });
        get_element_setter = ka.getDeclaredMethod("get_element_setter",
                new Class[] { int.class, int.class });
        get_length_setter = ka.getDeclaredMethod("get_length_setter",
                new Class[] { int.class });

        tester = getFieldWithAccess(ka, "tester");
        values = getFieldWithAccess(ka, "values");
        values_ids = getFieldWithAccess(ka, "values_ids");
        length = getFieldWithAccess(ka, "length");
        length_id = getFieldWithAccess(ka, "length_id");

    }
    
    public void setUp() {
        try {
            array = (IKoratArray) arrayConstr.newInstance(new Object[] { maxArraySize });
            myTester = new DtTester();
            initialize.invoke(array, myTester);
        } catch (Exception e) {
            e.printStackTrace();
            fail("Cannot create array object");
        }
    }
    
    public void testArray_initialized() throws Exception {
        
        assertEquals(maxArraySize, ((Object[])values.get(array)).length);
        assertEquals(myTester, tester.get(array));
        
    }

    public void testArray_SetGetMethods() throws Exception {

        Object tmp;

        for (int i = 0; i < maxArraySize; i++) {

            set.invoke(array, new Object[] { i, 'A' });

            // Do class types agree
            tmp = values.get(array);
            assertEquals(tmp.getClass(), arrayClass);

            // Is the value stored
            Object[] values = (Object[]) tmp;
            assertEquals('A', values[i]);

            // Does it get the value back
            Object value = get.invoke(array, new Object[] { new Integer(i) });
            assertNotNull(value);
            assertEquals('A', value);
            
        }
        
    }
    
    public void testArray_GetSetLength() throws Exception {
        int len = (Integer) getLength.invoke(array);
        assertEquals(3, len);

        setLength.invoke(array, new Object[] { 2 });
        len = (Integer) getLength.invoke(array);
        assertEquals("Size of length should be changed from the default value",
                2, len);
    }


    public void testArray_SetGetMethodsOutOfBound() throws Exception {
        
        int size = 1;
        setLength.invoke(array, new Object[] {size} );
        assertEquals(1, getLength.invoke(array, new Object[0]));
        
        
        try {
            set.invoke(array, new Object[] { size , 'A' });
        } catch (Throwable t) {
            
            assertTrue("Exception type is IndexOutOfBoundException",
                    t.getCause() instanceof IndexOutOfBoundsException);
            try {
                get.invoke(array, new Object[] { size });
            } catch (Throwable tt) {
                
                assertTrue("Exception type is IndexOutOfBoundException",
                        t.getCause() instanceof IndexOutOfBoundsException);
                return;
                
            }

            fail("Getting/Setting object value out of bounds is not allowed");
        }
        fail("Setting object value out of bounds is not allowed");

    }

    public void testArray_testFieldAccesses() throws Exception {
        
        int len_id = length_id.getInt(array);
        int[] values_id = (int[]) values_ids.get(array);
        
        int len = (Integer) getLength.invoke(array, new Object[0] );
        for (int i = 0; i < len; i++)
            get.invoke(array, new Object[] { i });
        
        int[] accList = myTester.getAccessedList();
        assertEquals(
                "Number of accessed fields is equal to the number of array elements plus 1 for length field ",
                1 + len, accList.length);
        assertEquals("Length field was accessed first", len_id, accList[0]);
        for (int i = 1; i < values_id.length; i++)
            assertEquals("Array elements are accessed in increased order",
                    values_id[i], accList[i + 1]);
        
        
    }
    
    public void testArray_TestSetters() throws Exception {

        int len = (Integer) getLength.invoke(array, new Object[0] );

        int len_field_id = -1;
        Setter lengthSetter = (Setter) get_length_setter.invoke(
                array, new Object[] { len_field_id });
        
        Setter [] elementSetter = new Setter[len];
        for (int i = 0; i < len; i++) {
            int position = i;
            int field_id = -1;
            elementSetter[i] = (Setter) get_element_setter.invoke(
                    array, new Object[] { position, field_id });
        }
       
        assertEquals(maxArraySize, length.getInt(array));
        int newArraySize = maxArraySize-1;
        lengthSetter.set(newArraySize);        
        assertEquals(newArraySize, length.getInt(array));
        
        lengthSetter.set(maxArraySize);        
        assertEquals(maxArraySize, length.getInt(array));
        
        for (int i = 0; i < elementSetter.length; i++) {
            
            assertEquals(null, get.invoke(array, new Object[]{ i }));
            Object newValue = 'X';
            elementSetter[i].set(newValue);
            assertEquals(newValue, get.invoke(array, new Object[]{ i }));
            
        }
    }

}
