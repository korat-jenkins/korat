package korat.instrumentation;

import static javassist.Modifier.TRANSIENT;

import java.io.IOException;
import java.text.MessageFormat;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.NotFoundException;

/**
 * Obsolete. 
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 *
 */
@Deprecated
class TouchInstrumenter extends AbstractInstrumenter {

    @Override
    protected void instrument(CtClass clz) throws CannotCompileException,
            NotFoundException, IOException {
        addTouchableInterface(clz);
        addTouchableImplementation(clz);
    }
    
    protected void addTouchableInterface(CtClass clz) {
        try {
            CtClass touchable = cp.get(IKoratTouchable.class.getName());
            clz.addInterface(touchable);
        } catch (NotFoundException e) {
            // should never reach this code
            throw new RuntimeException(e);
        }
    }

    /**
     * Implements KoratTouchable interface
     * 
     * @see IKoratTouchable
     */
    protected void addTouchableImplementation(CtClass clz) {
        addVisitedInTraversalField(clz);
        addTouchingInitializationMethod(clz);
        addTouchMethod(clz);
    }

    /**
     * <p>
     * Adds <code>__korat__touched </code> boolean flag, which signals whether
     * this object has already been touched.
     * 
     * <p>
     * Part of IKoratTouchable implementation
     * 
     * @see IKoratTouchable
     * 
     */
    protected void addVisitedInTraversalField(CtClass clz) {
        String koratTouchedName = IKoratTouchable.koratTouchedName;
        try {
            CtField koratTouched = new CtField(CtClass.booleanType,
                    koratTouchedName, clz);
            koratTouched.setModifiers(TRANSIENT);
            clz.addField(koratTouched);
        } catch (CannotCompileException e1) {
            throw new RuntimeException(e1);
        }
    }

    /**
     * Initializes process of touching by resetting __korat__touched flag.
     * Implements <code>__korat__touch__initialize()</code> operation from the
     * <code>IKoratTouchable</code> interface
     * 
     * 
     * <pre>
     *    public void __korat__touch__initialize() {
     *        if (!__korat__touched)
     *            return;
     *        __korat__touched = false;
     *        
     *        // For each field (denoted by its name {FieldName}) in this object:
     *        
     *        if ( {FieldName} != null &amp;&amp; {FieldName} instanceof korat.instrumentation.IKoratTouchable)
     *             ((korat.instrumentation.IKoratTouchable) {{FieldName}}).__korat__touch__initialize();
     *        //...
     *        
     *    }
     * </pre>
     * 
     * @see IKoratTouchable
     */
    protected void addTouchingInitializationMethod(CtClass clz) {

        String koratTouchedName = IKoratTouchable.koratTouchedName;
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

        String touchAllTemplate = " if ({0} != null "
            + "       && {0} instanceof korat.instrumentation.IKoratTouchable) \n"
            + "    ((korat.instrumentation.IKoratTouchable) {0} ).{1}(); \n";
        CtField[] fields = clz.getDeclaredFields();
        for (CtField f : fields) {

            String fieldName = f.getName();
            String getterName = InstrumentationManager.getGetterName(fieldName);
            try {

                clz.getDeclaredMethod(getterName);
                // Searches is this field is supposed to be visited during
                // touching. Those fields have already been instrumeted and
                // their getter methods created. The instrumentation continues 
                // only if method exists. Otherwise, continues with the next field
                //
                // This way, id and other unwanted fields are filtered out from
                // touching process.

                if (!f.getType().isPrimitive()) {
                    String touchInitAllBody = MessageFormat.format(
                            touchAllTemplate, new Object[] { fieldName,
                                    touchInitName });
                    touchInitBodyBuilder.append(touchInitAllBody);
                }

            } catch (NotFoundException e) {
                // there is not such method, continue loop
            }

        }

        String touchInitAllBody = "{\n" + touchInitBodyBuilder.toString()
                + "\n}";

        try {
            touchInit.setBody(touchInitAllBody);
            clz.addMethod(touchInit);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }

    }

