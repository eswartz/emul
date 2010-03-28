/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstExpr;
import org.ejs.eulang.ast.IAstExprStatement;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.TypeEngine;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstExprStatement extends AstTypedExpr implements IAstExprStatement  {

	private IAstTypedExpr expr;

	/**
	 * @param expr
	 */
	public AstExprStatement(IAstTypedExpr expr) {
		setExpr(expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "EXPR" + ":" + getTypeString();
	}
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		return result;
	}


	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		AstExprStatement other = (AstExprStatement) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.IAstExprStatement#setExpr(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	public void setExpr(IAstTypedExpr expr) {
		this.expr = reparent(this.expr, expr);		
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.IAstExprStatement#getExpr()
	 */
	public IAstTypedExpr getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { expr };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstExpr#equalValue(org.ejs.eulang.ast.IAstExpr)
	 */
	@Override
	public boolean equalValue(IAstExpr expr) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstExpr#simplify()
	 */
	@Override
	public IAstExpr simplify() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		LLType newType = null;
		if (canInferTypeFrom(expr)) {
			return updateType(expr, newType);
		}
		return false;
	}

}
