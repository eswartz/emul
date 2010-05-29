/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstReturnStmt extends IAstTypedNode, IAstStmt {
	IAstReturnStmt copy();
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
