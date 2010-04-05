/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstExprStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstExprStmt extends AstTypedExpr implements IAstExprStmt  {

	private IAstTypedExpr expr;

	/**
	 * @param expr
	 */
	public AstExprStmt(IAstTypedExpr expr) {
		setExpr(expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstExprStmt copy(IAstNode copyParent) {
		return fixup(this, new AstExprStmt(doCopy(expr, copyParent)));
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("EXPRSTMT");
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
		AstExprStmt other = (AstExprStmt) obj;
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
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getExpr() == existing) {
			setExpr((IAstTypedExpr) another);
		} else {
			throw new IllegalArgumentException();
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedExpr#equalValue(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public boolean equalValue(IAstTypedExpr expr) {
		return false;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		/*LLType newType = null;
		if (canInferTypeFrom(expr)) {
			return updateType(expr, newType) | updateType(this, newType);
		}
		return false;*/
		return inferTypesFromChildren(new ITyped[] { expr });
	}

}
