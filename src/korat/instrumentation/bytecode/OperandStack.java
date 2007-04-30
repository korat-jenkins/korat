package korat.instrumentation.bytecode;

import java.util.Arrays;
import java.util.Stack;

import javassist.CtClass;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class OperandStack {

    public static enum ElemKind { FIELD, LOCAL }
    
    public static enum ConsumedByKind { UNKNOWN, PUTFIELD, LOCAL_STORE, ARRAY_STORE, INVOKE }
    
    /**
     * Memento class that stores needed informations about stack elements
     * 
     * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
     * 
     */
    public static class StackElem {
        public CtClass opType;
        public ElemKind kind;
        public ConsumedByKind consumedBy = ConsumedByKind.UNKNOWN;
    }

    /**
     * <p>Stack of the array object pushed onto the stack. </p>
     * 
     * <p>A stack like this has to be maintained because when you get to, let's
     * say, ARRAYLENGTH instruction, you cannot know which array it reffers to.
     * You would want to query the top of the stack and find out what's there,
     * but I don't think that javassist lets you do that. One solution is to
     * monitor (track) stack changes by yourself. </p>
     * 
     */
    private Stack<StackElem> opStack = new Stack<StackElem>();

    private StackElem lastPoppedOut;

    public StackElem getLastPoppedOut() {
        return lastPoppedOut;
    }

    public void push(StackElem elem) {
        opStack.push(elem);
    }

    public void push(CtClass elemType) {
        push(elemType, ElemKind.LOCAL);
    }
    
    public void push(CtClass elemType, ElemKind kind) {
        StackElem elem = new StackElem();
        elem.opType = elemType;
        elem.kind = kind;
        push(elem);
    }

    public StackElem pop() {
        StackElem elem = opStack.pop();
        lastPoppedOut = elem;
        return elem;
    }

    public StackElem peek() {
        return opStack.peek();
    }

    public boolean isEmpty() {
        return opStack.isEmpty();
    }

    public void clear() {
        opStack.clear();
    }
    
    public StackElem get(int index) {
        return opStack.get(index);
    }

    public int size() {
        return opStack.size();
    }

    @Override
    public String toString() {
        String[] stack = new String[opStack.size()];
        for (int i = 0; i < opStack.size(); i++) {
            stack[i] = opStack.get(i).opType.getName();
        }
        return Arrays.toString(stack);
    }

    public OperandStack getClone() {
        OperandStack ret = new OperandStack();
        ret.opStack = new Stack<StackElem>();
        for (StackElem elem : opStack) {
            ret.opStack.push(elem);
        }
        ret.lastPoppedOut = lastPoppedOut;
        return ret;
    }
    
}
