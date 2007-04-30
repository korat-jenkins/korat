package korat.testing.impl;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 *
 */
public class CannotInvokePredicateException extends KoratMethodException {

    private static final long serialVersionUID = 4065045854470826333L;

    public CannotInvokePredicateException(Class cls, String methodName, String message, Throwable cause) {
        super(cls, methodName, message, cause);
    }

    public CannotInvokePredicateException(Class cls, String methodName, String message) {
        super(cls, methodName, message);
    }

    public CannotInvokePredicateException(Class cls, String methodName, Throwable cause) {
        super(cls, methodName, cause);
    }

    public CannotInvokePredicateException(Class cls, String methodName) {
        super(cls, methodName);
    }
    
}
