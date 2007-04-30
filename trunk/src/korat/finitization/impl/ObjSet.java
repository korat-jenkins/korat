package korat.finitization.impl;

import java.util.ArrayList;
import java.util.List;

import korat.finitization.IClassDomain;
import korat.finitization.IObjSet;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class ObjSet extends FieldDomain implements IObjSet {

    static class CacheElement {
        Object obj;

        ClassDomain oset;

        int objIndexWithinObjSet;

        int indexOfFirstObjectInNextClassDomain;

        CacheElement(Object obj, ClassDomain cd, int objIndexWithinObjSet,
                int indexOfFirstObjectInNextClassDomain) {
            this.obj = obj;
            this.oset = cd;
            this.objIndexWithinObjSet = objIndexWithinObjSet;
            this.indexOfFirstObjectInNextClassDomain = indexOfFirstObjectInNextClassDomain;
        }
    }

    private List<CacheElement> cache = new ArrayList<CacheElement>();

    private boolean isCacheValid = false;

    protected List<ClassDomain> domains;

    private boolean nullAllowed = false;

    private void updateCache() {
        cache.clear();

        int cnt = 0;
        int nDomains = domains.size();
        for (int iDom = 0; iDom < nDomains; iDom++) {
            ClassDomain cd = domains.get(iDom);
            int nObjs = cd.getSize();
            int firstInNext = cnt + nObjs;
            if (iDom == nDomains - 1)
                firstInNext = -1;

            for (int i = 0; i < nObjs; i++) {
                cache.add(new CacheElement(cd.getObject(i), cd, i, firstInNext));
            }
            cnt += nObjs;
        }
        isCacheValid = true;
    }

    ObjSet(Class classOfField) {
        super(classOfField);
        domains = new ArrayList<ClassDomain>();
        if (classOfField.isArray() || classOfField.isPrimitive())
            throw new IllegalArgumentException(
                    "classOfField parameter must be Class object of a class or interface");
    }

    ObjSet(String classOfFieldName) throws ClassNotFoundException {
        this(Class.forName(classOfFieldName, false,
                Finitization.getClassLoader()));
    }

    /**
     * @throws IllegalArgumentException -
     *             if this field is not assignable from the given class domain
     */
    @SuppressWarnings("unchecked")
    public boolean addClassDomain(IClassDomain domain) {
        // handles NullClassDomain separately
        if (domain.getClassOfObjects() == null) {
            boolean retVal = !isNullAllowed();
            setNullAllowed(true);
            return retVal;
        }

        if (!classOfField.isAssignableFrom(domain.getClassOfObjects())) {
            throw new IllegalArgumentException("" + "This field domain ("
                    + classOfField + ") is not assignable"
                    + "from the given class domain ("
                    + domain.getClassOfObjects() + ")");
        }

        if (domains.contains(domain))
            return false;
        
        if (domain.getSize() == 0)
            return false;

        domains.add((ClassDomain)domain);
        isCacheValid = false;

        return true;
    }

    public boolean removeClassDomain(IClassDomain domain) {
        // handles NullClassDomain separately
        if (domain.getClassOfObjects() == null) {
            boolean retVal = isNullAllowed();
            setNullAllowed(false);
            return retVal;
        }

        boolean isActuallyRemoved = domains.remove(domain);

        if (isActuallyRemoved)
            isCacheValid = false;

        return isActuallyRemoved;

    }

    public ClassDomain removeClassDomain(int index) {
        // handles NullClassDomain separately
        if (index == 0 && isNullAllowed()) {
            setNullAllowed(false);
            return NullClassDomain.getInstance();
        }

        if (!checkClassDomainIndex(index))
            return null;

        ClassDomain cd = domains.remove(index);
        if (cd != null)
            isCacheValid = false;

        return cd;
    }

    public List<IClassDomain> getClassDomains() {
        
        List<IClassDomain> ret = new ArrayList<IClassDomain>();
        for (ClassDomain c : domains)
            ret.add(c);
        
        return ret;
        
    }

    public ClassDomain getClassDomain(int classDomainIndex) {
        if (!checkClassDomainIndex(classDomainIndex))
            return null;

        return domains.get(classDomainIndex);
    }

    public ClassDomain getClassDomainFor(int objectIndex) {
        if (!isCacheValid)
            updateCache();

        if (objectIndex < 0 || objectIndex >= cache.size())
            return null;

        return cache.get(objectIndex).oset;
    }

    public ClassDomain getNextClassDomainFor(int objectIndex) {
        if (!checkObjectIndex(objectIndex))
            return null;

        ClassDomain cd = getClassDomainFor(objectIndex);
        int cdInd = domains.indexOf(cd);
        return getClassDomain(cdInd + 1);
    }

    public int getNumOfClassDomains() {
        return domains.size();
    }

    public int getSizeOfClassDomain(int classDomainIndex) {
        if (!checkClassDomainIndex(classDomainIndex))
            return -1;

        return domains.get(classDomainIndex).getSize();
    }

    public int getNumberOfElements() {
        if (!isCacheValid)
            updateCache();

        return cache.size();
    }

    public boolean isPrimitiveType() {
        return false;
    }
    
    public boolean isArrayType() {
        return false;
    }

    public Object[] getAllObjects() {

        List<Object> objs = new ArrayList<Object>();
        for (ClassDomain cd : domains) {
            objs.addAll(cd.getObjects());
        }

        return objs.toArray();
    }

    @SuppressWarnings("unchecked")
    public Object[] getObjectsOfClass(Class cls) {
        List<Object> objs = new ArrayList<Object>(getNumberOfElements());
        for (ClassDomain cd : domains) {
            Class classOfObjects = cd.getClassOfObjects();
            if (classOfObjects != null)
                if (cls.isAssignableFrom(classOfObjects))
                    objs.addAll(cd.getObjects());
        }

        return objs.toArray();
    }

    /**
     * Returns instances of the class domain with the given index in this
     * <code>ObjSet</code>.
     * 
     * @param classDomainIndex
     *            index of the class domain within this field domain
     * @return objects of the class domain with the given index or zero-size
     *         array if classDomainIndex is invalid.
     * @see #getObjectsOfClass(Class)
     * @see #getObject(int)
     * @see #getAllObjects()
     * 
     */
    public Object[] getObjectsOfClass(int classDomainIndex) {
        ClassDomain cd = getClassDomain(classDomainIndex);
        if (cd == null)
            return new Object[0];

        return cd.getObjects().toArray();
    }

    /**
     * Returns object with the given index in entire <code>ObjSet</code>.
     * 
     * @param objectIndex -
     *            zero based index of the object in this field domain
     * @return requested object. If <code>isNullAllowed</code> evaluates to
     *         <code>true</code>, the zero-index object in the
     *         <code>ObjSet</code> is always <code>null</code>. Also
     *         returns <code>null</code> if index is out of bounds.
     * 
     * @see #getAllObjects()
     * @see #getObjectsOfClass(Class)
     * @see #getObjectsOfClass(int)
     */
    public Object getObject(int objectIndex) {
        if (!isCacheValid)
            updateCache();

        if (objectIndex < 0 || objectIndex >= cache.size())
            return null;

        return cache.get(objectIndex).obj;
    }

    public void setNullAllowed(boolean allowed) {
        if (!nullAllowed && allowed) {
            domains.add(0, NullClassDomain.getInstance());
            isCacheValid = false;
        } else if (nullAllowed && !allowed) {
            domains.remove(0);
            isCacheValid = false;
        }

        nullAllowed = allowed;

    }

    public boolean isNullAllowed() {
        return nullAllowed;
    }

    public int getIndexOfFirstObjectInNextClassDomain(int objectIndex) {
        if (!isCacheValid)
            updateCache();

        if (objectIndex < 0 || objectIndex >= cache.size())
            return -1;

        CacheElement elem = cache.get(objectIndex);
        return elem.indexOfFirstObjectInNextClassDomain;
    }

    public int getClassDomainIndexFor(int objectIndex) {
        if (!isCacheValid)
            updateCache();

        if (objectIndex < 0 || objectIndex >= cache.size())
            return -1;

        return cache.get(objectIndex).objIndexWithinObjSet;
    }


}
