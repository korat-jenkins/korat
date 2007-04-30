package korat.finitization;

/**
 * The <code>IFinitization</code> interface is used to set up the bounds
 * for the search.  The <code>Finitization</code> class uses these bounds
 * to build the state space for search.
 *
 * @author korat.team
 */
public interface IFinitization {

    /**
     * Returns <code>Class</code> object of the finitized class
     * 
     * @return <code>Class</code> object for root class that is being finitized
     */
    Class getFinClass();

    /**
     * Helper.
     * 
     * <p/> First, tries to find a <code>Class</code> object for the given
     * <code>className</code> and then calls
     * <code>createClassDomain(Class, int)</code>. <br/> Searches in the
     * package of the finitized class, or in the default package.
     * 
     * @param className
     *            fully qualified name of the class or relative to the package
     *            name of the finitized class.
     * @param numOfInstances
     *            number of elements to be created
     * @return created <code>IClassDomain</code>
     * @see #createClassDomain(Class, int)
     */
    IClassDomain createClassDomain(String className, int numOfInstances);

    /**
     * Equivalent to <code>createClassDomain(className)</code>
     * 
     * @see #createClassDomain(String, int)
     * @param className
     * @return newly created <code>IClassDomain</code>
     */
    IClassDomain createClassDomain(String className);

    /**
     * Creates new <code>ClassDomain</code> if the <code>ClassDomain</code>
     * with the given <code>Class</code> hasn't already been created or
     * returns existing <code>ClassDomain</code>, no matter if it's size is
     * different from the given parameter <code>numOfInstances</code>
     * 
     * @param cls
     *            <code>Class</code> of the objects
     * @param numOfInstances
     *            number of instances to be created
     * @return created <code>IClassDomain</code>
     * @see #createClassDomain(String, int)
     */
    IClassDomain createClassDomain(Class cls, int numOfInstances);

    /**
     * Creates new empty class domain
     * <p>
     * Equivalent to <code>createClassDomain(cls, 0);</code>
     * 
     * @param cls
     *            Class of the object
     * @return created class domain
     * @see #createClassDomain(Class, int)
     */
    IClassDomain createClassDomain(Class cls);

    /**
     * Creates <code>IIntSet</code> according to the given parameters.
     * 
     * @param min
     *            minimal element of this set of integers (included)
     * @param diff
     *            step, the difference between to elements
     * @param max
     *            maximal element of this set of integers (included)
     * @return created <code>IIntSet</code>
     * @see #createIntSet(int)
     * @see #createIntSet(int, int)
     */
    IIntSet createIntSet(int min, int diff, int max);

    /**
     * Creates <code>IIntSet</code> with the given <code>min</code> and
     * <code>max</code> parameters. The difference between two elements is by
     * default 1. Equivalent of <code>createIntSet(min, 1, max)</code>.
     * 
     * @param min
     *            minimal element of this set of integers (included)
     * @param max
     *            maximal element of this set of integers (included)
     * @return created <code>IIntSet</code>
     * @see #createIntSet(int)
     * @see #createIntSet(int, int, int)
     */
    IIntSet createIntSet(int min, int max);

    /**
     * Creates <code>IIntSet</code> with the single value that is given as the
     * method parameter. Equivalent of
     * <code>createIntSet(value, 1, value)</code>.
     * 
     * @param singleValue
     *            single value that will be added to <code>IIntSet</code>
     * @return created <code>IIntSet</code>
     * @see #createIntSet(int, int)
     * @see #createIntSet(int, int, int)
     */
    IIntSet createIntSet(int singleValue);

    /**
     * Creates new <code>IBooleanSet</code>
     * 
     * @return newly created <code>IBooleanSet</code>
     */
    IBooleanSet createBooleanSet();

    /**
     * @param min - minimum value to be included in the set
     * @param diff - difference (step) between two consecutive values in the set
     * @param max - maximum value to be included in the set
     * @return newly created <code>IByteSet</code>
     */
    IByteSet createByteSet(byte min, byte diff, byte max);

    /**
     * Here the step (differecte) between two consecutive values in the set
     * defaults to 1. 
     * 
     * @param min - minimum value to be included in the set
     * @param max - maximum value to be included in the set
     * @return newly created <code>IByteSet</code>
     */
    IByteSet createByteSet(byte min, byte max);

