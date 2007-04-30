package korat.finitization.impl;

import java.util.ArrayList;
import java.util.List;


/**
 * Represents <em>Field domain for primitive type</em>. <br/>
 * 
 * <p/> <code>PrimitiveTypeSet</code> cannot contain any class domains,
 * because values of these fields are not objects. Actually, primitive type
 * field domain should contain exactly one class domain of the same primitive
 * type. Implementation of that would require parallel class hierarchy of class
 * domains on one side and the same hierarchy for field domains on the other
 * side, for all primitive types. That solution is not very convenient, so, in
 * this implementation, all of <code>IPrimitiveTypeSet</code> subtypes should
 * manage elements of that field domain internally.
 * 
 * @see BooleanSet
 * @see ByteSet
 * @see DoubleSet
 * @see FloatSet
 * @see IntSet
 * @see LongSet
 * @see ObjSet
 * @see ShortSet
 * 
 * @author korat.team
 * 
 */
public abstract class PrimitiveTypeSet extends FieldDomain {

    protected List<Number> primitives = new ArrayList<Number>();

    PrimitiveTypeSet(Class classOfObjects) {
        super(classOfObjects);
        if (!classOfObjects.isPrimitive()) {
            throw new RuntimeException(
                    "classOfObjects of the PrimitiveTypeSet must be Class of the primitive type");
        }
    }
    
    public int getClassDomainIndexFor(int objectIndex) {
        if (!checkObjectIndex(objectIndex))
            return -1;

        return objectIndex;
    }

    public int getIndexOfFirstObjectInNextClassDomain(int objectIndex) {
        return -1;
    }

    public int getNumberOfElements() {
        return primitives.size();
    }

    public int getNumOfClassDomains() {
        return 1;
    }

    public int getSizeOfClassDomain(int classDomainIndex) {
        if (!checkClassDomainIndex(classDomainIndex))
            return -1;

        return getNumberOfElements();
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
    
    public boolean isPrimitiveType() {
        return true;
    }
    
    public boolean isArrayType() {
        return false;
    }

}
