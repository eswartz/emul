/**
 * 
 */
package org.ejs.eulang.symbols;

import org.ejs.eulang.ast.IAstName;
import org.ejs.eulang.ast.IAstNode;


/**
 * A scope defining names.  These are nested hierarchically and 
 * searched from inside out.
 * 
 * @author eswartz
 *
 */
public interface IScope extends Iterable<ISymbol> {
    /** Get the owner of the scope (or null); this is, e.g.,
     * an IAstEnumDeclaration, IAstCompositeTypeSpecifier, etc. */
    public IAstNode getOwner();
    
    /** Set the owner of the scope (or null); this is, e.g.,
     * an IAstEnumDeclaration, IAstCompositeTypeSpecifier, etc. */
    public void setOwner(IAstNode owner);
    
    /** Get the parent scope (or null) */
    public IScope getParent();
    
    /** Set the parent scope (or null) */
    public void setParent(IScope parent);
    
    /** Look up a symbol in this scope and get the name, if registered. */
    public ISymbol get(String name);

    /**
     * Look up a symbol in any scope visible from this scope,
     * starting from this one and going up the parent chain
     */
    public ISymbol search(String name);
    

	/**
	 * Get the definition of the symbol with the given name
	 * @param name
	 * @return the node or <code>null</code>
	 */
	public IAstNode getNode(String name);
	
    /** Add a name to the scope.  Sets name's scope to this
     * and the name's parent to node.
     * @param name the name to add.  Current scope must be null.  
     * */
    public ISymbol add(IAstName name);
    /** Add a name with the given definition point to the scope.  Sets name's scope to this
     * and the name's parent to node.
     * @param name the name to add.  Current scope must be null.
     * @param def the node which defines the name  
     * */
    public ISymbol add(IAstName name, IAstNode def);

    /** Add a symbol to the scope.  Sets symbol name's scope to this.  
     * @param symbol the symbol to add.  Current symbol name's scope must be null.  
     * @return symbol
     * */
    public ISymbol add(ISymbol symbol);

	public ISymbol[] getSymbols();

}
