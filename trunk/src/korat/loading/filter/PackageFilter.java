package korat.loading.filter;

import java.util.Vector;

/**
 * PackageFilter is base class for all filters that handle with packages.
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 */
public abstract class PackageFilter implements IComparingFilter {

    Vector<String> packages;

    protected boolean compareRecursive = false;

    protected PackageFilter() {
        this.packages = new Vector<String>();
    }

    protected String getPackage(String className) {
        if (className == null)
            return null;
        int idx = className.lastIndexOf('.');
        if (idx == -1)
            return "";
        return className.substring(0, idx);
    }
    
    public void addPackage(String packageName) {
        String pck = packageName.trim();
        //COMPAT1.4
        //pck = pck.replace(".", "\\.");
        //pck = pck.replace("*", ".*");
        pck = replaceString(pck, '.', "\\.");
        pck = replaceString(pck, '*', ".*");
        this.packages.add(pck);
    }
    
    private String replaceString(String text, char a, String b){
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < text.length(); i++) {
            if (text.charAt(i)==a)
                sb.append(b);
            else
                sb.append(text.charAt(i));
        }
        return sb.toString();
    }

}
