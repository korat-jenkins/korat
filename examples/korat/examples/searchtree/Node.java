package korat.examples.searchtree;

import java.util.HashSet;
import java.util.Set;

// Node must not implement Comparable interface...
@SuppressWarnings("unchecked")
public class Node {
    Node left; // left child

    Node right; // right child

    int info; // data

    Node(Node left, Node right, int info) {
        this.left = left;
        this.right = right;
        this.info = info;
    }

    Node(int info) {
        this.info = info;
    }

    public Node() {

    }

    public String toString() {
        Set visited = new HashSet();
        visited.add(this);
        return toString(visited);
    }

    private String toString(Set visited) {
        StringBuffer buf = new StringBuffer();
        // buf.append(" ");
        // buf.append(System.identityHashCode(this));
        buf.append(" {");
        if (left != null)
            if (visited.add(left))
                buf.append(left.toString(visited));
            else
                buf.append("!tree");

        buf.append("" + this.info + "");

        if (right != null)
            if (visited.add(right))
                buf.append(right.toString(visited));
            else
                buf.append("!tree");
        buf.append("} ");
        return buf.toString();
    }

    public boolean equals(Object that) {
        if (!(that instanceof Node))
            return false;
        Node n = (Node) that;
        // if (this.info.compareTo(n.info) != 0)
        if (this.info > (n.info))
            return false;
        boolean b = true;
        if (left == null)
            b = b && (n.left == null);
        else
            b = b && (left.equals(n.left));
        if (right == null)
            b = b && (n.right == null);
        else
            b = b && (right.equals(n.right));
        return b;
    }

}
