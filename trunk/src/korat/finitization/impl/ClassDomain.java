package korat.finitization.impl;

import java.lang.reflect.Constructor;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import korat.finitization.IClassDomain;
import korat.instrumentation.IKoratArray;
import korat.testing.ITester;
import korat.testing.impl.TestCradle;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class ClassDomain implements IClassDomain {
    private final Class<?> classOfObjects;

    private List<Object> objects;

    protected boolean initialized = false;

    int numOfObjects;

    protected Constructor constructor;

    protected Object[] params;

    private boolean checkObjIndex(int ind) {
        if (ind < 0 || ind >= objects.size())
            return false;

        return true;
    }

    /**
     * 
     * @param classOfObjects -
     *            type of objects, has to be a Class object of a class
     * @param numOfObjects -
     *            number of objects of classOfObject type to be created
     */
    ClassDomain(Class<?> classOfObjects, int numOfObjects) {

        objects = new ArrayList<Object>();
        this.numOfObjects = numOfObjects;
        this.classOfObjects = classOfObjects;
        this.isomorphismCheck = defaultIsomorphismCheck(classOfObjects);

        if (classOfObjects.isInterface() || classOfObjects.isPrimitive())

            throw new IllegalArgumentException(
                    "classOfObject parameter must be Class object of a class");

    }

    /**
     * Helper.
     * 
     * @param classOfObjectsName -
     *            fully qualified name of the Class of the objects
     * @param numOfObjects -
     *            number of objects to be created
     * @throws ClassNotFoundException -
     *             throws if classOfObjectName is not valid
     */
    ClassDomain(String classOfObjectsName, int numOfObjects)
            throws ClassNotFoundException {

        this(Class.forName(classOfObjectsName, false,
                Finitization.getClassLoader()), numOfObjects);
    }

    ClassDomain(Class<?> classOfObjects) {
        this.classOfObjects = classOfObjects;
        this.isomorphismCheck = defaultIsomorphismCheck(classOfObjects);
    }

    private boolean defaultIsomorphismCheck(Class<?> clazz) {
        return clazz != null && !clazz.isPrimitive();
    }

    public Class<?> getClassOfObjects() {
        return classOfObjects;
    }

    public String getClassNameOfObjects() {
        return classOfObjects.getName();
    }

    public int getSize() {
        if (initialized)
            return objects.size();
        else
            return objects.size() + numOfObjects;
    }
    
    /**
     * Creates all uncreated objects. Constructor used for creation is 
     * <code>className(ITester interface);</code> constructor 
     * 
     * <p/> Size and other properties of class domain should be set before
     * calling this method, through constructor or some specialized interface.
     * 
     */
    public void initialize() {

        if (initialized)
            return;
        initialized = true;
        
        ITester tester = TestCradle.getInstance();

        //TODO:: generalize for other object types
        if (IKoratArray.class.isAssignableFrom(classOfObjects))
            for (Object o : objects) {
                ((IKoratArray)o).initialize(tester);                
                
            }
        for (int i = 0; i < numOfObjects; i++) {

            Object obj = null;
            try {

                Constructor<?> ctor = classOfObjects.getConstructor(ITester.class);
                obj = ctor.newInstance(tester);
                objects.add(obj);

            } catch (Exception e) {

                String msg = "{0}: object of class {1} cannot be created";
                msg = MessageFormat.format(msg, e.getMessage(),
                        classOfObjects.getName());
                System.out.println(msg);
                e.printStackTrace();
                throw new RuntimeException(msg, e);

            }

        }

        //TODO:: Initialize objects which are passed by reference!!!      

        initialized = true;

    }

    public boolean isInitialized() {
        return initialized;
    }

    /**
     * Returns object stored at a given position inside the class domain
     * 
     */
    public Object getObject(int index) {
        if (!isInitialized())
            initialize();

        if (!checkObjIndex(index)){
            String msg = "Index " + index
                    + " is out of bounds for Class Domain "
                    + classOfObjects.getName();
            throw new IndexOutOfBoundsException(msg);
        }

        return objects.get(index);
    }

    /**
     * 
     * @return list of all objects in this class domain
     */
    public List<Object> getObjects() {
        if (!isInitialized())
            initialize();

        return objects;
    }

    /**
     * Returns index of object <code>obj</code> in this class domain
     * 
     */
    public int getIndexOf(Object obj) {
        if (!isInitialized())
            initialize();

        return objects.indexOf(obj);
    }
    
    /**
     * Checks whether the object <code>obj</code> is the member of this class domain
     * 
     */
    public boolean contains(Object obj) {
        if (!isInitialized())
            initialize();

        return objects.contains(obj);
    }

    public boolean equals(Object other) {
        
        if (!(other instanceof ClassDomain))
            return false;

        ClassDomain icd = (ClassDomain) other;

        if (classOfObjects == null)
            return (icd.getClassOfObjects() == null);

        return classOfObjects == icd.getClassOfObjects();
        
    }

    public int hashCode() {
        return getClassNameOfObjects().hashCode();
    }

    private boolean isomorphismCheck;

    public void includeInIsomorphismCheck(boolean include) {
        isomorphismCheck = include;
    }

    public boolean isIncludedInIsomorphismChecking() {
        return isomorphismCheck;
    }

    public void addObject(Object o) {       
        assert (o != null);
        if (classOfObjects.isAssignableFrom(o.getClass()))
            objects.add(o);
    }

    public void addObjects(Object[] objs) {
        assert (objs != null);
        for (int i = 0; i < objs.length; i++) {
            addObject(objs[i]);
        }
    }

    public void addObjects(Collection col) {
        assert (col != null);
        for (Object obj : col) {
            addObject(obj);
        }
    }
    
    public String toString() {
        return classOfObjects.getName() + ":" + getSize();
    }

}
