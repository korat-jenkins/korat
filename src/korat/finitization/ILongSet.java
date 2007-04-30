package korat.finitization;

/**
 * Interface that represents field domain of the primitive type
 * <code>long</code>.
 * 
 * @author korat.team
 * 
 */
public interface ILongSet extends IPrimitiveTypeSet {

    void addLong(long l);

    void addRange(long min, long diff, long max);

    void removeLong(long l);

}
