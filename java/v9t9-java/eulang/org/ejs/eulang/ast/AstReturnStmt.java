/**
 * 
 */
package org.ejs.eulang.ast;

import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IAstNode;

/**
 * @author ejs
 *
 */
public class AstReturnStmt extends AstStatement implements IAstReturnStmt {

	private IAstExpression expr;

	/**
	 * @param expr2
	 */
	public AstReturnStmt(IAstExpression expr) {
		setExpr(expr);
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "return";
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstReturnStmt#getExpr()
	 */
	@Override
	public IAstExpression getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstReturnStmt#setExpr(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setExpr(IAstExpression expr) {
		this.expr = expr;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (expr == null)
			return NO_CHILDREN;
		return new IAstNode[] { expr };
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getReferencedNodes()
	 */
	@Override
	public IAstNode[] getReferencedNodes() {
		return getChildren();
	}

}
