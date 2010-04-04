/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.ast.IAstGotoStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstTypedExpr;

/**
 * @author ejs
 *
 */
public class AstGotoStmt extends AstStatement implements IAstGotoStmt {

	private IAstSymbolExpr label;
	private IAstTypedExpr expr;

	/**
	 * @param label
	 * @param test
	 */
	public AstGotoStmt(IAstSymbolExpr label, IAstTypedExpr test) {
		setLabel(label);
		setExpr(test);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstGotoStmt copy(IAstNode copyParent) {
		return fixup(this, new AstGotoStmt(doCopy(label, copyParent), doCopy(expr, copyParent)));
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "GOTO " + label.toString();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#getDumpChildren()
	 */
	@Override
	public IAstNode[] getDumpChildren() {
		if (expr != null)
			return new IAstNode[] { expr };
		return NO_CHILDREN;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 999;
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstGotoStmt other = (AstGotoStmt) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}



	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstGotoStmt#getExpr()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstGotoStmt#getLabel()
	 */
	@Override
	public IAstSymbolExpr getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (expr != null)
			return new IAstNode[] { label, expr };
		return new IAstNode[] { label };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChildren(IAstNode[] children) {
		if (expr != null) {
			setLabel((IAstSymbolExpr) children[0]);
			setExpr((IAstTypedExpr) children[1]);
		} else {
			setLabel((IAstSymbolExpr) children[0]);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getLabel() == existing) {
			setLabel((IAstSymbolExpr) another);
		} else if (getExpr() == existing) {
			setExpr((IAstTypedExpr) another);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstGotoStmt#setExpr(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		this.expr = reparent(this.expr, expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstGotoStmt#setLabel(org.ejs.eulang.ast.IAstSymbolExpr)
	 */
	@Override
	public void setLabel(IAstSymbolExpr symbol) {
		this.label = reparent(this.label, symbol);
	}

}
