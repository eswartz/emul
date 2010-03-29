/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.symbols.IScope;

/**
 * A name reference.  
 * 
 * @author eswartz
 *
 */
public interface IAstName extends IAstNode {
	IAstName copy(IAstNode copyParent);
	
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
