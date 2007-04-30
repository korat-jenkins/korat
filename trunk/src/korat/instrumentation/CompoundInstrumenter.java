package korat.instrumentation;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javassist.CannotCompileException;
import javassist.CtClass;
import javassist.NotFoundException;

/**
 * Instrumenter that consists of several other instrumenters. 
 * The result of the instrumentation is equal to the result
 * of the sequential instrumentation of all the contained 
 * instrumenters (<em>Composite</em> design pattern).
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
class CompoundInstrumenter extends AbstractInstrumenter {

    private List<AbstractInstrumenter> instrumenters = new LinkedList<AbstractInstrumenter>();
    
    public CompoundInstrumenter() {
        super();
    }

    public boolean add(AbstractInstrumenter instr) {
        return instrumenters.add(instr);
    }

    public boolean remove(AbstractInstrumenter instr) {
        return instrumenters.remove(instr);
    }

    @Override
    protected void instrument(CtClass clz) throws CannotCompileException,
            NotFoundException, IOException {
        for (AbstractInstrumenter instr : instrumenters) {
            instr.instrument(clz);
        }
    }

}
