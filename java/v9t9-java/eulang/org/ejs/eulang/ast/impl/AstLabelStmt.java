/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.coffee.core.utils.Check;
import org.ejs.eulang.ast.IAstLabelStmt;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSymbolExpr;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;

/**
 * @author ejs
 *
 */
public class AstLabelStmt extends AstStatement implements IAstLabelStmt {

	private IAstSymbolExpr label;

	/**
	 * @param label
	 * @param test
	 */
	public AstLabelStmt(IAstSymbolExpr label) {
		setLabel(label);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstLabelStmt copy(IAstNode copyParent) {
		return fixup(this, new AstLabelStmt(doCopy(label, copyParent)));
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return "LABEL " + label.toString();
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#getDumpChildren()
	 */
	@Override
	public IAstNode[] getDumpChildren() {
		return NO_CHILDREN;
	}
	
	

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 999;
		result = prime * result + ((label == null) ? 0 : label.hashCode());
		return result;
	}



	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		AstLabelStmt other = (AstLabelStmt) obj;
		if (label == null) {
			if (other.label != null)
				return false;
		} else if (!label.equals(other.label))
			return false;
		return true;
	}



	@Override
	public IAstSymbolExpr getLabel() {
		return label;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { label };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChildren(IAstNode[] children) {
		setLabel((IAstSymbolExpr) children[0]);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (getLabel() == existing) {
			setLabel((IAstSymbolExpr) another);
		} else {
			throw new IllegalArgumentException();
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstGotoStmt#setLabel(org.ejs.eulang.ast.IAstSymbolExpr)
	 */
	@Override
	public void setLabel(IAstSymbolExpr symbol) {
		Check.checkArg(symbol);
		this.label = reparent(this.label, symbol);
	}
	

}
