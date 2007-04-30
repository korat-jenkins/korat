package korat.gui.viz;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class UnsupportedTypeException extends Exception {

    private static final long serialVersionUID = 2636018789087731740L;

    public UnsupportedTypeException() {
        super();
    }

    public UnsupportedTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public UnsupportedTypeException(String message) {
        super(message);
    }

    public UnsupportedTypeException(Throwable cause) {
        super(cause);
    }

}
