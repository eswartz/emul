/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstDerefExpr;
import org.ejs.eulang.ast.IAstFieldExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLCodeType;
import org.ejs.eulang.types.LLSymbolType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.LLUpType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstDerefExpr extends AstTypedExpr implements IAstDerefExpr {

	private IAstTypedExpr expr;
	private boolean lhs;

	/**
	 * @param type
	 */
	public AstDerefExpr(IAstTypedExpr expr, boolean isLHS) {
		setExpr(expr);
		setLHS(lhs);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstDerefExpr copy(IAstNode copyParent) {
		return fixup(this, new AstDerefExpr(doCopy(expr, copyParent), lhs));
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
		AstDerefExpr other = (AstDerefExpr) obj;
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
		return typedString("DEREF");
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
	 * @see org.ejs.eulang.ast.IAstDerefExpr#setLHS(boolean)
	 */
	@Override
	public void setLHS(boolean lhs) {
		this.lhs = lhs;
	}
	/**
	 * 
	 */
	public boolean isLHS() {
		return lhs;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		boolean changed = false;
		
		// the type is fixed to be the base type of the dereferenced child.
		if (canInferTypeFrom(expr)) {
			LLType child = expr.getType();
			
			child = typeEngine.getBaseType(child);
			
			if (child instanceof LLUpType && child.getSubType() != null)
				child = child.getSubType();
			if (child instanceof LLSymbolType) {
				//if (((LLSymbolType)child).getSymbol().getType() != null)
					child = ((LLSymbolType)child).getRealType(typeEngine);
			}
			
			if (child instanceof LLCodeType) {
				expr.setParent(null);
				getParent().replaceChild(this, expr);
				return true;
			}
			
			changed |= updateType(this, child);
		} else if (canInferTypeFrom(this)) {
			// don't presume the base type, but update it if we know better
			if (!(expr instanceof IAstFieldExpr) || expr.getType() != null) {
				//changed |= updateType(expr, getType());
				expr.setType(getType());
			}
		}
		
		if (canInferTypeFrom(this) && canInferTypeFrom(expr) && !typeEngine.getBaseType(expr.getType()).equals(getType())) {
			if (getType().getBasicType() != BasicType.VOID) {
				if (!lhs || !(expr instanceof IAstFieldExpr)) {
					setExpr(createCastOn(typeEngine, expr, typeEngine.getPointerType(getType())));
				} else {
					if (!getType().equals(expr.getType())) {
						setType(expr.getType());
						changed = true;
					}
				}
			}
		}
		
		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		if (getType() != null && getType().isComplete() && expr.getType() != null) {
			if (getType().getBasicType() == BasicType.VOID)
				return;
			if (!typeEngine.getBaseType(expr.getType()).equals(getType()))
				throw new TypeException(expr, "type is not the child's base type");
		}
	}

}
