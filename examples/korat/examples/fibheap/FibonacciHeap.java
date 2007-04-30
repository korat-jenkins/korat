package korat.examples.fibheap;

import java.util.HashSet;
import java.util.Set;

import korat.finitization.IFieldDomain;
import korat.finitization.IFinitization;
import korat.finitization.IObjSet;
import korat.finitization.impl.FinitizationFactory;

public class FibonacciHeap {

    public FibonacciHeapNode Nodes;

    public FibonacciHeapNode minNode;

    public int size;

    private int getMin() {
        FibonacciHeapNode temp = Nodes;
        int min = Nodes.key;
        do {
            if (temp.key < min)
                min = temp.key;
            temp = temp.right;
        } while (temp != Nodes);
        return min;
    }

    public String toString() {
        if (Nodes == null)
            return size + "\n()\n";
        else
            return size + "\n" + Nodes.toString();
    }

    // used by Korat

    public boolean repOkOld() {
        if ((Nodes == null) || (minNode == null))
            return ((Nodes == null) && (minNode == null) && (size == 0));
        else
            return (Nodes.repOK(Nodes, null, 1,
                    new HashSet<FibonacciHeapNode>())
                    && (Nodes.contains(Nodes, minNode))
                    && (size == Nodes.getSize(Nodes))
                    && (getMin() == minNode.key) && (minNode.parent == null));
    }

    public boolean checkHeapified() {
        FibonacciHeapNode current = Nodes;
        do {
            if (!current.checkHeapified())
                return false;
            current = current.right;
        } while (current != Nodes);
        return true;
    }

    public boolean repOK() {
        if ((Nodes == null) || (minNode == null))
            return ((Nodes == null) && (minNode == null) && (size == 0));
        // checks that structural constrainst are satisfied
        Set<FibonacciHeapNode> visited = new HashSet<FibonacciHeapNode>();
        if (!Nodes.isStructural(visited, null))
            return false;
        if (!Nodes.contains(Nodes, minNode))
            return false;
        // checks that the total size is consistent
        if (visited.size() != size)
            return false;
        // checks that the degrees of all trees are fibonacci
        if (!Nodes.checkDegrees())
            return false;
        // checks that keys are heapified
        if (!checkHeapified())
            return false;
        if (getMin() != minNode.key)
            return false;
        return true;
    }

    public static IFinitization finFibonacciHeap(int size) {
        IFinitization f = FinitizationFactory.create(FibonacciHeap.class);

        IObjSet heaps = f.createObjSet(FibonacciHeapNode.class, true);
        heaps.addClassDomain(f.createClassDomain(FibonacciHeapNode.class,
                size));

        IFieldDomain sizes = f.createIntSet(0, size);

        f.set("size", sizes);
        f.set("Nodes", heaps);
        f.set("minNode", heaps);
        f.set(FibonacciHeapNode.class, "parent", heaps);
        f.set(FibonacciHeapNode.class, "right", heaps);
        f.set(FibonacciHeapNode.class, "left", heaps);
        f.set(FibonacciHeapNode.class, "child", heaps);

        IFieldDomain keys;
        IFieldDomain degrees;
        if (size == 0) {
            keys = f.createIntSet(0, 0);
            degrees = f.createIntSet(0, 0);
        } else {
            keys = f.createIntSet(0, size - 1);
            degrees = f.createIntSet(0, size - 1);
        }
        f.set(FibonacciHeapNode.class, "key", keys);
        f.set(FibonacciHeapNode.class, "degree", degrees);

        return f;
    }

}