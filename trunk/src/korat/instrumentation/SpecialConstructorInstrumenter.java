package korat.instrumentation;

import static javassist.Modifier.TRANSIENT;

import java.io.IOException;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.Descriptor;
import korat.testing.ITester;

/**
 * This instrumenter equips the given class with a special field
 * of <code>ITester</code> type and a special constructor that takes
 * one parameter of type <code>ITester</code> to initialize that 
 * special field. Classes under test need this field since it holds
 * an instance to be notified about field accesses.  
 * 
 * @see AbstractInstrumenter
 * @see FieldInstrumenter
 * @see ArrayFieldInstrumenter
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
class SpecialConstructorInstrumenter extends AbstractInstrumenter {

    @Override
    protected void instrument(CtClass clz) throws CannotCompileException, 
            NotFoundException, IOException {
        if (!InstrumentationManager.alreadyInstrumented.contains(clz.getSuperclass())) {
            addITesterField(clz);
        }
        addConstructors(clz);
    }
    
    /**
     * Adds <code>ITester</code> field that holds an instance of the <code>ITester</code> 
     * class that should be notified about field accesses. This field gets initialized
     * in the korat-special constructor.
     * 
     * @param clz
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    protected void addITesterField(CtClass clz) throws NotFoundException, CannotCompileException {
        String testerFieldName = InstrumentationManager.TESTER_FIELD_NAME;
        CtClass testerFieldType = cp.get(ITester.class.getName());
        CtField f = new CtField(testerFieldType, testerFieldName, clz);
        f.setModifiers(TRANSIENT);
        clz.addField(f);
    }
    
    /**
     * <p>
     * Adds needed constructors to class under test
     * </p>
     * 
     * <p>
     * First, if doesn't exist, no-arg constructor is added with empty body.
     * Then, special constructor with one parameter of <code>ITester</code>
     * type is added. That constructor is used by Korat to initialize class
     * under test passing it the instance of the <code>ITester</code>
     * interface. Class under test will store that instance in its own field
     * because that instance will be notified about every field access later on
     * (calling the <code>notifyFieldAccess</code> method).
     * </p>
     * 
     * @param clz
     * @throws NotFoundException
     * @throws CannotCompileException
     */
    protected void addConstructors(CtClass clz) throws NotFoundException,
            CannotCompileException {
        try {
            String noArgConstrDesc = Descriptor.ofConstructor(new CtClass[0]);
            clz.getConstructor(noArgConstrDesc);
        } catch (NotFoundException e) {
            addNoArgConstr(clz);
        }
        addKoratConstructor(clz);
    }

    /**
     * Adds no-arg constructor with empty body.
     * 
     * @param clz
     * @throws CannotCompileException
     */
    protected void addNoArgConstr(CtClass clz) throws CannotCompileException {
        CtConstructor noArgConstructor = new CtConstructor(new CtClass[0], clz);
        noArgConstructor.setBody("{}");
        clz.addConstructor(noArgConstructor);
    }

    /**
     * Adds a special constructor that Korat will call to initialize object
     * under test. This constructor only initializes <code>ITester</code> field.
     * 
     * @param clz
     * @throws NotFoundException
     * @throws CannotCompileException
     * @see #addITesterField(CtClass)
     */
    private void addKoratConstructor(CtClass clz) throws NotFoundException,
            CannotCompileException {

        CtClass testerFieldType = cp.get(ITester.class.getName());
        CtConstructor koratConstructor = new CtConstructor(
                new CtClass[] { testerFieldType }, clz);

        String superConstructorCall = "";
        if (InstrumentationManager.alreadyInstrumented.contains(clz.getSuperclass()))
            superConstructorCall = "super($1);";
        else
            superConstructorCall = "this();";
              
        StringBuffer constrSrc = new StringBuffer(); 
        constrSrc.append("{"); 
        constrSrc.append(superConstructorCall); 
        constrSrc.append(InstrumentationManager.TESTER_FIELD_NAME + " = $1;");                      
        constrSrc.append("}");

        koratConstructor.setBody(constrSrc.toString());
        clz.addConstructor(koratConstructor);

    }

}
