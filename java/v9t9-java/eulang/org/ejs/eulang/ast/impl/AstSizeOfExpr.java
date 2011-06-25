/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstSizeOfExpr;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstSizeOfExpr extends AstTypedExpr implements IAstSizeOfExpr {

	private IAstTypedExpr node;

	/**
	 * @param type
	 */
	public AstSizeOfExpr(IAstTypedExpr expr) {
		setExpr(expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstSizeOfExpr copy() {
		return fixup(this, new AstSizeOfExpr(doCopy(node)));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((node == null) ? 0 : node.hashCode());
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
		AstSizeOfExpr other = (AstSizeOfExpr) obj;
		if (node == null) {
			if (other.node != null)
				return false;
		} else if (!node.equals(other.node))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return typedString("SIZEOF");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstType#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { node };
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == node)
			setExpr((IAstTypedExpr) another);
		else
			throw new IllegalArgumentException();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstType#setSymbol(org.ejs.eulang.ast.IAstSymbolExpr)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		this.node = reparent(this.node, expr);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstType#getSymbol()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return node;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		boolean changed = false;
		
		changed |= updateType(this, typeEngine.PTRDIFF);
		
		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		if (getType() == null || !getType().isCompatibleWith(typeEngine.PTRDIFF)) {
			throw new TypeException(this, "sizeof should have int type");
		}
		if (node.getType() == null || !node.getType().isComplete()) {
			throw new TypeException(this, "cannot determine type of expression in sizeof");
		}
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedExpr#simplify(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public boolean simplify(TypeEngine engine) {
		boolean changed = super.simplify(engine);
		if (node.getType() != null && node.getType().isComplete()) {
			IAstIntLitExpr sizeof = new AstIntLitExpr(toString(), getType(), 
					node.getType().getBits() / 8);
			sizeof.setSourceRef(getSourceRef());
			getParent().replaceChild(this, sizeof);
			return true;
		}
		return changed;
	}
}