    /**
     * <p>
     * Adds touch method to the class that accesses all fields of given object
     * and invokes touch method on all objects associated with accessed fields.
     * 
     * <p>
     * This method is implemented as follows:
     * 
     * <pre>
     *       
     *       public void __korat__touch() {
     *        if (__korat__touched)
     *          return;
     *          
     *        koratTouched = true;
     *        
     *        // if superclass is koratTouchable
     *        super.__korat__touch();
     *        
     *        
     *        // - For Each Primitive Field-
     *               __get__fieldnameA__();     
     *        
     *        // - or -    
     *           
     *        // - For Each Non-Primitive Field     
     *               __get__fieldnameB__();
     *               if (fieldnameB != null &amp;&amp; fieldnameB instanceOf KoratTouchable)
     *                 ((KoratTouchable) fieldnameB).__korat__touch();
     *        
     *        }
     *        
     *       
     * </pre>
     * 
     * @see IKoratTouchable
     */
    protected void addTouchMethod(CtClass clz) {

        String koratTouchedName = IKoratTouchable.koratTouchedName;
        String touchName = IKoratTouchable.touchName;
        CtClass[] touchParam = new CtClass[0];

        CtMethod touch = new CtMethod(CtClass.voidType, touchName, touchParam,
                clz);

        //COMPAT1.4
        //StringBuilder touchBodyBuilder = new StringBuilder();
        StringBuffer touchBodyBuilder = new StringBuffer();

        StringBuffer checkIsTouched = new StringBuffer(); 
            checkIsTouched.append(" if (" + koratTouchedName + ")");
            checkIsTouched.append("    return; ");
            checkIsTouched.append(" else ");
            checkIsTouched.append(koratTouchedName + " = true;");
        touchBodyBuilder.append(checkIsTouched);

        // calls superclass __korat__touch()
        CtClass superClass;
        try {

            superClass = clz.getSuperclass();
            if (superClass != null) {

                boolean instrument = false;
                CtClass[] superclassInterfaces = clz.getSuperclass().getInterfaces();
                for (CtClass i : superclassInterfaces)
                    if ((Class) i.getClass() == IKoratTouchable.class) {
                        instrument = true;
                        break;
                    }

                if (instrument) {
                    String superCall = "super.__korat__touch();";
                    touchBodyBuilder.append(superCall);
                }

            }
        } catch (NotFoundException e1) {
            throw new RuntimeException(e1);
        }

        CtField[] fields = clz.getDeclaredFields();
        for (CtField f : fields) {

            String fieldName = f.getName();
            String getterName = InstrumentationManager.getGetterName(fieldName);
            try {

                clz.getDeclaredMethod(getterName);
                // continues only if method exists. Id and other
                // unwanted fields are filtered out.

                // touch field - invoke get_field method
                String fieldAccessTemplate = "{0}(); \n";
                String fieldAccessBody = MessageFormat.format(
                        fieldAccessTemplate, getterName);
                touchBodyBuilder.append(fieldAccessBody);

                // touch all fields of object associated with this field
                if (!f.getType().isPrimitive()) {
                    String touchAllTemplate = " if ({0} != null "
                            + "       && {0} instanceof korat.instrumentation.IKoratTouchable) \n"
                            + "    ((korat.instrumentation.IKoratTouchable) {0} ).{1}(); \n";
                    String touchAllBody = MessageFormat.format(
                            touchAllTemplate, new Object[] { fieldName,
                                    touchName });
                    touchBodyBuilder.append(touchAllBody);
                }

            } catch (NotFoundException e) {
                // there is not such method, continue loop
            }

        }

        String touchBody = "{\n" + touchBodyBuilder.toString() + "\n}";

        try {
            touch.setBody(touchBody);
            clz.addMethod(touch);
        } catch (CannotCompileException e) {
            throw new RuntimeException(e);
        }

    }

}