    /**
     * Creates <code>IByteSet</code> with the single value that is given as
     * the method parameter. Equivalent of
     * <code>createByteSet(value, 1, value)</code>.
     * 
     * @param singleValue
     *            single value that will be added to <code>IByteSet</code>
     * @return created <code>IByteSet</code>
     * @see #createByteSet(byte, byte)
     * @see #createByteSet(byte, byte, byte)
     */
    IByteSet createByteSet(byte singleValue);

    /**
     * @param min - minimum value to be included in the set
     * @param diff - difference (step) between two consecutive values in the set
     * @param max - maximum value to be included in the set
     * @return newly created <code>IShortSet</code>
     */
    IShortSet createShortSet(short min, short diff, short max);

    /**
     * Here the step (differecte) between two consecutive values in the set
     * defaults to 1. 
     * 
     * @param min - minimum value to be included in the set
     * @param max - maximum value to be included in the set
     * @return newly created <code>IShortSet</code>
     */
    IShortSet createShortSet(short min, short max);

    /**
     * Creates <code>IShortSet</code> with the single value that is given as
     * the method parameter. Equivalent of
     * <code>createShortSet(value, 1, value)</code>.
     * 
     * @param singleValue
     *            single value that will be added to <code>IShortSet</code>
     * @return created <code>IShortSet</code>
     * @see #createShortSet(short, short)
     * @see #createShortSet(short, short, short)
     */
    IShortSet createShortSet(short singleValue);

    /**
     * @param min - minimum value to be included in the set
     * @param diff - difference (step) between two consecutive values in the set
     * @param max - maximum value to be included in the set
     * @return newly created <code>ILongSet</code>
     */
    ILongSet createLongSet(long min, long diff, long max);

    /**
     * Here the step (differecte) between two consecutive values in the set
     * defaults to 1. 
     * 
     * @param min - minimum value to be included in the set
     * @param max - maximum value to be included in the set
     * @return newly created <code>ILongSet</code>
     */
    ILongSet createLongSet(long min, long max);

    /**
     * Creates <code>ILongSet</code> with the single value that is given as
     * the method parameter. Equivalent of
     * <code>createLongSet(value, 1, value)</code>.
     * 
     * @param singleValue
     *            single value that will be added to <code>ILongSet</code>
     * @return created <code>ILongSet</code>
     * @see #createLongSet(long, long)
     * @see #createLongSet(long, long, long)
     */
    ILongSet createLongSet(long singleValue);

    /**
     * @param min - minimum value to be included in the set
     * @param diff - difference (step) between two consecutive values in the set
     * @param max - maximum value to be included in the set
     * @return newly created <code>IDoubleSet</code>
     */
    IDoubleSet createDoubleSet(double min, double diff, double max);

    /**
     * Here the step (differecte) between two consecutive values in the set
     * defaults to 1. 
     * 
     * @param min - minimum value to be included in the set
     * @param max - maximum value to be included in the set
     * @return newly created <code>IDoubleSet</code>
     */
    IDoubleSet createDoubleSet(double min, double max);

    /**
     * Creates <code>IDoubleSet</code> with the single value that is given as
     * the method parameter. Equivalent of
     * <code>createDoubleSet(value, 1, value)</code>.
     * 
     * @param singleValue
     *            single value that will be added to <code>IDoubleSet</code>
     * @return created <code>IDoubletSet</code>
     * @see #createDoubleSet(double, double)
     * @see #createDoubleSet(double, double, double)
     */
    IDoubleSet createDoubleSet(double singleValue);

    /**
     * Here the step (differecte) between two consecutive values in the set
     * defaults to 1. 
     * 
     * @param min - minimum value to be included in the set
     * @param max - maximum value to be included in the set
     * @return newly created <code>IFloatSet</code>
     */
    IFloatSet createFloatSet(float min, float diff, float max);

    /**
     * Here the step (differecte) between two consecutive values in the set
     * defaults to 1. 
     * 
     * @param min - minimum value to be included in the set
     * @param max - maximum value to be included in the set
     * @return newly created <code>IFloatSet</code>
     */
    IFloatSet createFloatSet(float min, float max);

