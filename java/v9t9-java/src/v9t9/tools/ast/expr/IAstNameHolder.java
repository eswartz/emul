/**
 * 
 */
package v9t9.tools.ast.expr;

/**
 * Interface for nodes which hold IAstNames telling how the name
 * is used.
 * <p>
 * When IAstNameHolder#getRoleForName() returns
 * NAME_DEFINED, then IAstName#getParent() refers to the node
 * that owns the name.  Otherwise (NAME_REFERENCED), IAstName
 * is not owned by the node holding the name.
 * 
 * @author eswartz
 *
 */
public interface IAstNameHolder {
    static public final int NAME_DEFINED = 0;
    static public final int NAME_REFERENCED = 1;
    
    /** Tell how the name is used */
    public int getRoleForName();
}
