package korat.finitization;

/**
 * <p>
 * IDoubleSet represents field domain for double fields
 * 
 * @author korat.team
 * 
 */
public interface IDoubleSet extends IPrimitiveTypeSet {

    void addDouble(double d);

    void addRange(double min, double diff, double max);

    void removeDouble(double d);

}
