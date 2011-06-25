/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstGotoStmt extends IAstStmt, IAstTypedExpr {
	IAstGotoStmt copy();
	IAstSymbolExpr getLabel();
	void setLabel(IAstSymbolExpr symbol);
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
