package korat.finitization.impl;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import korat.finitization.IArraySet;
import korat.finitization.IBooleanSet;
import korat.finitization.IByteSet;
import korat.finitization.IClassDomain;
import korat.finitization.IDoubleSet;
import korat.finitization.IFieldDomain;
import korat.finitization.IFinitization;
import korat.finitization.IFloatSet;
import korat.finitization.IIntSet;
import korat.finitization.ILongSet;
import korat.finitization.IObjSet;
import korat.finitization.IShortSet;
import korat.instrumentation.IKoratArray;
import korat.instrumentation.InstrumentationManager;
import korat.instrumentation.KoratArrayManager;
import korat.utils.ReflectionUtils;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 */
public class Finitization implements IFinitization {

    private Class rootClass;

    private ClassDomain rootClassDomain;

    private Map<IClassDomain, Map<String, IFieldDomain>> clsDomainsMap;

    private Map<Class<?>, IClassDomain> classDomains;

    private StateSpace stateSpace;
    
    private boolean handleArraysAsObjects;

    private List<Finitization> includedFinitizations;

    protected static ClassLoader classLoader;

    static {
        classLoader = Finitization.class.getClassLoader();
    }

    public static void setClassLoader(ClassLoader classLoader2) {
        classLoader = classLoader2;
    }

    public static ClassLoader getClassLoader() {
        return classLoader;
    }

    public Finitization(Class myClass) {

        this.rootClass = myClass;
        this.rootClassDomain = new ClassDomain(myClass, 1);
        this.clsDomainsMap = new LinkedHashMap<IClassDomain, Map<String, IFieldDomain>>();
        this.classDomains = new LinkedHashMap<Class<?>, IClassDomain>();

        putClsDomainsMap(rootClassDomain,
                new LinkedHashMap<String, IFieldDomain>());

        this.includedFinitizations = new ArrayList<Finitization>();

    }

    private Class getClassFromName(String className) {

        try {
            Class cls = null;
            try {
                // first chance - treat className as full class name
                cls = Class.forName(className, false,
                        Finitization.getClassLoader());
            } catch (ClassNotFoundException e) {
                // second chance - add the package of the root class to
                // the full class name
                String myClassName = rootClass.getName();
                int lastDotPos = myClassName.lastIndexOf(".");
                String pack = "";
                // class does not belong to the default package
                if (lastDotPos != -1)
                    pack = myClassName.substring(0, lastDotPos) + ".";
                String fullClassName = pack + className;
                try {
                    cls = Class.forName(fullClassName, false,
                            Finitization.getClassLoader());
                } catch (ClassNotFoundException e2) {
                    // third chance - threat class as the inner class
                    // of the root class
                    fullClassName = myClassName + "$" + className;
                    cls = Class.forName(fullClassName, false,
                            Finitization.getClassLoader());
                }
            }

            return cls;
        } catch (ClassNotFoundException e) {
            String msg = "Class #className# doesn't exist.";
            msg = msg.replaceAll("#className#", className);

            throw new RuntimeException(msg);
        }

    }

    private String parseClassName(String fullFieldName) {
        String className;
        int i = fullFieldName.lastIndexOf(".");
        if (i == -1)
            className = rootClass.getName();
        else
            className = fullFieldName.substring(0, i);

        return className;
    }

    private String parseFieldName(String fullFieldName) {

        String fieldName;
        int i = fullFieldName.lastIndexOf(".");
        if (i == -1)
            fieldName = fullFieldName;
        else
            fieldName = fullFieldName.substring(i + 1);

        return fieldName;

    }

    private Map<String, IFieldDomain> putClsDomainsMap(IClassDomain cd,
            Map<String, IFieldDomain> mfd) {
        classDomains.put(cd.getClassOfObjects(), cd);
        return clsDomainsMap.put(cd, mfd);
    }

