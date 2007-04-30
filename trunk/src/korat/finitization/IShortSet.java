package korat.finitization;

/**
 * Interface that represents field domain of the primitive type
 * <code>short</code>.
 * 
 * @author korat.team
 * 
 */
public interface IShortSet extends IPrimitiveTypeSet {

    void addShort(short s);

    void addRange(short min, short diff, short max);

    void removeShort(short s);

}