    /**
     * Creates <code>IFloatSet</code> with the single value that is given as
     * the method parameter. Equivalent of
     * <code>createFloatSet(value, 1, value)</code>.
     * 
     * @param singleValue
     *            single value that will be added to <code>IFloatSet</code>
     * @return created <code>IFloatSet</code>
     * @see #createFloatSet(float, float)
     * @see #createFloatSet(float, float, float)
     */
    IFloatSet createFloatSet(float singleValue);

    /**
     * Creates <code>IObjSet</code> according to the given parameters.
     * 
     * @param fieldBaseClass
     *            Base type of the field that is used in type checking. If
     *            <code>fieldBaseClass</code> is a <code>Class</code> object
     *            of a reference type, new <code>IObjSet</code> will be
     *            returned, else an exception will be thrown.
     * 
     * @param includeNull
     *            whether to include null in the <code>IObjSet</code>.
     * 
     * @return - created <code>IObjSet</code>
     * @see #createObjSet(Class)
     * @see #createObjSet(String)
     * @see #createObjSet(String, boolean)
     */
    IObjSet createObjSet(Class fieldBaseClass, boolean includeNull);

    /**
     * Creates <code>IObjSet</code> and automatically creates given number of
     * instances of the same class
     * 
     * <p>
     * Equivalent to <br>
     * 
     * <pre>
     * IClassDomain c = f.createClassDomain(fieldBaseClass, numOfInstances);
     * IObjSet toReturn = f.createObjSet(fieldBaseClass, includeNull);
     * toReturn.addClassDomain(c);
     * return toReturn;
     * </pre>
     * 
     * 
     * @param fieldBaseClass -
     *            class of the field
     * @param numOfInstances -
     *            instances of the <code>fieldBaseClass</code> that are to be
     *            created in the class domain of <code>fieldBaseClass</code>
     * @param includeNull -
     *            whether to include null in the <code>IObjSet</code>
     * @return newly created <code>IObjSet</code>
     */
    IObjSet createObjSet(Class fieldBaseClass, int numOfInstances,
            boolean includeNull);

    /**
     * Helper. <br/> Calls <code>createObjSet(fieldBaseClass, false);</code>
     * 
     * @param fieldBaseClass
     *            Base type of the field
     * @return newly created <code>IObjSet</code>
     * @see #createObjSet(Class, boolean)
     * @see #createObjSet(String)
     * @see #createObjSet(String, boolean)
     */
    IObjSet createObjSet(Class fieldBaseClass);

    /**
     * Helper.
     * 
     * <br/> First, tries to find a <code>Class</code> object for the given
     * <code>fieldBaseClassName</code> and then calls
     * <code>createObjSet(Class, int, boolean)</code>.
     * 
     * <br/> Searches in the package of the finitized class, or in the default
     * package.
     * 
     * @param fieldBaseClass
     * @param numOfInstances
     * @return newly created <code>IObjSet</code>
     */
    IObjSet createObjSet(Class fieldBaseClass, int numOfInstances);

    /**
     * Helper.
     * 
     * <br/> First, tries to find a <code>Class</code> object for the given
     * <code>fieldBaseClassName</code> and then calls
     * <code>createObjSet(Class, boolean)</code>.
     * 
     * <br/> Searches in the package of the finitized class, or in the default
     * package.
     * 
     * @param fieldBaseClassName
     *            name of the base type of the field
     * @param includeNull
     *            weather to include null in the <code>IObjSet</code>.
     * @return created <code>IObjSet</code>
     * @see #createObjSet(Class, boolean)
     * @see #createObjSet(Class)
     * @see #createObjSet(String)
     */
    IObjSet createObjSet(String fieldBaseClassName, boolean includeNull);

    /**
     * Helper.
     * 
     * <br/> First, tries to find a <code>Class</code> object for the given
     * <code>fieldBaseClassName</code> and then calls
     * <code>createObjSet(Class, int, boolean)</code>.
     * 
     * <br/> Searches in the package of the finitized class, or in the default
     * package.
     * 
     * @param fieldBaseClassName
     * @param numOfInstances
     * @param includeNull
     * @return newly created <code>IObjSet</code>
     */
    IObjSet createObjSet(String fieldBaseClassName, int numOfInstances,
            boolean includeNull);

    /**
     * Helper. <br> Calls <code>createFieldDomain(fieldBaseClassName, false);
     * 
     * @param fieldBaseClassName name of the base type of the field
     * @return created <code>IObjSet</code>
     * @see #createObjSet(Class, boolean)
     * @see #createObjSet(Class)
     * @see #createObjSet(String, boolean)
     */
    IObjSet createObjSet(String fieldBaseClassName);

