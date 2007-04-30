package korat.finitization;

/**
 * Interface that represents <em>Field domain for primitive type</em>. <br/>
 * 
 * <p/> <code>IPrimitiveTypeSet</code> cannot contain any class domains,
 * because values of these fields are not objects. Actually, primitive type
 * field domain should contain exactly one class domain of the same primitive
 * type. Implementation of that would require parallel class hierarchy of class
 * domains on one side and the same hierarchy for field domains on the other
 * side, for all primitive types. That solution is not very convenient, so, in
 * this implementation, all of <code>IPrimitiveTypeSet</code> subtypes should
 * manage elements of that field domain internally.
 * 
 * <p/> This interface doesn't contain any methods, but should be used as a
 * supertype for all primitive type field domains, like <code>IIntSet</code>,
 * <code>IDoubleSet</code>,...
 * 
 * @see IBooleanSet
 * @see IByteSet
 * @see IDoubleSet
 * @see IFloatSet
 * @see IIntSet
 * @see ILongSet
 * @see IObjSet
 * @see IShortSet
 * 
 * @author korat.team
 * 
 */
interface IPrimitiveTypeSet extends IFieldDomain {

}
