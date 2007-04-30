package korat.gui.viz.metamodel;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * Use SigFactory to create object of this type.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class AlloySig {

    private String name = "";

    private AlloySig extendsSig = null;

    private List<AlloyAtom> atoms = new LinkedList<AlloyAtom>();

    AlloySig(String name) {
        this(name, null);
    }

    AlloySig(String name, AlloySig extendsSig) {
        this.name = name;
        this.extendsSig = extendsSig;
    }

    public AlloySig getExtendsSig() {
        return extendsSig;
    }

    public void setExtendsSig(AlloySig extendsSig) {
        this.extendsSig = extendsSig;
    }

    public String getName() {
        return name;
    }

    public List<AlloyAtom> getAtoms() {
        return atoms;
    }

    public void addAtom(AlloyAtom a) {
        // if (!atoms.contains(a))
        atoms.add(a);
        if (extendsSig != null)
            extendsSig.addAtom(a);
    }

    public boolean isPrimitive() {
        return "Int".equals(name);
    }

}
