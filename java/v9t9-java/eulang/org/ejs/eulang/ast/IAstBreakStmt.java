/**
 * 
 */
package org.ejs.eulang.ast;

/**
 * @author ejs
 *
 */
public interface IAstBreakStmt extends IAstTypedExpr, IAstStmt {

	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
