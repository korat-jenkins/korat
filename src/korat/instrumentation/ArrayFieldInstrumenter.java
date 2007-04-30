package korat.instrumentation;

import static javassist.Modifier.isAbstract;
import static javassist.Modifier.isNative;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ClassFile;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.MethodInfo;
import korat.instrumentation.bytecode.ArrayBytecodesVisitor;
import korat.instrumentation.bytecode.BytecodeInstruction;
import korat.instrumentation.bytecode.OperandStack;
import korat.instrumentation.bytecode.StackTracerVisitor;

/**
 * This class does all the instrumentation related to array fields.
 * First, it replaces declarations all array fields with the corresponding 
 * <code>IKoratArray</code> field declarations. Then, in all methods, it 
 * replaces accesses to those fields with appropriate method calls to their 
 * corresponding <code>IKoratArray</code> fields. For instance, the following
 * piece of code
 * <pre>
 *     class C {
 *         int[] foo;
 *         void f() {
 *             foo = new int[10];
 *             foo[0] = 15;
 *             int x = foo[0];
 *             x += foo.length()
 *         }
 *     }
 * </pre> 
 * after the instrumentation looks like
 * <pre>
 *     class C {
 *         Korat_Array_int __koratArray_foo;
 *         // ... (stuff that FieldInstrumenter adds to foo field)
 *         void f() {
 *             __koratArray_foo = new Korat_Array_int(10);
 *             __koratArray_foo.set(0, 15);
 *             int x = __koratArray_foo.get(0);
 *             x += __koratArray_foo.getLength();    
 *         }
 *     }
 * </pre>
 * @see ArrayGenerator
 * @see AbstractInstrumenter
 * @see FieldInstrumenter
 * @see SpecialConstructorInstrumenter

 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
class ArrayFieldInstrumenter extends FieldInstrumenter {

    /**
     * Keep the track of the arrays fields that need to be removed at the end.
     */
    private Set<CtField> fieldsToRemove = new HashSet<CtField>();

    @Override
    protected void instrumentFieldDeclarations(CtClass clz) throws NotFoundException, CannotCompileException {
        for (CtField f : clz.getDeclaredFields()) {
            if (!shouldProcessField(f)) 
                continue;
            if (f.getType().isArray())
                handleArrayFieldDeclaration(clz, f);
        }
    }
    
    /**
     * For the given array field creates new field of the corresponding
     * KoratArray_<b>fieldType</b> type. For the newly created field,
     * <code>handleNonArrayField</code> is called. The given field is than
     * added to <code>fieldsToRemove</code> set since it is not possible to
     * delete it right away.
     * 
     * @see FieldInstrumenter#handleFieldDeclaration(CtClass, CtField)
     */
    protected void handleArrayFieldDeclaration(CtClass clz, CtField f)
            throws CannotCompileException, NotFoundException {
        CtClass arrayFieldType = f.getType();
        ArrayGenerator arrGen = new ArrayGenerator(arrayFieldType);
        CtClass arrayClz = arrGen.getArrayCtClass();
        String koratArrayFieldName = InstrumentationManager.getKoratArrayFieldName(f.getName());
        CtField arrField = new CtField(arrayClz, koratArrayFieldName, clz);
        clz.addField(arrField);
        handleFieldDeclaration(clz, arrField);
        fieldsToRemove.add(f);
    }

    /**
     * <p>
     * Traverses through all methods in the given class, looks for the bytecode
     * instructions related to arrays and modifies them appropriately. Since
     * all array fields have been replaced with fields of the KoratArray
     * classes, field accesses also have to be replaced with method calls to
     * corresponding substitution field.
     * <p>
     * 
     */
    @SuppressWarnings("unchecked")
    @Override
    protected void replaceFieldAccesses(CtClass clz) {

        ClassFile cf = clz.getClassFile();
        ConstPool cPool = cf.getConstPool();
        List<MethodInfo> methods = cf.getMethods();
        try {

            OperandStack stack = new OperandStack();
            
            for (MethodInfo mi : methods) {
                CodeAttribute ca = mi.getCodeAttribute();
                if (ca == null)
                    continue;
                if (isAbstract(mi.getAccessFlags()) || 
                        isNative(mi.getAccessFlags()))
                    continue;
                if (InstrumentationManager.isKoratMethod(mi.getName()))
                    continue;

                CodeIterator cit = ca.iterator();
                stack.clear();
                ArrayBytecodesVisitor arrayBytecodesVisitor = 
                    new ArrayBytecodesVisitor(cp, cPool, stack);
                StackTracerVisitor tracerVisitor = 
                    new StackTracerVisitor(cp, cPool, clz, mi, stack);

                while (cit.hasNext()) {
                    int idx = cit.next();
                    BytecodeInstruction instr = new BytecodeInstruction(cit, idx);
                    // first let the tracer
                    // since it won't modify any bytecode
                    instr.accept(tracerVisitor); 
                    // then let arrayBytecode visitor
                    instr.accept(arrayBytecodesVisitor);
                }

                ca.setMaxStack(ca.computeMaxStack());
            }
            removeOriginalArrayFields(clz);

        } catch (BadBytecode e) {
            System.err.println("INSTRUMENTATION ERRROR!");
            e.printStackTrace();
            System.exit(1);
        } catch (NotFoundException e) {
            System.err.println("INSTRUMENTATION ERRROR!");
            e.printStackTrace();
            System.exit(1);
        }

    }
    
    /**
     * Removes original array fields from the class.
     * 
     * @param clz - class of the fields to be removed
     * @throws NotFoundException
     */
    private void removeOriginalArrayFields(CtClass clz)
            throws NotFoundException {
        List<CtField> list = new LinkedList<CtField>();
        for (CtField f : fieldsToRemove) {
            if (f.getType() == clz) {
                clz.removeField(f);
                list.add(f);
            }
        }
        fieldsToRemove.removeAll(list);
    }
    
}
