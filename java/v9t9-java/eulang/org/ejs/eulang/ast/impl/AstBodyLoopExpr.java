/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstBodyLoopExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public abstract class AstBodyLoopExpr extends AstLoopStmt implements IAstBodyLoopExpr {

	protected IAstTypedExpr expr;

	/**
	 * @param scope
	 * @param body
	 */
	public AstBodyLoopExpr(IScope scope, IAstTypedExpr expr, IAstTypedExpr body) {
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
		AstBodyLoopExpr other = (AstBodyLoopExpr) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		return true;
	}

	@Override
	public IAstTypedExpr getExpr() {
		return expr;
	}

	@Override
	public void setExpr(IAstTypedExpr expr) {
		this.expr = reparent(this.expr, expr);
	}

	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { expr, body };
	}

	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == expr) {
			setExpr((IAstTypedExpr) another);
		} else {
			super.replaceChild(existing, another);
		}
	}

}