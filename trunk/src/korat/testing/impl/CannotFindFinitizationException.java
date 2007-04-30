package korat.testing.impl;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class CannotFindFinitizationException extends KoratMethodException {

    private static final long serialVersionUID = -4529089297942267157L;

    public CannotFindFinitizationException(Class cls, String methodName, String message, Throwable cause) {
        super(cls, methodName, message, cause);
    }

    public CannotFindFinitizationException(Class cls, String methodName, String message) {
        super(cls, methodName, message);
    }

    public CannotFindFinitizationException(Class cls, String methodName, Throwable cause) {
        super(cls, methodName, cause);
    }

    public CannotFindFinitizationException(Class cls, String methodName) {
        super(cls, methodName);
    }
    
}
