package korat.examples.dag;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.Stack;

import korat.finitization.IArraySet;
import korat.finitization.IFinitization;
import korat.finitization.IIntSet;
import korat.finitization.IObjSet;
import korat.finitization.impl.FinitizationFactory;


/**
 * <p>DAG class represents directed acyclic graphs.
 * </p> 
 * 
 * <p> DAGs are characterized by having directed edges between nodes,
 * and not having directed cycles, i.e. for any vertex v, there is no 
 * nonempty directed path that starts and ends on v.
 * </p>
 * 
 * <p>
 * Nodes of a DAG are represented by DAGNode class, and edges are represented
 * by children field of DAGNode.
 * </p> 
 * 
 * @see DAGNode
 */
public class DAG {

    /**
     * Nodes list contains all nodes of a DAG
     */
    //transient fields are skipped during visualization 
    transient private List<DAGNode> nodes = new LinkedList<DAGNode>();
    
    /**
     * Roots list contains roos of a DAG. It is used for visualization and root
     * counting. It must be updated for every DAG, by invoking <code>calcRoot</code>
     * method from the predicate (See the final lines of repOK methods) 
     * 
     * @see DAG#calcRoots()
     */
    private List<DAGNode> roots = new LinkedList<DAGNode>();
    
    public List<DAGNode> getNodes() {
        return nodes;
    }

    /**
     * Number of nodes in a DAG. 
     */
    int size;

    public int getSize() {
        return size;
    }
    
    public void setSize(int size) {
        this.size = size;
    }
    
    /**
     * <p>Finitization method is used to bound the search space.
     * </p> 
     * 
     * <p>
     * Finitization for DAGs is done the following way: 
     * </p>
     * <ul>
     *  <li><code>size</code> field of DAG objects may have one integer
     *      value, provided from command line
     *  </li>
     *  <li><code>nodes</code> list of DAG objects may have <i>nodeNum</i>
     *      DAGNode objects. <code>Null</code> value is not included   
     *  </li>
     *  <li><code>children</code> array field of DAGNode objects may have
     *      from 0 to <i>nodeNum-1</i> elements, all of which are DAGNodes.
     *      <code>Null</code> value is not included. 
     *  </li>
     * </ul>
     *     
     * 
     * @param nodeNum - number of nodes
     * @return finitization object, that bounds the search space
     */
    public static IFinitization finDAG(int nodeNum) {
        IFinitization f = FinitizationFactory.create(DAG.class);

        f.set("size", f.createIntSet(nodeNum));

        IObjSet nodes = f.createObjSet(DAGNode.class, nodeNum, false);
        f.addAll("nodes", nodes);
        
        IIntSet arrLen = f.createIntSet(0, nodeNum - 1);
        IArraySet childrenArray = f.createArraySet(DAGNode[].class, arrLen, nodes, nodeNum);
        
        f.set("DAGNode.children", childrenArray);
        return f;
    }
    
    /**
     * There can be many finitization methods, with different arguments and/or names
     * The use of finitization method parameters can be convenient way to pass some 
     * additional info to the class from the command line.
     * 
     * @param maxNodes - maximal number of nodes
     * @param baseRepOK - base repOK implementation (from repOK1 to repOK7)
     * @param numOfRoots - exact number of roots (1 <= numOfRoots <=MaxNodes), or 0 for any
     * @return finitization for DAG example
     */
    public static IFinitization finDAG(int maxNodes, int baseRepOK, int numOfRoots) {
        IFinitization f = finDAG(maxNodes);
        
        // These field assignments are not part of finitization, but a convenient way  
        // to pass additional info from the command line
        BASE_REP_OK = baseRepOK;
        NUM_OF_ROOTS = numOfRoots;
        
        return f;
    }
    
