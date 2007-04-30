package korat.finitization;

import java.util.List;

/**
 * Interface that represents <em>Field domains for reference types</em>.
 * 
 * <p/> <code>IObjSet</code> contains an ordered list of class domains (<code>IClassDomain</code>).
 * Ordering of objects within a class domain must be preserved in each
 * <code>IObjSet</code>.
 * 
 * @author korat.team
 * @see IClassDomain
 */
public interface IObjSet extends IFieldDomain {

    /**
     * Adds new class domain. <code>classOfObjects</code> of the given
     * <code>IClassDomain</code> must be assignable to the
     * <code>classOfField</code> of this <code>IObjSet</code>.
     * 
     * @param domain
     *            Class domain
     * @return if class domain is already added, return false else return true
     * @see IFieldDomain#getClassOfField()
     * @see IClassDomain#getClassOfObjects()
     * @see IFieldDomain
     * @see IClassDomain
     */
    boolean addClassDomain(IClassDomain domain);

    /**
     * Removes class domain from this <code>IObjSet</code>
     * 
     * @param domain
     *            Class domain to be removed
     * @return returns <code>true</code> if domain existed in this field
     *         domain
     */
    boolean removeClassDomain(IClassDomain domain);

    /**
     * Removes class domain from <code>IObjSet</code>
     * 
     * @param index
     *            index of class domain to be removed. If index is out of
     *            bounds, should return <code>null</code> instead of throwing
     *            an exception
     * @return removed class domain
     * @see #removeClassDomain(IClassDomain)
     */
    IClassDomain removeClassDomain(int index);

    /**
     * Returns the list of all class domains
     * 
     * @return list of all class domains
     */
    List<IClassDomain> getClassDomains();

    /**
     * Returns all objest in entire <code>IObjSet</code>.Those are all
     * objects that can be assigned to the field that this
     * <code>IFieldDomain</code> is assigned to.
     * 
     * @return all objects in this field domain.
     */
    Object[] getAllObjects();

    /**
     * Returns instances (direct or indirect) of the given class in entire
     * <code>IObjSet</code>
     * 
     * @param cls
     *            base class
     * @return objects of this field domain which are instances (direct or
     *         indirect) of the given class. If there are no such objects,
     *         returns zero-size array
     * @see #getAllObjects()
     * 
     */
    Object[] getObjectsOfClass(Class cls);

    /**
     * Sets whether <code>null</code> is allowed or not. By default
     * <code>null</code> is not allowed.
     * 
     * @param allowed
     *            whether <code>null</code> value is allowed.
     * @see #isNullAllowed()
     */
    void setNullAllowed(boolean allowed);

    /**
     * Is null allowed or not.
     * 
     * @return is null allowed
     * @see #isNullAllowed()
     */
    boolean isNullAllowed();
}
