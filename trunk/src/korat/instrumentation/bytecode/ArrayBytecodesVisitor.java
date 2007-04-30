package korat.instrumentation.bytecode;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.Opcode;
import korat.instrumentation.ArrayGenerator;
import korat.instrumentation.InstrumentationManager;
import korat.instrumentation.bytecode.OperandStack.ConsumedByKind;
import korat.instrumentation.bytecode.OperandStack.ElemKind;
import korat.instrumentation.bytecode.OperandStack.StackElem;

/**
 * Visitor that handles each bytecode instruction in order to instrument all
 * accesses to array fields.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class ArrayBytecodesVisitor extends JavassistInstructionVisitor {

    public ArrayBytecodesVisitor(ClassPool classPool, ConstPool constPool,
            OperandStack operandStack) {
        super(classPool, constPool, operandStack);
    }

    /**
     * Replaces retrieval of the array field with the retrieval of its
     * corresponding <em>KoratArray</em> field.
     * 
     * @param cit -
     *            CodeIterator
     * @param idx -
     *            current position in the <code>cit</code>
     * @param cpIdx -
     *            index for the array field in the <code>cPool</code>
     * @throws NotFoundException
     */
    private void delegateArrayGetfield(int cpIdx, CodeIterator cit, int idx) {
        try {

            if (!checkArrayType(cpIdx))
                return;

            String fieldName = constPool.getFieldrefName(cpIdx);
            int classIdx = constPool.getFieldrefClass(cpIdx);
            String targetClassName = constPool.getClassInfo(classIdx);

            CtClass targetClz = classPool.get(targetClassName);
            String koratArrayFieldName = InstrumentationManager.getKoratArrayFieldName(fieldName);
            CtClass koratArrayClass = targetClz.getDeclaredField(
                    koratArrayFieldName).getType();

            String methodDesc = Descriptor.ofMethod(koratArrayClass,
                    new CtClass[0]);

            Bytecode codes = new Bytecode(constPool);
            codes.addInvokevirtual(targetClz,
                    InstrumentationManager.getGetterName(koratArrayFieldName),
                    methodDesc);

            cit.write(codes.get(), idx);

        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitGETFIELD(BytecodeInstruction instr) {
        int cpIdx = instr.u16bitAt(1);
        String fieldType = constPool.getFieldrefType(cpIdx);
        if (fieldType.indexOf("[") != -1) { // array field
            delegateArrayGetfield(cpIdx, instr.getCit(), instr.getIdx());
        }
    }

    /**
     * Replaces setting array field with the setting its corresponding
     * <em>KoratArray</em> field.
     * 
     * @param cit -
     *            iterator through method's body code
     * @param idx -
     *            current position in the <code>cit</code>
     * @param cpIdx -
     *            index for the array field in the <code>cPool</code>
     * @throws NotFoundException
     */
    private void delegateArrayPutfield(int cpIdx, CodeIterator cit, int idx) {
        try {

            if (!checkArrayType(cpIdx))
                return;

            String fieldName = constPool.getFieldrefName(cpIdx);
            int classIdx = constPool.getFieldrefClass(cpIdx);
            String targetClassName = constPool.getClassInfo(classIdx);

            CtClass targetClz = classPool.get(targetClassName);

            String koratArrayFieldName = InstrumentationManager.getKoratArrayFieldName(fieldName);
            CtField koratArrayField = targetClz.getDeclaredField(koratArrayFieldName);

            String descr = Descriptor.of(koratArrayField.getType());

            Bytecode codes = new Bytecode(constPool);
            codes.addPutfield(targetClz, koratArrayFieldName, descr);

            cit.write(codes.get(), idx);

        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void visitPUTFIELD(BytecodeInstruction instr) {
        int cpIdx = instr.u16bitAt(1);
        String fieldType = constPool.getFieldrefType(cpIdx);
        if (fieldType.indexOf("[") != -1) { // array field
            delegateArrayPutfield(cpIdx, instr.getCit(), instr.getIdx());
        }
    }

    /**
     * <p>
     * This is used when replacing NEWARRAY bytecode instructions. NEWARRAY is
     * used for arrays of primitive types creation. NEWARRAY instruction is 2
     * byte long. The second byte represents the primitive type of the array: 4 -
     * boolean, 5 - char, and so on. <code>koratClassNames</code> maps those
     * byte numbers to </em>KoratArray</em> class names.</p>
     * 
     */
    private static String[] koratArrayClassNames = new String[16];

    static {

        koratArrayClassNames[Opcode.T_BOOLEAN] = ArrayGenerator.getArrayClassName("boolean");
        koratArrayClassNames[Opcode.T_CHAR] = ArrayGenerator.getArrayClassName("char");
        koratArrayClassNames[Opcode.T_DOUBLE] = ArrayGenerator.getArrayClassName("double");
        koratArrayClassNames[Opcode.T_FLOAT] = ArrayGenerator.getArrayClassName("float");
        koratArrayClassNames[Opcode.T_BYTE] = ArrayGenerator.getArrayClassName("byte");
        koratArrayClassNames[Opcode.T_SHORT] = ArrayGenerator.getArrayClassName("short");
        koratArrayClassNames[Opcode.T_INT] = ArrayGenerator.getArrayClassName("int");
        koratArrayClassNames[Opcode.T_LONG] = ArrayGenerator.getArrayClassName("long");

    };

    /**
     * Replaces creation of the array with the creation of the <em>KoratArray</em>
     * field. Just invokes its constructor with the <code>int</code> parameter
     * that represents the length of the array.
     * 
     * @param arrayType - 
     *            type of array in terms of JVM
     * @param cit -
     *            iterator through method's body code
     * @param idx -
     *            current position in the <code>cit</code>
     * @throws BadBytecode
     */
    private void delegateNewArray(int arrayType, CodeIterator cit, int idx) {

        // check how this new array is going to be consumed
        if (operandStack.peek().consumedBy != ConsumedByKind.PUTFIELD)
            return;
        
        String koratClassName = koratArrayClassNames[arrayType];
        int locals = cit.get().getMaxLocals();
        int localVar1 = locals + 1;

        try {

            Bytecode codes = new Bytecode(constPool);
            codes.addIstore(localVar1);
            codes.addNew(koratClassName);
            codes.addOpcode(Opcode.DUP);
            codes.addIload(localVar1);
            String descr = Descriptor.ofMethod(CtClass.voidType,
                    new CtClass[] { CtClass.intType });
            codes.addInvokespecial(koratClassName, "<init>", descr);

            int diff = codes.getSize() - 2;
            // -2 because original instruction
            // (NEWARRAY) is 2 bytes long.

            cit.insertGap(idx, diff);
            cit.write(codes.get(), idx);
            cit.get().setMaxLocals(localVar1 + 1);

        } catch (BadBytecode e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void visitNEWARRAY(BytecodeInstruction instr) {
        delegateNewArray(instr.byteAt(1), instr.getCit(), instr.getIdx());
    }

    private void delegateANewArray(int cpIdx, CodeIterator cit, int idx) {

        // check how this new array is going to be consumed
        if (operandStack.peek().consumedBy != ConsumedByKind.PUTFIELD)
            return;
        
        String arrayComponentClassName = constPool.getClassInfo(cpIdx);
        try {

            if (!checkArrayType(arrayComponentClassName))
                return;
            String koratClassName = ArrayGenerator.getArrayClassName(arrayComponentClassName);

            int locals = cit.get().getMaxLocals();
            int localVar1 = locals + 1;

            Bytecode codes = new Bytecode(constPool);
            codes.addIstore(localVar1);
            codes.addNew(koratClassName);
            codes.addOpcode(Opcode.DUP);
            codes.addIload(localVar1);
            String descr = Descriptor.ofMethod(CtClass.voidType,
                    new CtClass[] { CtClass.intType });
            codes.addInvokespecial(koratClassName, "<init>", descr);

            int diff = codes.getSize() - 3;
            // -3 because original instruction
            // (ANEWARRAY) is 2 bytes long.

            cit.insertGap(idx, diff);
            cit.write(codes.get(), idx);
            cit.get().setMaxLocals(localVar1 + 1);

        } catch (BadBytecode e) {
            throw new RuntimeException(e);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    @Override
    public void visitANEWARRAY(BytecodeInstruction instr) {
        delegateANewArray(instr.u16bitAt(1), instr.getCit(), instr.getIdx());
    }

    /**
     * Replaces all kinds of array accesses (ARRAYLENGTH, IALOAD, IASTORE,
     * LALOAD, LASTORE, ...) with the specified method call.
     * 
     * @param cit -
     *            code iterator
     * @param idx -
     *            current position in the <code>cit</code>
     * @param methodName -
     *            name of the method to be called
     * @param retType -
     *            return type of the method to be called
     * @param args -
     *            actual arguments for the method to be called
     * @throws BadBytecode
     */
    private void delegateArrayAccess(String methodName, CtClass retType,
            CtClass[] args, CodeIterator cit, int idx) {

        try {

            StackElem elem = operandStack.getLastPoppedOut();
            if (elem.kind != ElemKind.FIELD) {
                return;
            }
            
            CtClass opType = elem.opType;
            if (!opType.isArray()) {
                throw new RuntimeException(
                        "Invalid operand type on top of the stack. Should be an instance of the array class "
                                + "but is instance of " + opType.getName());
            }

            if (!checkArrayType(opType.getComponentType()))
                return;

            opType = getKoratArrayType(opType);
            Bytecode codes = new Bytecode(constPool);
            codes.addInvokevirtual(opType, methodName, retType, args);

            int diff = codes.getSize() - 1;
            // -1 because original instruction (GETARRAYLENGTH, IALOAD,
            // LALOAD, ..., IASTORE, LASTORE, ...) is only one byte long.

            cit.insertGap(idx, diff);
            cit.write(codes.get(), idx);

        } catch (BadBytecode e) {
            throw new RuntimeException(e);
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private CtClass getKoratArrayType(CtClass opType) {
        CtClass clz = null;
        String arrayType = "";
        String name = "";
        try {
            arrayType = opType.getComponentType().getName();
            name = ArrayGenerator.getArrayClassName(arrayType);
            clz = classPool.get(name);
        } catch (NotFoundException e) {
            throw new RuntimeException("Cannot find Korat array type: arrayType = " 
                    + arrayType + ",  koratArrayClassName = " + name, e);
        }
        return clz;
    }

    @Override
    public void visitAALOAD(BytecodeInstruction instr) {
        try {
            CtClass componentType = operandStack.getLastPoppedOut().opType.getComponentType();
            if (componentType == null) {
                throw new IllegalStateException(
                        "Operand on top of the stack is not of an array type.");
            }
            delegateArrayAccess("get", componentType,
                    new CtClass[] { CtClass.intType }, instr.getCit(),
                    instr.getIdx());
        } catch (NotFoundException e) {
            throw new RuntimeException("vistAALOAD: Cannot find array component type", e);
        }
    }

    @Override
    public void visitAASTORE(BytecodeInstruction instr) {
        try {
            CtClass componentType = operandStack.getLastPoppedOut().opType.getComponentType();
            if (componentType == null) {
                throw new IllegalStateException(
                        "Operand on top of the stack is not of an array type.");
            }
            delegateArrayAccess("set", CtClass.voidType, new CtClass[] {
                    CtClass.intType, componentType }, instr.getCit(),
                    instr.getIdx());
        } catch (NotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * array.length -> __korat_KoratArray_array.getLength()
     */
    @Override
    public void visitARRAYLENGTH(BytecodeInstruction instr) {
        delegateArrayAccess("getLength", CtClass.intType, new CtClass[0],
                instr.getCit(), instr.getIdx());
    }

    /**
     * byteArray[i] -> __korat_KoratArray_byteArray.get(i)
     */
    @Override
    public void visitBALOAD(BytecodeInstruction instr) {
        delegateArrayAccess("get", CtClass.byteType,
                new CtClass[] { CtClass.intType }, instr.getCit(),
                instr.getIdx());
    }

    /**
     * byteArray[i] = x -> __korat_KoratArray_byteArray.set(i, x)
     */
    @Override
    public void visitBASTORE(BytecodeInstruction instr) {
        delegateArrayAccess("set", CtClass.voidType, new CtClass[] {
                CtClass.intType, CtClass.byteType }, instr.getCit(),
                instr.getIdx());
    }

    /**
     * charArray[i] -> __korat_KoratArray_charArray.get(i)
     */
    @Override
    public void visitCALOAD(BytecodeInstruction instr) {
        delegateArrayAccess("get", CtClass.charType,
                new CtClass[] { CtClass.intType }, instr.getCit(),
                instr.getIdx());
    }

    /**
     * charArray[i] = x -> __korat_KoratArray_charArray.set(i, x)
     */
    @Override
    public void visitCASTORE(BytecodeInstruction instr) {
        delegateArrayAccess("set", CtClass.voidType, new CtClass[] {
                CtClass.intType, CtClass.charType }, instr.getCit(),
                instr.getIdx());
    }

    /**
     * doubleArray[i] -> __korat_KoratArray_doubleArray.get(i)
     */
    @Override
    public void visitDALOAD(BytecodeInstruction instr) {
        delegateArrayAccess("get", CtClass.doubleType,
                new CtClass[] { CtClass.intType }, instr.getCit(),
                instr.getIdx());
    }

    /**
     * doubleArray[i] = x -> __korat_KoratArray_doubleArray.set(i, x)
     */
    @Override
    public void visitDASTORE(BytecodeInstruction instr) {
        delegateArrayAccess("set", CtClass.voidType, new CtClass[] {
                CtClass.intType, CtClass.doubleType }, instr.getCit(),
                instr.getIdx());
    }

    /**
     * floatArray[i] -> __korat_KoratArray_floatArray.get(i)
     */
    @Override
    public void visitFALOAD(BytecodeInstruction instr) {
        delegateArrayAccess("get", CtClass.floatType,
                new CtClass[] { CtClass.intType }, instr.getCit(),
                instr.getIdx());
    }

    /**
     * floatArray[i] = x -> __korat_KoratArray_floatArray.set(i, x)
     */
    @Override
    public void visitFASTORE(BytecodeInstruction instr) {
        delegateArrayAccess("set", CtClass.voidType, new CtClass[] {
                CtClass.intType, CtClass.floatType }, instr.getCit(),
                instr.getIdx());
    }

    /**
     * intArray[i] -> __korat_KoratArray_intArray.get(i)
     */
    @Override
    public void visitIALOAD(BytecodeInstruction instr) {
        delegateArrayAccess("get", CtClass.intType,
                new CtClass[] { CtClass.intType }, instr.getCit(),
                instr.getIdx());
    }

    /**
     * intArray[i] = x -> __korat_KoratArray_intArray.set(i, x)
     */
    @Override
    public void visitIASTORE(BytecodeInstruction instr) {
        delegateArrayAccess("set", CtClass.voidType, new CtClass[] {
                CtClass.intType, CtClass.intType }, instr.getCit(),
                instr.getIdx());
    }

    /**
     * longArray[i] -> __korat_KoratArray_longArray.get(i)
     */
    @Override
    public void visitLALOAD(BytecodeInstruction instr) {
        delegateArrayAccess("get", CtClass.longType,
                new CtClass[] { CtClass.intType }, instr.getCit(),
                instr.getIdx());
    }

    /**
     * longArray[i] = x -> __korat_KoratArray_longArray.set(i, x)
     */
    @Override
    public void visitLASTORE(BytecodeInstruction instr) {
        delegateArrayAccess("set", CtClass.voidType, new CtClass[] {
                CtClass.intType, CtClass.longType }, instr.getCit(),
                instr.getIdx());
    }

    /**
     * shortArray[i] -> __korat_KoratArray_shortArray.get(i)
     */
    @Override
    public void visitSALOAD(BytecodeInstruction instr) {
        delegateArrayAccess("get", CtClass.shortType,
                new CtClass[] { CtClass.intType }, instr.getCit(),
                instr.getIdx());
    }

    /**
     * shortArray[i] = x -> __korat_KoratArray_shortArray.set(i, x)
     */
    @Override
    public void visitSASTORE(BytecodeInstruction instr) {
        delegateArrayAccess("set", CtClass.voidType, new CtClass[] {
                CtClass.intType, CtClass.shortType }, instr.getCit(),
                instr.getIdx());
    }

}