    private void initializeClassDomains() {

        for (IClassDomain icd : clsDomainsMap.keySet()) {
            ClassDomain cd = (ClassDomain) icd;
            cd.initialize();
            Class cls = cd.getClassOfObjects();

            // TODO: Check!
            Class superCls = cls.getSuperclass();
            while (superCls != null) {
                IClassDomain superDomain = getClassDomain(superCls);
                if (superDomain != null) {
                    superDomain.addObjects(cd.getObjects());
                }
                superCls = superCls.getSuperclass();
            }
        }

    }

    private void appendFields(List<CVElem> fieldsList, Object obj,
            Map<String, IFieldDomain> fieldsMap) {

        for (String fieldName : fieldsMap.keySet()) {
            IFieldDomain fd = fieldsMap.get(fieldName);
            CVElem elem = null;

            if (obj instanceof IKoratArray || obj.getClass().isArray()) {
                
                elem = ArrayElementCVElem.create(obj, fieldName,
                        (FieldDomain) fd, stateSpace);
                
            } else if (fd.isArrayType()) {
                
                elem = CVElem.create(obj, fieldName, (FieldDomain) fd,
                        stateSpace);
                elem.setExcludeFromSearch(!handleArraysAsObjects);
                
            } else {
                
                elem = CVElem.create(obj, fieldName, (FieldDomain) fd,
                        stateSpace);
                
            }

            fieldsList.add(elem);
        }

    }

    private void appendClassDomain(List<CVElem> list, ClassDomain cd) {

        for (int i = 0; i < cd.getSize(); i++) {
            Object obj = cd.getObject(i);
            Map<String, IFieldDomain> fieldsMap = clsDomainsMap.get(cd);
            appendFields(list, obj, fieldsMap);
        }

    }

    private void createStateSpace() {

        stateSpace = new StateSpace(); // new CachedStateSpace();
        List<CVElem> candidates = new LinkedList<CVElem>();

        for (IClassDomain cd : clsDomainsMap.keySet()) {
            appendClassDomain(candidates, (ClassDomain) cd);
        }

        stateSpace.setStructureList(candidates.toArray(new CVElem[0]));
        ClassDomain cd = rootClassDomain;
        stateSpace.setRootObject(cd.getObject(0));

    }

    private void initializeFieldSetters() {
        if (stateSpace == null)
            throw new RuntimeException("Cannot execute this method!");

        stateSpace.initialize();

    }
    
    public boolean areArraysHandledAsObjects() {
        return handleArraysAsObjects;
    }

    public void handleArraysAsObjects(boolean handleAsObjects) {
        this.handleArraysAsObjects = handleAsObjects;
    }

    public Class getFinClass() {
        return rootClass;
    }

    public IClassDomain createClassDomain(String className, int numOfInstances) {
        Class cls = getClassFromName(className);
        return createClassDomain(cls, numOfInstances);
    }

    public IClassDomain createClassDomain(Class cls, int numOfInstances) {

        ClassDomain cd = getClassDomain(cls);

        if (cd != null) {

            // HACK:: there are objects inserted with add()
            if (cd.getSize() != cd.numOfObjects)
                throw new UnsupportedOperationException(
                        "This is not yet implemented");

            if (cd.numOfObjects < numOfInstances)
                cd.numOfObjects = numOfInstances;

        } else {
            cd = new ClassDomain(cls, numOfInstances);

            putClsDomainsMap(cd, new LinkedHashMap<String, IFieldDomain>());
            // DELETE:
            // classDomains.put(cls, cd);
            // clsDomainsMap.put(cd, new LinkedHashMap<String, IFieldDomain>());
        }

        return cd;

    }

    public IBooleanSet createBooleanSet() {
        return new BooleanSet();
    }

    public IIntSet createIntSet(int min, int diff, int max) {
        return new IntSet(min, diff, max);
    }

    public IIntSet createIntSet(int min, int max) {
        return new IntSet(min, max);
    }

    public IIntSet createIntSet(int singleValue) {
        return new IntSet(singleValue);
    }

    public IByteSet createByteSet(byte min, byte diff, byte max) {
        return new ByteSet(min, diff, max);
    }

