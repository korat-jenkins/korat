package korat.utils;

import java.util.BitSet;

/**
 * 
 * Uses bitset to check if field is already contained.
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 * List of integers with stack access policy
 */
public class IntListBS extends IntList {

    private BitSet bs;
    
    public IntListBS() {
        this(1024);
    }
    
    public IntListBS(int initSize) {
        bs = new BitSet(initSize);
    }

    public boolean contains(int elem) {
        return bs.get(elem);
    }

    public boolean add(int arg0) {
        
        boolean ret = super.add(arg0); 

        if (ret)
            bs.set(arg0);
        
        return ret;
        
    }

    public int removeLast() {
        
        int ret = super.removeLast();
        bs.set(ret, false);
        return ret;
        
    }

    public void clear() {
        bs.clear();
        super.clear();
    }

    public int set(int index, int elem) {

        bs.set(get(index), true);
        bs.set(elem);
        return super.set(index, elem);
        
    }

}
