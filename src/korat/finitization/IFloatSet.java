package korat.finitization;

/**
 * Interface that represents field domain of the primitive type
 * <code>float</code>.
 * 
 * @author korat.team
 * 
 */
public interface IFloatSet extends IPrimitiveTypeSet {

    void addFloat(float f);

    void addRange(float min, float diff, float max);

    void removeFloat(float f);

}
