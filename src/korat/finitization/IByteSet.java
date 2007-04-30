package korat.finitization;

/**
 * <p>
 * IByteSet represents field domain for byte fields
 *
 * @author korat.team
 * 
 */
public interface IByteSet extends IPrimitiveTypeSet{

    void addByte(byte b);

    void addRange(byte min, byte diff, byte max);

    void removeByte(byte b);

}
