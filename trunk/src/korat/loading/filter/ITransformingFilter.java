package korat.loading.filter;

/**
 * Base interface for all filters that transform input
 * 
 * @author Sasa Misailovic <sasa.misailovic@gmail.com>
 * 
 * @param <Type>
 */
public interface ITransformingFilter<Type> extends IFilter {

    Type transform(Type t);

}
