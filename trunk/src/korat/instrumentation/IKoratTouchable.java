package korat.instrumentation;

/**
 * 
 * <p>
 * IKoratTouchable interface is applied to all instrumented classes. I allows to
 * invoke <code>__korat_touch()</code> operation and transitively access
 * (touch) all fields of given object and all objects reachable from it.
 * 
 * <p>
 * The default implementation of this interface is provided during
 * instrumentation.
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public interface IKoratTouchable {

    public static String koratTouchedName = "__korat_touched__";

    public static String touchName = "__korat__touch";

    public static String touchInitName = "__korat__touch__initialize";

    /**
     * To avoid loops and multiple field accesses, there must be a tag which
     * denotes if the field has been visited or not. Initialize operation resets
     * tags for all visible objects.
     * 
     */
    public void __korat__touch__initialize();

    /**
     * Touch operation touches (accesses) all fields of given object and all
     * objects reachable from it.
     * 
     */
    public void __korat__touch();

}
