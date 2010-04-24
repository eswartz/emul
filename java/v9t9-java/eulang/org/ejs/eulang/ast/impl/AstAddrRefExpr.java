/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstAddrRefExpr;
import org.ejs.eulang.ast.IAstDerefExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstAddrRefExpr extends AstTypedExpr implements IAstAddrRefExpr {

	private IAstTypedExpr expr;

	/**
	 * @param type
	 */
	public AstAddrRefExpr(IAstTypedExpr expr) {
		setExpr(expr);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstAddrRefExpr copy(IAstNode copyParent) {
		return fixup(this, new AstAddrRefExpr(doCopy(expr, copyParent)));
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
		AstAddrRefExpr other = (AstAddrRefExpr) obj;
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
		return typedString("ADDRREF");
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstType#getChildren()
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
		if (existing == expr)
			setExpr((IAstTypedExpr) another);
		else
			throw new IllegalArgumentException();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstType#setSymbol(org.ejs.eulang.ast.IAstSymbolExpr)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		this.expr = reparent(this.expr, expr);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstType#getSymbol()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return expr;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		boolean changed = false;
		
		// the type is fixed to be the pointer-to the dereferenced child.
		if (canInferTypeFrom(expr)) {
			IAstTypedExpr theExpr = expr;
			LLType child = typeEngine.getBaseType(expr.getType());
			do {
				child = typeEngine.getPointerType(child);
				if (theExpr instanceof IAstAddrRefExpr)
					theExpr = ((IAstAddrRefExpr) theExpr).getExpr();
				else
					break;
			}
			while (true);
			changed |= updateType(this, child);
		}
		
		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		if (getType() != null && getType().isComplete()) {
			if (!expr.getType().equals(getType().getSubType()))
				throw new TypeException(expr, "pointer base type does not match in parent");
		}
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstTypedExpr#simplify(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public IAstTypedExpr simplify(TypeEngine engine) {
		if (expr instanceof IAstDerefExpr) {
			return (IAstTypedExpr) ((IAstDerefExpr) expr).getExpr().copy(this);
		}
		return super.simplify(engine);
	}

}
