/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstGotoStmt extends IAstStmt, IAstTypedExpr {
	IAstGotoStmt copy(IAstNode copyParent);
	IAstSymbolExpr getLabel();
	void setLabel(IAstSymbolExpr symbol);
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
