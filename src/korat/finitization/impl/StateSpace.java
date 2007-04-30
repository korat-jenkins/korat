package korat.finitization.impl;

import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;

import korat.instrumentation.Setter;

/**
 * Represents the state space scheme required by <code>ITestCaseGenerator</code>.
 * Provides operations required by Korat testing engine.
 * 
 * @author korat.team
 */
public class StateSpace {

    protected Object rootObject;

    protected boolean initialized;

    protected CVElem[] structureList;
      
    protected int getIndex(Object obj, String fieldName) {
        
        int ind = -1;

        for (int i = 0; i < structureList.length; i++) {
            CVElem elem = structureList[i];
            if (elem.obj == obj && elem.fieldName.equals(fieldName)) {
                ind = i;
                break;
            }
        }
        return ind;
        
    }

    protected CVElem getCVElem(Object obj, String fieldName) {
        int index = getIndex(obj, fieldName);
        if (index < 0 || index >= structureList.length)
            return null;

        return structureList[index];
    }

    /**
     * Returns <code>ICVElem</code> corresponding to the given index.
     * 
     * @param candidateVectorIndex -
     *            index in candidate vector
     * @return an entry in the candidate vector
     */
    public CVElem getCVElem(int candidateVectorIndex) {
        return structureList[candidateVectorIndex];
    }

    protected static StateSpace lastInstance = null;

    public static StateSpace getLastInstance() {
        return lastInstance;
    }

    public StateSpace() {
        lastInstance = this;
    }

    public CVElem[] getStructureList() {
        return structureList;
    }

    /**
     * Feature that will be used by <code>Finitization</code> object to build
     * <code>StateSpace</code>.
     * 
     * <p/>For example, <code>structureList</code> can contain structures like
     * <code> (Object, String, IFieldDomain)</code>, where the
     * <code>Object</code> parameter is the object, <code>String</code>
     * represents field name of that object, and <code>IFieldDomain</code> is
     * the the domain assigned to that field.
     */
    public void setStructureList(CVElem[] structureList) {
        this.structureList = structureList;
    }

    /**
     * Returns root object of this structure (previously set by
     * <code>IFinitization </code>). This object is the one that is actualy
     * being tested.
     * 
     * @return root object of this structure
     * @see #setRootObject(Object)
     */
    public Object getRootObject() {
        return rootObject;
    }

    /**
     * <code>Finitization</code> should set the root object of the structure,
     * after it creates the <code>StateSpace</code>.
     * 
     * @param root
     *            represents the test case object, the one that is being tested.
     * @see #getRootObject()
     */
    public void setRootObject(Object root) {
        rootObject = root;
    }

    /**
     * Returns field name connected with the given index in candidate vector.<br/>
     * 
     * @param candidateVectorIndex -
     *            index in candidate vector
     * @return requested field name
     */
    public String getFieldName(int candidateVectorIndex) {
        return getCVElem(candidateVectorIndex).getFieldName();
    }

    /**
     * Returns Object connected with the given index in candidate vector.<br/>
     * 
     * @param candidateVectorIndex -
     *            index in candidate vector
     * @return requested object
     */
    public Object getObject(int candidateVectorIndex) {
        return getCVElem(candidateVectorIndex).getObj();
    }

    /**
     * Returns the index of the field <code>fieldName</code> of the Object
     * <code>obj</code> in scheme.
     * 
     * <br/> Required for the Korat search algorithm.
     * 
     * @param obj
     *            object of interest
     * @param fieldName
     *            name of the field of the given object
     * @return requested index
     */
    public int getIndexInCandidateVector(Object obj, String fieldName) {
        return getIndex(obj, fieldName);
    }

    /**
     * Returns FieldDomain connected with the given index in candidate vector.<br/>
     * Required for the Korat search algorithm.
     * 
     * @param candidateVectorIndex -
     *            index in candidate vector
     * @return requested IFieldDomain
     */
    public FieldDomain getFieldDomain(int candidateVectorIndex) {
        return getCVElem(candidateVectorIndex).getFieldDomain();
    }

