/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstExprStmt extends IAstStmt, IAstTypedExpr {
	IAstExprStmt copy();
	void setExpr(IAstTypedExpr expr);
	IAstTypedExpr getExpr();

}