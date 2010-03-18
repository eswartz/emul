/**
 * 
 */
package v9t9.tools.asm.decomp.expr;

/**
 * A name in the DOM.
 * <p>
 * A name is owned only by one node (i.e. has a single parent)
 * but may be referenced by multiple nodes.  The IAstNameHolder
 * interface exists on all nodes that hold IAstName to distinguish
 * the roles. 
 * 
 * @see IAstNameHolder
 * 
 * @author eswartz
 *
 */
public interface IAstName extends IAstNode {
    /** Get the name
     * 
     * @return name (never null)
     */
    public String getName();

    /** Set the name
     * 
     * @param name (must not be null)
     */
    public void setName(String name);
    
    /** Get the scope of the name 
     * 
     * @return the scope (never null)
     */
    public IScope getScope();
    
    /** Set the scope of the name.
     * 
     * This doesn't do refactoring!
     * 
     * @param scope the scope (must not be null)
     */
    public void setScope(IScope scope);
}
