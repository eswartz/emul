/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * This is an expression with types or expressions used to instantiate
 * a generic type.  It expands to a specific unique instance of a
 * type.
 * @author ejs
 *
 */
public interface IAstInstanceExpr extends IAstTypedExpr, IAstType {
	IAstInstanceExpr copy();
	
	IAstSymbolExpr getSymbolExpr();
	void setSymbolExpr(IAstSymbolExpr symExpr);
	IAstNodeList<IAstTypedExpr> getExprs();
	void setExprs(IAstNodeList<IAstTypedExpr> list);
	
}
