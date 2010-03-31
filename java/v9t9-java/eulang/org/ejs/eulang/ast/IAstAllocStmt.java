/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.eulang.symbols.ISymbol;


/**
 * This node allocates a variable and optionally assigns an initial value.
 * @author ejs
 *
 */
public interface IAstAllocStmt extends IAstStmt, IAstTypedNode {
	IAstAllocStmt copy(IAstNode copyParent);
	
	IAstSymbolExpr getSymbolExpr();
	void setSymbolExpr(IAstSymbolExpr id);
	ISymbol getSymbol();
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
	
	IAstType getTypeExpr();
	void setTypeExpr(IAstType type);
}
