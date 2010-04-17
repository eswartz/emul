/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTestBodyLoopExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.IAstTypedNode;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public abstract class AstTestBodyLoopExpr extends AstLoopStmt implements IAstTestBodyLoopExpr {

	private IAstTypedExpr expr;


	/**
	 * @param doCopy
	 * @param doCopy2
	 */
	public AstTestBodyLoopExpr(IScope scope, IAstTypedExpr expr, IAstTypedExpr body) {
		super(scope, body);
		setExpr(expr);
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
		AstTestBodyLoopExpr other = (AstTestBodyLoopExpr) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstRepeatExpr#getExpr()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstRepeatExpr#setExpr(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		this.expr = reparent(this.expr, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { expr, body };
	}

		/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == expr) {
			setExpr((IAstTypedExpr) another);
		} else {
			super.replaceChild(existing, another);
		}
	}

	protected abstract LLType getExpressionType(TypeEngine typeEngine);
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstLoopStmt#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		return super.inferTypeFromChildren(typeEngine) | updateType(expr, getExpressionType(typeEngine));
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		super.validateChildTypes(typeEngine);
		
		LLType kidType = ((IAstTypedNode) expr).getType();
		if (kidType != null && kidType.isComplete()) {
			if (!typeEngine.getBaseType(getExpressionType(typeEngine)).equals(typeEngine.getBaseType(kidType))) {
				throw new TypeException(body, "expression type is expected to be " + getExpressionType(typeEngine));
			}
		}

	}
}
