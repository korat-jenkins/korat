package korat.finitization;

import java.util.Collection;

/**
 * 
 * <p>
 * Class domain is a set of objects of some concrete reference type. Class
 * domain can be either initialized by specifying the number of objects that
 * will be created or by adding already created objects to it.
 * 
 * 
 * <p>
 * Elements of the class domain must be ordered, and accessible by an index.
 * 
 * <p>
 * Primitive types or abstract types (represented by either abstract class or
 * interface) can not form their class domain
 * 
 * <p>
 * Implementation should override <code>boolean equals(Object)</code> method
 * because two <code>IClassDomain</code>s are equal if their
 * <code>classOfObjects</code> are same.
 * 
 * @author korat.team
 * 
 */
public interface IClassDomain {

    /**
     * Returns the <code>Class</code> object for elements of this Domain
     * 
     */
    Class getClassOfObjects();

    /**
     * Returns the name of class type for this domain
     * 
     * <p>
     * Equal to <code> {IClassDomain}.getClassOfObjects.getName(); </code>
     */
    String getClassNameOfObjects();

    /**
     * Returns number of objects in this class domain
     * 
     */
    int getSize();

    /**
     * <p>
     * If this option is set, the class domain will be included in isomorphism
     * checking. It means, that in case where one object from this class domain
     * is used as a value for some field, it and all objects from this class
     * domain with lower indices, will not be used as a value for any other
     * field in current candidate.
     * 
     * <p>
     * By default, all class domains are included in isomorphism checking
     * 
     * @param include -
     *            true if the class domain should be included in isomorphism
     *            checking
     */
    void includeInIsomorphismCheck(boolean include);

    /**
     * @return is this class domain is included in isomorphism check
     */
    boolean isIncludedInIsomorphismChecking();

    /**
     * Add existing object into class domain. The added object is already
     * initialized and <code>initialize</code> operation will not affect it.
     * 
     * @param obj -
     *            object to be added, null is not allowed
     */
    public void addObject(Object obj);

    /**
     * Add all objects of the existing array to the class domain
     * 
     * @param objs -
     *            array of objects, null is not allowed
     */
    public void addObjects(Object[] objs);

    /**
     * Add all objects of the existing collection to the class domain
     * 
     * @param col -
     *            collection of objects, null is not allowed
     */
    public void addObjects(Collection col);

}
