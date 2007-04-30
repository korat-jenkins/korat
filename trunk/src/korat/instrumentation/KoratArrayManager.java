package korat.instrumentation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import korat.testing.ITester;
import korat.testing.impl.TestCradle;
import korat.utils.ReflectionUtils;

/**
 * Class KoratArrayManager provides some helper methods for creation and manipulation
 * of automatically generated array classes and objects
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public final class KoratArrayManager {

    /**
     * creation of instances not allowed
     */
    private KoratArrayManager() {
    }

    /**
     * Creates KorratArray_<b>Type</b> class based on existing array type. <b>Type</b> is
     * type of array components
     * 
     * @param arrayClass -
     *            array class for which corresponding KoratArray_<b>Type</b> will be
     *            generated
     * @return - Generated KoratArray_<b>Type</b> Class
     */
    public static Class createArrayClass(Class arrayClass) throws Exception {
        Class clz = new korat.instrumentation.ArrayGenerator(arrayClass).getArrayClass();

        //COMPAT1.4
        //clz = TestCradle.getInstance().getClassLoader().loadClass(
        //          clz.getCanonicalName());
        clz = TestCradle.getInstance().getClassLoader().loadClass(
                clz.getName());        
        return clz;
    }

    /**
     * Creates instance of koratArrayClz
     * 
     * @param koratArrayClz -
     *            array class for which corresponding KoratArray_<b>Type</b> will be
     *            generated
     * @return - Generated KoratArray_<b>Type</b> Class
     */
    public static IKoratArray createArray(Class<?> koratArrayClz, int size)
            throws Exception {
        Constructor c = koratArrayClz.getConstructor(new Class[] { int.class });
        IKoratArray ret = (IKoratArray) c.newInstance(new Object[] { size });
        return ret;
    }

    /**
     * Creates instance of KorratArray_<b>Type</b> class based on given array
     * component type. <b>Type</b> is type of array components
     * 
     * @param elementType -
     *            component class for which corresponding KoratArray_<b>Type</b>
     *            will be generated
     * @param size -
     *            maximal size of KoratArray_<b>Type</b>
     * @param iselemtype -
     *            any value
     * @return - Object of generated KoratArray_<b>Type</b> Class
     */
    public static IKoratArray createArray(Class<?> elementType, int size,
            int iselemtype) throws Exception {
        Class<?> arrayType;
        try {
            arrayType = Class.forName(elementType.getName() + "[]");
        } catch (ClassNotFoundException e) {
            // should never reach this point
            throw new RuntimeException(e);
        }
        return createArray(arrayType, size);
    }
    
    public static void initializeArray(IKoratArray array, ITester tcListener) {
        array.initialize(tcListener);   
    }
   /**
    *   Returns ElementSetter
    */
    public static Setter getElementSetter(Object array, int elemIndex,
            int elem_id) {  
        return ((IKoratArray)array).get_element_setter(elemIndex, elem_id);
    }

    /**
     * Returns length setter
     */
    public static Setter getLengthSetter(Object array, int len_id) {
        return ((IKoratArray)array).get_length_setter(len_id);
    }

    public static Object[] getArrayValues(Object koratArray) {
        IKoratArray array = (IKoratArray) koratArray;
        int len = array.getLength();
        Object [] ret = new Object[len];
        for (int i = 0; i <len; i++)
            ret[i] =array.getValue(i);
        return ret;
    }
    
    /**
     * @param obj - object that contains field of the class name
     * @param name - name of the array field
     * @return  values of the array as an Object array. For primitive types, automatic
     *          wrapper classes are returned as array elements.
     */
    public static Object[] getArrayValues(Object obj, String name) {
        Object[] objs = new Object[0];
        try {
            
            String koratArrayFieldName;
            if (obj instanceof IKoratArray)
                koratArrayFieldName = name;
            else 
                koratArrayFieldName = InstrumentationManager.getKoratArrayFieldName(name);
            
            Field koratArrayField = ReflectionUtils.getField(obj.getClass(), koratArrayFieldName);            
            
            IKoratArray koratArrayObj;
            if (obj instanceof IKoratArray) 
                koratArrayObj = (IKoratArray) obj;
            else
                koratArrayObj = (IKoratArray) koratArrayField.get(obj);

            if (koratArrayObj == null)
                throw new RuntimeException("Error. KoratArray object cannot be null");
            
            return getArrayValues(koratArrayObj);
            
        } catch (Exception e) {
            System.err.println("Cannot get korat array values for object " 
                    + obj + ", and field name " + name);
        }
        return objs;
    }

    public static int getArrayLength(Object koratArrayObj) {
        return ((IKoratArray)koratArrayObj).getLength();
    }

}
