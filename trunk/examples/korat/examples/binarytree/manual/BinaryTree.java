package korat.examples.binarytree.manual;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import korat.finitization.IClassDomain;
import korat.finitization.IFinitization;
import korat.finitization.IIntSet;
import korat.finitization.IObjSet;
import korat.finitization.impl.FinitizationFactory;
import korat.instrumentation.IKoratTouchable;
import korat.instrumentation.Setter;
import korat.testing.ITester;

/**
 * <p>
 * This is manually instrumented examples that illustrates some of the
 * principles that korat instrumentation uses. To run this example, the switch
 * <code>--excludePackages korat.examples.binarytree.manual</code> has to be
 * specified since this it doesn't need to be instrumented again (online by Korat).
 * </p>
 * 
 * Usage: <br/>
 * <pre>
 *   $ java korat.Korat --class korat.examples.binarytree.manual.BinaryTree \
 *                      --excludePackages korat.examples.binarytree.manual  \
 *                      --args 3
 * </pre>
 * 
 */
public class BinaryTree /* the rest is added by Korat */ implements IKoratTouchable {

    public static class Node /* the rest is added by Korat */ implements IKoratTouchable  {

        // instance of the ITester class to be initialized in the special 
        // constructor added by Korat. This instance gets notified about 
        // field accesses.
        transient private ITester __myTester;

        // special constructor added by Korat instrumentation
        public Node(ITester __myTester) {
            this.__myTester = __myTester;
        }
        
        // start -- IKoratTouchable interface implementation -----------------
        
        transient private boolean __korat_touched__;
        
        public void __korat__touch__initialize() {
            if (!__korat_touched__)
                return;
            __korat_touched__ = false;
            if (left instanceof IKoratTouchable)
                ((IKoratTouchable)left).__korat__touch__initialize();
            if (right instanceof IKoratTouchable)
                ((IKoratTouchable)right).__korat__touch__initialize();
        }
        
        public void __korat__touch() {
            if (__korat_touched__)
                return;
            __korat_touched__ = true;
            if (left instanceof IKoratTouchable)
                ((IKoratTouchable)left).__korat__touch();
            if (right instanceof IKoratTouchable)
                ((IKoratTouchable)right).__korat__touch();
        }

        // end -- IKoratTouchable interface implementation -------------------

        private Node left;

        // start -- instrumentation for field left ---------------------------
        
        transient private int __id_left = -1;
        
        void setLeft(Node left) {
            this.left = left;
        }

        static class Korat_left_setter extends Setter {
            Node ___this;
            Korat_left_setter(Node ___this) {
                this.___this = ___this;
            }
            @Override
            public void set(Object o) {
                ___this.left = (Node) o;
            }
        }
        
        public Object __korat_get_left_setter(int field_id) {
            __id_left = field_id;
            return new Korat_left_setter(this);
        }

        Node getLeft() {
            __myTester.notifyFieldAccess(__id_left);
            return left;
        }

        // end -- instrumentation for field left -----------------------------
        
        private Node right;

        // start -- instrumentation for field right --------------------------
        
        transient private int __id_right = -1;
        
        void setRight(Node right) {
            this.right = right;
        }

        static class Korat_right_setter extends Setter {
            Node ___this;
            Korat_right_setter(Node ___this) {
                this.___this = ___this;
            }
            @Override
            public void set(Object o) {
                ___this.right = (Node) o;
            }
        }

        public Object __korat_get_right_setter(int field_id) {
            __id_right = field_id;
            return new Korat_right_setter(this);
        }

        Node getRight() {
            __myTester.notifyFieldAccess(__id_right);
            return right;
        }
        
        // end -- instrumentation for field right ----------------------------

    }


    // instance of the ITester class to be initialized in the special 
    // constructor added by Korat. This instance gets notified about 
    // field accesses. 
    transient private ITester __myTester;

    // special constructor added by Korat instrumentation 
    public BinaryTree(ITester __myTester) {
        this.__myTester = __myTester;
    }
    
    // start -- IKoratTouchable interface implementation -----------------
    
