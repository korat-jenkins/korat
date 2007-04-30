package korat.finitization.impl;

import java.lang.reflect.Method;

import korat.instrumentation.InstrumentationManager;
import korat.instrumentation.Setter;
import korat.utils.ReflectionUtils;

public class CVElem {

    public static CVElem create(Object o, String fName, FieldDomain fDomain,
            StateSpace stateSpace) {

        return new CVElem(o, fName, fDomain, stateSpace);
    }

    protected boolean initialized;

    protected final Object obj;

    protected final String fieldName;

    protected final FieldDomain fieldDomain;

    protected final StateSpace stateSpace;

    protected Setter setter;

    protected int indexInStateSpace = -1;

    private boolean excludeFromSearch;

    protected CVElem(Object o, String fName, FieldDomain fDomain,
            StateSpace stateSpace) {

        this.obj = o;
        this.fieldDomain = fDomain;
        this.fieldName = fName;
        this.stateSpace = stateSpace;

    }

    /**
     * @param exclude - is field that is represented with this CV element
     *                  excluded from search
     */
    public void setExcludeFromSearch(boolean exclude){
        excludeFromSearch = exclude;
    }
    
    /**
     * @return - is field that is represented with this CV element
     *           excluded from search
     */
    public boolean isExcludedFromSearch() {
        return excludeFromSearch;
    }

    
    /**
     * @return Returns the fieldDomain.
     */
    public FieldDomain getFieldDomain() {
        return fieldDomain;
    }

    /**
     * @return Returns the fieldName.
     */
    public String getFieldName() {
        return fieldName;
    }

    /**
     * @return Returns the obj.
     */
    public Object getObj() {
        return obj;
    }

    /**
     * 
     * @return Returns the <code>StateSpace</code> that this
     *  <code>CVElem</code> belongs to.
     */
    public StateSpace getMyStateSpace() {
        return stateSpace;
    }

    /**
     * Sets setter class for this CVElem.
     * 
     * 
     * @see Setter
     * 
     * @param setter - setter
     */
    public void setSetter(Setter setter) {
        if (this.setter == null)
            this.setter = setter;
    }

    /**
     * 
     * @return Setter object for field <code>fieldName</code>
     * of object <code>obj</code>
     * 
     * 
     * @see Setter
     */
    public Setter getSetter() {
        return this.setter;
    }

    /**
     * Initializes this CVElem
     * 
     * @param index -
     *            index in State Space
     */
    public void initialize(int index) {

        if (initialized)
            return;
        initialized = true;

        this.indexInStateSpace = index;

        String setterMethodName = InstrumentationManager.getGetSetterName(fieldName);
        Class[] setterMethodArgs = new Class[] { int.class };
        Object[] args = new Integer[] { index };

        try {

            Method getSetter = ReflectionUtils.getMethod(obj.getClass(),
                    setterMethodName, setterMethodArgs);
            setSetter((Setter) getSetter.invoke(obj, args));

        } catch (ClassCastException e) {

            throw new RuntimeException(
                    "ERROR IN INSTRUMENTATION: RETURNED CLASS FOR FIELD "
                            + fieldName + " IS NOT SETTER ", e);

        } catch (Exception e) {

            throw new RuntimeException(
                    "ERROR IN INSTRUMENTATION: CANNOT FIND SETTER FOR FIELD "
                            + fieldName + " ", e);

        }

    }

}