    public IByteSet createByteSet(byte min, byte max) {
        return new ByteSet(min, max);
    }

    public IByteSet createByteSet(byte singleValue) {
        return new ByteSet(singleValue);
    }

    public IDoubleSet createDoubleSet(double min, double diff, double max) {
        return new DoubleSet(min, diff, max);
    }

    public IDoubleSet createDoubleSet(double min, double max) {
        return new DoubleSet(min, max);
    }

    public IDoubleSet createDoubleSet(double singleValue) {
        return new DoubleSet(singleValue);
    }

    public IFloatSet createFloatSet(float min, float diff, float max) {
        return new FloatSet(min, diff, max);
    }

    public IFloatSet createFloatSet(float min, float max) {
        return new FloatSet(min, max);
    }

    public IFloatSet createFloatSet(float singleValue) {
        return new FloatSet(singleValue);
    }

    public ILongSet createLongSet(long min, long diff, long max) {
        return new LongSet(min, diff, max);
    }

    public ILongSet createLongSet(long min, long max) {
        return new LongSet(min, max);
    }

    public ILongSet createLongSet(long singleValue) {
        return new LongSet(singleValue);
    }

    public IShortSet createShortSet(short min, short diff, short max) {
        return new ShortSet(min, diff, max);
    }

    public IShortSet createShortSet(short min, short max) {
        return new ShortSet(min, max);
    }

    public IShortSet createShortSet(short singleValue) {
        return new ShortSet(singleValue);
    }

    public IObjSet createObjSet(Class fieldBaseClass, boolean includeNull) {

        IObjSet oset = null;
        if (fieldBaseClass.isPrimitive())
            throw new RuntimeException(
                    "Cannot create ObjSet with primitive type as fieldBaseClass");
        else {
            oset = new ObjSet(fieldBaseClass);
            oset.setNullAllowed(includeNull);
        }

        return oset;

    }

    public IObjSet createObjSet(Class fieldBaseClass) {
        return createObjSet(fieldBaseClass, false);
    }

    public IObjSet createObjSet(String fieldBaseClassName, boolean includeNull) {
        Class cls = getClassFromName(fieldBaseClassName);
        return createObjSet(cls, includeNull);
    }

    public IObjSet createObjSet(String fieldBaseClassName) {
        return createObjSet(fieldBaseClassName, false);
    }

    public IFieldDomain createObjSet(IClassDomain classDomain) {
        return createObjSet(classDomain, false);
    }

    public IFieldDomain createObjSet(IClassDomain classDomain,
            boolean includeNull) {
        IObjSet ret = createObjSet(classDomain.getClassOfObjects());
        ret.addClassDomain(classDomain);
        ret.setNullAllowed(includeNull);
        return ret;
    }

    public void set(Class cls, String fieldName, IFieldDomain fieldDomain) {

        // getField() checks if the field exists. If not, RuntimeException is
        // thrown.
        // Check is valid only for non-KoratArray domains
        if (!(IKoratArray.class.isAssignableFrom(cls)))
            ReflectionUtils.getField(cls, fieldName);

        ClassDomain classDomain = (ClassDomain) classDomains.get(cls);
        if (classDomain == null) {
            classDomain = (ClassDomain) createClassDomain(cls);
        }
        Map<String, IFieldDomain> fieldsMap = clsDomainsMap.get(classDomain);
        if (fieldsMap == null) {
            throw new RuntimeException(
                    "Fields Map should be initialized in addClassDomain");
        }
        if (fieldsMap.containsKey(fieldName)) {
            String msg = "Field domain has already been set for th field #fName#.";
            msg = msg.replaceAll("#fName#", fieldName);
            throw new RuntimeException(msg);
        }

        fieldsMap.put(fieldName, fieldDomain);

        // ---------------------
        // Arrays stuff
        // ---------------------
        if (fieldDomain.isArrayType()) {
            ArraySet aset = (ArraySet) fieldDomain;

            IClassDomain cd = aset.getClassDomain(0);
            fieldsMap = new LinkedHashMap<String, IFieldDomain>();
            putClsDomainsMap(cd, fieldsMap);
            IFieldDomain componentsSet = aset.getComponentsSet();
            fieldsMap.put("length", aset.getArraySizes());
            if (componentsSet.isArrayType()) {
                ArraySet compSet = (ArraySet) componentsSet;
                set(compSet.getClassOfField(), "length",
                        compSet.getArraySizes());
            }

            for (int i = 0; i < aset.getMaxLength(); i++) {

                String arrayElemFieldName = Integer.toString(i);
                fieldsMap.put(arrayElemFieldName, componentsSet);

                if (componentsSet.isArrayType()) {
                    ArraySet compSet = (ArraySet) componentsSet;
                    set(compSet.getClassOfField(), arrayElemFieldName,
                            compSet.getComponentsSet());
                }

            }

        }

    }

