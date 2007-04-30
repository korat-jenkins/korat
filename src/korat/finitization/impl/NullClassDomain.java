package korat.finitization.impl;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Aleksandar Milicevic <aca.milicevic@gmail.com>
 * 
 */
public class NullClassDomain extends ClassDomain {

    private static NullClassDomain instance = new NullClassDomain();

    private NullClassDomain() {
        super(null);
        initialize();
    }

    public static NullClassDomain getInstance() {
        return instance;
    }

    public int getSize() {
        return 1;
    }

    public void initialize() {
        initialized = true;
    }

    public Object getObject(int index) {
        return null;
    }

    @Override
    public List<Object> getObjects() {
        List<Object> lst = new ArrayList<Object>();
        lst.add(null);
        return lst;
    }

    public int getIndexOf(Object obj) {
        if (obj == null)
            return 0;
        else
            return -1;
    }

    public boolean contains(Object obj) {
        if (obj == null)
            return true;
        else
            return false;
    }
}
