package korat.utils.io;

/**
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public interface IDeserializer {

    /**
     * Reads next object from the stream. If the stream is closed or there are
     * no more objects to read from the stream null is returned
     * 
     */
    public Object readObject();

}