    /**
     * Checks for basic DAG properties. Checks if graph contains loops. If it
     * does, returns false, otherwise returns true. 
     */
    public boolean repOK1() {
        Set<DAGNode> visited = new HashSet<DAGNode>();
        Set<DAGNode> path = new HashSet<DAGNode>();
        for (DAGNode node : getNodes()) {
            if (!visited.contains(node)) {
                if (!node.repOK1(path, visited)) {
                    return false;
                }
            }
        }
        boolean ok = size == visited.size();
        if (ok)
            calcRootsViz(); 
        return ok;  
    }
    
    /**
     * Similar to the previous one, except Stack is used instead of Set, and
     * elements are visited in other fashion.  
     */
    public boolean repOK2() {
        Set<DAGNode> visited = new HashSet<DAGNode>();
        Stack<DAGNode> path = new Stack<DAGNode>();
        for (int i = getNodes().size() - 1; i >= 0; i--) {
            DAGNode node = getNodes().get(i);
            if (visited.add(node))
                if (!node.repOK2(path, visited)) return false;
        }
        boolean ok = size == visited.size();
        if (ok)
            calcRootsViz(); 
        return ok;
    }
    
    /**
     * Additionally requires that for each node, the children should be ordered
     * by the number of their (direct) children
     */
    public boolean repOK3() {
        Set<DAGNode> visited = new HashSet<DAGNode>();
        Stack<DAGNode> path = new Stack<DAGNode>();
        for (int i = getNodes().size() - 1; i >= 0; i--) {
            DAGNode node = getNodes().get(i);
            if (visited.add(node))
                if (node.repOK3(path, visited) == -1) return false;
        }
        boolean ok = size == visited.size();
        if (ok)
            calcRootsViz(); 
        return ok;
    }
    
    /**
     * Additionally requires that for each node, the children should be ordered
     * by the number of their (total) descendants
     */
    public boolean repOK4() {
        Set<DAGNode> visited = new HashSet<DAGNode>();
        Stack<DAGNode> path = new Stack<DAGNode>();
        // weight map contains the number of total descendants 
        // for every node
        HashMap<DAGNode, Integer> weight = new HashMap<DAGNode, Integer>();

        for (int i = getNodes().size() - 1; i >= 0; i--) {
            DAGNode node = getNodes().get(i);
            if (visited.add(node))
                if (node.repOK4(path, visited, weight) == -1) return false;
        }
        boolean ok = size == visited.size();
        if (ok)
            calcRootsViz(); 
        return ok;
    }
          
    /**
     *  Additionally requires that all nodes of DAG are connected
     */
    public boolean repOK6() {
        if (getNodes().isEmpty()) return size == 0;
        Set<DAGNode> visited = new HashSet<DAGNode>();
        Stack<DAGNode> path = new Stack<DAGNode>();
        HashMap<DAGNode, Integer> weight = new HashMap<DAGNode, Integer>();
        // contains partitions of connected DAG components
        HashMap<DAGNode, Set<DAGNode>> partitions = new HashMap<DAGNode, Set<DAGNode>>(); 
        for (DAGNode node : getNodes())
            if (visited.add(node))
                if (node.repOK6(path, visited, weight, partitions) == -1) return false;
        // all nodes mast be part of one partition
        Set<DAGNode> p1 = partitions.get(nodes.get(0));
        boolean ok = (p1 != null && size == p1.size());
        if (ok)
            calcRootsViz(); 
        return ok;
    }
    
    /**
     *  DAG doesn't have to be connected but nodes without any edges (in or out) are not allowed
     */
    public boolean repOK7() {
        if (getNodes().isEmpty()) return size == 0;
        Set<DAGNode> visited = new HashSet<DAGNode>();
        Stack<DAGNode> path = new Stack<DAGNode>();
        HashMap<DAGNode, Integer> weight = new HashMap<DAGNode, Integer>();
        // contains all nodes that are connected to some other node
        Set<DAGNode> notAlone = new HashSet<DAGNode>(); 
        // check properties for all nodes, like for repOK4
        for (DAGNode node : getNodes())
            if (visited.add(node))
                if (node.repOK7(path, visited, weight, notAlone) == -1) return false;
        
        // single components are not allowed
        boolean ok = (size == notAlone.size());
        if (ok)
            calcRootsViz(); 
        return ok;
    }
    
