package korat.instrumentation.bytecode;

import static korat.instrumentation.bytecode.BytecodeUtils.PUTFIELD;
import static korat.instrumentation.bytecode.BytecodeUtils.PUTSTATIC;
import static korat.instrumentation.bytecode.BytecodeUtils.AASTORE;
import static korat.instrumentation.bytecode.BytecodeUtils.BASTORE;
import static korat.instrumentation.bytecode.BytecodeUtils.CASTORE;
import static korat.instrumentation.bytecode.BytecodeUtils.DASTORE;
import static korat.instrumentation.bytecode.BytecodeUtils.FASTORE;
import static korat.instrumentation.bytecode.BytecodeUtils.IASTORE;
import static korat.instrumentation.bytecode.BytecodeUtils.LASTORE;
import static korat.instrumentation.bytecode.BytecodeUtils.SASTORE;

import javassist.bytecode.CodeIterator;
import korat.utils.ReflectionUtils;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class BytecodeInstruction {

    private int opcode;

    private CodeIterator cit;

    private int idx;
    
    public BytecodeInstruction(CodeIterator cit, int idx) {
        this.cit = cit;
        this.idx = idx;
        this.opcode = cit.byteAt(idx);
    }

    public int getOpcode() {
        return opcode;
    }

    public CodeIterator getCit() {
        return cit;
    }

    public int getIdx() {
        return idx;
    }
    
    public void accept(BytecodeVisitor visitor) {
        String instr = BytecodeUtils.OPCODE_NAMES[opcode];
        if (!BytecodeUtils.ILLEGAL.equals(instr)) {
            String visitMethodName = BytecodeUtils.getVisitMethodName(instr);
            Class[] params = new Class[] { BytecodeInstruction.class };
            Object[] args = new Object[] { this };
            visitor.preVisit(this);
            ReflectionUtils.invoke(visitor, visitMethodName, params, args);
            visitor.postVisit(this);
        }
    }

    public int byteAt(int offset) {
        return cit.byteAt(idx + offset);
    }
    
    public int u16bitAt(int offset) {
        return cit.u16bitAt(idx + offset);
    }

    @Override
    public String toString() {
        return BytecodeUtils.OPCODE_NAMES[getOpcode()];
    }

    public boolean isInvoke() {
        return toString().toUpperCase().startsWith("INVOKE");
    }

    public boolean isPutField() {
        return opcode == PUTFIELD || opcode == PUTSTATIC;
    }

    public boolean isLocalStore() {
        String name = toString().toUpperCase();
        return name.startsWith("ASTORE") ||
               name.startsWith("DSTORE") ||
               name.startsWith("FSTORE") ||
               name.startsWith("ISTORE") ||
               name.startsWith("LSTORE");
    }

    public boolean isArrayStore() {
        return opcode == AASTORE ||
               opcode == BASTORE ||
               opcode == CASTORE ||
               opcode == DASTORE ||
               opcode == FASTORE ||
               opcode == IASTORE ||
               opcode == LASTORE ||
               opcode == SASTORE;
    }
    
    
}
