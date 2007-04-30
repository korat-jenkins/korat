package korat.gui.viz.metamodel;

import java.util.LinkedList;
import java.util.List;

/**
 * 
 * Use FieldFactory to create object of this type.
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class AlloyField {

    private String name = "";

    private List<AlloySig> types = new LinkedList<AlloySig>();

    private List<AlloyAtom> values = new LinkedList<AlloyAtom>();

    AlloyField(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<AlloySig> getTypes() {
        return types;
    }

    public List<AlloyAtom> getValues() {
        return values;
    }

}
