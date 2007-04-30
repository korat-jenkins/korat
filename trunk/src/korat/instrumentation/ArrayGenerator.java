package korat.instrumentation;

import static javassist.Modifier.FINAL;
import static javassist.Modifier.PROTECTED;
import static javassist.Modifier.PUBLIC;
import static javassist.Modifier.TRANSIENT;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import korat.testing.ITester;

/**
 * 
 * <p>ArrayGenerator is used to generate Korat Array classes. They are used
 * during the instrumentation, as a suitable replacement for standard array classes.</p>
 * 
 * <p>Korat Arrays implement IKoratArray interface, that can distinguish them among other
 * classes.</p>
 * 
 * <p>The template for Korat Array classes is the given in following methods, which do parts of 
 * instrumentation. Parameters that are changed for each different type of arrays are printed 
 * in bold. Here, the interface is listed:</p>
 * 
 * 
 * <pre>
 * public class static class Korat_Array_<b>Type</b> implements IKoratArray {
 * 
 *      static class Korat_Array_<b>Type</b>_Element_Setter extends Setter {...}
 *      static class Korat_Array_<b>Type</b>_Length_Setter extends Setter {...}
 *      
 *      korat.testing.ITester tester;
 *      
 *      <b>Type</b>[] values;
 *      int[] values_ids;
 *      
 *      int length;
 *      int length_id;
 *
 *      public Korat_Array_<b>Type</b> (int maxSize);
 *      public setTester(korat.testing.ITester tester);
 *      
 *      public Setter get_element_setter(int position, int field_id);
 *      public <b>Type</b> get(int index);
 *      public void set(int position, Korat_Array_<b>Type</b> newValue);
 *      public Object [] getValues();
 *      
 *      public Setter get_length_setter(int field_id);
 *      public int getLength(int index);
 *      public void setLength(int newValue);
 *      
 *      // + methods that implement IKoratTouchable (if required)
 * }
 * </pre>
 * 
 * ArrayGenerator is initialized with type of array for which a KoratArray
 * 
 * @see IKoratArray
 * @see IInstrumenter
 * 
 * @see #createArrayElementMethods(CtClass)
 * @see #createArrayElementSetter(CtClass)
 * @see #createInitializers(CtClass)
 * @see #createFields(CtClass)
 * @see #createLengthMethods(CtClass)
 * @see #createLengthSetter(CtClass)
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class ArrayGenerator {

    private static final String _ELEMENT_SETTER = "_Element_Setter";

    public static final String _LENGTH_SETTER = "_Length_Setter";

    public static final String ARRAY_FULLNAME_PREFIX = "korat.instrumentation.$koratcreated$.Korat_Array_";

    public static final String GET_LENGTH_SETTER_METHOD_NAME = "get_length_setter";

    public static final String GET_ELEMENT_SETTER_METHOD_NAME = "get_element_setter";

    static Map<String, Class> generatedClasses = new HashMap<String, Class>();

    private boolean TRACE = false; // used for debugging the arrays!

    private ClassPool cp;

    private CtClass arrayType;

    private CtClass componentType;

    private String className;

    private static final CtClass indexType = CtClass.intType;

    /**
     * Creates korat array for the given type of array, which is represented as javassist
     * CtClass. It must represent array type e.g. <code>int[].class</code>, 
     * <code>Object[].class</code>
     * 
     * @see #ArrayGenerator(Class)
     * 
     * @param arrayType - Array type for which this 
     * @throws NotFoundException - given array type does not exist
     */
    public ArrayGenerator(CtClass arrayType) throws NotFoundException {
        if (!arrayType.isArray())
            throw new IllegalArgumentException("Arugment class is not an array");

        this.cp = ClassPool.getDefault();
        this.arrayType = arrayType;

        componentType = arrayType.getComponentType();
        className = getArrayClassName(componentType.getName());

        // We'll se about that...
        if (componentType.isArray()) {
                
            ArrayGenerator compGen = new ArrayGenerator(componentType);
            componentType = compGen.getArrayCtClass();
            compGen.getArrayClass();
                
         }
    }

    public ArrayGenerator(Class arrayType) throws NotFoundException {
        this(ClassPool.getDefault().get(arrayType.getName()));
    }

    /**
     * Used to show debug messages inside the generated classes
     * This instrumentation is applied only if trace flag is set.
     * 
     * @param msg - debug message
     * @return string to prepend to method's source code
     */
    protected String addTrace(String msg) {
        if (!TRACE)
            return "";
        msg = msg.replaceAll("\\$(\\d)+", "\" + $0 + \"");
        String s = " System.out.println(getClass() + \": " + msg + "\"); ";
        return s;
    }

    /**
     * @return the class name array
     */
    public String getArrayClassName() {
        try {
            return getArrayClassName(arrayType.getComponentType().getName());
        } catch (NotFoundException e) {
            //should never reach this point
            throw new RuntimeException(e);
        }
    }

    /**
     * @param componentName name of the component type
     * @return the name of array class that corresponds to the given type of
     * components
     */
    public static String getArrayClassName(String componentName) {
        return ARRAY_FULLNAME_PREFIX
                + componentName.replace('.', '_').replace('[', '$').replace(']', '$');
    }
    
    /**
     * Generates the CtClass object, representing the Korat Array and all 
     * its inner classes
     * 
     * @return - array of CtClasses, the first of which is the KoratArray class
     *           followed by its inner classes
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    protected CtClass[] generateKoratArray() throws CannotCompileException,
            NotFoundException {

        CtClass koratClass = cp.makeClass(className);
        
        
        setImplementedInterfaces(koratClass);
        createFields(koratClass);
        createInitializers(koratClass);
        CtClass elementSetter = createArrayElementSetter(koratClass);
        createArrayElementMethods(koratClass);
        CtClass lengthSetter = createLengthSetter(koratClass);
        createLengthMethods(koratClass);
        implementIKoratTouchableInterface(koratClass);
        
        return new CtClass[] {koratClass, elementSetter, lengthSetter};
        
    }

    protected void setImplementedInterfaces(CtClass clz) {
        CtClass[] ints = new CtClass[2];
        try {
            ints[0] = cp.get(IKoratArray.class.getName());
            ints[1] = cp.get(Serializable.class.getName());
        } catch (NotFoundException e) {
            // should never reach this point 
            //(IKoratArray and Serializable should exist on the classpath)
            throw new RuntimeException(e);
        }
        clz.setInterfaces(ints);
    }

    /**
     * Gets the the class file for korat array. 
     * If this Korat array hasn't been generated before, it will be generated.
     * Otherwise it is returned from the javassist repository.
     * 
     * @see #generateKoratArray()
     * @see #getArrayCtClasses()
     * @return class file for korat array
     */
    public Class<?> getArrayClass() {
        Class<?> clazz = generatedClasses.get(className);
        if (clazz != null)
            return clazz;

        try {
            CtClass[] clz = getArrayCtClasses();
            
            //Load all inner classes
            for (int i = 1; i < clz.length; i++) {
                Class<?> c = clz[i].toClass();
                generatedClasses.put(className, c);
            }
            
            //It's ok now to load the KoratArray class
            clazz = clz[0].toClass();
            generatedClasses.put(className, clazz);
            
        } catch (Exception e) {
            throw new RuntimeException("There has been problem with array instrumentation!", e);
        }
        return clazz;
    }

    /**
     * Gets CtArrayClass [] containing korat array at position [0] and
     * all its inner classes, if created for the first time
     * 
     * @see #getArrayClass()
     * @see #generateKoratArray()
     * @return korat array (at position 0) and all its inner classes, 
     *         if created for the first time
     * @throws NotFoundException 
     * @throws CannotCompileException 
     */
    public CtClass[] getArrayCtClasses() throws CannotCompileException, NotFoundException {
        CtClass[] generatedClasses = null;
        try {
            
            CtClass koratArray = cp.get(className);
            CtClass elemSetter = cp.get(className+"$"+_ELEMENT_SETTER);
            CtClass lenSetter = cp.get(className+"$"+_LENGTH_SETTER);
            
            generatedClasses = new CtClass[]{koratArray, elemSetter, lenSetter};
            
        } catch (NotFoundException e) { // class does not yet exist, create it
                generatedClasses = generateKoratArray();
                
                for (CtClass c : generatedClasses) {
                    c.stopPruning(true);
                    InstrumentationManager.addToAlreadyInstrumented(c);
                }
                
        }
        return generatedClasses;
    }
    
    /**
     * @return CtClass for the KoratArray.
     */
    public CtClass getArrayCtClass() {
        try {
            return getArrayCtClasses()[0];
        } catch (Exception e) {
            throw new RuntimeException("There has been problem with array instrumentation!", e);
        }
    }

    /**
     * Generates fields for the KoratArray classes. Fields are given in the following table.
     * 
     * <table cellspacing="7" cellpadding="3">
     * <tr>
     * <td style="white-space:nowrap;"><i>Field</i></td>
     * <td><i>Description</i></td>
     * </tr>
     * <tr>
     * <td style="white-space:nowrap;"><code><b>Type</b>[] values </code></td>
     * <td>represents values stored in array object</td>
     * </tr>
     *
     * <tr>
     * <td style="white-space:nowrap;"><code>int[] values_ids </code></td>
     * <td>represents field identifiers for corresponding values elements</td>
     * </tr>
     *
     * <tr>
     * <td style="white-space:nowrap;"><code>int length </code></td>
     * <td>represents the actual length of the array</td>
     * </tr>
     *
     * <tr>
     * <td style="white-space:nowrap;"><code>int length_id </code></td>
     * <td>represents field identifier for the length</td>
     * </tr>
     *
     * <tr>
     * <td style="white-space:nowrap;"><code>korat.testing.ITester tester </code></td>
     * <td>object to which the field accesses will be reported</td>
     * </tr>
     * </table>
     * 
     * <p>For detailed explanation on purpose of _id fields, please see 
     * <code>FieldInstrumenter</code> class. </p>
     * 
     * @see FieldInstrumenter
     * 
     */
    protected void createFields(CtClass clz) throws CannotCompileException,
            NotFoundException {
        
        CtClass componentArrayType = cp.get(componentType.getName() + "[]");

        CtField values = new CtField(componentArrayType, "values", clz);
        values.setModifiers(PUBLIC);
        clz.addField(values);

        CtClass indexArray = cp.get(indexType.getName() + "[]");
        CtField values_ids = new CtField(indexArray, "values_ids", clz);
        values_ids.setModifiers(PROTECTED | TRANSIENT);
        clz.addField(values_ids);

        CtField length = new CtField(CtClass.intType, "length", clz);
        length.setModifiers(PROTECTED);
        clz.addField(length);
        CtField length_id = new CtField(indexType, "length_id", clz);
        length_id.setModifiers(PROTECTED | TRANSIENT);
        clz.addField(length_id);

        CtField koratTester = new CtField(cp.get(ITester.class.getName()), "tester", clz);
        koratTester.setModifiers(PROTECTED | TRANSIENT | FINAL);
        clz.addField(koratTester);

    }

    /**
     * Creates constructor and initializer for the KoratArray. 
     * Constructor prototype is the following:
     * 
     *  <pre>
     *      public Korat_Array_<b>Type</b> (int maxSize)
     *  </pre>
     *  Argument <code>maxSize</code> represents the maximal size array object can have
     *  
     *  The body of this constructor is:
     * 
     * <pre>
     *  public Korat_Array_<b>Type</b> (int maxSize){
     *    values = new <b>Type</b> [maxSize];  
     *    values_ids = new int[maxSize];  
     *    length = maxSize; 
     *    for (int i = 0; i < maxSize; i++) 
     *       values_ids[i] = -1;
     *  }
     * </pre>
     * 
     * Method setTester allows initialization of KoratArray with arbitrary
     * ITester object.
     * 
     * <pre>
     *      public void setTester(korat.testing.ITester tester) {
     *         this.tester = tester;
     *      }
     * 
     * </pre>
     *  
     */
    protected void createInitializers(CtClass clz)
            throws CannotCompileException, NotFoundException {

        // --- Array_...(int maxSize)
        CtConstructor c = new CtConstructor(new CtClass[] { CtClass.intType },
                clz);
        String compType = componentType.getName();
        c.setBody("{ " + 
                     addTrace("Constructor") + 
                  "  values = new " + compType + "[$1]; " + 
                  "  values_ids = new int[$1]; " + 
                  "  length = $1; " +
                  "  for (int i = 0; i < $1; i++) " +
                  "    values_ids[i] = -1; " +
                  "}");
        c.setModifiers(PUBLIC);
        clz.addConstructor(c);
        
        CtMethod setTester = new CtMethod(CtClass.voidType, "initialize",
                new CtClass[] { cp.get(ITester.class.getName()) }, clz);
        setTester.setBody("{ " 
                + addTrace("initialize()") 
                + "  tester = $1;"
                + "}");
        clz.addMethod(setTester);

    }

    /**
     * Creates setter classes for array values (elements of values array).
     * Setter class is the static inner class of Korat_Array_<b>Type</b>
     * 
     * <pre>
     * static class _Element_Setter extends Setter{
     *      // pointer to the owner object of the _element_setter_class
     *      // this class couldn't be implemented as non static inner class
     *      // since javassist doesn't allow it
     *      Korat_Array_<b>Type</b> _this;    
     *      
     *      int position;   // element position inside values array
     *      
     *      _Element_Setter(KoratArray_<b>Type</b> owner, int pos) {
     *          _this = owner;
     *          position = pos;
     *      }
     *      
     *      // <em>for primitive types only :: </em>
     *      // sets the new value, overrides the corresponding set method from Setter class
     *      void set(<b>Type</b> newValue) {
     *          _this.values[position] = newValue;
     *      }
     *      
     *      // <em>for other types :: </em>
     *      // sets the new value, overrides the corresponding set method from Setter class      
     *      void set(Object newValue) {
     *          _this.values[position] = (<b>Type</b>)newValue;
     *      }
     * }
     * </pre>
     * 
     */
    protected CtClass createArrayElementSetter(CtClass clz)
            throws NotFoundException, CannotCompileException {

        String setterClassName = _ELEMENT_SETTER;
        CtClass setterClz = NestedClassCreator.createNestedClass(
                setterClassName, clz, cp.get(Setter.class.getName()));

        // add reference field to this object
        CtField myRef = new CtField(clz, "_this", setterClz);
        setterClz.addField(myRef);
        CtField myPosition = new CtField(CtClass.intType, "position", setterClz);
        setterClz.addField(myPosition);

        // add constructor to initialize reference field
        CtConstructor ctor = new CtConstructor(
                new CtClass[] { clz, indexType }, setterClz);
        ctor.setBody("{ _this = $1; position = $2;}");
        setterClz.addConstructor(ctor);

        // add appropriate set implementation
        CtClass param = componentType;
        if (!param.isPrimitive())
            param = cp.get("java.lang.Object");

        CtClass[] params = new CtClass[] { param };

        CtMethod setterMethod = new CtMethod(CtClass.voidType, "set", params,
                setterClz);
        
        if (param.isPrimitive()) { 
            setterMethod.setBody(
                    " { " +
                    "     _this.values[position] = $1;" +
                    " }");
        } else { 
             setterMethod.setBody(
                     " { " + 
                     "     _this.values[position] = ("+ componentType.getName() + ") $1;" +
                     " }");
        }
        setterClz.addMethod(setterMethod);
        return setterClz;

    }

    /**
     * Creates methods for KoratArray class, that manipulate array values.
     * 
     * <pre>
     * //Used to initialize KoratArray, sets field_id for array elements, 
     * //and returns corresponding setters
     * public Setter get_element_setter(int position, int field_id) {
     *      values_ids[position] = field_id;
     *      return new _Element_Setter(this, position);
     * }
     *  
     * // Returns the value of the index'th element of an array
     * public <b>Type</b> get(int index) {
     *      tester.notifyFieldAccess(values_ids[index]);
     *      return values[index];
     * }
     * 
     * // Sets the value of the array element at position position. 
     * public void set(int position, Korat_Array_<b>Type</b> newValue) {
     *      if (index >= length)
     *          throw new IndexOutOfBoundsException();     * 
     *      values[position] = newValue;
     * }
     * 
     * // Returns element at position index; this is part of IKoratArray interface,
     * // used by Korat internally, instead of java reflection
     * public Object getValue(int index) {
     *      if (index >= length)
     *          throw new IndexOutOfBoundsException();
     *      // For Primitive Types:
     *      return new <b>TypeWrapper</b> values[index];
     *      // ---------------------
     *      // For other types
     *      return values[index];
     * }
     * </pre>
     * 
     */
    protected void createArrayElementMethods(CtClass clz)
            throws CannotCompileException, NotFoundException {

        // --- Object get_element_setter(int elementIndex, int element_id) ---
        CtClass setter = null;
        try {
            setter = cp.get(Setter.class.getName());
        } catch (NotFoundException e) {
        }
        CtMethod getSetter = new CtMethod(setter,
                GET_ELEMENT_SETTER_METHOD_NAME, new CtClass[] {
                        CtClass.intType, indexType }, clz);
        String setterFullName = clz.getName() + "._Element_Setter";
        getSetter.setBody(
                "{" +
                   addTrace("get_element_setter($1, $2)") +
                "  values_ids[$1] = $2; " +
                "  return new " + setterFullName + "(this, $1);" +
                "}");
        clz.addMethod(getSetter);

        // --- <Type> get(int index) -----
        CtMethod getValue = new CtMethod(componentType, "get",
                new CtClass[] { CtClass.intType }, clz);

        getValue.setBody(
                "{" + 
                  addTrace("get($1)") +
                  " tester.notifyFieldAccess(values_ids[$1]); " +
                  " if ($1 >= length) " +
                  "    throw new IndexOutOfBoundsException(); " +
                  " return values[$1];" + "}");
        clz.addMethod(getValue);

        // --- void set(int index, <Type> value)
        CtMethod setValue = new CtMethod(CtClass.voidType, "set",
                new CtClass[] { CtClass.intType, componentType }, clz);
        setValue.setBody(
                    "{ " + 
                    addTrace("set($1, $2)") + 
                    "  if ($1 >= length) " +
                    "    throw new IndexOutOfBoundsException(); " +
                    "  values[$1] = $2;" +
                    "}");
        clz.addMethod(setValue);
        
        // --- Object [] getValues()
        CtClass objClass = cp.get("java.lang.Object");
        
        CtMethod getValues = new CtMethod(objClass, "getValue",
                new CtClass[] {CtClass.intType} , clz);
        if (componentType.isPrimitive()) {
            String compObjType = getPrimitiveClassCounterpart(componentType.getSimpleName());
            getValues.setBody(
                    "{ " +
                    addTrace("getValue($1)") +
                    "if ($1 >= length) " +
                    "    throw new IndexOutOfBoundsException(); " +                    
                    "   return new " + compObjType + "(values[$1]);" +
                    "}");
        } else
            getValues.setBody(
                    "{ " + 
                        addTrace("getValue($1)") +
                    "   if ($1 >= length) " +
                    "      throw new IndexOutOfBoundsException(); " +
                    "   return values[$1];" +
                    "}");
        clz.addMethod(getValues);

    }
    

    /**
     * Creates setter classes for array length. 
     * Setter class is the static inner class of Korat_Array_<b>Type</b>
     * 
     * <pre>
     * static class _Length_Setter extends Setter{
     *      // pointer to the owner object of the _length_setter class
     *      Korat_Array_<b>Type</b> _this;    
     *      
     *      int position;   // element position inside values array
     *      
     *      _Length_Setter(KoratArray_<b>Type</b> owner, int pos) {
     *          _this = owner;
     *      }
     *      
     *      // sets the new value, overrides the corresponding set method from Setter class
     *      void set(<b>Type</b> newLength) {
     *          _this.length = newLength;
     *      }
     * }
     * </pre>
     * 
     */
    protected CtClass createLengthSetter(CtClass clz) throws NotFoundException,
            CannotCompileException {

        String setterClassName = _LENGTH_SETTER;
        
        CtClass setterClz = NestedClassCreator.createNestedClass(
                setterClassName, clz, cp.get(Setter.class.getName()));

        // add reference field to this obj
        CtField myRef = new CtField(clz, "_this", setterClz);
        setterClz.addField(myRef);

        // add constructor to initialize reference field
        CtConstructor ctor = new CtConstructor(new CtClass[] { clz }, setterClz);
        ctor.setBody("{ _this = $1; }");
        setterClz.addConstructor(ctor);

        // add appropriate set implementation

        CtMethod setterMethod = new CtMethod(CtClass.voidType, "set",
                new CtClass[] { CtClass.intType }, setterClz);
        setterMethod.setBody("{ _this.length = $1; }");
        setterClz.addMethod(setterMethod);

        return setterClz;

    }

    /**
     * Creates methods for KoratArray class, that manipulate array length.
     * 
     * <pre>
     * //Used in initialization of KoratArray, sets field_id for array length, 
     * //and returns corresponding setter
     * public Setter get_length_setter(int field_id) {
     *      length_id = field_id;
     *      return new _Length_Setter(this);
     * }
     *  
     * // Returns the value of the array length
     * public int getLength(int index) {
     *      tester.notifyFieldAccess(length_id]);
     *      return length;
     * }
     * 
     * // Sets the value of the array element at position position. 
     * public void setLength(int newValue) {
     *      length = newValue;
     * }
     * </pre>
     * 
     */
    protected void createLengthMethods(CtClass clz)
            throws CannotCompileException {

        // --- Object get_length_setter(int length_id) ---
        CtClass setter = null;
        try {
            setter = cp.get(Setter.class.getName());
        } catch (NotFoundException e) {
        }

        CtMethod getSetter = new CtMethod(setter,
                GET_LENGTH_SETTER_METHOD_NAME,
                new CtClass[] { CtClass.intType }, clz);
        String lengthSetterName = clz.getName() + "._Length_Setter";
        getSetter.setBody(
                "{" + 
                   addTrace(GET_LENGTH_SETTER_METHOD_NAME) +
                "  length_id = $1;" +
                "  return new " + lengthSetterName + "($0);" + 
                "}");
        clz.addMethod(getSetter);

        // --- int getLength() -----
        CtMethod getValue = new CtMethod(CtClass.intType, "getLength",
                new CtClass[] {}, clz);
        getValue.setBody(
                "{" +
                   addTrace("getLength()") +
                "  tester.notifyFieldAccess(length_id); " +
                "  return length;" + 
                "}");
        clz.addMethod(getValue);

        // --- void set(int newLength)
        CtMethod setValue = new CtMethod(CtClass.voidType, "setLength",
                new CtClass[] { CtClass.intType }, clz);
        setValue.setBody(
                "{" +
                   addTrace("setLength") + 
                "  length = $1; " +
                "}");
        clz.addMethod(setValue);

    }

    /**
     * Implements IKoratTouchable interface by creating one field and two
     * methods, specified by this interface
     * 
     * <pre>
     *  //*actual field and method names may be different 
     *  
     *  boolean koratTouched;  
     *   
     *  public void __korat__touch__initialize() {
     *    if (!koratTouched)  
     *      return;
     *    
     *    koratTouched = false;
     *  }
     *   
     *  public void __korat__touch() {
     *    if (koratTouched)
     *      return;
     *      
     *    koratTouched = true;
     *    
     *    for (int i = 0; i < length; i++)
     *      get(i);
     *    getLength();
     *    
     *    for (int i = 0; i < length; i++)
     *      if (values[i] && values[i] instanceOf KoratTouchable)
     *        ((KoratTouchable) values[i]).__korat__touch();
     *  }
     * </pre>
     * 
     * @param clz
     * @see IKoratTouchable
     */
    protected void implementIKoratTouchableInterface(CtClass clz) {

        // ---- create boolean field ------------
        String koratTouchedName = IKoratTouchable.koratTouchedName;
        try {
            CtField koratTouched = new CtField(CtClass.booleanType,
                    koratTouchedName, clz);
            koratTouched.setModifiers(TRANSIENT);
            clz.addField(koratTouched);
        } catch (CannotCompileException e1) {
            throw new RuntimeException(e1);
        }
        
        // ---- create touch__initialize method -------------
        String touchInitName = IKoratTouchable.touchInitName;
        CtClass[] touchInitParams = new CtClass[0];

        CtMethod touchInit = new CtMethod(CtClass.voidType, touchInitName,
                touchInitParams, clz);

        String touchInitBody = " if (! {0} ) return; " + "  {0} = false; \n";
        touchInitBody = MessageFormat.format(touchInitBody,
                new Object[] { koratTouchedName });

        //COMPAT1.4
        //StringBuilder touchInitBodyBuilder = new StringBuilder();
        StringBuffer touchInitBodyBuilder = new StringBuffer();
        touchInitBodyBuilder.append(touchInitBody);

        if (!componentType.isPrimitive()) {
            String addT = 
                    "for (int i = 0; i < length; i++) \n" +
                    "   if (values[i] != null " +
                    "       && values[i] instanceof korat.instrumentation.IKoratTouchable) \n" +
                    "   ((korat.instrumentation.IKoratTouchable) values[i] ).{0}(); \n"; 
            addT = MessageFormat.format(addT, touchInitName);

            touchInitBodyBuilder.append(addT);
        }

        String touchInitAllBody = "{\n" + touchInitBodyBuilder.toString()
                + "\n}";

        try {
            touchInit.setBody(touchInitAllBody);
            clz.addMethod(touchInit);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }

        // --- create touch method ------------------------
        String touchName = IKoratTouchable.touchName;
        CtClass[] touchParam = new CtClass[0];

        CtMethod touch = new CtMethod(CtClass.voidType, touchName, touchParam,
                clz);

        //COMPAT1.4
        //StringBuilder touchBodyBuilder = new StringBuilder();
        StringBuffer touchBodyBuilder = new StringBuffer();

        String checkIsTouched = 
                " if (" + koratTouchedName + ") return;" +
                " else " + koratTouchedName + " = true; ";
        touchBodyBuilder.append(checkIsTouched);

        String addT;
        addT =  "for (int i = 0; i < length; i++) \n" + 
                "   get(i); \n" +
                "getLength(); \n";
        touchBodyBuilder.append(addT);

        if (!componentType.isPrimitive()) {
            addT = "for (int i = 0; i < length; i++) \n" +
                   "   if (values[i] != null " +
                   "       && values[i] instanceof korat.instrumentation.IKoratTouchable) \n" +
                   "   ((korat.instrumentation.IKoratTouchable) values[i] ).{0}(); \n";
            addT = MessageFormat.format(addT, touchName);

            touchBodyBuilder.append(addT);
        }

        String touchBody = "{\n" + touchBodyBuilder.toString() + "\n}";

        try {
            touch.setBody(touchBody);
            clz.addMethod(touch);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }

    }
    
    /**
     * Returns name of the wrapper class corresponding to the primitive type
     * 
     * @param primitiveClassName - name of primitive class (boolean, byte, char, double, float, int, long, short)
     * @return - name of its object counterpart (Boolean, Byte, Character, Double, Float, Integer, Long, Short)
     */
    private String getPrimitiveClassCounterpart (String primitiveClassName) {
        if ("int".equals(primitiveClassName))
            return "Integer";
        else if ("char".equals(primitiveClassName))
            return "Character";
        else if ("boolean".equals(primitiveClassName))
            return "Boolean";
        else if ("byte".equals(primitiveClassName))
            return "Byte";
        else if ("double".equals(primitiveClassName))
            return "Double";
        else if ("float".equals(primitiveClassName))
            return "Float";
        else if ("long".equals(primitiveClassName))
            return "Long";
        else if ("short".equals(primitiveClassName))
            return "Short";
        else
            throw new InvalidParameterException("Wrong parameter value: " + primitiveClassName); 
    }


}
