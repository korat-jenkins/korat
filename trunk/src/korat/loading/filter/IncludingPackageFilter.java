package korat.loading.filter;


/**
 * Confirms if some package is contained in list of packages 
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public class IncludingPackageFilter extends PackageFilter {

    public IncludingPackageFilter() {
        super();
    }
    
    public boolean allowProcessing(String className) {
        String p = getPackage(className);
        for (String pck : packages) 
            if (p.matches(pck))
                return true;
        return false;
    }

}
