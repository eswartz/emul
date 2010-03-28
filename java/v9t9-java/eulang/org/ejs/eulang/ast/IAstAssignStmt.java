/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * This node assigns a new value to the location of the id.
 * @author ejs
 *
 */
public interface IAstAssignStmt extends IAstStatement, IAstTypedExpr,
		IAstExpr {

	IAstSymbolExpr getSymbol();
	void setSymbol(IAstSymbolExpr id);
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
