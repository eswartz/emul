/**
 * 
 */
package org.ejs.eulang.ast;

import v9t9.tools.ast.expr.IAstExpression;

/**
 * @author ejs
 *
 */
public interface IAstAssignStmt extends IAstStatement, IAstTypedExpr,
		IAstExpression {

	IAstIdExpr getId();
	void setId(IAstIdExpr id);
	IAstType getTypeExpr();
	void setTypeExpr(IAstType type);
	IAstTypedExpr getExpr();
	void setExpr(IAstTypedExpr expr);
}
