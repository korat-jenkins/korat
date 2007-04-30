package korat.instrumentation.bytecode;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.ExceptionTable;
import javassist.bytecode.MethodInfo;
import korat.instrumentation.bytecode.OperandStack.ConsumedByKind;
import korat.instrumentation.bytecode.OperandStack.ElemKind;
import korat.instrumentation.bytecode.OperandStack.StackElem;

/**
 * <p>This class is intended to be used for tracing the types of values on the 
 * operand stack during the traversal of the instructions of a <b>single</b>
 * method.</p> 
 * 
 * <p>The <code>accept</code> method should be called on each
 * bytecode instruction of a certain method with an instance of this class
 * as a visitor. After returning, current state of the operand stack can be
 * obtained. </p>
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class StackTracerVisitor extends JavassistInstructionVisitor {

    /**
     * <p>List of local variables for the current method. This list 
     * keeps only the types of local variables, since that is only
     * needed for the purpose of stack tracing. </p>
     * 
     * <p> NOTE: method parameters are also treated as local varables. 
     * If method takes <em>n</em> parameters, then first <em>n</em>
     * entries in this list is reserved for those parameters. </p> 
     */
    private List<CtClass> locals;

    /**
     * <p>Set of handle PCs for all catch blocks in the code.</p>
     */
    private Set<Integer> handlePCs;

    /**
     * Code iterator is needed for computeConstumedBy operation.
     * This field is set in the <code>preVisit</code> method for 
     * each bytecode instruction.
     */
    private CodeIterator cit;
    
    /**
     * Creates visitor and initializes the list of local variables. 
     * Method parameters are also treated as local variables so this is 
     * the right time to initialize first <em>n</em> entries (<em>n</em> is 
     * the number parameters that method takes) with the types of method params.
     * 
     * @param classPool - javassist's class pool
     * @param constPool - javassist's constant pool
     * @param mi - javassist's method info
     * @param operandStack - operand stack 
     */
    public StackTracerVisitor(ClassPool classPool, ConstPool constPool, 
            CtClass clz, MethodInfo mi, OperandStack operandStack) {
        super(classPool, constPool, operandStack);
        initLocals(clz, mi);
        initHandlePCs(mi);
    }

    private void initHandlePCs(MethodInfo mi) {
        ExceptionTable et = mi.getCodeAttribute().getExceptionTable();
        int n = et.size();
        handlePCs = new HashSet<Integer>(5);
        for (int i = 0; i < n; i++) {
            handlePCs.add(et.handlerPc(i));
        }
    }

    private void initLocals(CtClass clz, MethodInfo mi) {
        int maxLocals = mi.getCodeAttribute().getMaxLocals();
        locals = new ArrayList<CtClass>(maxLocals);
        try {
            CtClass[] params = Descriptor.getParameterTypes(mi.getDescriptor(), classPool);
            int thisVar = 0;
            if (!Modifier.isStatic(mi.getAccessFlags())) {
                thisVar = 1;
                locals.add(clz);
            } 
            for (int i = 0; i < params.length; i++) {
                locals.add(null);
                locals.set(thisVar + i, params[i]);
            }
            for (int i = thisVar + params.length; i < maxLocals; i++) {
                locals.add(null);
            }
        } catch (NotFoundException e) {
            throw new RuntimeException("Cannot find parameters of method: " + mi.getName(), e);
        }
    }

    /* --------------------------------------------------------------------
     *  UTILS
     * -------------------------------------------------------------------- */
    
    private boolean doCheck = false;
    
    /**
     * Checks type compatibility: <code>actual</code> type has to be subtype of the 
     * <code>expected</code> type, i.e. (in terms of java reflection) <code>expected</code>
     * has to be assignable from <code>actual</code>
     */
    private void checkType(CtClass expected, CtClass actual) {
        if (doCheck) {
            try { 
                // NOT COMPLETE!  
                //(since it is impossible to check all that statically)
                if (actual != null && !actual.subtypeOf(expected))
                    throw new RuntimeException("Type missmatch: expected: " + 
                            expected.getName() + ", actual: " + actual.getName() + "!");
                if (actual == null && !expected.subtypeOf(objectType)) {
                    throw new RuntimeException("Type missmatch: expected: " + 
                            expected.getName() + ", actual: null!");
                }
            } catch (NotFoundException e) {
                throw new RuntimeException("Type not found", e);
            }
        }
    }

    /**
     * Checks only if the given type is array. Doesn't check anything about
     * array component type.
     */
    private void checkArray(CtClass actual) {
        if (doCheck) {
            if (!actual.isArray())
                throw new RuntimeException("Type missmatch: array expected!");
        }
    }
    
    /**
     * Checks only if the given type is array and if its component
     * type is compatible with the given component type. 
     */
    private void checkArray(CtClass actual, CtClass componentType) {
        if (doCheck) {
            if (!actual.isArray())
                throw new RuntimeException("Type missmatch: array expected!");
            try {
                checkType(componentType, actual.getComponentType());
            } catch (NotFoundException e) {
                throw new RuntimeException("Array component not found!");
            }
        }
    }

    private boolean arrayPushed;
    
    private int pushedArrayIdx;
    
    private boolean lookingAhead = false;

    private void computeConsumedBy() {
        if (lookingAhead)
            return;
        if (pushedArrayIdx == -1)
            return;
        lookingAhead = true;
        OperandStack origStack = operandStack;
        operandStack = operandStack.getClone();
        StackElem elem = operandStack.get(pushedArrayIdx);
        int elemIdx = pushedArrayIdx;
        int pos = cit.lookAhead();
        try {
            while (true) {
                int idx = cit.next();
                BytecodeInstruction instr = new BytecodeInstruction(cit, idx);
                instr.accept(this);
                if (elemIdx >= operandStack.size() || elem != operandStack.get(elemIdx)) {
                    setConsumedBy(elem, instr);
                    operandStack = origStack;
                    break;
                }
            }
        } catch (Exception e) {
            //give up and roll back
            operandStack = origStack;
        } finally {
            cit.move(pos);
            lookingAhead = false;
        }
    }

    private void setConsumedBy(StackElem elem, BytecodeInstruction instr) {
        if (instr.isInvoke()) {
            elem.consumedBy = ConsumedByKind.INVOKE;
        } else if (instr.isPutField()) {
            elem.consumedBy = ConsumedByKind.PUTFIELD;
        } else if (instr.isLocalStore()) {
            elem.consumedBy = ConsumedByKind.LOCAL_STORE;
        } else if (instr.isArrayStore()) {
            elem.consumedBy = ConsumedByKind.ARRAY_STORE;
        } else {
            elem.consumedBy = ConsumedByKind.UNKNOWN;
        }
    }

    private void push(StackElem elem) {
        operandStack.push(elem);
        if (!lookingAhead && elem != null && elem.opType != null && elem.opType.isArray()) {
            arrayPushed = true;
            pushedArrayIdx = operandStack.size() - 1;
        }
    }
    
    private void push(CtClass type, ElemKind kind) {
        StackElem elem = new StackElem();
        elem.opType = type;
        elem.kind = kind;
        push(elem);
    }
    
    private void push(CtClass type) {
        push(type, ElemKind.LOCAL);
    }
    
    /* --------------------------------------------------------------------
     *  HELPERS
     * -------------------------------------------------------------------- */
    
    /**
     * Pops <em>index</em> and <em>array</em> values from the operand 
     * stack and pushes the array's component type which is provided
     * through the <code>type</code> parameter. This helper is used
     * for handling various <em>_ALOAD</em> instructions, but not for
     * <em>AALOAD</em>.
     * 
     * @param type - array component type
     */
    private void arrayLoad(CtClass type) {
        StackElem idx = operandStack.pop();
        checkType(intType, idx.opType);
        StackElem arr = operandStack.pop();
        checkArray(arr.opType);
        push(type, arr.kind);
    }
    
    /**
     * Pops <em>value</em>, <em>index</em> and <em>array</em> values 
     * from the operand stack. This helper is used for handling 
     * various <em>_ASTORE</em> instructions.
     * 
     * @param type - array component type
     */
    private void arrayStore(CtClass type) {
        StackElem val = operandStack.pop();
        checkType(type, val.opType);
        StackElem idx = operandStack.pop();
        checkType(intType, idx.opType);
        StackElem arr = operandStack.pop();
        checkArray(arr.opType, val.opType);
    }
    
    /**
     * <p>Gets the type at <code>idx</code>-th position in the local variables
     * list and pushed that type on the operand stack. </p> 
     * 
     * <p>Note that java specification guarantees that all local variables 
     * must be initialized before reading, so the corresponding store method 
     * will be always called before this method for all local variables except
     * for method parameters (which are also kept in the same list and treated 
     * exactly the same as the local variables): they are initialized in the 
     * constructor's body. This helper is used for handling various 
     * instructions for loading a local variable to operand stack.</p>
     * 
     * @param type - type of the local variable
     * @param idx - index of the local variable
     */
    private void localLoad(CtClass type, int idx) {
        CtClass val = locals.get(idx);
        checkType(type, val);
        push(val, ElemKind.LOCAL);
    }
    
    /**
     * Pops the value from the stack and stores it in the local variables list. 
     * This helper is used for handling various instructions for storing a value
     * from the top of the stack to a local variable.  
     * 
     * @param type - type of the local variable
     * @param idx - index of the local variable
     */
    private void localStore(CtClass type, int idx) {
        CtClass val = operandStack.pop().opType;
        checkType(type, val);
        locals.set(idx, val);
    }
    
    /**
     * 
     * @param isStatic - is get static field
     * @param cpIdx - index in the constant pool
     */
    private void getfieldInstr(boolean isStatic, int cpIdx) {
        try {
            if (!isStatic)
                operandStack.pop(); //objref
            String fieldTypeName = constPool.getFieldrefType(cpIdx);
            CtClass fieldType = Descriptor.toCtClass(fieldTypeName, classPool);
            push(fieldType, ElemKind.FIELD);
        } catch (NotFoundException e) {
            throw new RuntimeException("Unknown fieldType", e);
        }
    }
    
    /**
     * 
     * @param isStatic - is get static field
     */
    private void putfieldInstr(boolean isStatic) {
        operandStack.pop(); //value
        if (!isStatic)
            operandStack.pop(); //objref
    }
    
    
    /**
     * Pops the type <code>from</code> from the operand stack and pushes the 
     * type <code>to</code> to the operand stack. This helper is used for
     * handling various conversion bytecode instructions.
     * 
     * @param from - type to convert from
     * @param to - type to convert to
     */
    private void conv(CtClass from, CtClass to) {
        StackElem val = operandStack.pop();
        checkType(from, val.opType);
        push(to, ElemKind.LOCAL);
    }
    
    /**
     * Pops a type from the operand stack, checks if it is compatible with the
     * given <code>type</code> type and then pushes it back to the operand stack.
     * This helper is used for handling unary arithmetic bytecode instructions.   
     * 
     * @param type - type that the unary operation takes.
     */
    private void unOp(CtClass type) {
        StackElem val1 = operandStack.pop();
        checkType(type, val1.opType);
        push(type, ElemKind.LOCAL);
    }
    
    /**
     * Pops two types from the operand stack, checks if they are the same and 
     * compatible with the given <code>type</code> type and then pushes the 
     * same type to the operand stack. This helper is used for handling binary
     * arithmetic bytecode instructions.
     * 
     * @param type - type that the binary operation takes.
     */
    private void binOp(CtClass type) {
        StackElem val1 = operandStack.pop();
        checkType(type, val1.opType);
        StackElem val2 = operandStack.pop();
        checkType(type, val2.opType);
        push(type, ElemKind.LOCAL);
    }
    
    /**
     * Pops two types from the operand stack, checks if they are compatible 
     * with the given <code>type</code> type and then pushes the type 
     * <code>int</code> to the operand stack. This helper is used for 
     * handling various comparison bytecode instructions.
     * 
     * @param type - type of the values to be compared. 
     */
    private void cmp(CtClass type) {
        StackElem val1 = operandStack.pop();
        checkType(type, val1.opType);
        StackElem val2 = operandStack.pop();
        checkType(type, val2.opType);
        push(intType, ElemKind.LOCAL);
    }
    
    /**
     * Takes two values on top of the operand stack.
     * This helper is used for handling branch instructions that consume two
     * values from the top of the operand stack.
     */
    private void ifInstr(CtClass type) {
        operandStack.pop(); //val2
        operandStack.pop(); //val1
    }
    
    /**
     * Takes only the value on top of the operand stack.
     * This helper is used for handling branch instructions that consume one
     * value from the top of the operand stack.
     */
    private void branchInstr(CtClass type) {
        operandStack.pop(); //val1
    }
    
    /**
     * Pops the right number of values from the top of the operand stack (which is
     * equal to the number of method parameters of the method to be invoked), then
     * if the call is not static pops object reference from the operand stack, and 
     * finally pushes the return type of the method to the stack.  This method is 
     * used for handling various invoke bytecode instructions. 
     * 
     * @param isInterface - is it an interface method call
     * @param isStatic - is it a static method call
     * @param cpIdx - index in the constant pool for the method to be invoked.
     */
    private void invokeInstr(boolean isInterface, boolean isStatic, int cpIdx) {
        try {
            String methodDesc;
            if (isInterface) {
                methodDesc = constPool.getInterfaceMethodrefType(cpIdx);
            } else {
                methodDesc = constPool.getMethodrefType(cpIdx);
            }
            CtClass[] params = Descriptor.getParameterTypes(methodDesc, classPool);
            for (int i = params.length - 1; i >= 0; i--) {
                CtClass arg = operandStack.pop().opType;
                checkType(params[i], arg);
            }
            if (!isStatic) {
                operandStack.pop(); //objref
            }
            CtClass returnType = Descriptor.getReturnType(methodDesc, classPool);
            if (returnType != voidType) {
                push(returnType, ElemKind.LOCAL);
            }
        } catch (NotFoundException e) {
            throw new RuntimeException("Unknown fieldType", e);
        }
    }
    
    /**
     * Helper for handling various return bytecode instructions. 
     * 
     * @param type - the type of the return value.
     */
    private void returnInstr(CtClass type) {
        CtClass result = operandStack.pop().opType;
        checkType(type, result);
    }
    
    /**
     * Helper for handling ldc instructions.
     *  
     * @param idx - index in the constant pool.
     */
    private void ldcInstr(int idx) {
        Object ldcVal = constPool.getLdcValue(idx);
        if (ldcVal instanceof String) {
            push(stringType, ElemKind.LOCAL);
        } else if (ldcVal instanceof Integer) {
            push(intType, ElemKind.LOCAL);
        } else if (ldcVal instanceof Float) {
            push(floatType, ElemKind.LOCAL);
        } else {
            //throw new RuntimeException("Invalid constant for ldc instruction: index = " + idx);
            push(classType, ElemKind.LOCAL);
        }
    }
    
    /**
     * Helper for handling ldc2_w instruction.
     *  
     * @param idx - index in the constant pool.
     */
    private void ldc2Instr(int idx) {
        Object ldcVal = constPool.getLdcValue(idx);
        if (ldcVal instanceof Long) {
            push(longType, ElemKind.LOCAL);
        } else if (ldcVal instanceof Double) {
            push(doubleType, ElemKind.LOCAL);
        } else { 
            //throw new RuntimeException("Invalid constant for ldc2 instruction");
            push(classType, ElemKind.LOCAL);
        }
    }

    /**
     * @param type - the type whose jvm category should be returned.
     * @return returns jvm category (1 or 2) of the given type. Only 
     * <code>long</code> and <code>double</code> are of category 2.
     */
    private int jvmCategory(CtClass type) {
        if (type == doubleType || type == longType)
            return 2;
        else
            return 1;
    }
    
    /* --------------------------------------------------------------------
     *  PRE_VISIT METHODS
     * -------------------------------------------------------------------- */
    
    @Override
    public void preVisit(BytecodeInstruction instr) {
        if (!lookingAhead) {
            arrayPushed = false;
            pushedArrayIdx = -1;
        }
        cit = instr.getCit();
        if (handlePCs.contains(instr.getIdx())) {
            // push an exception on top of the stack
            push(objectType, ElemKind.LOCAL); 
        }
    }
    @Override
    public void postVisit(BytecodeInstruction instr) {
        if (lookingAhead)
            return;
        if (arrayPushed)
            computeConsumedBy();
    }
    
    /* --------------------------------------------------------------------
     *  VISITOR METHODS
     * -------------------------------------------------------------------- */
    
    @Override
    public void visitAALOAD(BytecodeInstruction instr) {
        CtClass idx = operandStack.pop().opType;
        checkType(intType, idx);
        CtClass arr = operandStack.pop().opType;
        checkArray(arr);
        try {
            //TODO: this may not work for multidimensional arrays
            push(arr.getComponentType());
        } catch (NotFoundException e) {
            throw new RuntimeException("AALOAD: array component not found!", e);
        }
    }

    @Override
    public void visitAASTORE(BytecodeInstruction instr) {
        CtClass val = operandStack.pop().opType;
        CtClass idx = operandStack.pop().opType;
        checkType(intType, idx);
        CtClass arr = operandStack.pop().opType;
        checkArray(arr, val); 
        try {
            checkType(arr.getComponentType(), val);
        } catch (NotFoundException e) {
            throw new RuntimeException("AASTORE: array component not found!", e);
        }
    }

    @Override
    public void visitACONST_NULL(BytecodeInstruction instr) {
        push(objectType);
    }

    @Override
    public void visitALOAD(BytecodeInstruction instr) {
        localLoad(objectType, instr.byteAt(1));        
    }
    
    @Override
    public void visitALOAD_0(BytecodeInstruction instr) {
        localLoad(objectType, 0);
    }

    @Override
    public void visitALOAD_1(BytecodeInstruction instr) {
        localLoad(objectType, 1);
    }

    @Override
    public void visitALOAD_2(BytecodeInstruction instr) {
        localLoad(objectType, 2);
    }

    @Override
    public void visitALOAD_3(BytecodeInstruction instr) {
        localLoad(objectType, 3);
    }

    @Override
    public void visitANEWARRAY(BytecodeInstruction instr) {
        CtClass count = operandStack.pop().opType;
        checkType(intType, count);
        int cpIdx = instr.u16bitAt(1);
        String arrayComponentClassName = constPool.getClassInfo(cpIdx);
        String desc = "[L" + Descriptor.toJvmName(arrayComponentClassName) + ";";
        try {
            CtClass c = Descriptor.toCtClass(desc, classPool);
            push(c);
        } catch (NotFoundException e) {
            throw new RuntimeException("Invalid NEWARRAY array type", e);
        }
    }
    
    @Override
    public void visitARETURN(BytecodeInstruction instr) {
        returnInstr(objectType);
    }

    @Override
    public void visitARRAYLENGTH(BytecodeInstruction instr) {
        CtClass arr = operandStack.pop().opType;
        checkArray(arr);
        push(intType);
    }
    
    @Override
    public void visitASTORE(BytecodeInstruction instr) {
        localStore(objectType, instr.byteAt(1));
    }
    
    @Override
    public void visitASTORE_0(BytecodeInstruction instr) {
        localStore(objectType, 0);
    }

    @Override
    public void visitASTORE_1(BytecodeInstruction instr) {
        localStore(objectType, 1);
    }

    @Override
    public void visitASTORE_2(BytecodeInstruction instr) {
        localStore(objectType, 2);
    }

    @Override
    public void visitASTORE_3(BytecodeInstruction instr) {
        localStore(objectType, 3);
    }

    @Override
    public void visitATHROW(BytecodeInstruction instr) {
        //cannot put exception on top of the stack, 
        //because that exception will be consumed 
        //somewhere in the catch block that doesn't 
        //have to be in the same method.
    }
    
    @Override
    public void visitBALOAD(BytecodeInstruction instr) {
        arrayLoad(byteType);
    }

    @Override
    public void visitBASTORE(BytecodeInstruction instr) {
        arrayStore(byteType);
    }
    
    @Override
    public void visitBIPUSH(BytecodeInstruction instr) {
        push(byteType);
    }
    
    @Override
    public void visitCALOAD(BytecodeInstruction instr) {
        arrayLoad(charType);
    }

    @Override
    public void visitCASTORE(BytecodeInstruction instr) {
        arrayStore(charType);
    }
    
    @Override
    public void visitCHECKCAST(BytecodeInstruction instr) {
        CtClass objref = operandStack.pop().opType;
        checkType(objectType, objref);
        int cpIdx = instr.u16bitAt(1);
        String className = constPool.getClassInfo(cpIdx);
        try {
            push(Descriptor.toCtClass(className, classPool));
        } catch (NotFoundException e) {
            throw new RuntimeException("CHECKCAST: Cannot find type to cast to.", e);
        }
    }

    @Override
    public void visitD2F(BytecodeInstruction instr) {
        conv(doubleType, floatType);
    }

    @Override
    public void visitD2I(BytecodeInstruction instr) {
        conv(doubleType, intType);
    }

    @Override
    public void visitD2L(BytecodeInstruction instr) {
        conv(doubleType, longType);
    }

    @Override
    public void visitDADD(BytecodeInstruction instr) {
        binOp(doubleType);
    }

    @Override
    public void visitDALOAD(BytecodeInstruction instr) {
        arrayLoad(doubleType);
    }

    @Override
    public void visitDASTORE(BytecodeInstruction instr) {
        arrayStore(doubleType);
    }
    
    @Override
    public void visitDCMPG(BytecodeInstruction instr) {
        cmp(doubleType);
    }

    @Override
    public void visitDCMPL(BytecodeInstruction instr) {
        cmp(doubleType);
    }

    @Override
    public void visitDCONST_0(BytecodeInstruction instr) {
        push(doubleType);
    }

    @Override
    public void visitDCONST_1(BytecodeInstruction instr) {
        push(doubleType);
    }

    @Override
    public void visitDDIV(BytecodeInstruction instr) {
        binOp(doubleType);
    }

    @Override
    public void visitDLOAD(BytecodeInstruction instr) {
        localLoad(doubleType, instr.byteAt(1));
    }

    @Override
    public void visitDLOAD_0(BytecodeInstruction instr) {
        localLoad(doubleType, 0);
    }

    @Override
    public void visitDLOAD_1(BytecodeInstruction instr) {
        localLoad(doubleType, 1);
    }

    @Override
    public void visitDLOAD_2(BytecodeInstruction instr) {
        localLoad(doubleType, 2);
    }

    @Override
    public void visitDLOAD_3(BytecodeInstruction instr) {
        localLoad(doubleType, 3);
    }

    @Override
    public void visitDMUL(BytecodeInstruction instr) {
        binOp(doubleType);
    }

    @Override
    public void visitDNEG(BytecodeInstruction instr) {
        unOp(doubleType);
    }

    @Override
    public void visitDREM(BytecodeInstruction instr) {
        binOp(doubleType);
    }

    @Override
    public void visitDRETURN(BytecodeInstruction instr) {
        returnInstr(doubleType);
    }

    @Override
    public void visitDSTORE(BytecodeInstruction instr) {
        localStore(doubleType, instr.byteAt(1));
    }

    @Override
    public void visitDSTORE_0(BytecodeInstruction instr) {
        localStore(doubleType, 0);
    }

    @Override
    public void visitDSTORE_1(BytecodeInstruction instr) {
        localStore(doubleType, 1);
    }

    @Override
    public void visitDSTORE_2(BytecodeInstruction instr) {
        localStore(doubleType, 2);
    }

    @Override
    public void visitDSTORE_3(BytecodeInstruction instr) {
        localStore(doubleType, 3);
    }

    @Override
    public void visitDSUB(BytecodeInstruction instr) {
        binOp(doubleType);
    }
    
    @Override
    public void visitDUP_X1(BytecodeInstruction instr) {
        StackElem val1 = operandStack.pop();
        StackElem val2 = operandStack.pop();
        push(val1);
        push(val2);
        push(val1);
    }

    @Override
    public void visitDUP_X2(BytecodeInstruction instr) {
        StackElem val1 = operandStack.pop();
        StackElem val2 = operandStack.pop();
        if (jvmCategory(val2.opType) == 1) {
            StackElem val3 = operandStack.pop();
            push(val1);
            push(val3);
            push(val2);
            push(val1);
        } else {
            push(val1);
            push(val2);
            push(val1);
        }
    }

    @Override
    public void visitDUP(BytecodeInstruction instr) {
        StackElem val = operandStack.pop();
        push(val);
        push(val);
    }

    @Override
    public void visitDUP2_X1(BytecodeInstruction instr) {
        StackElem val1 = operandStack.pop();
        if (jvmCategory(val1.opType) == 1) {
            StackElem val2 = operandStack.pop();
            StackElem val3 = operandStack.pop();
            push(val1);
            push(val3);
            push(val2);
            push(val1);
        } else {
            StackElem val2 = operandStack.pop();
            push(val1);
            push(val2);
            push(val1);
        }
    }

    @Override
    public void visitDUP2_X2(BytecodeInstruction instr) {
        StackElem val1 = operandStack.pop();
        if (jvmCategory(val1.opType) == 1) {
            StackElem val2 = operandStack.pop();
            StackElem val3 = operandStack.pop();
            if (jvmCategory(val3.opType) == 1) {
                StackElem val4 = operandStack.pop();
                push(val2);
                push(val1);
                push(val4);
                push(val3);
                push(val2);
                push(val1);
            } else {
                push(val2);
                push(val1);
                push(val3);
                push(val2);
                push(val1);
            }
        } else {
            StackElem val2 = operandStack.pop();
            if (jvmCategory(val2.opType) == 1) {
                StackElem val3 = operandStack.pop();
                push(val1);
                push(val3);
                push(val2);
                push(val1);
            } else {
                push(val1);
                push(val2);
                push(val1);
            }
        }
    }

    @Override
    public void visitDUP2(BytecodeInstruction instr) {
        StackElem val1 = operandStack.pop();
        if (jvmCategory(val1.opType) == 1) {
            StackElem val2 = operandStack.pop();
            push(val2);
            push(val1);
            push(val2);
            push(val1);
        } else {
            push(val1);
            push(val1);
        }
    }
    
    @Override
    public void visitF2D(BytecodeInstruction instr) {
        conv(floatType, doubleType);
    }

    @Override
    public void visitF2I(BytecodeInstruction instr) {
        conv(floatType, intType);
    }

    @Override
    public void visitF2L(BytecodeInstruction instr) {
        conv(floatType, longType);
    }

    @Override
    public void visitFADD(BytecodeInstruction instr) {
        binOp(floatType);
    }

    @Override
    public void visitFALOAD(BytecodeInstruction instr) {
        arrayLoad(floatType);
    }

    @Override
    public void visitFASTORE(BytecodeInstruction instr) {
        arrayStore(floatType);
    }

    @Override
    public void visitFCMPG(BytecodeInstruction instr) {
        cmp(floatType);
    }

    @Override
    public void visitFCMPL(BytecodeInstruction instr) {
        cmp(floatType);
    }

    @Override
    public void visitFCONST_0(BytecodeInstruction instr) {
        push(floatType);
    }

    @Override
    public void visitFCONST_1(BytecodeInstruction instr) {
        push(floatType);
    }

    @Override
    public void visitFCONST_2(BytecodeInstruction instr) {
        push(floatType);
    }

    @Override
    public void visitFDIV(BytecodeInstruction instr) {
        binOp(floatType);
    }
    
    @Override
    public void visitFLOAD(BytecodeInstruction instr) {
        localLoad(floatType, instr.byteAt(1));
    }

    @Override
    public void visitFLOAD_0(BytecodeInstruction instr) {
        localLoad(floatType, 0);
    }

    @Override
    public void visitFLOAD_1(BytecodeInstruction instr) {
        localLoad(floatType, 1);
    }

    @Override
    public void visitFLOAD_2(BytecodeInstruction instr) {
        localLoad(floatType, 2);
    }

    @Override
    public void visitFLOAD_3(BytecodeInstruction instr) {
        localLoad(floatType, 3);
    }

    @Override
    public void visitFMUL(BytecodeInstruction instr) {
        binOp(floatType);
    }

    @Override
    public void visitFNEG(BytecodeInstruction instr) {
        unOp(floatType);
    }

    @Override
    public void visitFREM(BytecodeInstruction instr) {
        binOp(floatType);
    }

    @Override
    public void visitFRETURN(BytecodeInstruction instr) {
        returnInstr(floatType);
    }

    @Override
    public void visitFSTORE(BytecodeInstruction instr) {
        localStore(floatType, instr.byteAt(1));
    }

    @Override
    public void visitFSTORE_0(BytecodeInstruction instr) {
        localStore(floatType, 0);
    }

    @Override
    public void visitFSTORE_1(BytecodeInstruction instr) {
        localStore(floatType, 1);
    }

    @Override
    public void visitFSTORE_2(BytecodeInstruction instr) {
        localStore(floatType, 2);
    }

    @Override
    public void visitFSTORE_3(BytecodeInstruction instr) {
        localStore(floatType, 3);
    }

    @Override
    public void visitFSUB(BytecodeInstruction instr) {
        binOp(floatType);
    }

    @Override
    public void visitGETFIELD(BytecodeInstruction instr) {
        getfieldInstr(false, instr.u16bitAt(1));
    }

    @Override
    public void visitGETSTATIC(BytecodeInstruction instr) {
        getfieldInstr(true, instr.u16bitAt(1));
    }
    
    @Override
    public void visitGOTO_W(BytecodeInstruction instr) {
        //operand stack remains the same
    }

    @Override
    public void visitGOTO(BytecodeInstruction instr) {
        //operand stack remains the same
    }

    @Override
    public void visitI2B(BytecodeInstruction instr) {
        conv(intType, byteType);
    }

    @Override
    public void visitI2C(BytecodeInstruction instr) {
        conv(intType, charType);
    }

    @Override
    public void visitI2D(BytecodeInstruction instr) {
        conv(intType, doubleType);
    }

    @Override
    public void visitI2F(BytecodeInstruction instr) {
        conv(intType, floatType);
    }

    @Override
    public void visitI2L(BytecodeInstruction instr) {
        conv(intType, longType);
    }

    @Override
    public void visitI2S(BytecodeInstruction instr) {
        conv(intType, shortType);
    }

    @Override
    public void visitIADD(BytecodeInstruction instr) {
        binOp(intType);
    }

    @Override
    public void visitIALOAD(BytecodeInstruction instr) {
        arrayLoad(intType);
    }

    @Override
    public void visitIAND(BytecodeInstruction instr) {
        binOp(intType);
    }

    @Override
    public void visitIASTORE(BytecodeInstruction instr) {
        arrayStore(intType);
    }

    @Override
    public void visitICONST_0(BytecodeInstruction instr) {
        push(intType);
    }

    @Override
    public void visitICONST_1(BytecodeInstruction instr) {
        push(intType);
    }

    @Override
    public void visitICONST_2(BytecodeInstruction instr) {
        push(intType);
    }

    @Override
    public void visitICONST_3(BytecodeInstruction instr) {
        push(intType);
    }

    @Override
    public void visitICONST_4(BytecodeInstruction instr) {
        push(intType);
    }

    @Override
    public void visitICONST_5(BytecodeInstruction instr) {
        push(intType);
    }

    @Override
    public void visitICONST_M1(BytecodeInstruction instr) {
        push(intType);
    }

    @Override
    public void visitIDIV(BytecodeInstruction instr) {
        binOp(intType);
    }
    
    @Override
    public void visitIF_ACMPEQ(BytecodeInstruction instr) {
        ifInstr(objectType);
    }

    @Override
    public void visitIF_ACMPNE(BytecodeInstruction instr) {
        ifInstr(objectType);
    }

    @Override
    public void visitIF_ICMPEQ(BytecodeInstruction instr) {
        ifInstr(intType);
    }

    @Override
    public void visitIF_ICMPGE(BytecodeInstruction instr) {
        ifInstr(intType);
    }

    @Override
    public void visitIF_ICMPGT(BytecodeInstruction instr) {
        ifInstr(intType);
    }

    @Override
    public void visitIF_ICMPLE(BytecodeInstruction instr) {
        ifInstr(intType);
    }

    @Override
    public void visitIF_ICMPLT(BytecodeInstruction instr) {
        ifInstr(intType);
    }

    @Override
    public void visitIF_ICMPNE(BytecodeInstruction instr) {
        ifInstr(intType);
    }

    @Override
    public void visitIFEQ(BytecodeInstruction instr) {
        branchInstr(intType);
    }

    @Override
    public void visitIFGE(BytecodeInstruction instr) {
        branchInstr(intType);
    }

    @Override
    public void visitIFGT(BytecodeInstruction instr) {
        branchInstr(intType);
    }
    
    @Override
    public void visitIFLE(BytecodeInstruction instr) {
        branchInstr(intType);
    }

    @Override
    public void visitIFLT(BytecodeInstruction instr) {
        branchInstr(intType);
    }

    @Override
    public void visitIFNE(BytecodeInstruction instr) {
        branchInstr(intType);
    }

    @Override
    public void visitIFNONNULL(BytecodeInstruction instr) {
        branchInstr(objectType);
    }

    @Override
    public void visitIFNULL(BytecodeInstruction instr) {
        branchInstr(objectType);
    }

    @Override
    public void visitIINC(BytecodeInstruction instr) {
        // leaves the operans stack unchanged
    }

    @Override
    public void visitILOAD(BytecodeInstruction instr) {
        localLoad(intType, instr.byteAt(1));
    }

    @Override
    public void visitILOAD_0(BytecodeInstruction instr) {
        localLoad(intType, 0);
    }

    @Override
    public void visitILOAD_1(BytecodeInstruction instr) {
        localLoad(intType, 1);
    }

    @Override
    public void visitILOAD_2(BytecodeInstruction instr) {
        localLoad(intType, 2);
    }

    @Override
    public void visitILOAD_3(BytecodeInstruction instr) {
        localLoad(intType, 3);
    }

    @Override
    public void visitIMUL(BytecodeInstruction instr) {
        binOp(intType);
    }

    @Override
    public void visitINEG(BytecodeInstruction instr) {
        unOp(intType);
    }

    @Override
    public void visitINSTANCEOF(BytecodeInstruction instr) {
        CtClass objref = operandStack.pop().opType;
        checkType(objectType, objref);
        push(intType);
    }

    @Override
    public void visitINVOKEINTERFACE(BytecodeInstruction instr) {
        invokeInstr(true, false, instr.u16bitAt(1));
    }

    @Override
    public void visitINVOKESPECIAL(BytecodeInstruction instr) {
        invokeInstr(false, false, instr.u16bitAt(1));
    }

    @Override
    public void visitINVOKESTATIC(BytecodeInstruction instr) {
        invokeInstr(false, true, instr.u16bitAt(1));
    }

    @Override
    public void visitINVOKEVIRTUAL(BytecodeInstruction instr) {
        invokeInstr(false, false, instr.u16bitAt(1));
    }

    @Override
    public void visitIOR(BytecodeInstruction instr) {
        binOp(intType);
    }

    @Override
    public void visitIREM(BytecodeInstruction instr) {
        binOp(intType);
    }

    @Override
    public void visitIRETURN(BytecodeInstruction instr) {
        returnInstr(intType);
    }

    @Override
    public void visitISHL(BytecodeInstruction instr) {
        binOp(intType);
    }

    @Override
    public void visitISHR(BytecodeInstruction instr) {
        binOp(intType);
    }

    @Override
    public void visitISTORE(BytecodeInstruction instr) {
        localStore(intType, instr.byteAt(1));
    }

    @Override
    public void visitISTORE_0(BytecodeInstruction instr) {
        localStore(intType, 0);
    }

    @Override
    public void visitISTORE_1(BytecodeInstruction instr) {
        localStore(intType, 1);
    }

    @Override
    public void visitISTORE_2(BytecodeInstruction instr) {
        localStore(intType, 2);
    }

    @Override
    public void visitISTORE_3(BytecodeInstruction instr) {
        localStore(intType, 3);
    }

    @Override
    public void visitISUB(BytecodeInstruction instr) {
        binOp(intType);
    }

    @Override
    public void visitIUSHR(BytecodeInstruction instr) {
        binOp(intType);
    }

    @Override
    public void visitIXOR(BytecodeInstruction instr) {
        binOp(intType);
    }

    @Override
    public void visitJSR_W(BytecodeInstruction instr) {
        // we represent returnAddress type with objectType
        push(objectType); 
    }

    @Override
    public void visitJSR(BytecodeInstruction instr) {
        // we represent returnAddress type with objectType
        push(objectType); 
    }

    @Override
    public void visitL2D(BytecodeInstruction instr) {
        conv(longType, doubleType);
    }

    @Override
    public void visitL2F(BytecodeInstruction instr) {
        conv(longType, floatType);
    }

    @Override
    public void visitL2I(BytecodeInstruction instr) {
        conv(longType, intType);
    }

    @Override
    public void visitLADD(BytecodeInstruction instr) {
        binOp(longType);
    }

    @Override
    public void visitLALOAD(BytecodeInstruction instr) {
        arrayLoad(longType);
    }

    @Override
    public void visitLAND(BytecodeInstruction instr) {
        binOp(longType);
    }

    @Override
    public void visitLASTORE(BytecodeInstruction instr) {
        arrayStore(longType);
    }

    @Override
    public void visitLCMP(BytecodeInstruction instr) {
        cmp(longType);
    }

    @Override
    public void visitLCONST_0(BytecodeInstruction instr) {
        push(longType);
    }

    @Override
    public void visitLCONST_1(BytecodeInstruction instr) {
        push(longType);
    }

    @Override
    public void visitLDC(BytecodeInstruction instr) {
        ldcInstr(instr.byteAt(1));
    }

    @Override
    public void visitLDC2_W(BytecodeInstruction instr) {
        ldc2Instr(instr.u16bitAt(1));
    }
    
    @Override
    public void visitLDC_W(BytecodeInstruction instr) {
        ldcInstr(instr.u16bitAt(1));
    }
    
    @Override
    public void visitLDIV(BytecodeInstruction instr) {
        binOp(longType);
    }

    @Override
    public void visitLLOAD(BytecodeInstruction instr) {
        localLoad(longType, instr.byteAt(1));
    }

    @Override
    public void visitLLOAD_0(BytecodeInstruction instr) {
        localLoad(longType, 0);
    }

    @Override
    public void visitLLOAD_1(BytecodeInstruction instr) {
        localLoad(longType, 1);
    }

    @Override
    public void visitLLOAD_2(BytecodeInstruction instr) {
        localLoad(longType, 2);
    }

    @Override
    public void visitLLOAD_3(BytecodeInstruction instr) {
        localLoad(longType, 3);
    }

    @Override
    public void visitLMUL(BytecodeInstruction instr) {
        binOp(longType);
    }

    @Override
    public void visitLNEG(BytecodeInstruction instr) {
        unOp(longType);
    }
    
    @Override
    public void visitLOOKUPSWITCH(BytecodeInstruction instr) {
        CtClass key = operandStack.pop().opType;
        checkType(intType, key);
    }

    @Override
    public void visitLOR(BytecodeInstruction instr) {
        binOp(longType);
    }

    @Override
    public void visitLREM(BytecodeInstruction instr) {
        binOp(longType);
    }

    @Override
    public void visitLRETURN(BytecodeInstruction instr) {
        returnInstr(longType);
    }

    @Override
    public void visitLSHL(BytecodeInstruction instr) {
        binOp(longType);
    }

    @Override
    public void visitLSHR(BytecodeInstruction instr) {
        binOp(longType);
    }

    @Override
    public void visitLSTORE(BytecodeInstruction instr) {
        localStore(longType, instr.byteAt(1));
    }

    @Override
    public void visitLSTORE_0(BytecodeInstruction instr) {
        localStore(longType, 0);
    }

    @Override
    public void visitLSTORE_1(BytecodeInstruction instr) {
        localStore(longType, 1);
    }

    @Override
    public void visitLSTORE_2(BytecodeInstruction instr) {
        localStore(longType, 2);
    }

    @Override
    public void visitLSTORE_3(BytecodeInstruction instr) {
        localStore(longType, 3);
    }
    
    @Override
    public void visitLSUB(BytecodeInstruction instr) {
        binOp(longType);
    }

    @Override
    public void visitLUSHR(BytecodeInstruction instr) {
        binOp(longType);
    }

    @Override
    public void visitLXOR(BytecodeInstruction instr) {
        binOp(longType);
    }

    @Override
    public void visitMONITORENTER(BytecodeInstruction instr) {
        operandStack.pop();
    }

    @Override
    public void visitMONITOREXIT(BytecodeInstruction instr) {
        operandStack.pop();
    }

    @Override
    public void visitMULTIANEWARRAY(BytecodeInstruction instr) {
        int dimNum = instr.byteAt(3);
        for (int i = 0; i < dimNum; i++) {
            CtClass dim = operandStack.pop().opType;
            checkType(intType, dim);
        }
        try {
            int cpIdx = instr.u16bitAt(1);
            CtClass clz = Descriptor.toCtClass(constPool.getClassInfo(cpIdx), classPool);
            push(clz);
        } catch (NotFoundException e) {
            throw new RuntimeException("Invalid MULTINEWARRAY type", e);
        }
    }

    @Override
    public void visitNEW(BytecodeInstruction instr) {
        int cpIdx = instr.u16bitAt(1);
        String className = constPool.getClassInfo(cpIdx);
        try {
            push(classPool.get(className));
        } catch (NotFoundException e) {
            throw new RuntimeException("NEW instruction: invalid class to create", e);
        }
    }

    @Override
    public void visitNEWARRAY(BytecodeInstruction instr) {
        CtClass count = operandStack.pop().opType;
        checkType(intType, count);
        int atype = instr.byteAt(1);
        String arrayTypeName = BytecodeUtils.javaArrayNames[atype];
        try {
            push(classPool.get(arrayTypeName));
        } catch (NotFoundException e) {
            throw new RuntimeException("Invalid NEWARRAY array type", e);
        }
    }

    @Override
    public void visitNOP(BytecodeInstruction instr) {
        // leaves the operand stack unchanged
    }

    @Override
    public void visitPOP(BytecodeInstruction instr) {
        operandStack.pop();
    }

    @Override
    public void visitPOP2(BytecodeInstruction instr) {
        CtClass val1 = operandStack.pop().opType;
        if (jvmCategory(val1) == 1) {
            operandStack.pop();
        }
    }
    
    @Override
    public void visitPUTFIELD(BytecodeInstruction instr) {
        putfieldInstr(false);
    }

    @Override
    public void visitPUTSTATIC(BytecodeInstruction instr) {
        putfieldInstr(true);
    }

    @Override
    public void visitRET(BytecodeInstruction instr) {
        // leaves the operand stack unchanged 
    }

    @Override
    public void visitRETURN(BytecodeInstruction instr) {
        // leaves the operand stack unchanged
    }

    @Override
    public void visitSALOAD(BytecodeInstruction instr) {
        arrayLoad(shortType);
    }

    @Override
    public void visitSASTORE(BytecodeInstruction instr) {
        arrayStore(shortType);
    }

    @Override
    public void visitSIPUSH(BytecodeInstruction instr) {
        push(intType);
    }

    @Override
    public void visitSWAP(BytecodeInstruction instr) {
        StackElem val1 = operandStack.pop();
        StackElem val2 = operandStack.pop();
        push(val1);
        push(val2);
    }
    
    @Override
    public void visitTABLESWITCH(BytecodeInstruction instr) {
        CtClass index = operandStack.pop().opType;
        checkType(intType, index);
    }

    @Override
    public void visitWIDE(BytecodeInstruction instr) {
        //TODO!!!
        throw new RuntimeException("Unsupported bytecode instruction: WIDE");
    }

    @Override
    public void visitBREAKPOINT(BytecodeInstruction instr) {
        //
        throw new RuntimeException("Unsupported bytecode instruction: BREAKPOINT");
    }
    
    @Override
    public void visitIMPDEP1(BytecodeInstruction instr) {
        throw new RuntimeException("Unsupported bytecode instruction: IMPDEP1");
    }

    @Override
    public void visitIMPDEP2(BytecodeInstruction instr) {
        throw new RuntimeException("Unsupported bytecode instruction: IMPDEP2");
    }

}
