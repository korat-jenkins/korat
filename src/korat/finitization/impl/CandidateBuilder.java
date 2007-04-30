package korat.finitization.impl;

import korat.instrumentation.Setter;
import korat.utils.IIntList;


/**
 * <code>CandidateBuilder</code> is responsible for building the object
 * structure from candidate vector. CandidateBuilder is initialized with State
 * Space that contains mapping from candidate vector to objects that are used to 
 * build candidate
 * 
 * @author korat.team
 */
public class CandidateBuilder {

    StateSpace stateSpace;
    
    IIntList changedFields;

    public CandidateBuilder(){
        
    }
    
    public CandidateBuilder(StateSpace stateSpace, IIntList changedFields){
        this.stateSpace = stateSpace;
        this.changedFields = changedFields;
    }

    /**
     * Sets the state space
     * 
     */
    public void setStateSpace(StateSpace stateSpace) {
        this.stateSpace = stateSpace;
    }

    /**
     * Creates object structure from the candidate vector
     * 
     * @param candidateVector -
     *            candidate vector
     * @return - object structure (test case input)
     */
    public Object buildCandidate(int[] candidateVector) {
        if (stateSpace == null)
            return null;

        int n = candidateVector.length;
        CVElem[] structureList = stateSpace.getStructureList();
        assert (n == structureList.length);
            

        for (int i = 0; i < n; i++) {
            
            if(!changedFields.contains(i))
                continue;

            CVElem elem = structureList[i];
            int index = candidateVector[i];

            FieldDomain fd = elem.getFieldDomain();
            Class clsOfField = fd.getClassOfField();
            Setter setter = elem.getSetter();
            
            if (!fd.isPrimitiveType() && !fd.isArrayType()) {

                ObjSet set = (ObjSet) fd;
                Object value = set.getObject(index);
                setter.set(value);
                
            } else if (fd.isArrayType()) {

                ArraySet aset = (ArraySet) fd;
                Object ai = aset.getArray(index);
                setter.set(ai);
                
            } else if (clsOfField == int.class) {

                IntSet set = (IntSet) fd;
                int value = set.getInt(index);
                setter.set(value);
                
            } else if (clsOfField == boolean.class) {

                BooleanSet set = (BooleanSet) fd;
                boolean value = set.getBoolean(index);
                setter.set(value);

            } else if (clsOfField == byte.class) {

                ByteSet set = (ByteSet) fd;
                byte value = set.getByte(index);
                setter.set(value);

            } else if (clsOfField == double.class) {

                DoubleSet set = (DoubleSet) fd;
                double value = set.getDouble(index);
                setter.set(value);

            } else if (clsOfField == float.class) {

                FloatSet set = (FloatSet) fd;
                float value = set.getFloat(index);
                setter.set(value);

            } else if (clsOfField == long.class) {

                LongSet set = (LongSet) fd;
                long value = set.getLong(index);
                setter.set(value);

            } else if (clsOfField == short.class) {

                ShortSet set = (ShortSet) fd;
                short value = set.getShort(index);
                setter.set(value);

            } 

        }

        return stateSpace.getRootObject();
    }

}
