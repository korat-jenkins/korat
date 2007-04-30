package korat.loading.filter;

/**
 * ExcludingPackageFilter
 * 
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class ExcludingPackageFilter extends PackageFilter {

    /**
     * Returns true if package of <code>className</code> class
     * in not the one in the list of excluded packages
     * 
     */
    public boolean allowProcessing(String className) {
        boolean ret = true;
        String p = getPackage(className);
        for (String pck : packages) {
            ret &= !p.matches(pck);
        }
        return ret;
    }

}
