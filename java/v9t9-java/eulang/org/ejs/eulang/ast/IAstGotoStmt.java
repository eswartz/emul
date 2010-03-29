/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstGotoStmt extends IAstStatement {
	IAstSymbolExpr getLabel();
	void setLabel(IAstSymbolExpr symbol);
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