    /**
     * Helper. <br>
     * Calls <code>createObjSet(fieldBaseClassName, numOfInstances, false);
     * 
     * @param fieldBaseClassName
     * @param numOfInstances
     * @return newly created <code>IObjSet</code>
     */
    IObjSet createObjSet(String fieldBaseClassName, int numOfInstances);
    
    /**
     * Helper. <br>
     * Creates new ObjectSet for class of the <code>classDomain</code>
     * and adds <code>classDomain</code>. Null value is not allowed
     * 
     * @param classDomain
     * @return newly created <code>IFieldDomain</code>
     */
    IFieldDomain createObjSet(IClassDomain classDomain);
    
    /**
     * Similar to <code>createObjSet(IClassDomain classDomain)</code>
     * but also can include null value 
     * 
     * @see #createObjSet(IClassDomain) 
     * @return  created <code>IFieldDomain</code>
     */
    IFieldDomain createObjSet(IClassDomain classDomain, boolean includeNull);

    /**
     * <p>
     * Creates an instance of the IArraySet.
     * </p>
     * 
     * <p>
     * Use this method to create a field domain for the field of an array type.
     * If the given parameter doesn't stand <code>clz.isArray()</code> an
     * <code>IllegalArgumentException</code> will be thrown. The other two
     * parameters are used to set bounds on array length and array components.
     * An array will take any int value from the given
     * <code>IIntSet array$length</code> parameter and any of the array
     * components will take any value from the given
     * <code>IFieldDomain array$values</code>
     * </p>
     * 
     * @param clz
     *            class of the array field
     * @param array$length
     *            possible values for the array length
     * @param array$values
     *            possible values for the array components
     * @param count
     *            number of arrays to create
     * @return created IArraySet
     */
    IArraySet createArraySet(Class clz, IIntSet array$length,
            IFieldDomain array$values, int count);

    /**
     * Helper.<br/> Parses <code>fullFieldName</code> into
     * <code>className</code> and <code>fieldName</code> according to
     * following format:<br/>
     * <ul>
     * <li>if <code>fullFieldName</code> does not contains "." it is
     * considered as a <code>fieldName</code> of the class which this
     * finitization is for;</li>
     * <li>if <code>fullFieldName</code> contains "." then the substring
     * after the last "." in the <code>fullFieldName</code> is considered as a
     * <code>fieldName</code> and the substring before the last "." is taken
     * as <code>className</code>. <code>className</code> should be a fully
     * qualified name of the class or a relative name in the package of the
     * finitized field;</li>
     * </ul>
     * 
     * Then calls <code>set(className, fieldName, fieldDomain)</code>
     * 
     * @param fullFieldName -
     *            name of the field ( format: ClassName.FieldName )
     * @param fieldDomain -
     *            field domain
     * @see #set(Class, String, IFieldDomain)
     * @see #set(String, String, IFieldDomain)
     */
    void set(String fullFieldName, IFieldDomain fieldDomain);

    /**
     * Helper.<br/> Searches for the class with the given name (in the default
     * package and in package of the finitized class), and then calls
     * <code>set(Class, String, IFieldDomain)</code>
     * 
     * @param className -
     *            fully qualified name of the class or name relative to package
     *            name of the finitized class
     * @param fieldName -
     *            name of the field
     * @param fieldDomain -
     *            domain of the field
     * @see #set(String, IFieldDomain)
     * @see #set(Class, String, IFieldDomain)
     */
    void set(String className, String fieldName, IFieldDomain fieldDomain);

    /**
     * Assigns <code>fieldDomain</code> to the given field (<code>fieldName</code>)
     * of the given class (<code>cls</code>).<br/>
     * 
     * <b><u>Note</u></b> that you cannot assign field domain to the field of
     * some class if the class domain (ObjSet) for that class has not been
     * created before (e.g. with the call
     * <code>createObjSet(cls, numOfInstances)</code>)
     * 
     * @param cls -
     *            <code>Class</code> object of the class
     * @param fieldName -
     *            field name of the given class
     * @param fieldDomain -
     *            domain of the field
     * @see #set(String, IFieldDomain)
     * @see #set(String, String, IFieldDomain)
     */
    void set(Class cls, String fieldName, IFieldDomain fieldDomain);

