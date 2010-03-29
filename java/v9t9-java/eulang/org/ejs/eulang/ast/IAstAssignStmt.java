/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * This node assigns a new value to the location of the id.
 * @author ejs
 *
 */
public interface IAstAssignStmt extends IAstStmt, IAstTypedExpr {
	IAstAssignStmt copy(IAstNode copyParent);
	IAstSymbolExpr getSymbol();
	void setSymbol(IAstSymbolExpr id);
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
