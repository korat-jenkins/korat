package korat.examples.disjset;

import java.util.BitSet;

import korat.finitization.IArraySet;
import korat.finitization.IClassDomain;
import korat.finitization.IFinitization;
import korat.finitization.IIntSet;
import korat.finitization.IObjSet;
import korat.finitization.impl.FinitizationFactory;

public class DisjSet {

    // helper class
    public static class Record {

        public int parent;

        public int rank;

        public Record() {

        }

        public Record(Record rec) {
            parent = rec.parent;
            rank = rec.rank;
        }

    }

    // end of helper class

    private Record[] elements;

    private int size;

    // toString()
    // public String toString() {
    // Record[] mirror = new Record[size];
    // for (int i = 0; i < size; i++)
    // mirror[i] = new Record(elements[i]);
    //
    // StringBuffer str = new StringBuffer("[");
    //
    // for (int i = 0; i < size; i++)
    // if (mirror[i].parent != -1) {
    // int k = findForToString(i);
    // if (i == 0)
    // str.append("{" + i);
    // else
    // str.append(", {" + i);
    // mirror[i].parent = -1;
    // for (int j = i + 1; j < size; j++)
    // if ((mirror[j].parent != -1) && (findForToString(j) == k)) {
    // str.append(", " + j);
    // mirror[j].parent = -1;
    // }
    // str.append("}");
    // }
    //
    // str.append("]");
    // return str.toString();
    // }

    // a "find" method that does not use path compression; needed for toString()
    // in order not to modify the structure while printing it
    // private int findForToString(int el) {
    // int temp = el;
    // while (elements[temp].parent != temp)
    // temp = elements[temp].parent;
    // return temp;
    // }

    public boolean allDifferent() {
        int n = size - 1;
        // for (int n = 1; n < size; n++) // generates fewer candidates, but
        // slower
        for (int i = 0; i < n; i++)
            for (int j = i + 1; j <= n; j++)
                if (elements[i] == elements[j])
                    return false;
        return true;
    }

    // methods used by Korat
    public boolean repOK() {

        if (elements.length != size)
            return false;

        if (!allDifferent())
            return false;

        int numRoots = 0, numElRankZero = 0;
        BitSet seenParent = new BitSet(size);
        for (int i = 0; i < size; i++) {
            int parentID = elements[i].parent;
            if (parentID < 0 || parentID >= size)
                return false;
            if (parentID != i) {
                int parentRank = elements[parentID].rank;
                if (parentRank <= elements[i].rank)
                    return false;
                if (elements[i].rank == parentRank - 1)
                    seenParent.set(parentID);
            } else
                numRoots += 1;
        }

        for (int i = 0; i < size; i++)
            if (!seenParent.get(i) && elements[i].rank == 0)
                numElRankZero += 1;

        if (numRoots > numElRankZero)
            return false;

        return true;
    }

    public static IFinitization finDisjSet(int size) {

        IFinitization f = FinitizationFactory.create(DisjSet.class);

        IClassDomain bindingsCD = f.createClassDomain(Record.class, size);
        IObjSet bindings = f.createObjSet(Record.class);
        bindings.addClassDomain(bindingsCD);

        IIntSet lens = f.createIntSet(0, size);
        IArraySet elems = f.createArraySet(Record[].class, lens, bindings, 1);

        f.set("size", f.createIntSet(0, size));
        f.set(Record.class, "parent", f.createIntSet(0, size - 1));
        f.set(Record.class, "rank", f.createIntSet(0, size - 1));
        f.set("elements", elems);

        return f;

    }
}
