/**
 * 
 */
package org.ejs.eulang.ast;

import v9t9.tools.ast.expr.IAstExpression;

/**
 * @author ejs
 *
 */
public interface IAstReturnStmt extends IAstStatement {
	IAstExpression getExpr();
	void setExpr(IAstExpression expr);
}
