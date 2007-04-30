package korat.examples.sortedlist;

import java.util.Set;

import korat.finitization.IClassDomain;
import korat.finitization.IFinitization;
import korat.finitization.IIntSet;
import korat.finitization.IObjSet;
import korat.finitization.impl.FinitizationFactory;

@SuppressWarnings("unchecked")
public class SortedList {
    
    public static class Entry {
        Object element;

        Entry next;

        Entry previous;

        public String toString() {
            return "[" + (element != null ? element.toString() : "null") + "]";
        }
    }
    
    public Entry header;

    private int size = 0;

    public boolean repOK() {
        // check cyclicity
        if (header == null)
            return false;
        if (header.element != null)
            return false;
        Set visited = new java.util.HashSet();
        visited.add(header);
        Entry current = header;
        while (true) {
            Entry next = current.next;
            if (next == null)
                return false;
            if (next.previous != current)
                return false;
            current = next;
            if (!visited.add(next))
                break;
        }
        if (current != header)
            return false; // maybe not needed
        
        // check size
        if (visited.size() - 1 != size)
            return false;
        
        // check ordering
        if ((header.next != header)
                && (!(header.next.element instanceof Comparable)))
            return false;
        for (current = header.next; current.next != header; current = current.next) {
            if ((!(current.next.element instanceof Comparable))
                    || (((Comparable) current.element).compareTo((Comparable) current.next.element) > 0))
                return false;
        }
        return true;
    }

    public static IFinitization finSortedList(int minSize, int maxSize,
            int numEntries, int numElems) {
        IFinitization f = FinitizationFactory.create(SortedList.class);

        IObjSet entries = f.createObjSet(Entry.class, true);
        entries.addClassDomain(f.createClassDomain(Entry.class, numEntries));

        IIntSet sizes = f.createIntSet(minSize, maxSize);

        IObjSet elems = f.createObjSet(Integer.class);
        IClassDomain elemsClassDomain = f.createClassDomain(Integer.class);
        elemsClassDomain.includeInIsomorphismCheck(false);
        for (int i = 1; i <= numElems; i++)
            elemsClassDomain.addObject(new Integer(i));
        elems.addClassDomain(elemsClassDomain);
        elems.setNullAllowed(true);

        f.set("header", entries);
        f.set("size", sizes);
        f.set(Entry.class, "element", elems);
        f.set(Entry.class, "next", entries);
        f.set(Entry.class, "previous", entries);
        return f;
    }

}
