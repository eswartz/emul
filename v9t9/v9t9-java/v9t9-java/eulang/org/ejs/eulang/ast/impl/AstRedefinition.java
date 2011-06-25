/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstRedefinition;
import org.ejs.eulang.ast.IAstTypedExpr;

/**
 * @author ejs
 *
 */
public class AstRedefinition extends AstNode implements IAstRedefinition {

	private String symExpr;
	private IAstTypedExpr expr;

	public AstRedefinition(String symExpr, IAstTypedExpr expr) {
		setSymbol(symExpr);
		setExpr(expr);
	}

	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + ((symExpr == null) ? 0 : symExpr.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		AstRedefinition other = (AstRedefinition) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (symExpr == null) {
			if (other.symExpr != null)
				return false;
		} else if (!symExpr.equals(other.symExpr))
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstNode copy() {
		return fixup(this, new AstRedefinition(symExpr, (IAstTypedExpr) expr.copy()));
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "'" + symExpr + "'";
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstRedefinition#getExpr()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstRedefinition#getSymbolExpr()
	 */
	@Override
	public String getSymbol() {
		return symExpr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstRedefinition#setExpr(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		this.expr = reparent(this.expr, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstRedefinition#setSymbolExpr(org.ejs.eulang.ast.IAstSymbolExpr)
	 */
	@Override
	public void setSymbol(String id) {
		this.symExpr = id;

	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { expr };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == expr)
			setExpr((IAstTypedExpr) another);
		else
			throw new IllegalArgumentException();
	}

}
