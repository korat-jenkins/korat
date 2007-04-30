package korat.examples.binheap;

import java.util.HashSet;

import korat.finitization.IClassDomain;
import korat.finitization.IFinitization;
import korat.finitization.IObjSet;
import korat.finitization.impl.FinitizationFactory;


@SuppressWarnings("unused")
public class BinomialHeap  {

    // internal class BinomialHeapNode
    public static class BinomialHeapNode {

        private int key; // element in current node

        // depth of the binomial tree having the current node as its root
        private int degree;

        // pointer to the parent of the current node
        private BinomialHeapNode parent;

        // pointer to the next binomial tree in the list
        private BinomialHeapNode sibling;

        // pointer to the first child of the current node
        private BinomialHeapNode child;

        public int getSize() {
            return (1 + ((child == null) ? 0 : child.getSize()) + ((sibling == null) ? 0
                    : sibling.getSize()));
        }

        private BinomialHeapNode reverse(BinomialHeapNode sibl) {
            BinomialHeapNode ret;
            if (sibling != null)
                ret = sibling.reverse(this);
            else
                ret = this;
            sibling = sibl;
            return ret;
        }

        public String toString() {
            BinomialHeapNode temp = this;
            String ret = "";
            while (temp != null) {
                ret += "(";
                if (temp.parent == null)
                    ret += "Parent: null";
                else
                    ret += "Parent: " + temp.parent.key;
                ret += "  Degree: " + temp.degree + "  Key: " + temp.key + ") ";
                if (temp.child != null)
                    ret += temp.child.toString();
                temp = temp.sibling;
            }
            if (parent == null)
                ret += " ";
            return ret;
        }

        // procedures used by Korat
        private boolean repCheckWithRepetitions(int key_, int degree_,
                Object parent_, HashSet<BinomialHeapNode> nodesSet) {

            BinomialHeapNode temp = this;

            int rightDegree = 0;
            if (parent_ == null) {
                while ((degree_ & 1) == 0) {
                    rightDegree += 1;
                    degree_ /= 2;
                }
                degree_ /= 2;
            } else
                rightDegree = degree_;

            while (temp != null) {
                if ((temp.degree != rightDegree) || (temp.parent != parent_)
                        || (temp.key < key_) || (nodesSet.contains(temp)))
                    return false;
                else {
                    nodesSet.add(temp);
                    if (temp.child == null) {
                        temp = temp.sibling;

                        if (parent_ == null) {
                            if (degree_ == 0)
                                return (temp == null);
                            while ((degree_ & 1) == 0) {
                                rightDegree += 1;
                                degree_ /= 2;
                            }
                            degree_ /= 2;
                            rightDegree++;
                        } else
                            rightDegree--;
                    } else {
                        boolean b = temp.child.repCheckWithRepetitions(
                                temp.key, temp.degree - 1, temp, nodesSet);
                        if (!b)
                            return false;
                        else {
                            temp = temp.sibling;

                            if (parent_ == null) {
                                if (degree_ == 0)
                                    return (temp == null);
                                while ((degree_ & 1) == 0) {
                                    rightDegree += 1;
                                    degree_ /= 2;
                                }
                                degree_ /= 2;
                                rightDegree++;
                            } else
                                rightDegree--;
                        }
                    }
                }
            }

            return true;
        }

        private boolean repCheckWithoutRepetitions(int key_,
                HashSet<Integer> keysSet, int degree_, // equal keys not allowed
                Object parent_, HashSet<BinomialHeapNode> nodesSet) {
            BinomialHeapNode temp = this;

            int rightDegree = 0;
            if (parent_ == null) {
                while ((degree_ & 1) == 0) {
                    rightDegree += 1;
                    degree_ /= 2;
                }
                degree_ /= 2;
            } else
                rightDegree = degree_;

            while (temp != null) {
                if ((temp.degree != rightDegree) || (temp.parent != parent_)
                        || (temp.key <= key_) || (nodesSet.contains(temp))
                        || (keysSet.contains(new Integer(temp.key)))) {
                    return false;
                } else {
                    nodesSet.add(temp);
                    keysSet.add(new Integer(temp.key));
                    if (temp.child == null) {
                        temp = temp.sibling;

                        if (parent_ == null) {
                            if (degree_ == 0)
                                return (temp == null);
                            while ((degree_ & 1) == 0) {
                                rightDegree += 1;
                                degree_ /= 2;
                            }
                            degree_ /= 2;
                            rightDegree++;
                        } else
                            rightDegree--;
                    } else {
                        boolean b = temp.child.repCheckWithoutRepetitions(
                                temp.key, keysSet, temp.degree - 1, temp,
                                nodesSet);
                        if (!b)
                            return false;
                        else {
                            temp = temp.sibling;

                            if (parent_ == null) {
                                if (degree_ == 0)
                                    return (temp == null);
                                while ((degree_ & 1) == 0) {
                                    rightDegree += 1;
                                    degree_ /= 2;
                                }
                                degree_ /= 2;
                                rightDegree++;
                            } else
                                rightDegree--;
                        }
                    }
                }
            }

            return true;
        }

