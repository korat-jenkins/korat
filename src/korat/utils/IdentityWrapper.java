package korat.utils;

public class IdentityWrapper implements Cloneable {
    Object object;

    public IdentityWrapper(Object o) {
        this.object = o;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof IdentityWrapper))
            return false;
        return this.object == ((IdentityWrapper) obj).object;
    }

    public int hashCode() {
        return System.identityHashCode(object);
    }
}
