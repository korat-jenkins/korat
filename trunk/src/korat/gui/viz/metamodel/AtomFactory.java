package korat.gui.viz.metamodel;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.Map;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class AtomFactory {

    private static AtomFactory instance = new AtomFactory();

    public static AtomFactory getInstance() {
        return instance;
    }

    private AtomFactory() {

    }

    private Map<Object, AlloyAtom> allAtoms = new IdentityHashMap<Object, AlloyAtom>();
    
    private LinkedList<AlloyAtom> allAtomsOrdered = new LinkedList<AlloyAtom>();

    public AlloyAtom getAlloyAtom(AlloySig sig, Object obj) {

        AlloyAtom a = allAtoms.get(obj);
        if (a == null) {
            a = new AlloyAtom(sig,
                    "" /* sig.getName() + sig.getAtoms().size() */, obj);
            allAtoms.put(obj, a);
            allAtomsOrdered.add(a);
        }

        return a;

    }

    public AlloyAtom getIntAlloyAtom(AlloySig sig, int i) {

        AlloyAtom a = allAtoms.get(i);
        if (a == null) {
            a = new AlloyAtom(sig, Integer.toString(i), new Integer(i));
            allAtoms.put(i, a);
            allAtomsOrdered.add(a);
        }

        return a;

    }

    public void clear() {
        allAtoms.clear();
        allAtomsOrdered.clear();
    }

    public void initAtomNames() {

        Map<AlloySig, Integer> cnt = new HashMap<AlloySig, Integer>();

        for (AlloyAtom atom : allAtomsOrdered) {

            AlloySig sig = atom.getMySig();
            if (!sig.isPrimitive()) {
                Integer idx = cnt.get(sig);
                if (idx == null)
                    idx = 0;
                atom.setName(sig.getName() + idx);
                cnt.put(sig, idx + 1);
            }

        }

    }

}
