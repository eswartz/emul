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
public interface IAstSymbolExpr extends IAstExpr, IAstTypedExpr {
    /** Get the symbol referenced */
    public ISymbol getSymbol();
    
    /** Set the symbol */
    public void setSymbol(ISymbol symbol);
}
