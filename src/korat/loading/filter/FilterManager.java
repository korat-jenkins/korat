package korat.loading.filter;

/**
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 *
 */
public class FilterManager {

    private static CompoundComparingFilter filter;
    
    /**
     * @return the final comparing filter
     */
    public static IComparingFilter getFilter() {
        return getCompoundFilter();
    }

    private static CompoundComparingFilter getCompoundFilter() {
        if (filter == null) {
            filter = new CompoundComparingFilter();
            filter.add(new DefaultFilters.DefaultPackageFilter());
        }
        return filter;
    }

    public static void addFilter(IComparingFilter flt) {
        getCompoundFilter().add(flt);
    }

}
