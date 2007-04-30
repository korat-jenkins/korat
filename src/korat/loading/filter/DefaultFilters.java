package korat.loading.filter;

/**
 * 
 * Default filters - java system packages exclusion filter project packages
 * exclusion filter name transformer that does not change input
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public interface DefaultFilters {

    /**
     * Default package filter. Excludes java system classes and Korat classes
     * 
     * @see ExcludeSystemPackagesFilter
     * @see ExcludeKoratPackagesFilter
     * 
     * @author Sasa
     *
     */
    public static class DefaultPackageFilter extends CompoundComparingFilter {
        public DefaultPackageFilter() {
            this.add(new ExcludeSystemPackagesFilter());
            this.add(new ExcludeKoratPackagesFilter());
        }
    }
    
    /**
     * Excludes all classes from java.* and com.sun.* and its subpackages
     * 
     * @author Sasa Misailovic <sasa.misailovic@gmail.com>
     * 
     */
    public static class ExcludeSystemPackagesFilter extends PackageFilter {
        public boolean allowProcessing(String className) {
            return !(   className.matches("^java\\..*")
                     || className.matches("^javax\\..*")
                     || className.matches("^sun\\..*")
                     || className.matches("^sunw\\..*")
                     || className.matches("^com\\.sun\\..*")
                     || className.matches("^org\\.omg\\..*")
                     || className.matches("^org\\.w3c\\..*")
                     || className.matches("^org\\.xml\\..*") 
                     || className.matches("^net\\.jini\\..*")
                     || className.matches("^javassist\\..*")
                     || className.matches("^bcel\\..*")
                    );
        }
    }

    /**
     * Excludes all classes from projects main package and its subpackages
     * 
     * @author Sasa Misailovic <sasa.misailovic@gmail.com>
     * 
     */
    public static class ExcludeKoratPackagesFilter extends PackageFilter {
        public boolean allowProcessing(String className) {
            if (className == null)
                return false;
            
            return    className.matches("^korat\\.examples\\..*")
                   || className.matches("^.*\\.\\$koratcreated\\$\\..*")
                   || ! className.matches("^korat\\..*");
                   

        }
    }
    
    /**
     * Default class name transforming filter, returns the original name
     * unaltered
     * 
     * @author Sasa Misailovic <sasa.misailovic@gmail.com>
     * 
     */
    public static class IdentityNameTransformer implements
            ITransformingFilter<String> {
        public String transform(String t) {
            return t;
        }
    }

}
