package korat.loading.filter;


public interface IComparingFilter extends IFilter {

    boolean allowProcessing(String className);

}
