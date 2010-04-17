/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstRepeatExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstRepeatExpr extends AstLoopStmt implements IAstRepeatExpr {

	private IAstTypedExpr expr;


	/**
	 * @param doCopy
	 * @param doCopy2
	 */
	public AstRepeatExpr(IScope scope, IAstTypedExpr expr, IAstTypedExpr body) {
		super(scope, body);
		setExpr(expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("REPEAT");
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
		AstRepeatExpr other = (AstRepeatExpr) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstRepeatExpr copy(IAstNode c) {
		return (IAstRepeatExpr) fixupLoop(new AstRepeatExpr(getScope().newInstance(getCopyScope(c)), doCopy(expr, c), doCopy(body, c)));
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

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstLoopStmt#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		return super.inferTypeFromChildren(typeEngine) | updateType(expr, typeEngine.INT);
	}
}
