/**
 * 
 */
package org.ejs.eulang.ast;


/**
 * @author ejs
 *
 */
public interface IAstExprStmt extends IAstStatement, IAstTypedExpr {

	/**
	 * @param expr2
	 */
	void setExpr(IAstTypedExpr expr);

	/**
	 * @return the expr
	 */
	IAstTypedExpr getExpr();

}