package korat.utils;

/**
 * List of integers with stack policy
 * 
 * Different mechanism for checking if the field index is already contained in
 * the accessed field list. This implementation should work faster than
 * IntListBS, which uses java.util.BitSet to check for presence of value inside
 * the list.
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class IntListAI implements IIntList {
    
    protected int[] elems;

    protected int lastElementIndex = -1;
    
    int cvelems[];

    int cnt;

    public IntListAI(int candidateVectorSize) {
        elems = new int[candidateVectorSize];
        cvelems = new int[candidateVectorSize];
        cnt = 1;
    }


    public boolean contains(int elem) {
        return cvelems[elem] == cnt;
    }


    public boolean add(int elem) {
        if (contains(elem))
            return false;
        
        elems[++lastElementIndex] = elem;
        cvelems[elem] = cnt;
        
        return true;
    }


    public int removeLast() {
        int ret = elems[lastElementIndex--];
        cvelems[ret] = -1;
        return ret;
    }


    public void clear() {
        cnt++;
        //it is ok to overflow.
        if (cnt == -1) {
            cvelems = new int[cvelems.length];
            cnt = 1;
        }
        lastElementIndex = -1;
    }


    public int set(int idx, int elem) {
        cvelems[idx] = cnt;
        return elems[idx] = elem;
    }


    public int numberOfElements() {
        return lastElementIndex + 1;
    }

    public boolean isEmpty() {
        return lastElementIndex == -1;
    }

    public int get(int idx) {
        return elems[idx];
    }

    public int[] toArray() {
        int[] ret = new int[numberOfElements()];
        for (int i = 0; i < numberOfElements(); i++)
            ret[i] = elems[i];
        return ret;
    }
    
    public String toString() {
        StringBuffer sb = new StringBuffer("[");
        int size = numberOfElements();
        for (int i = 0; i < size-1; i++)
            sb.append(elems[i]).append(", ");
        sb.append(elems[size-1]).append("]");
        
        return sb.toString();
    }

}