    public void set(String className, String fieldName, IFieldDomain fieldDomain) {

        Class cls = getClassFromName(className);
        set(cls, fieldName, fieldDomain);

    }

    public void set(String fullFieldName, IFieldDomain fieldDomain) {

        String fieldName = parseFieldName(fullFieldName);
        String className = parseClassName(fullFieldName);

        if (fieldDomain.isArrayType())
            fieldName = InstrumentationManager.getKoratArrayFieldName(fieldName);

        set(className, fieldName, fieldDomain);

    }

    public ClassDomain getClassDomain(Class cls) {

        return (ClassDomain) classDomains.get(cls);
    }

    public IClassDomain getClassDomain(String name) {

        Class cls = getClassFromName(name);
        return getClassDomain(cls);

    }

    public IFieldDomain getFieldDomain(Class cls, String fieldName) {

        IClassDomain cd = classDomains.get(cls);
        if (cd == null)
            return null;

        Map fieldsMap = (Map) clsDomainsMap.get(cd);
        if (fieldsMap == null)
            return null;

        return (IFieldDomain) fieldsMap.get(fieldName);

    }

    public IFieldDomain getFieldDomain(String className, String fieldName) {

        Class cls = getClassFromName(className);
        return getFieldDomain(cls, fieldName);

    }

    public IFieldDomain getFieldDomain(String fullFieldName) {

        String fieldName = parseFieldName(fullFieldName);
        String className = parseClassName(fullFieldName);
        return getFieldDomain(className, fieldName);

    }

    /**
     * Returns the <code>IStateSpace</code> according to the bounds of the
     * system
     * 
     */
    public StateSpace getStateSpace() {
        if (stateSpace == null)
            initialize();

        return stateSpace;
    }

    /**
     * Does all needed initialization. Korat search algorithm should call this
     * before getting <code>StateSpace</code>
     * 
     */
    public void initialize() {

        initializeClassDomains();
        createStateSpace();
        initializeFieldSetters();

    }

