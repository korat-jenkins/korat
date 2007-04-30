package korat.utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class ReflectionUtils {

    static Map<Class, Map<String, Field>> cache = new HashMap<Class, Map<String, Field>>();

    public static Field getFieldWithAccess(Object obj, String fieldName) {
        return getFieldWithAccess(obj.getClass(), fieldName);
    }

    public static Field getFieldWithAccess(Class cls, String fieldName) {
        Field f = getField(cls, fieldName);
        f.setAccessible(true);
        return f;
    }

    /**
     * Gets field from class cls, either declared in cls or in one
     * of its superclasses
     * 
     * @param cls - declaring class of the field 
     * @param fieldName - name of the field
     * @return requested field
     */
    public static Field getField(Class cls, String fieldName) {

        Field f = null;

        Map<String, Field> inner = cache.get(cls);
        if (inner != null) {
            f = inner.get(fieldName);
            if (f != null)
                return f;
        } else {
            inner = new HashMap<String, Field>();
            cache.put(cls, inner);
        }

        try {
            while (cls != null) {
                try {
                    f = cls.getDeclaredField(fieldName);
                    f.setAccessible(true);
                } catch (NoSuchFieldException e) {
                    f = null;
                }
                if (f != null)
                    break;
                cls = cls.getSuperclass();
            }

        } catch (SecurityException e) {
            throw new RuntimeException("SecurityException", e);
        }

        if (f == null)
            throw new RuntimeException("NoSuchFieldException");

        inner.put(fieldName, f);

        return f;
    }

    public static Object getFieldValue(Object obj, Field field) {

        try {

            return field.get(obj);

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return null;

    }

    public static void setFieldValue(Object obj, Field field, Object value) {
        field.setAccessible(true);
        try {
            field.set(obj, value);
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("IllegalArgumentException", e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("IllegalAccessExceptione", e);
        }
    }

    private static void getAllFieldsRecursive(Class clz, List<Field> fldLst) {

        if (clz.isPrimitive())
            return;

        for (Field f : clz.getDeclaredFields()) {
            f.setAccessible(true);
            fldLst.add(f);
        }

        if (clz.getSuperclass() == Object.class)
            return;
        else
            getAllFieldsRecursive(clz.getSuperclass(), fldLst);

    }

    public static Field[] getAllFields(Class clz) {

        ArrayList<Field> fieldList = new ArrayList<Field>();
        getAllFieldsRecursive(clz, fieldList);

        if (fieldList.isEmpty())
            return new Field[0];
        else
            return fieldList.toArray(new Field[0]);

    }

    public static Field[] getAllNonStaticFields(Class clz) {

        List<Field> fList = new ArrayList<Field>();
        getAllFieldsRecursive(clz, fList);

        List<Field> nonStaticFields = new LinkedList<Field>();
        for (Field f : fList) {
            if ((f.getModifiers() & Modifier.STATIC) == 0) {
                nonStaticFields.add(f);
            }
        }

        if (nonStaticFields.isEmpty())
            return new Field[0];
        else
            return nonStaticFields.toArray(new Field[0]);

    }

    public static Field[] getDeclaredNonStaticFields(Class clz) {

        List<Field> nonStaticFields = new LinkedList<Field>();
        for (Field f : clz.getDeclaredFields()) {
            if (!Modifier.isStatic(f.getModifiers())) {
                f.setAccessible(true);
                nonStaticFields.add(f);
            }
        }

        if (nonStaticFields.isEmpty())
            return new Field[0];
        else
            return nonStaticFields.toArray(new Field[0]);

    }

    /**
     * Gets method from class <code>clz</code> or any of its superclasses. If no method can be found 
     * <code>NoSuchMethodException</code> is raised.
     * 
     * @param clz - declaring class of the method
     * @param methodName - name of the method
     * @param methodArgs - method arguments
     * @return requested method
     * @throws NoSuchMethodException
     */
    public static Method getMethod(Class<? extends Object> clz,
            String methodName, Class[] methodArgs) throws NoSuchMethodException {

        if (clz == null)
            throw new NoSuchMethodException(methodName + "(" + methodArgs
                    + ") method does not exist ");

        try {

            return clz.getDeclaredMethod(methodName, methodArgs);

        } catch (NoSuchMethodException e) {

            return getMethod(clz.getSuperclass(), methodName, methodArgs);

        }
    }

    /**
     * <p>Invokes method of a given obj and set of parameters</p>
     * 
     * @param obj - Object for which the method is going to be executed. Special
     *          if obj is of type java.lang.Class, method is executed as static method
     *          of class given as obj parameter
     * @param method - name of the object
     * @param params - actual parameters for the method
     * @return - result of the method execution
     * 
     * <p>If there is any problem with method invocation, a RuntimeException is thrown</p>
     */
    public static Object invoke(Object obj, String method, Class[] params, Object[] args) {
        try {
            Class<?> clazz = null; 
            if (obj instanceof Class) {
                clazz = (Class) obj;
            } else { 
                clazz = obj.getClass();
            }
            Method methods = clazz.getMethod(method, params);
            if (obj instanceof Class) {
                return methods.invoke(null, args);
            } else {
                return methods.invoke(obj, args);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
    }
}