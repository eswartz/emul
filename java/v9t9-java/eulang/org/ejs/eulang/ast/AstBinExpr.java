/**
 * 
 */
package org.ejs.eulang.ast;

import org.ejs.coffee.core.utils.Check;

import v9t9.tools.ast.expr.IAstExpression;
import v9t9.tools.ast.expr.IAstNode;

/**
 * @author ejs
 *
 */
public class AstBinExpr extends AstTypedExpr implements IAstBinExpr {

	private IAstTypedExpr right;
	private IAstTypedExpr left;
	private IOperation oper;

	public AstBinExpr(IOperation op, IAstTypedExpr left, IAstTypedExpr right) {
		setOp(op);
		setLeft(left);
		setRight(right);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return oper.getName() + ":" + getType();
	}
	
	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { left, right };
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstNode#getReferencedNodes()
	 */
	@Override
	public IAstNode[] getReferencedNodes() {
		return getChildren();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBinExpr#getOp()
	 */
	@Override
	public IOperation getOp() {
		return oper;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBinExpr#setOp(org.ejs.eulang.ast.IOperation)
	 */
	@Override
	public void setOp(IOperation operator) {
		Check.checkArg(operator);
		this.oper = operator;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBinExpr#getLeft()
	 */
	@Override
	public IAstTypedExpr getLeft() {
		return left;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBinExpr#getRight()
	 */
	@Override
	public IAstTypedExpr getRight() {
		return right;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBinExpr#setLeft(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setLeft(IAstTypedExpr expr) {
		Check.checkArg(expr);
		this.left = expr;
		if (getType() == null)
			setType(left.getType());
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstBinExpr#setRight(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setRight(IAstTypedExpr expr) {
		Check.checkArg(expr);
		this.right = expr;
		if (getType() == null)
			setType(right.getType());
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#equalValue(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public boolean equalValue(IAstExpression expr) {
		return false;
	}

	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.IAstExpression#simplify()
	 */
	@Override
	public IAstExpression simplify() {
		return this;
	}

}
