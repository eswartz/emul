/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.symbols.ISymbol;

/**
 * An symbol used in an expression
 * 
 * @author eswartz
 * 
 */
public interface IAstSymbolExpr extends IAstTypedExpr {
	IAstSymbolExpr copy(IAstNode copyParent);

	/** Get the symbol referenced */
	public ISymbol getSymbol();

	/** Set the symbol */
	public void setSymbol(ISymbol symbol);

	/**
	 * Get the original definition of the symbol was defined, if it was defined in a define statement.
	 * 
	 * @return define statement or <code>null</code>
	 */
	IAstDefineStmt getDefinition();

	/**
	 * Get the abstract matching version of the definition for this symbol's type, if it was defined in a define statement.
	 * <p>
	 * This returns a possibly generic or untyped AST from the choices available in the definition.
	 * 
	 * @return body of definition or <code>null</code> if not in a define
	 */
	IAstTypedExpr getBody();
	
	/**
	 * Get the actual resolved version of the definition used for this symbol's type, if it was defined in a define statement.
	 * <p>
	 * This returns either the {@link #getBody()}, a concrete instance generated from a generic
	 * type.
	 * @return expanded and type-specific body of the define, or <code>null</code> if not in a define
	 */
	IAstTypedExpr getInstance();
}
