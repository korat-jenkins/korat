package korat.examples.doublylinkedlist;


public class Entry {

    ListObject element;

    Entry next;

    Entry previous;

    public String toString() {
        return "[" + (element != null ? element.toString() : "null") + "]";
    }

}
