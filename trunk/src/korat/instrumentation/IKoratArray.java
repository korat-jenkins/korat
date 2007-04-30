package korat.instrumentation;

import korat.testing.ITester;

/**
 * IKoratArray represents korat arrays. These arrays are generated at run-time.
 * For further information, please see <code>ArrayGenerator</code> class
 * 
 * @see ArrayGenerator

 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public interface IKoratArray extends IKoratTouchable {

    /**
     * Initializes array with ITester object to which the accessed
     * field indexes will be reported
     * @see ArrayGenerator#createInitializers(javassist.CtClass)
     * @param tester
     */
    void initialize(ITester tester);
    
    /**
     * Returns the length of the korat array
     * 
     * @return - length of an array
     */
    int getLength();   
    
    /**
     * Returns value at position index in the korat array. Position must be 
     * between 0 and KoratArray length.
     * 
     * @see ArrayGenerator#createArrayElementMethods(javassist.CtClass)
     * 
     * @param index - array position of the value
     * @return - value at position index
     */  
    Object getValue(int index);

    /**
     * @see ArrayGenerator#createArrayElementMethods(javassist.CtClass) 
     */
    Setter get_element_setter(int position, int field_id);
    
    /**
     *@see ArrayGenerator#createLengthMethods(javassist.CtClass) 
     */
    Setter get_length_setter(int field_id);
    
}
