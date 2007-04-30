package korat.examples.doublylinkedlist;

public class ListObject {

    static int objectID = 0;

    private int myID;

    public ListObject() {
        myID = objectID++;
    }

    public String toString() {
        return "#" + myID;
    }
}