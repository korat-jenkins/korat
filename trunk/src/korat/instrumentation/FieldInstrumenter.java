package korat.instrumentation;

import java.io.IOException;
import java.text.MessageFormat;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.expr.ExprEditor;
import javassist.expr.FieldAccess;

import static javassist.Modifier.*;
/**
 * This class does all the instrumentation related to non-array fields. 
 * For all declared fields, this instrumenter does the following:
 * <ul>
 *   <li> adds "id field" of type int </li>
 *   <li> adds special getter method </li>
 *   <li> creates nested setter class </li>
 *   <li> adds get setter method.</li>
 * </ul>
 * Then, in all methods, all accesses to those fields are replaced with
 * the call to "special getter" method, which, before returning the 
 * value of the field informs the Korat search engine about that 
 * particular field access. Korat uses this information to prune large
 * amount of the potentially huge state space, thus improving efficiency
 * a lot. For more info see {@link #handleFieldDeclaration(CtClass, CtField)}, 
 * {@link #handleFieldDeclaration(CtClass, CtField)}.
 * 
 * <p>
 * Setter classes serve for improving performance when building 
 * test case from the candidate vector because this technique does not 
 * use (slow) java reflection mechanism. For more info see
 * {@link #handleFieldDeclaration(CtClass, CtField)}
 * {@link #createNestedSetterClass(CtClass, CtField)}
 * </p>
 * 
 * @see AbstractInstrumenter
 * @see ArrayFieldInstrumenter
 * @see SpecialConstructorInstrumenter
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
class FieldInstrumenter extends AbstractInstrumenter {

    @Override
    protected void instrument(CtClass clz) throws CannotCompileException,
            NotFoundException, IOException {
        instrumentFieldDeclarations(clz);
        replaceFieldAccesses(clz);
    }

    /**
     * <p>
     * For each field in the given class adds additional stuff needed for
     * instrumentation (by calling <code>handleFieldDeclaration</code>. 
     * Fields of type array are not handled by this class.
     * </p>
     * 
     * @param clz
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    protected void instrumentFieldDeclarations(CtClass clz) throws NotFoundException, CannotCompileException {
        for (CtField f : clz.getDeclaredFields()) {
            if (!shouldProcessField(f))
                continue;
            if (f.getType().isArray())
                continue;
            handleFieldDeclaration(clz, f);
        }
    }
    
    /**
     * For the given field:
     * <ul>
     * <li> adds "id field" of type int </li>
     * <li> adds special getter method </li>
     * <li> creates nested setter class </li>
     * <li> adds get setter method </li>
     * </ul>
     * 
     * <p>
     * For each field, "id filed" is added to store its index in the candidate
     * vector. These indices remain the same during the state space exploration
     * so it is a good idea to calculate them only once at the beginning of the
     * exploration and to keep them for the rest of execution. It is most
     * efficient to do that this way, with one extra "id field" per field.
     * </p>
     * 
     * <p>
     * Special getter method has to inform the instance of the
     * <code>ITestCradle</code> that the field has been accessed before
     * returning the value of the requested field. In order to do that, it first
     * checks if the corresponding "id field" is equal to -1. If it is, it
     * retrieves index in the candidate vector for the requested field (time
     * consuming operation) and stores it in the "id field". After that,
     * notifies <code>ITestCradle</code> that the element with index "id
     * field" has been accessed.
     * </p>
     *       
     * <p>
     * Nested setter classes are used to improve performances. They extend
     * <code>ISetter</code> abstract class and override one appropriate method
     * that the <code>ICandidateBuilder</code> will call in order to build
     * object (test case) from the <code>IStateSpace</code>. This
     * is efficient way to build test case because it avoids using java reflection.
     * Static nested classes can access and set fields directly. It would be
     * more convenient to use inner nested classes, but Javassist doesn't
     * provide support for them.
     * </p>
     * 
     * <p>
     * Get setter method just returns previously created <code>ISetter</code>
     * class for the given field.
     * </p>
     * 
     * @param clz
     * @param f
     * @throws CannotCompileException
     * @throws NotFoundException
     */
    protected void handleFieldDeclaration(CtClass clz, CtField f)
            throws NotFoundException, CannotCompileException {
        addIdField(clz, f);
        addGetterMethod(clz, f);
        createNestedSetterClass(clz, f);
        addGetSetterMethod(clz, f);
    }

    /**
     * Just adds one extra <code>int</code> field. It's important to set its
     * initial value to -1 denoting that id is not yet initialized.
     * 
     * @param clz
     * @param f
     * @throws CannotCompileException
     */
    protected void addIdField(CtClass clz, CtField f)
            throws CannotCompileException {
        String idFieldName = InstrumentationManager.getIdFieldName(f.getName());
        CtField idField = new CtField(CtClass.intType, idFieldName, clz);
        idField.setModifiers(TRANSIENT);
        clz.addField(idField, "-1");
    }

    /**
     * Adds special getter method which before returning the value of the field
     * notifies the instance of the <code>ITestCradle</code> about the field access.
     * 
     * @param clz
     * @param f
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    protected void addGetterMethod(CtClass clz, CtField f)
            throws NotFoundException, CannotCompileException {
    
        String fieldName = f.getName();
        String idFieldName = InstrumentationManager.getIdFieldName(fieldName);
        String getterMethodName = InstrumentationManager.getGetterName(fieldName);

        CtMethod getterMethod = new CtMethod(f.getType(), getterMethodName,
                new CtClass[0], clz);
        String getterSrc = getGetterSrc(clz, fieldName, idFieldName);

        getterMethod.setBody(getterSrc);
        clz.addMethod(getterMethod);

    }
    
    /**
     * Returns the source for the "korat getter method".
     * It looks something like the following:
     * <pre>
     *     public int __korat_get_<b>foo</b>() {
     *         if (__myTester != null)
     *             __myTester.notifyFieldAccess(__id_<b>foo</b>);
     *         return foo;
     *     }  
     * </pre>
     * @param clz
     * @param fieldName
     * @param idFieldName
     * @return the source for the "korat getter method"
     */
    protected String getGetterSrc(CtClass clz, String fieldName, String idFieldName) {
        StringBuilder getterBody = new StringBuilder();
        getterBody.append("'{' "); 
        getterBody.append("  if ({0} != null)");
        getterBody.append("    {0}.notifyFieldAccess({1});"); 
        getterBody.append("  return {2}; "); 
        getterBody.append("'}'");
        return MessageFormat.format(getterBody.toString(), 
                InstrumentationManager.TESTER_FIELD_NAME, idFieldName, fieldName);
    }
    
    /**
     * <p>
     * Searches for all read field accesses (GETFIELD instructions) and replaces
     * them with appropriate getter method call (those getter methods that were
     * generated in the <code>addGetterMethod</code> operation).
     * </p>
     * 
     * @param clz
     * @throws CannotCompileException
     */
    protected void replaceFieldAccesses(CtClass clz) throws CannotCompileException {
        for (CtMethod m : clz.getDeclaredMethods()) {
            if (!shouldProcessMethod(m)) 
                continue;
            if (m.getName().startsWith(InstrumentationManager.GETTER_PREFIX))
                continue;
            
            m.instrument(new ExprEditor() {
                @Override
                public void edit(FieldAccess f) throws CannotCompileException {
                    try {
                        CtField field = f.getField();
                        if (!shouldProcessField(field))
                            return;
                        //-----------------------------
                        //TODO: this may not be needed
                        if (field.getType().isArray()) {
                            // instrument (if needed) declaring class only
                            InstrumentationManager.instrumentClassIfNeeded(field.getDeclaringClass());
                            return;
                        }
                        //-----------------------------
                        if (f.isReader()) {
                            String fieldName = f.getFieldName();
                            String getterName = InstrumentationManager.getGetterName(fieldName);
                            CtClass declaringClass = field.getDeclaringClass();
                            InstrumentationManager.instrumentClassIfNeeded(declaringClass);
                            f.replace("$_ = $0." + getterName + "();");
                        }
                    } catch (NotFoundException e) {
                        throw new RuntimeException(e);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
        }
    }
    
    /**
     * <p>
     * Nested setter classes are implemented in the following way:
     * <ul>
     * <li> static nested classes </li>
     * 
     * <li> have constructor that takes one parameter of the type same as type
     * of <code>clz</code> parameter. That because static nested class don't
     * have "second this" and they need explicit reference to the object of
     * enclosing class. They need that object since their job is to set field
     * values for that object. </li>
     * 
     * <li> override appropriate <code>set</code> method. It is enough to
     * override only one method, based on the type of the field which setter
     * class should set. </li>
     * </ul>
     * </p>
     * 
     * @param clz
     * @param f
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    protected void createNestedSetterClass(CtClass clz, CtField f)
            throws NotFoundException, CannotCompileException {

        String fieldName = f.getName();

        String setterClassName = InstrumentationManager.getSetterClassName(fieldName);
        CtClass setterClz = NestedClassCreator.createNestedClass(
                setterClassName, clz, cp.get(Setter.class.getName()));

        // add reference field to this obj
        CtField myRef = new CtField(clz, InstrumentationManager.SETTER_THIS_FIELD_NAME, setterClz);
        setterClz.addField(myRef);

        // add constructor to initialize reference field
        CtConstructor ctor = new CtConstructor(new CtClass[] { clz }, setterClz);
        ctor.setBody(InstrumentationManager.SETTER_THIS_FIELD_NAME + " = $1;");
        setterClz.addConstructor(ctor);

        // add appropriate set() implementatin
        CtClass param = f.getType();
        if (!param.isPrimitive())
            param = cp.get("java.lang.Object");
        CtClass[] params = new CtClass[] { param };

        CtMethod setterMethod = new CtMethod(CtClass.voidType, "set", params,
                setterClz);
        String setterMethodSrc = getSetterSrc(f);
        setterMethod.setBody(setterMethodSrc);

        setterClz.addMethod(setterMethod);

        InstrumentationManager.alreadyInstrumented.add(setterClz);

    }
    
    /**
     * Source for the setter's set method.
     * It looks something like this: 
     * 
     * <pre>
     *     public void set(Object val) {
     *         ___this.<b>foo</b> = (<b>Foo</b>)val;
     *     }
     * </pre>
     * 
     * @param f field that should be set
     * @return the source code for the setter's set method
     * @throws NotFoundException
     */
    private String getSetterSrc(CtField f) throws NotFoundException {

        StringBuilder setterMethodSrc = new StringBuilder(); 
        setterMethodSrc.append("'{' ");
        setterMethodSrc.append("  {0}.{1} = ({2})$1;");
        setterMethodSrc.append("'}'");

        return MessageFormat.format(setterMethodSrc.toString(), InstrumentationManager.SETTER_THIS_FIELD_NAME,
                f.getName(), f.getType().getName());

    }
    
    /**
     * Adds getter method for the setter of the given field.
     * 
     * @param clz
     * @param f
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    protected void addGetSetterMethod(CtClass clz, CtField f)
            throws NotFoundException, CannotCompileException {
    
        String fieldName = f.getName();
        String idFieldName = InstrumentationManager.getIdFieldName(fieldName);
        String getterMethodName = InstrumentationManager.getGetSetterName(fieldName);    
        String setterClassName = InstrumentationManager.getSetterClassName(fieldName);
        CtClass isetter = cp.get(Setter.class.getName());
        CtMethod getSetterMethod = new CtMethod(isetter, getterMethodName,
                new CtClass[] { CtClass.intType }, clz);        
        String getSetterSrc = getGetSetterSrc(clz, fieldName, idFieldName,
                setterClassName);        
        getSetterMethod.setBody(getSetterSrc);
        clz.addMethod(getSetterMethod);
    
    }
       
    /**
     * Returns source code for the "get setter" method.
     * It looks something like: 
     * 
     * <pre>
     *     public ISetter __korat_get_<b>foo</b>_setter(int fieldId) {
     *         this.__id_<b>foo</b> = fieldId;
     *         return new Korat_<b>foo</b>_setter(this);
     *     }
     * </pre>
     * 
     * @param clz
     * @param fieldName
     * @param idFieldName
     * @param setterClassName
     * @return source code for the "get setter" method
     */
    protected String getGetSetterSrc(CtClass clz, String fieldName,
            String idFieldName, String setterClassName) {

        StringBuffer getSetterBody = new StringBuffer(); 
        getSetterBody.append("'{'");
        getSetterBody.append("   this.{0} = $1;");
        getSetterBody.append("   return new {1}(this); ");
        getSetterBody.append("'}'");
        return MessageFormat.format(getSetterBody.toString(),  
            idFieldName, clz.getName() + "." + setterClassName);
    
    }
    
}
