package korat.testing.impl;

/**
 * 
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class KoratMethodException extends KoratTestException {

    private static final long serialVersionUID = -7838792721550266628L;

    protected Class cls;

    protected String methodName;

    public KoratMethodException(Class cls, String methodName) {
        super();
        this.cls = cls;
        this.methodName = methodName;
    }

    public KoratMethodException(Class cls, String methodName,
            String message, Throwable cause) {
        super(message, cause);
        this.cls = cls;
        this.methodName = methodName;
    }

    public KoratMethodException(Class cls, String methodName,
            String message) {
        super(message);
        this.cls = cls;
        this.methodName = methodName;
    }

    public KoratMethodException(Class cls, String methodName,
            Throwable cause) {
        super(cause);
        this.cls = cls;
        this.methodName = methodName;
    }

    public Class getCls() {
        return cls;
    }

    public void setCls(Class cls) {
        this.cls = cls;
    }
    
    public String getMethodName() {
        return methodName;
    }

    public void setMethodName(String methodName) {
        this.methodName = methodName;
    }
    
}
