/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ITyped;
import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstReturnStmt;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstReturnStmt extends AstTypedExpr implements IAstReturnStmt {

	private IAstTypedExpr expr;

	/**
	 * @param expr2
	 */
	public AstReturnStmt(IAstTypedExpr expr) {
		setExpr(expr);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstReturnStmt copy(IAstNode copyParent) {
		return fixup(this, new AstReturnStmt(doCopy(expr, copyParent)));
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 99;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstReturnStmt other = (AstReturnStmt) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("return");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstReturnStmt#getExpr()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstReturnStmt#setExpr(v9t9.tools.ast.expr.IAstExpression)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		this.expr = reparent(this.expr, expr);
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
		return inferTypesFromChildren(new ITyped[] { expr });
	}
	
}
