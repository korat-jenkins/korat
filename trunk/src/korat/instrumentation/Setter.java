package korat.instrumentation;

/**
 * Base class for all setters. Setters are used to set the field values for
 * candidate structures. There is one setter per field. Most setters are
 * implemented as anonymous classes, during instrumentation.
 * 
 * There are set methods for all primitive types and one set method for
 * reference types (including arrays). Each particular setter redefines one of
 * <code>set</code> methods
 * 
 * Setters are used instead of reflection mechanism which proved to be rather
 * slow.
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public abstract class Setter {

    public void set(boolean b) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void set(byte b) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void set(short s) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void set(int i) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void set(long l) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void set(float f) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void set(double d) {
        throw new UnsupportedOperationException("Not implemented");
    }

    public void set(Object o) {
        throw new UnsupportedOperationException("Not implemented");
    }

}
