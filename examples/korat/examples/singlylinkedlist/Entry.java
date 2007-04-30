package korat.examples.singlylinkedlist;


public class Entry {

    SerializableObject element;

    Entry next;

    public String toString() {
        return "[" + (element != null ? element.toString() : "null") + "]";
    }

}
