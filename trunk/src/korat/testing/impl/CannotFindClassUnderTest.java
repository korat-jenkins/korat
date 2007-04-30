package korat.testing.impl;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class CannotFindClassUnderTest extends KoratTestException {

    private static final long serialVersionUID = -3153614910905623474L;

    private String fullClassName;
    
    public CannotFindClassUnderTest(String fullClassName) {
        super();
        this.fullClassName = fullClassName;
    }

    public CannotFindClassUnderTest(String fullClassName, String message, 
            Throwable cause) {
        super(message, cause);
        this.fullClassName = fullClassName;
    }

    public CannotFindClassUnderTest(String fullClassName, String message) {
        super(message);
        this.fullClassName = fullClassName;
    }

    public CannotFindClassUnderTest(String fullClassName, Throwable cause) {
        super(cause);
        this.fullClassName = fullClassName;
    }

    public String getFullClassName() {
        return fullClassName;
    }

}
