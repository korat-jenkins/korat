package korat.finitization.impl;

import java.lang.reflect.Method;

import korat.instrumentation.ArrayGenerator;
import korat.instrumentation.Setter;

/**
 * 
 * CVElem representing elements of an array
 * 
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class ArrayElementCVElem extends CVElem {

    protected final int arrayIndex;
    
    /**
     * Parameter fName can be either number, representing the index of array element
     * or "length", representing the length of the array
     * 
     */
    public static ArrayElementCVElem create(Object o, String fName,
            FieldDomain fDomain, StateSpace myStateSpace) {

        try {
            
            int idx = Integer.parseInt(fName);
            return new ArrayElementCVElem(o, idx, fDomain, myStateSpace);
            
        } catch (NumberFormatException e) {
            
            assert ("length".equals(fName));
            assert ( fDomain.getClassOfField() == int.class );
            return new ArrayElementCVElem(o, "length", fDomain, myStateSpace);
            
        }

    }

    protected ArrayElementCVElem(Object o, int elementIndex,
            FieldDomain fDomain, StateSpace stateSpace) {

        super(o, String.valueOf(elementIndex), fDomain, stateSpace);

        arrayIndex = elementIndex;

    }

    private ArrayElementCVElem(Object o, String fName, FieldDomain fDomain,
            StateSpace stateSpace) {

        super(o, fName, fDomain, stateSpace);

        arrayIndex = -1;
    }

    public void initialize(int indexInStateSpace) {

        if (initialized)
            return;

        initialized = true;

        this.indexInStateSpace = indexInStateSpace;

        String setterMethodName;
        Class[] setterMethodArgs;
        Object[] args;

        if (arrayIndex == -1) { // length

            setterMethodName = ArrayGenerator.GET_LENGTH_SETTER_METHOD_NAME;
            setterMethodArgs = new Class[] { int.class };
            args = new Integer[] { indexInStateSpace };

        } else { // array element

            setterMethodName = ArrayGenerator.GET_ELEMENT_SETTER_METHOD_NAME;
            setterMethodArgs = new Class[] { int.class, int.class };
            args = new Integer[] { arrayIndex, indexInStateSpace };

        }

        try {

            Method getSetter = this.obj.getClass().getDeclaredMethod(
                    setterMethodName, setterMethodArgs);
            setSetter((Setter) getSetter.invoke(obj, args));

        } catch (Exception e) {
            throw new RuntimeException("ERROR IN INSTRUMENTATION: ", e);
        }

    }


}
