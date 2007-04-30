package korat.examples.searchtree;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import korat.finitization.IFinitization;
import korat.finitization.IIntSet;
import korat.finitization.IObjSet;
import korat.finitization.impl.FinitizationFactory;

@SuppressWarnings("unchecked")
public class SearchTree {
    private Node root; // root node

    private int size; // number of nodes in the tree

    public boolean repOK() {
        // checks that empty tree has size zero
        if (root == null)
            return size == 0;
        // checks that the input is a tree
        if (!isAcyclic())
            return false;
        // checks that size is consistent
        if (numNodes(root) != size)
            return false;
        // checks that data is ordered
        if (!isOrdered(root))
            return false;
        return true;
    }

    private boolean isAcyclic() {
        Set visited = new HashSet();
        visited.add(root);
        LinkedList workList = new LinkedList();
        workList.add(root);
        while (!workList.isEmpty()) {
            Node current = (Node) workList.removeFirst();
            if (current.left != null) {
                // checks that the tree has no cycle
                if (!visited.add(current.left))
                    return false;
                workList.add(current.left);
            }
            if (current.right != null) {
                // checks that the tree has no cycle
                if (!visited.add(current.right))
                    return false;
                workList.add(current.right);
            }
        }
        return true;
    }

    private int numNodes(Node n) {
        if (n == null)
            return 0;
        return 1 + numNodes(n.left) + numNodes(n.right);
    }

    private boolean isOrdered(Node n) {
        return isOrdered(n, -1, -1);
    }

    private boolean isOrdered(Node n, int min, int max) {
        // if (n.info == null)
        // return false;
        if (n.info == -1)
            return false;
        // if ((min != null && n.info.compareTo(min) <= 0)
        // || (max != null && n.info.compareTo(max) >= 0))
        if ((min != -1 && n.info <= (min)) || (max != -1 && n.info >= (max)))

            return false;
        if (n.left != null)
            if (!isOrdered(n.left, min, n.info))
                return false;
        if (n.right != null)
            if (!isOrdered(n.right, n.info, max))
                return false;
        return true;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer();
        buf.append("{");
        if (root != null)
            buf.append(root.toString());
        buf.append("}");
        return buf.toString();
    }
   
    public static IFinitization finSearchTree(int numNodes) throws Exception {
        return finSearchTree(numNodes, numNodes, numNodes, 0, numNodes - 1);
    }
    
    public static IFinitization finSearchTree(int numNodes, int minSize,
            int maxSize, int minData, int maxData) throws Exception {

        IFinitization f = FinitizationFactory.create(SearchTree.class);

        IObjSet nodes = f.createObjSet(Node.class, numNodes);
        nodes.setNullAllowed(true);

        IIntSet sizes = f.createIntSet(minSize, maxSize);
        IIntSet values = f.createIntSet(minData, maxData);

        f.set("root", nodes);
        f.set("size", sizes);
        f.set("Node.left", nodes);
        f.set("Node.right", nodes);
        f.set("Node.info", values);

        return f;

    }

}
