package korat.testing.impl;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class KoratTestException extends Exception {

    private static final long serialVersionUID = -1483361098567057107L;

    public KoratTestException() {
        super();
    }

    public KoratTestException(String message, Throwable cause) {
        super(message, cause);
    }

    public KoratTestException(String message) {
        super(message);
    }

    public KoratTestException(Throwable cause) {
        super(cause);
    }

}