    public IArraySet createArraySet(Class clz, IIntSet array$length,
            IFieldDomain array$values, int count) {

        // TODO: check cls against array$values.getClassOf...

        ClassDomain arrays = null;
        IntSet _array$length = (IntSet) array$length;
        FieldDomain _array$values = (FieldDomain) array$values;

        if (!clz.isArray())
            throw new IllegalArgumentException("clz must be of array type");

        Class arrayClass = null;
        try {

            arrayClass = KoratArrayManager.createArrayClass(clz);
            arrays = (ClassDomain) createClassDomain(arrayClass);

            int maxSize = _array$length.getMax();

            for (int i = 0; i < count; i++)
                arrays.addObject(KoratArrayManager.createArray(arrayClass,
                        maxSize));

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        if (arrays == null)
            throw new RuntimeException("arrays class domain must be resolved");

        return new ArraySet(arrays, _array$length, _array$values);

    }

    public IClassDomain createClassDomain(String className) {
        return createClassDomain(className, 0);
    }

    public IClassDomain createClassDomain(Class cls) {
        return createClassDomain(cls, 0);
    }

    public IObjSet createObjSet(Class fieldBaseClass, int numOfInstances,
            boolean includeNull) {

        IObjSet ret = createObjSet(fieldBaseClass, includeNull);
        IClassDomain clz = createClassDomain(fieldBaseClass, numOfInstances);
        ret.addClassDomain(clz);

        return ret;

    }

    public IObjSet createObjSet(Class fieldBaseClass, int numOfInstances) {
        return createObjSet(fieldBaseClass, numOfInstances, false);
    }

    public IObjSet createObjSet(String fieldBaseClassName, int numOfInstances,
            boolean includeNull) {
        Class cls = getClassFromName(fieldBaseClassName);
        return createObjSet(cls, numOfInstances, includeNull);
    }

    public IObjSet createObjSet(String fieldBaseClassName, int numOfInstances) {
        return createObjSet(fieldBaseClassName, numOfInstances, false);
    }

    public void addAll(String fullFieldName, IObjSet objSet) {

        String fieldName = parseFieldName(fullFieldName);
        String className = parseClassName(fullFieldName);

        addAll(className, fieldName, objSet);

    }

    public void addAll(String className, String fieldName, IObjSet objSet) {

        Class cls = getClassFromName(className);
        addAll(cls, fieldName, objSet);

    }

    @SuppressWarnings("unchecked")
    public void addAll(Class cls, String fieldName, IObjSet objSet) {

        // checks if the field exists. If not, RuntimeException is thrown
        Field fld = ReflectionUtils.getField(cls, fieldName);
        if (!Collection.class.isAssignableFrom(fld.getType())) {
            throw new RuntimeException(
                    "Cannot execute addAll on field that is not a Collection");
        }

        try {

            ClassDomain cd = getClassDomain(cls);
            for (Object obj : cd.getObjects()) {
                Object fldVal = fld.get(obj);
                if (fldVal != null) {
                    Collection col = (Collection) fldVal;
                    for (Object o : objSet.getAllObjects()) {
                        col.add(o);
                    }
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public int[] getInitialCandidateVector() {

        int size = getStateSpace().getTotalNumberOfFields();
        int[] ret = new int[size];
        Set<ArraySet> visited = new HashSet<ArraySet>();

        for (int i = 0; i < size; i++) {
            CVElem c = getStateSpace().getCVElem(i);

            if (!(c.getFieldDomain() instanceof ArraySet))
                continue;
            
            if (!c.isExcludedFromSearch())
                continue;

            ArraySet as = (ArraySet) c.getFieldDomain();

            if (visited.add(as)) {
                int next = 0;
                ret[i] = next;
                for (int j = i + 1; j < size; j++) {
                    CVElem c2 = getStateSpace().getCVElem(j);
                    if (c2.getFieldDomain() == as && c2.isExcludedFromSearch()) {
                        ret[j] = ++next;
                    }
                }
                if (next > c.getFieldDomain().getNumberOfElements())
                    throw new RuntimeException("There are not enough arrays to initialize objects.");

            }
        }

        return ret;

    }

    public boolean includeFinitization(IFinitization ifin) {

        if (getIncludedFinitization(ifin.getFinClass()) != null)
            return false;

        Finitization fin = (Finitization) ifin;
        includedFinitizations.add(fin);

        for (IClassDomain cd : fin.clsDomainsMap.keySet()) {

            Map<String, IFieldDomain> val = fin.clsDomainsMap.get(cd);

            if (this.clsDomainsMap.containsKey(cd))
                throw new UnsupportedOperationException("NOT SUPPORTED");
            else
                putClsDomainsMap(cd, val);

        }

        return true;

    }

    public IFinitization getIncludedFinitization(Class clazz) {

        for (int i = 0; i < includedFinitizations.size(); i++)
            if (includedFinitizations.get(i).getFinClass() == clazz)
                return includedFinitizations.get(i);
        return null;

    }

}
