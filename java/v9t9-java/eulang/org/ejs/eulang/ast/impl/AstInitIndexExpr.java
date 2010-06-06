/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstInitIndexExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstInitIndexExpr extends AstTypedExpr implements IAstInitIndexExpr {

	private IAstTypedExpr index;

	public AstInitIndexExpr(IAstTypedExpr index) {
		setIndex(index);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstIndexExpr#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstInitIndexExpr copy() {
		return fixup(this, new AstInitIndexExpr(doCopy(index)));
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("INDEX");
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((index == null) ? 0 : index.hashCode());
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
		AstInitIndexExpr other = (AstInitIndexExpr) obj;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstIndexExpr#getIndex()
	 */
	@Override
	public IAstTypedExpr getIndex() {
		return index;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstIndexExpr#setIndex(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public void setIndex(IAstTypedExpr index) {
		this.index = reparent(this.index, index);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { index };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == getIndex()) {
			setIndex((IAstTypedExpr) another);
		} else {
			throw new IllegalArgumentException();
		}
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {

    	boolean changed = false;
    	changed |= updateType(index, typeEngine.INT);
    	changed |= updateType(this, index.getType());
    	
    	return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
	}
}
