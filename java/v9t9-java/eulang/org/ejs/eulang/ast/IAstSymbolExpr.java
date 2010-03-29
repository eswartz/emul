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
}
