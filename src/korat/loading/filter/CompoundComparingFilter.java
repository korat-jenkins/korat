package korat.loading.filter;

import java.util.HashSet;
import java.util.Set;

/**
 * Compound comparing filter consists of one or more comparing filters.
 * It allows processing of the class, given by its className only if all 
 * contained filters allow the processing.
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class CompoundComparingFilter implements IComparingFilter {

    protected Set<IComparingFilter> filters;

    public CompoundComparingFilter() {
        filters = new HashSet<IComparingFilter>();
    }

    public CompoundComparingFilter(IComparingFilter[] filters) {
        this();
        add(filters);
    }

    public void add(IComparingFilter filter) {
        filters.add(filter);
    }

    public void add(IComparingFilter[] filters) {
        for (IComparingFilter f : filters)
            this.add(f);
    }

    public void add(Object[] filters) {
        for (int i = 0; i < filters.length; i++)
            this.add((IComparingFilter) filters[i]);
    }

    public boolean remove(IComparingFilter filter) {
        return filters.remove(filter);
    }
    
    public boolean allowProcessing(String className) {
        for (IComparingFilter f : filters)
            if (f.allowProcessing(className) == false)
                return false;
        return true;
    }

}
