package korat.finitization.impl;

import korat.finitization.IArraySet;
import korat.finitization.IFieldDomain;
import korat.finitization.IIntSet;

/**
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class ArraySet extends FieldDomain implements IArraySet {

    protected IntSet lengthsSet;

    protected FieldDomain valuesSet;

    private ClassDomain arrays;

    public ArraySet(ClassDomain arrays, IntSet lengthsSet,
            FieldDomain valuesSet) {

        super(arrays.getClassOfObjects());

        this.arrays = arrays;
        this.arrays.initialize();
        this.lengthsSet = lengthsSet;
        this.valuesSet = valuesSet;

    }

    /**
     * Returns the array at position <code>index</code> in this field domain.
     * 
     * @param index -
     *            index of array in field domain's list
     * @return - array at position <code>index</code> in this field domain
     *
     */
    public Object getArray(int index) {
        return arrays.getObject(index);
    }

    @Override
    public ClassDomain getClassDomain(int classDomainIndex) {
        return arrays;
    }

    @Override
    public ClassDomain getClassDomainFor(int objectIndex) {
        return arrays;
    }

    @Override
    public int getClassDomainIndexFor(int objectIndex) {

        return arrays.getIndexOf(objectIndex);

    }

    @Override
    public int getIndexOfFirstObjectInNextClassDomain(int objectIndex) {
        return -1;
    }

    @Override
    public ClassDomain getNextClassDomainFor(int objectIndex) {
        return null;
    }

    @Override
    public int getNumOfClassDomains() {
        return 1;
    }

    @Override
    public int getNumberOfElements() {
        return arrays.getSize();
    }

    @Override
    public int getSizeOfClassDomain(int classDomainIndex) {

        if (!checkClassDomainIndex(classDomainIndex))
            return -1;

        return getNumberOfElements();

    }

    public IIntSet getArraySizes() {
        return lengthsSet;
    }

    public IFieldDomain getComponentsSet() {
        return valuesSet;
    }

    public void setArraySizes(IIntSet sizes) {
        lengthsSet = (IntSet)sizes;
    }

    public void setComponentsSet(IFieldDomain componentsSet) {
        valuesSet = (IntSet)componentsSet;
    }

    /**
     * Returns maximal length of array objects in this <code>IArraySet</code>.
     * 
     */
    public int getMaxLength() {

        return ((IntSet)lengthsSet).getMax();
    }


    public boolean isArrayType() {
        return true;
    }
    
    public boolean isPrimitiveType() {
        return false;
    }

}
