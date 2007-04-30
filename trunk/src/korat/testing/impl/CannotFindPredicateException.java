package korat.testing.impl;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class CannotFindPredicateException extends KoratMethodException {

    private static final long serialVersionUID = 6737285687008444264L;

    public CannotFindPredicateException(Class cls, String methodName,
            String message, Throwable cause) {
        super(cls, methodName, message, cause);
    }

    public CannotFindPredicateException(Class cls, String methodName,
            String message) {
        super(cls, methodName, message);
    }

    public CannotFindPredicateException(Class cls, String methodName,
            Throwable cause) {
        super(cls, methodName, cause);
    }

    public CannotFindPredicateException(Class cls, String methodName) {
        super(cls, methodName);
    }

}
