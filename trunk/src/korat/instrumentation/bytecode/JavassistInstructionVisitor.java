package korat.instrumentation.bytecode;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.NotFoundException;
import javassist.bytecode.ConstPool;
import korat.loading.filter.FilterManager;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class JavassistInstructionVisitor extends VisitorSupport {

    protected ClassPool classPool;
    
    protected ConstPool constPool;
    
    protected OperandStack operandStack;

    protected CtClass objectType;
    protected CtClass stringType;
    protected CtClass classType;
    protected CtClass booleanType;  
    protected CtClass byteType;
    protected CtClass charType;
    protected CtClass doubleType;
    protected CtClass floatType;
    protected CtClass intType;
    protected CtClass longType;
    protected CtClass shortType;
    protected CtClass voidType;

    protected boolean checkArrayType(int cpIdx) throws NotFoundException {
        String fieldTypeName = constPool.getFieldrefType(cpIdx);
        CtClass fieldClz = classPool.get(fieldTypeName);
        return checkArrayType(fieldClz.getComponentType());
    }

    protected boolean checkArrayType(CtClass componentType) throws NotFoundException {
        return checkArrayType(componentType.getName());
    }

    protected boolean checkArrayType(String arrayComponentClassName) throws NotFoundException {
        return FilterManager.getFilter().allowProcessing(arrayComponentClassName);
    }

    public JavassistInstructionVisitor(ClassPool classPool,
            ConstPool constPool, OperandStack localStack) {

        this.classPool = classPool;
        this.constPool = constPool;
        this.operandStack = localStack;
        try {
            this.objectType = classPool.get("java.lang.Object");
            this.stringType = classPool.get("java.lang.String");
            this.classType = classPool.get("java.lang.Class");
            this.booleanType = CtClass.booleanType;
            this.byteType = CtClass.byteType;
            this.charType = CtClass.charType;
            this.doubleType = CtClass.doubleType;
            this.floatType = CtClass.floatType;
            this.intType = CtClass.intType;
            this.longType = CtClass.longType;
            this.shortType = CtClass.shortType;
            this.voidType = CtClass.voidType;
        } catch (NotFoundException e) {
        }

    }

}
