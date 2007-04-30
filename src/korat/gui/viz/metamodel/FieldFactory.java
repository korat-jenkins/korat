package korat.gui.viz.metamodel;

import java.util.LinkedList;
import java.util.List;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class FieldFactory {

    private static FieldFactory instance = new FieldFactory();

    public static FieldFactory getInstance() {
        return instance;
    }

    private FieldFactory() {

    }

    private List<AlloyField> allFields = new LinkedList<AlloyField>();

    public AlloyField createAlloyField(String name) {

        AlloyField alloyField = new AlloyField(name);
        allFields.add(alloyField);
        return alloyField;

    }

    public void clear() {
        allFields.clear();
    }

    public List<AlloyField> getAllFields() {
        return allFields;
    }

}
