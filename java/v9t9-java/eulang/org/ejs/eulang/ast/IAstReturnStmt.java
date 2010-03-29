/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstReturnStmt extends IAstStmt, IAstTypedNode {
	IAstReturnStmt copy(IAstNode copyParent);
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