    /**
     * Helper, returns IFieldIndex connected with the given field in the given
     * object. Calls getFieldDomain(getIndexInCandidateVector(obj, filedName))
     * <br/> Should be implemented as final template method.
     * 
     * @param obj
     * @param fieldName -
     *            name of the field of the given object
     * @return requested IFieldInformation
     */
    public FieldDomain getFieldDomain(Object obj, String fieldName) {

        CVElem elem = getCVElem(obj, fieldName);
        if (elem == null)
            return null;
        else
            return elem.getFieldDomain();

    }

    /**
     * Returns total number of fields for all objects included in finitization
     * 
     * @return total number of fields.
     */
    public int getTotalNumberOfFields() {
        return structureList.length;
    }

    public String toString() {

        StringBuffer ret = new StringBuffer();

        int i = 0;
        for (CVElem elem : structureList) {
            Object obj = elem.getObj();
            ret.append("[" + i + "]");
            i++;
            if (obj == null)
                ret.append("null");
            else
                ret.append(obj.toString());
            ret.append(".");
            ret.append(elem.getFieldName());
            ret.append(" -> ");
            ret.append(elem.getFieldDomain().getClassOfField());
            ret.append(":");
            ret.append(elem.getFieldDomain().getNumberOfElements());
            ret.append("\n");
        }

        return ret.toString();

    }


    /**
     * Returns the index of the field <code>fieldName</code> of the Object
     * <code>obj</code> in scheme. At the same time, if the returned index is
     * greater than or equal to zero (meaning that there is a valid
     * <code>CVElem</code> associated with the given object and its field
     * named <code>fieldName</code>), the given setter object will be set for
     * that <code>CVElem</code>.
     * 
     * <br/> Required for the korat search algorithm.
     * 
     * @param obj
     *            object of interest
     * @param fld
     *            name of the field of the given object
     * @param setter
     * @return requested index
     */
    public int getIndexInCandidateVector(Object obj, String fld, Setter setter) {

        int ret = getIndex(obj, fld);
        if (ret == -1)
            return -1;

        CVElem el = structureList[ret];
        el.setSetter(setter);

        return ret;

    }

    /**
     * Initializes the state space
     *
     */
    public void initialize() {

        if (initialized)
            return;
        initialized = true;

        initializeFieldMap();
        
        for (int i = 0; i < structureList.length; i++)
            structureList[i].initialize(i);
        
    }
    
    
    protected Map<Object, int[]> objFields;
    
    private void initializeFieldMap(){
        
        //for worst case, when each object has only one field:
        objFields = new IdentityHashMap<Object, int[]>(2*structureList.length);
        
        
        Map<Object, Vector<Integer>> objVect = new IdentityHashMap<Object, Vector<Integer>>();
        
        for (int fldIndex = 0; fldIndex < structureList.length; fldIndex++) {
            
            Object obj = structureList[fldIndex].obj;
                        
            if (objVect.containsKey(obj)) {
                objVect.get(obj).add(fldIndex);
                
            } else {
                Vector<Integer> fields = new Vector<Integer>();
                fields.add(fldIndex);
                objVect.put(obj, fields);
                
            }
            
        }
        
        for (Entry<Object, Vector<Integer>> e: objVect.entrySet()) {
            int [] val = new int[e.getValue().size()];
            for (int i = 0; i < e.getValue().size(); i++)
                val[i] = e.getValue().get(i);
            objFields.put(e.getKey(), val);   
        }
        
        
    }

    private static final int [] zeroSizeInt = new int [0];
    public int[] getFieldIndicesFor(Object obj) {
    
        int[] ret = objFields.get(obj); 
        //objects that have no fields will not appear in objFields list
        //so, a zero sized int array should be returned
        if (ret == null)
            return zeroSizeInt; 
        
        return ret;
        
    }
}
