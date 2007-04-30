package korat.finitization.impl;

import korat.finitization.IBooleanSet;

/**
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class BooleanSet extends FieldDomain implements IBooleanSet {

    public BooleanSet() {
        super(boolean.class);
    }

    /**
     * gets boolean value for the corresponding candidate vector index
     * 
     * @param index -
     *            candidate vector index
     * @return - boolean value: <code>false</code> if <code>index == 0</code>
     *         i or <code>true</code> if <code>index == 1</code>
     */
    public boolean getBoolean(int index) {
        if (index == 0)
            return false;
        else if (index == 1)
            return true;
        else
            throw new RuntimeException("Candidate vector index "
                    + index + " for Boolean Set is not allowed");
    }

    public Class getClassOfField() {
        return boolean.class;
    }

    public boolean isPrimitiveType() {
        return true;
    }
    
    public boolean isArrayType() {
        return false;
    }

    public int getNumberOfElements() {
        return 2;
    }

    public int getClassDomainIndexFor(int objectIndex) {
        if (objectIndex == 0) 
            return 0;
        else // for any other objectIndex
            return 1;
    }

    public int getNumOfClassDomains() {
        return 1;
    }

    public ClassDomain getClassDomain(int classDomainIndex) {
        return null;
    }

    public ClassDomain getClassDomainFor(int objectIndex) {
        return null;
    }

    public ClassDomain getNextClassDomainFor(int objectIndex) {
        return null;
    }
    
    public int getSizeOfClassDomain(int classDomainIndex) {
        if (classDomainIndex == 0)
            return 2;
        else
            return -1;
    }

    public int getIndexOfFirstObjectInNextClassDomain(int objectIndex) {
        return -1;
    }




}
