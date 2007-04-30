package korat.testing.impl;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class CannotInvokeFinitizationException extends KoratMethodException {

    private static final long serialVersionUID = -1074713750811471941L;

    public CannotInvokeFinitizationException(Class cls, String methodName,
            String message, Throwable cause) {
        super(cls, methodName, message, cause);
    }

    public CannotInvokeFinitizationException(Class cls, String methodName,
            String message) {
        super(cls, methodName, message);
    }

    public CannotInvokeFinitizationException(Class cls, String methodName,
            Throwable cause) {
        super(cls, methodName, cause);
    }

    public CannotInvokeFinitizationException(Class cls, String methodName) {
        super(cls, methodName);
    }

}
