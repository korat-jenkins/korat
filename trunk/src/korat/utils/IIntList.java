package korat.utils;

/**
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public interface IIntList {

    /**
     * Returns number of the elements contained in this list.
     * 
     */
    int numberOfElements();

    /**
     * Returns true if there are no elements in the list, false if there is at
     * least one element in the list
     * 
     */
    boolean isEmpty();

    /**
     * Returns whether list contains element <code>elem</code>
     * 
     */
    boolean contains(int elem);

    /**
     * Returns array representation of IntList elements
     * 
     */
    int[] toArray();

    /**
     * Adds an element to the end of IIntList. New elements can always be added
     * to IIntList
     * 
     */
    boolean add(int arg0);

    /**
     * Removes last element from the IIntList. This operation can be
     * successfully carried out only if the list is not empty.
     * 
     */
    int removeLast();

    /**
     * Clears all elements from the list.
     * 
     */
    void clear();

    /**
     * Gets i-th element from the list. This operation can be successfully
     * carried out only if the index of the object is smaller then the number of
     * the elements in the list. Otherwise, the results are unpredictable.
     * 
     * <p>
     * Number of elements in the list can be obtained by invoking
     * <code>numberOfElements()</code>.
     * 
     */
    int get(int index);

    /**
     * Sets i-th element of the list to value <code>elem</code>. This
     * operation can be successfully carried out only if the index of the object
     * is smaller then the number of the elements in the list. Otherwise, the
     * results are unpredictable.
     * 
     * <p>
     * Number of elements in the list can be obtained by invoking
     * <code>numberOfElements()</code>.
     * 
     */
    int set(int index, int elem);
}