    /**
     * Helper. <br/> Searches for the <code>Class</code> object of the class
     * with the given name and then calls <code>getObjSet(Class)</code>
     * 
     * @param name -
     *            name of the class of which the <code>ObjSet</code> is
     *            requested.
     * @return existing
     *         <code>ObjSet<code> for the given class name or <code>null</code> if is 
     *         not been created yet.
     * @see #getClassDomain(Class)
     */
    IClassDomain getClassDomain(String name);

    /**
     * Searches for the <code>ObjSet</code> of the given <code>Class</code>
     * object.
     * 
     * @param cls -
     *            <code>Class</code> object of the class
     * @return existing
     *         <code>ObjSet<code> for the given class name or null if is 
     *         not been created yet.
     * @see #getClassDomain(String)
     */
    IClassDomain getClassDomain(Class cls);

    /**
     * @param cls
     * @param fieldName
     * @return newly created <code>IFieldDomain</code>
     * @see #getFieldDomain(String)
     * @see #getFieldDomain(String, String)
     */
    IFieldDomain getFieldDomain(Class cls, String fieldName);

    /**
     * @param className
     * @param fieldName
     * @return newly created <code>IFieldDomain</code>
     * @see #getFieldDomain(Class, String)
     * @see #getFieldDomain(String)
     */
    IFieldDomain getFieldDomain(String className, String fieldName);

    /**
     * @param fullFieldName
     * @return newly created <code>IFieldDomain</code>
     * @see #getFieldDomain(Class, String)
     * @see #getFieldDomain(String, String)
     */
    IFieldDomain getFieldDomain(String fullFieldName);

    /**
     * Adds all objects from the given <code>objSet</code> to the given field
     * which has to be of type <code>Collection</code>.
     * 
     * @param fullFieldName - full name of the field: &lt;className&gt;.&lt;fieldName&gt;
     * @param objSet - set of objects to be added to the given field (collection) 
     * @see #addAll(Class, String, IObjSet)
     * @see #addAll(String, String, IObjSet)
     */
    void addAll(String fullFieldName, IObjSet objSet);

    /**
     * Adds all objects from the given <code>objSet</code> to the given field
     * which has to be of type <code>Collection</code>.
     * 
     * @param className - name of the field's declaring class (class which contains the field)
     * @param fieldName - name of the field 
     * @param objSet - set of objects to be added to the given field (collection)
     * @see #addAll(String, IObjSet)
     * @see #addAll(Class, String, IObjSet)
     */
    void addAll(String className, String fieldName, IObjSet objSet);

    /**
     * Adds all objects from the given <code>objSet</code> to the given field
     * which has to be of type <code>Collection</code>.
     * 
     * @param cls - field's declaring class (class which contains the field)
     * @param fieldName - name of the field
     * @param objSet - set of objects to be added to the given field (collection)
     * @see #addAll(String, IObjSet)
     * @see #addAll(String, String, IObjSet)
     */
    void addAll(Class cls, String fieldName, IObjSet objSet);

    /**
     * Includes information from finitization <code>fin</code> into current
     * finitization. Only one finitization of the same type can be included.
     * 
     * @param fin - finitization which will be included in current finitization
     * @return - is finitization including successful
     * @see #getIncludedFinitization(Class)
     */
    boolean includeFinitization(IFinitization fin);

    /**
     * Returns the included finitization, if there is such, for type given 
     * by <code>clazz</code> parameter
     * 
     * 
     * @param clazz - class for which the finitization has been done
     * @return - included finitization the root of which is clazz, if exists. 
     *           Otherwise returns null
     * @see #includeFinitization(IFinitization)
     */
    IFinitization getIncludedFinitization(Class clazz);
    
    /**
     * <p>If selected, arrays will be handled like other objects during Korat search.
     * This implies that one array object might be shared among many objects. </p>
     * 
     * <p>Another option is to treat array objects as special "field containers", that
     * may belong to only one object.</p>
     * 
     * 
     * @param handleAsObjects - should arrays be treated like objects during search.
     * 
     * @see IFinitization#areArraysHandledAsObjects()
     * */
    void handleArraysAsObjects(boolean handleAsObjects);
    
    /**
     * @return - are arrays treated as objects during Korat search
     */
    boolean areArraysHandledAsObjects();
}
