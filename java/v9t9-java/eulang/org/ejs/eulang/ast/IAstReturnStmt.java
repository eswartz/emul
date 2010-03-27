/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstReturnStmt extends IAstStatement, IAstTypedNode {
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
