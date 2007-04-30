package korat.gui.viz.metamodel;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class SigDoesNotExistException extends Exception {

    private static final long serialVersionUID = 7418475581232595004L;

    private String sigName = null;

    public SigDoesNotExistException(String sigName, String message,
            Throwable cause) {
        super(message, cause);
        this.sigName = sigName;
    }

    public SigDoesNotExistException(String sigName, String message) {
        this(sigName, message, null);
    }

    public SigDoesNotExistException(String sigName) {
        this(sigName, null, null);
    }

    public SigDoesNotExistException(String sigName, Throwable cause) {
        this(sigName, null, cause);
    }

    public String getSigName() {
        return sigName;
    }

}
