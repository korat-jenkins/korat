package korat.utils;


/**
 * Slow - use one of its derivatives, BS or AI
 * 
 * @see IntListAI
 * @see IntListBS
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class IntList implements IIntList {

    private static final int defaultInitSize = 50;
    
    private int initSize = defaultInitSize;

    private int[] elems = new int[initSize];

    private int lastElementIndex = -1;


    public int numberOfElements() {
        return lastElementIndex + 1;
    }

    public boolean isEmpty() {
        return lastElementIndex == -1;
    }

    public boolean contains(int elem) {
        for (int i = 0; i <= lastElementIndex; i++) {
            if (elems[i] == elem) {
                return true;
            }
        }
        return false;
    }

    public int[] toArray() {
        int[] ret = new int[numberOfElements()];
        for (int i = 0; i < numberOfElements(); i++)
            ret[i] = elems[i];
        return ret;
    }

    public boolean add(int elem) {

        if (contains(elem)) {
            return false;
        }
        
        if (numberOfElements() == elems.length) {
            int[] newElems = new int[elems.length + initSize];
            initSize = initSize * 2;
            for (int i = 0; i < numberOfElements(); i++)
                newElems[i] = elems[i];
            elems = newElems;
        }

        elems[++lastElementIndex] = elem;

        return true;
    }

    /*
     * %% precond: numberOfElements() > 0
     */
    public int removeLast() {
        return elems[lastElementIndex--];
    }


    public void clear() {
        lastElementIndex = -1;
    }

    /*
     * %% precond: numberOfElements() > arg0
     */
    public int get(int idx) {
        return elems[idx];
    }

    /*
     * %% precond: numberOfElements() > index
     */
    public int set(int idx, int elem) {
        return elems[idx] = elem;
    }

}