    transient private boolean __korat_touched__;
    
    public void __korat__touch__initialize() {
        if (!__korat_touched__)
            return;
        __korat_touched__ = false;
        if (root instanceof IKoratTouchable)
            ((IKoratTouchable)root).__korat__touch__initialize();
    }
    
    public void __korat__touch() {
        if (__korat_touched__)
            return;
        __korat_touched__ = true;
        if (root instanceof IKoratTouchable)
            ((IKoratTouchable)root).__korat__touch();
    }

    // end -- IKoratTouchable interface implementation -----------------
    
    private Node root;

    // start -- instrumentation for field ROOT ---------------------------

    transient private int __id_root = -1;
    
    static class Korat_root_setter extends Setter {
        BinaryTree ___this;
        Korat_root_setter(BinaryTree ___this) {
            this.___this = ___this;
        }
        public void set(Object o) {
            ___this.root = (Node) o;
        }
    }

    public void setRoot(Node root) {
        this.root = root;
    }

    public Object __korat_get_root_setter(int field_id) {
        __id_root = field_id;
        return new Korat_root_setter(this);
    }

    public Node getRoot() {
        __myTester.notifyFieldAccess(__id_root);
        return root;
    }

    // end -- instrumentation for field ROOT ---------------------------

    private int size;

    // start -- instrumentation for field SIZE ---------------------------

    transient private int __id_size = -1;

    public void setSize(int size) {
        this.size = size;
    }

    static class Korat_size_setter extends Setter {
        BinaryTree ___this;
        Korat_size_setter(BinaryTree ___this) {
            this.___this = ___this;
        }
        @Override
        public void set(int i) {
            ___this.size = i;
        }
    }

    public Object __korat_get_size_setter(int field_id) {
        __id_size = field_id;
        return new Korat_size_setter(this);
    }

    public int getSize() {
        __myTester.notifyFieldAccess(__id_size);
        return size;
    }

    // end -- instrumentation for field SIZE ---------------------------

    @SuppressWarnings("unchecked")
    public boolean has(Node n) {

        if (getRoot() == null)
            return false;

        LinkedList workList = new LinkedList();
        workList.add(getRoot());
        while (!workList.isEmpty()) {
            Node current = (Node) workList.removeFirst();
            if (current == n)
                return true;
            if (current.getLeft() != null)
                workList.add(current.getLeft());
            if (current.getRight() != null)
                workList.add(current.getRight());
        }
        return false;
    }

    // Class invariant ::
    @SuppressWarnings("unchecked")
    public boolean repOK() {

        if (getRoot() == null)
            return getSize() == 0;

        // checks that tree has no cycle
        Set visited = new HashSet();
        visited.add(getRoot());
        LinkedList workList = new LinkedList();
        workList.add(getRoot());
        while (!workList.isEmpty()) {
            Node current = (Node) workList.removeFirst();
            if (current.getLeft() != null) {
                if (!visited.add(current.getLeft()))
                    return false;

                workList.add(current.getLeft());
            }
            if (current.getRight() != null) {
                if (!visited.add(current.getRight()))
                    return false;

                workList.add(current.getRight());
            }
        }

        // checks that size is consistent
        return (visited.size() == getSize());
    }

    public static IFinitization finBinaryTree(int size) throws Exception {
        return finBinaryTree(size, size, size);
    }
    
    public static IFinitization finBinaryTree(int numNodes, int minSize,
            int maxSize) throws Exception {
        IFinitization f = FinitizationFactory.create(BinaryTree.class);

        IClassDomain nodeDomain = f.createClassDomain(Node.class, numNodes);
        IObjSet nodes = f.createObjSet(Node.class, true);
        nodes.addClassDomain(nodeDomain);

        IIntSet sizes = f.createIntSet(minSize, maxSize);

        f.set("root", nodes);
        f.set("size", sizes);
        f.set("Node.left", nodes);
        f.set("Node.right", nodes);

        return f;
    }

    public String toString() {
        return "BinaryTree";
    }

}
