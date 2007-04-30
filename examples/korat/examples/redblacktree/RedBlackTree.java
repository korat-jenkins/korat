package korat.examples.redblacktree;

import java.util.Set;

import korat.finitization.IClassDomain;
import korat.finitization.IFinitization;
import korat.finitization.IIntSet;
import korat.finitization.IObjSet;
import korat.finitization.impl.FinitizationFactory;

@SuppressWarnings("unchecked")
public class RedBlackTree {

    private Node root = null;

    private int size = 0;

    private static final int RED = 0;

    private static final int BLACK = 1;

    public static class Node  {

        int key;

        int value;

        Node left = null;

        Node right = null;

        Node parent;

        int color = BLACK;

    }

    // --------------------- FINITIZATION ------------------------//
    public static IFinitization finRedBlackTree(int size) {
        return finRedBlackTree(size, size, size, size);
    }

    public static IFinitization finRedBlackTree(int numEntries, int minSize,
            int maxSize, int numKeys) {
        IFinitization f = FinitizationFactory.create(RedBlackTree.class);

        IClassDomain entryDomain = f.createClassDomain(Node.class, numEntries);
        IObjSet entries = f.createObjSet(Node.class, true);
        entries.addClassDomain(entryDomain);

        IIntSet sizes = f.createIntSet(minSize, maxSize);

        IIntSet keys = f.createIntSet(-1, numKeys - 1);

        IIntSet values = f.createIntSet(0);

        IIntSet colors = f.createIntSet(0, 1);

        f.set("root", entries);
        f.set("size", sizes);
        f.set("Node.left", entries);
        f.set("Node.right", entries);
        f.set("Node.parent", entries);
        f.set("Node.color", colors);
        f.set("Node.key", keys);
        f.set("Node.value", values);

        return f;

    }

    // --------------------- FINITIZATION ------------------------//

    // ------------------------ repOK ---------------------------//
    public boolean repOK() {
        if (root == null)
            return size == 0;
        // RootHasNoParent
        if (root.parent != null)
            return debug("RootHasNoParent");
        Set visited = new java.util.HashSet();
        visited.add(new Wrapper(root));
        java.util.LinkedList workList = new java.util.LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            Node current = (Node) workList.removeFirst();
            // Acyclic
            // // if (!visited.add(new Wrapper(current)))
            // // return debug("Acyclic");
            // Parent Definition
            Node cl = current.left;
            if (cl != null) {
                if (!visited.add(new Wrapper(cl)))
                    return debug("Acyclic");
                if (cl.parent != current)
                    return debug("parent_Input1");
                workList.add(cl);
            }
            Node cr = current.right;
            if (cr != null) {
                if (!visited.add(new Wrapper(cr)))
                    return debug("Acyclic");
                if (cr.parent != current)
                    return debug("parent_Input2");
                workList.add(cr);
            }
        }
        // SizeOk
        if (visited.size() != size)
            return debug("SizeOk");
        if (!repOkColors())
            return false;
        return repOkKeysAndValues();
    }

    private boolean repOkColors() {
        // RedHasOnlyBlackChildren
        java.util.LinkedList workList = new java.util.LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            Node current = (Node) workList.removeFirst();
            Node cl = current.left;
            Node cr = current.right;
            if (current.color == RED) {
                if (cl != null && cl.color == RED)
                    return debug("RedHasOnlyBlackChildren1");
                if (cr != null && cr.color == RED)
                    return debug("RedHasOnlyBlackChildren2");
            }
            if (cl != null)
                workList.add(cl);
            if (cr != null)
                workList.add(cr);
        }
        // SimplePathsFromRootToNILHaveSameNumberOfBlackNodes
        int numberOfBlack = -1;
        workList = new java.util.LinkedList();
        workList.add(new Pair(root, 0));
        while (!workList.isEmpty()) {
            Pair p = (Pair) workList.removeFirst();
            Node e = p.e;
            int n = p.n;
            if (e != null && e.color == BLACK)
                n++;
            if (e == null) {
                if (numberOfBlack == -1)
                    numberOfBlack = n;
                else if (numberOfBlack != n)
                    return debug("SimplePathsFromRootToNILHaveSameNumberOfBlackNodes");
            } else {
                workList.add(new Pair(e.left, n));
                workList.add(new Pair(e.right, n));
            }
        }
        return true;
    }

    private boolean repOkKeysAndValues() {
        // BST1 and BST2
        // this was the old way of determining if the keys are ordered
        // java.util.LinkedList workList = new java.util.LinkedList();
        // workList = new java.util.LinkedList();
        // workList.add(root);
        // while (!workList.isEmpty()) {
        // Entry current = (Entry)workList.removeFirst();
        // Entry cl = current.left;
        // Entry cr = current.right;
        // if (current.key==current.key) ;
        // if (cl != null) {
        // if (compare(current.key, current.maximumKey()) <= 0)
        // return debug("BST1");
        // workList.add(cl);
        // }
        // if (cr != null) {
        // if (compare(current.key, current.minimumKey()) >= 0)
        // return debug("BST2");
        // workList.add(cr);
        // }
        // }
        // this is the new (Alex's) way to determine if the keys are ordered
        if (!orderedKeys(root, null, null))
            return debug("BST");
        // touch values
        java.util.LinkedList workList = new java.util.LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            Node current = (Node) workList.removeFirst();

            if (current.left != null)
                workList.add(current.left);
            if (current.right != null)
                workList.add(current.right);
        }
        return true;
    }

    private boolean orderedKeys(Node e, Object min, Object max) {
        if (e.key == -1)
            return false;
        if (((min != null) && (compare(e.key, min) <= 0))
                || ((max != null) && (compare(e.key, max) >= 0)))
            return false;
        if (e.left != null)
            if (!orderedKeys(e.left, min, e.key))
                return false;
        if (e.right != null)
            if (!orderedKeys(e.right, e.key, max))
                return false;
        return true;
    }

    private final boolean debug(String s) {
        // System.out.println(s);
        return false;
    }

    private final class Pair {
        Node e;

        int n;

        Pair(Node e, int n) {
            this.e = e;
            this.n = n;
        }
    }

    private static final class Wrapper {
        Node e;

        Wrapper(Node e) {
            this.e = e;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof Wrapper))
                return false;
            return e == ((Wrapper) obj).e;
        }

        public int hashCode() {
            return System.identityHashCode(e);
        }
    }

    private int compare(Object k1, Object k2) {
        return ((Comparable) k1).compareTo(k2);
    }
}
