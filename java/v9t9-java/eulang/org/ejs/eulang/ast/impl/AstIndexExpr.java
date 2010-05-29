/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstIndexExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;

/**
 * @author ejs
 *
 */
public class AstIndexExpr extends AstTypedExpr implements IAstIndexExpr {

	private IAstTypedExpr expr;
	private IAstTypedExpr index;

	public AstIndexExpr(IAstTypedExpr expr, IAstTypedExpr index) {
		setExpr(expr);
		setIndex(index);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstIndexExpr#copy(org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public IAstIndexExpr copy() {
		return fixup(this, new AstIndexExpr(doCopy(expr), doCopy(index)));
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
		result = prime * result + ((expr == null) ? 0 : expr.hashCode());
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
		AstIndexExpr other = (AstIndexExpr) obj;
		if (expr == null) {
			if (other.expr != null)
				return false;
		} else if (!expr.equals(other.expr))
			return false;
		if (index == null) {
			if (other.index != null)
				return false;
		} else if (!index.equals(other.index))
			return false;
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstIndexExpr#getExpr()
	 */
	@Override
	public IAstTypedExpr getExpr() {
		return expr;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstIndexExpr#getIndex()
	 */
	@Override
	public IAstTypedExpr getIndex() {
		return index;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstIndexExpr#setExpr(org.ejs.eulang.ast.IAstTypedExpr)
	 */
	@Override
	public void setExpr(IAstTypedExpr expr) {
		this.expr = reparent(this.expr, expr);
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
		if (expr != null)
			return new IAstNode[] { expr, index };
		else
			return new IAstNode[] { index };
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChild(org.ejs.eulang.ast.IAstNode, org.ejs.eulang.ast.IAstNode)
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == getExpr()) {
			setExpr((IAstTypedExpr) another);
		} else if (existing == getIndex()) {
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
		
		LLType arrayType = null;
		LLType elType = type;
		
		if (canInferTypeFrom(expr)) {
			arrayType = expr.getType();
			elType = arrayType.getSubType();
		}
    	if (arrayType == null || !arrayType.isComplete()) {
    		arrayType = typeEngine.getArrayType(elType, 0, null);
    		changed |= updateType(expr, arrayType);
    	} 
		
    	changed |= updateType(this, elType);

    	changed |= updateType(index, typeEngine.INT);
    	
    	return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		if (type == null || !type.isComplete())
			return;
		if (expr != null) {
			if (expr.getType() != null && expr.getType().isComplete()) {
				if (!type.equals(expr.getType().getSubType()))
					throw new TypeException(this, "array element type and result type do not match");
			}
		}
		if (index.getType() != null && index.getType().isComplete()) {
			if (index.getType().getBasicType() != BasicType.INTEGRAL)
				throw new TypeException(this, "array index type is not integral");
		}
			
	}
}
