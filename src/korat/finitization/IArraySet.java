package korat.finitization;

/**
 * <p>
 * Interface IArraySet represents field domain (set of values a field can take)
 * for array fields.
 * 
 * <p>
 * Each array is characterized by size and the type of its elements. <br/> It is
 * possible to create arrays with different size. Field domain for array size is
 * represented by <code>IIntSet</code>.
 * 
 * <p>
 * Elements of an array are treated as non-array fields. All elements are of the
 * same type. Bound on the values each element of an array can take is
 * represented by <code>IFieldDomain</code>. 
 * 
 * 
 * @author korat.team
 * 
 */
public interface IArraySet extends IFieldDomain {

    /**
     * Sets domain for the size of the array. Bounds this <code>IArraySet</code>
     * only to arrays of the given sizes.
     * 
     * @param sizes
     *            all possible values for the size of the array
     * 
     */
    void setArraySizes(IIntSet sizes);

    /**
     * Gets the domain for the size of the array.
     * 
     * @return class domain containing <code>int</code>s - all possible sizes
     *         of the array.
     */
    IIntSet getArraySizes();

    /**
     * Sets the field domain for the components of the array. Each component of
     * the array within this <code>IArraySet</code> is bounded by elements of
     * the given <code>componentSet</code>.
     * 
     * @param componentsSet
     *            set of values that array components within this
     *            <code>IArraySet</code> can take, either <code>ObjSet</code>
     *            in case the type array components is reference type, or field
     *            domain of corresponding primitive type.
     */
    void setComponentsSet(IFieldDomain componentsSet);

    /**
     * Gets the class domain of array components in this <code>IArraySet</code>.
     * 
     * @return <code>IClassDomain</code> that has been assigned for the
     *         components of the arrays in this <code>IArraySet</code>.
     */
    IFieldDomain getComponentsSet();

}