        public boolean repOk(int size) {
            // replace 'repCheckWithoutRepetitions' with
            // 'repCheckWithRepetitions' if you don't want to allow equal keys
            return repCheckWithRepetitions(0, size, null,
                    new HashSet<BinomialHeapNode>());
        }

        boolean checkDegree(int degree) {
            for (BinomialHeapNode current = this.child; current != null; current = current.sibling) {
                degree--;
                if (current.degree != degree)
                    return false;
                if (!current.checkDegree(degree))
                    return false;
            }
            return (degree == 0);
        }

        boolean isHeapified() {
            for (BinomialHeapNode current = this.child; current != null; current = current.sibling) {
                if (!(key <= current.key))
                    return false;
                if (!current.isHeapified())
                    return false;
            }
            return true;
        }

        boolean isTree(java.util.Set<NodeWrapper> visited,
                BinomialHeapNode parent) {
            if (this.parent != parent)
                return false;
            for (BinomialHeapNode current = this.child; current != null; current = current.sibling) {
                if (!visited.add(new NodeWrapper(current)))
                    return false;
                if (!current.isTree(visited, this))
                    return false;
            }
            return true;
        }
    }

    public static final class NodeWrapper {
        BinomialHeapNode node;

        NodeWrapper(BinomialHeapNode n) {
            this.node = n;
        }

        public boolean equals(Object o) {
            if (!(o instanceof NodeWrapper))
                return false;
            return node == ((NodeWrapper) o).node;
        }

        public int hashCode() {
            return System.identityHashCode(node);
        }
    }

    // end of helper class BinomialHeapNode

    private BinomialHeapNode Nodes;

    private int size;

    public int getSize() {
        return size;
    }

    public String toString() {
        if (Nodes == null)
            return size + "\n()\n";
        else
            return size + "\n" + Nodes.toString();
    }

    // procedures used by Korat
    public boolean repOkOld() {
        if (size == 0)
            return (Nodes == null);
        if (Nodes == null)
            return false;

        return (Nodes.repOk(size)) && (size == Nodes.getSize());
    }

    boolean checkDegrees() {
        int degree_ = size;
        int rightDegree = 0;
        for (BinomialHeapNode current = Nodes; current != null; current = current.sibling) {
            if (degree_ == 0)
                return false;
            while ((degree_ & 1) == 0) {
                rightDegree++;
                degree_ /= 2;
            }
            if (current.degree != rightDegree)
                return false;
            if (!current.checkDegree(rightDegree))
                return false;
            rightDegree++;
            degree_ /= 2;
        }
        return (degree_ == 0);
    }

    boolean checkHeapified() {
        for (BinomialHeapNode current = Nodes; current != null; current = current.sibling) {
            if (!current.isHeapified())
                return false;
        }
        return true;
    }

    public boolean repOK() {
        if (size == 0)
            return (Nodes == null);
        if (Nodes == null)
            return false;
        // checks that list of trees has no cycles
        java.util.Set<NodeWrapper> visited = new java.util.HashSet<NodeWrapper>();
        for (BinomialHeapNode current = Nodes; current != null; current = current.sibling) {
            // checks that the list has no cycle
            if (!visited.add(new NodeWrapper(current)))
                return false;
            if (!current.isTree(visited, null))
                return false;
        }
        // checks that the total size is consistent
        if (visited.size() != size)
            return false;
        // checks that the degrees of all trees are binomial
        if (!checkDegrees())
            return false;
        // checks that keys are heapified
        if (!checkHeapified())
            return false;
        return true;
    }

    public static IFinitization finBinomialHeap(int size) {

        IFinitization f = FinitizationFactory.create(BinomialHeap.class);

        IClassDomain heapsDomain = f.createClassDomain(BinomialHeapNode.class,
                size);
        IObjSet heaps = f.createObjSet(BinomialHeapNode.class);
        heaps.setNullAllowed(true);
        heaps.addClassDomain(heapsDomain);

        f.set("size", f.createIntSet(0, size));
        f.set("Nodes", heaps);
        f.set(BinomialHeapNode.class, "parent", heaps);
        f.set(BinomialHeapNode.class, "sibling", heaps);
        f.set(BinomialHeapNode.class, "child", heaps);
        f.set(BinomialHeapNode.class, "key", f.createIntSet(1, size));
        f.set(BinomialHeapNode.class, "degree", f.createIntSet(0, size));

        return f;
    }

}
