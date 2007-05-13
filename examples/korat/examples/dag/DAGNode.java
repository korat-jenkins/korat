package korat.examples.dag;


import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * Node for Direct Acyclic Graph. Contains array of DAGNodes, that represent the
 * children of a given DAGNode.
 * 
 * @see DAG
 * 
 */
public class DAGNode {

    private static int autoId = 0;

    transient int id = autoId++;

    DAGNode[] children;
   
    public int getNumberOfChildren() {
        if (children == null) return 0;
        // Children cannot have null value, according to the finitization
        return children.length;
    }
    
    public DAGNode getChild(int i) {
        return children[i];
    }

    /**
     * For each node, checks that neither node, nor any of its descendents is
     * not on the visited path. Additionally, no two children point to the same
     * object.
     * 
     */
    public boolean repOK1(Set<DAGNode> path, Set<DAGNode> visited) {
        if (path.contains(this)) return false;
        path.add(this);
        visited.add(this);
        for (int i = 0; i < getNumberOfChildren(); i++) {
            DAGNode child = getChild(i);
            // two children of a DAG cannot be the same object
            for (int j = 0; j < i; j++)
                if (child == getChild(j))
                    return false;
            // check property for every child of this node
            if (!child.repOK1(path, visited)) return false;
        }
        path.remove(this);
        return true;
    }

    /**
     * Similar to the repOK1, except Stack is used instead of Set
     * 
     */
    public boolean repOK2(Stack<DAGNode> path, Set<DAGNode> visited) {
        path.push(this);
        for (int i = 0; i < getNumberOfChildren(); i++) {
            DAGNode child = getChild(i);
            for (int j = 0; j < i; j++)
                if (child == getChild(j)) return false;
            if (path.search(child) != -1) return false;
            if (!visited.add(child)) continue;
            if (!child.repOK2(path, visited)) return false;
        }
        path.pop();
        return true;
    }

    /**
     * For each node, the children should be ordered by the number of their
     * (direct) children
     * 
     * returns -1 if the DAG property is violated;
     * returns the number of (direct) children otherwise
     */ 
    public int repOK3(Stack<DAGNode> path, Set<DAGNode> visited) {
        path.push(this);
        int max = Integer.MAX_VALUE;
        for (int i = 0; i < getNumberOfChildren(); i++) {
            DAGNode child = getChild(i);
            for (int j = 0; j < i; j++)
                if (child == getChild(j)) return -1;
            if (path.search(child) != -1) return -1;
            if (!visited.add(child)) continue;

            int n = child.repOK3(path, visited);
            if (n == -1) return -1;
            if (n > max) return -1;
            max = n;
        }
        path.pop();
        return getNumberOfChildren();
    }

    /**
     * For each node, the children should be ordered by the number of their
     * (total) descendants
     * 
     * returns -1 if the property is violated; 
     * returns the number of (total) descendants otherwise
     */
    public int repOK4(Stack<DAGNode> path, Set<DAGNode> visited,
                      HashMap<DAGNode, Integer> weight) {
        path.push(this);
        int max = Integer.MAX_VALUE;
        int descendants = 1;
        for (int i = 0; i < getNumberOfChildren(); i++) {
            DAGNode child = getChild(i);
            for (int j = 0; j < i; j++)
                if (child == getChild(j)) return -1;
            if (path.search(child) != -1) return -1;
            int n;
            // return the total number of descendants
            if (!visited.add(child)) {
                // if child was visited, get value from cache
                n = weight.get(child);
            } else {
                // if the child was not visited, find it
                n = child.repOK4(path, visited, weight);
                if (n == -1) return -1;
            }
            if (n > max) return -1;
            max = n;
            descendants += n;
        }
        path.pop();
        weight.put(this, descendants);  // store the number of descendants
        return descendants;
    }
    
    private void updatePartitions(DAGNode parent, DAGNode child, HashMap<DAGNode, Set<DAGNode>> partitions) {
        Set<DAGNode> parentsPartition = partitions.get(parent);
        Set<DAGNode> childsPartition = partitions.get(child);
        
        if (parentsPartition == null) {
            if (childsPartition == null) {
                // parent -> null
                // child  -> null
                Set<DAGNode> p = new HashSet<DAGNode>();
                p.add(parent);
                p.add(child);
                partitions.put(parent, p);
                partitions.put(child, p);
            } else {
                // parent -> null
                // child  -> {...}
                childsPartition.add(parent);
                partitions.put(parent, childsPartition);
            }
        } else {
            if (childsPartition == null) {
                // parent -> {...}
                // child  -> null
                parentsPartition.add(child);
                partitions.put(child, parentsPartition);
            } else {
                // parent -> {...}
                // child  -> {...}
                parentsPartition.addAll(childsPartition);
                for (DAGNode n : childsPartition) {
                    partitions.put(n, parentsPartition);
                }
            }
        }
        
    }
    
    /**
     * Additionally requires that graph is connected
     */
    public int repOK6(Stack<DAGNode> path, Set<DAGNode> visited,
            HashMap<DAGNode, Integer> weight, HashMap<DAGNode, Set<DAGNode>> partitions) {
        path.push(this);
        int max = Integer.MAX_VALUE;
        int descendants = 1;
        int numChildren = getNumberOfChildren();
        if (numChildren == 0) {
            Set<DAGNode> p = partitions.get(this);
            if (p == null) {
                p = new HashSet<DAGNode>();
                p.add(this);
                partitions.put(this, p);
            }
        }
        for (int i = 0; i < numChildren; i++) {
            DAGNode child = getChild(i);
            updatePartitions(this, child, partitions);
            for (int j = 0; j < i; j++)
                if (child == getChild(j)) return -1;
            if (path.search(child) != -1) return -1;
            int n;
            if (!visited.add(child)) {
                n = weight.get(child);
            } else {
                n = child.repOK6(path, visited, weight, partitions);
                if (n == -1) return -1;
            }
            if (n > max) return -1;
            max = n;
            descendants += n;
        }
        path.pop();
        weight.put(this, descendants);
        return descendants;
    }

    /**
     *  DAG doesn't have to be connected but nodes without any edges (in or out) are not allowed
     */
    public int repOK7(Stack<DAGNode> path, Set<DAGNode> visited, HashMap<DAGNode, Integer> weight, Set<DAGNode> notAlone) {
        path.push(this);
        int max = Integer.MAX_VALUE;
        int descendants = 1;
        int numChildren = getNumberOfChildren();
        
        for (int i = 0; i < numChildren; i++) {
            DAGNode child = getChild(i);
            notAlone.add(this);
            notAlone.add(child);
            for (int j = 0; j < i; j++)
                if (child == getChild(j)) return -1;
            if (path.search(child) != -1) return -1;
            int n;
            if (!visited.add(child)) {
                n = weight.get(child);
            } else {
                n = child.repOK7(path, visited, weight, notAlone);
                if (n == -1) return -1;
            }
            if (n > max) return -1;
            max = n;
            descendants += n;
        }
        path.pop();
        weight.put(this, descendants);
        return descendants;
    }


}
