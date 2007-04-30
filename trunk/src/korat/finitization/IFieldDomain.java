package korat.finitization;


/**
 * Interface that represents <em>Field domains</em>. <br/>
 * 
 * <p/> <em>Field domain</em> is an ordered list of elements assignable to the
 * given field.
 * 
 * <p/> Field Domain can be either <code>IPrimitiveTypeSet</code> or
 * <code>IObjSet</code>.
 * 
 * <p/> <code>IPrimitiveTypeSet</code> cannot contain any class domains,
 * because values of these fields are not objects. Actually, primitive type
 * field domain implicitly contain exactly one class domain of the same primitive
 * type. 
 * 
 * <p/> <code>IObjSet</code> consists of several class domains (<code>IClassDomain</code>).
 * Ordering of objects within a class domain must be preserved in each
 * <code>IObjSet</code>.
 * 
 * <p/> Conceptually every field domain should consist of class domains.
 * Therefore, and because of the Korat search algorithm, some of the methods
 * that retrieve informations about contained class domains are placed here, not
 * in <code>IObjSet</code>. <code>IPrimitiveTypeSet</code> should handle
 * methods that return <code>IClassDomain</code> by returning
 * <code>null</code>, and methods that retrieve some kind of indices in the
 * manner as if <code>IPrimitiveTypeSet</code> consists of exactly one class
 * domain.
 * 
 * Other operations provide various ways to manipulate the structure and
 * retrieve valuable informations about class domains and elements of this
 * <code>IFieldDomain</code>.
 * 
 * @author korat.team
 * @see IClassDomain
 * @see IPrimitiveTypeSet
 * @see IObjSet
 * 
 */
public interface IFieldDomain {

    /**
     * @return type of the field that this domain can accept
     */
    Class getClassOfField();

    /**
     * Does this field domain represent primitive type.
     * 
     * @return <code>true</code> for primitive data types and strings,
     *         <code>false</code> otherwise
     * 
     */
    boolean isPrimitiveType();

    /**
     * Number of all elements in this field domain. These are all elements that
     * can be assigned to a field associated with this field domain in the
     * process of generating test cases.
     * 
     * @return overall number of elements in field domain
     */
    int getNumberOfElements();

    /**
     * Does this field domain represent array type.
     * 
     * @return <code>true</code> if class of this FieldDomain is array,
     *         <code>false</code> otherwise
     * 
     */   
    boolean isArrayType();

    /**
     * Number of class domains contained in this field domain.
     * 
     * <p/> <code>IPrimitiveTypeSet</code>s should return <code>1</code>.
     * 
     * @return number of class domains in this field domain
     * @see IPrimitiveTypeSet
     */
    int getNumOfClassDomains();

}
