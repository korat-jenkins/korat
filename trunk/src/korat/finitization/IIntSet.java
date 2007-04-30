package korat.finitization;

/**
 * Interface that represents field domain of the primitive type <code>int</code>.
 * 
 * @author korat.team
 */
public interface IIntSet extends IPrimitiveTypeSet {

    void addInt(int i);

    void addRange(int min, int diff, int max);

    void removeInt(int i);

}