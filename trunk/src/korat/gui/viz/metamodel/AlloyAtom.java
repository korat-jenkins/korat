package korat.gui.viz.metamodel;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class AlloyAtom {

    private String name = "";

    private AlloySig mySig = null;

    private Object myObj;

    AlloyAtom(AlloySig mySig, String name, Object obj) {
        this.mySig = mySig;
        this.name = name;
        this.myObj = obj;
    }

    public String getName() {
        return name;
    }

    public AlloySig getMySig() {
        return mySig;
    }

    public Object getMyObj() {
        return myObj;
    }

    void setName(String name) {
        this.name = name;
    }

}
