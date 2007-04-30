package korat.utils.io;

/**
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public interface ISerializer {

    /**
     * Serializes next object to the stream
     * 
     */
    public void serialize(Object toSerialize);

}