    /**
     * Finds all roots of DAG, and updates roots list
     */
    private void calcRoots() {
        Set<DAGNode> notRootNodes = new HashSet<DAGNode>();
        for (DAGNode n : getNodes()) {
            for (int i = 0; i < n.children.length; i++) {
                notRootNodes.add(n.children[i]);
            }
        }
        roots.clear();
        for (DAGNode n : getNodes()) {
            if (!notRootNodes.contains(n)) {
                roots.add(n);
            }
        }
    }
    
    /**
     * Root finding for visualization purposes only may be turned off during
     * search.
     */
    private void calcRootsViz() {
        if (VISUALIZE)
            calcRoots();
    }

    
    /**
     * <p>This repOK method is used for batch executions. It allows specifying
     * the number of roots that DAG should have. It also allows specifying 
     * which repOKx method to execute.</p>  
     * 
     * <p>Parameters baseRepOKToCall and numOfRoots can be provided from
     * the command line, by invoking the corresponding finitization
     * method.</p> 
     * 
     * @see DAG#finDAG(int, int, int)
     * 
     * @param baseRepOKToCall - the id of some of the base repOKs  
     * @param numOfRoots    - the number of roots that DAG should have
     * 
     * @see DAG#callBaseRepOK(int)
     * @see DAG#repOK1()
     * @see DAG#repOK2()
     * @see DAG#repOK3()
     * @see DAG#repOK4()
     * @see DAG#repOK6()
     * @see DAG#repOK7()
     */
    public boolean repOK_roots(int baseRepOKToCall, int numOfRoots) {
        if (!callBaseRepOK(baseRepOKToCall))
            return false;
        //check the number of roots
        calcRoots();
        if (numOfRoots != roots.size())
            return false;
        return true;
    }
    
    /**
     * Helper method, executes repOK method with given id 
     */
    private boolean callBaseRepOK(int id) {
        switch (id) {
        case 1: return repOK1();
        case 2: return repOK2();
        case 3: return repOK3();
        case 4: return repOK4();
        case 6: return repOK6();
        case 7: return repOK7();
        default:
            throw new RuntimeException("Invalid repOK number!");
        }
    }
    
    /**
     * Parameter VISUALIZE allows finding of DAG roots, just for purpose
     * of visualization. Switching this flag to OFF may increase speed
     * of search when visualization of nodes is not required
     * 
     * @see DAG#calcRoots()
     * @see DAG#calcRootsViz()
     * 
     */
    transient private static boolean VISUALIZE = true;
    
    /**
     * Selects which repOK method to invoke during search
     * 
     * @see DAG#callBaseRepOK(int)
     * @see DAG#finDAG(int, int, int)
     */
    transient private static int BASE_REP_OK;
    
    /**
     * The number of roots that DAG may have. The value can be provided
     * from the command line. If value of the field is 0, then DAG may have
     * any number of roots, depending on the size of the DAG.
     * 
     * @see DAG#finDAG(int, int, int)
     */
    transient private static int NUM_OF_ROOTS;
    
    /**
     * <p>Predicate method that will be executed by default. To change repOK, 
     * issue --predicate <i>other_repok</i> from the command line
     * </p>
     * 
     * <p>Parameters baseRepOK and numOfRoots are provided from the
     * command line when Korat execution starts. There must be 
     * corresponding finitization method, that will set the values 
     * of these fields 
     * </p>
     * 
     * @see DAG#finDAG(int, int, int)
     */
    public boolean repOK() {
        if (BASE_REP_OK != 0 && NUM_OF_ROOTS != 0) {
            return repOK_roots(BASE_REP_OK, NUM_OF_ROOTS);
        } else {
            // if no optional parameters set, this will be the
            // invoked predicate:
            return repOK7();
        }
    }
}
