/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstPointerType;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.types.LLPointerType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstPointerType extends AstTypedExpr implements IAstPointerType {

	private IAstType baseType;

	/**
	 * @param type
	 */
	public AstPointerType(IAstType baseType) {
		setBaseType(baseType);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstPointerType copy(IAstNode copyParent) {
		return fixup(this, new AstPointerType(doCopy(baseType, copyParent)));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((baseType == null) ? 0 : baseType.hashCode());
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
		AstPointerType other = (AstPointerType) obj;
		if (baseType == null) {
			if (other.baseType != null)
				return false;
		} else if (!baseType.equals(other.baseType))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return baseType.toString() + "^";
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstType#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		return new IAstNode[] { baseType };
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == baseType) 
			setBaseType((IAstType) another);
		else
			throw new IllegalArgumentException();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstArrayType#setBaseType(org.ejs.eulang.ast.IAstType)
	 */
	@Override
	public void setBaseType(IAstType typeExpr) {
		this.baseType = reparent(this.baseType, typeExpr);
		setType(null);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstArrayType#getBaseType()
	 */
	@Override
	public IAstType getBaseType() {
		return baseType;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstTypedNode#inferTypeFromChildren(org.ejs.eulang.ast.TypeEngine)
	 */
	@Override
	public boolean inferTypeFromChildren(TypeEngine typeEngine)
			throws TypeException {
		boolean changed = false;
		
		
		if (canReplaceType(this)) {
			//LLPointerType ptr = typeEngine.getPointerType(getConcreteType(typeEngine, this, baseType.getType()));  
			LLPointerType ptr = typeEngine.getPointerType(baseType.getType());  
			changed |= updateType(this, ptr);
		}
		
		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		if (baseType.getType() != null && baseType.getType().isComplete()) {
			if (!baseType.getType().equals(getType().getSubType()))
				throw new TypeException(this, "pointer base type does not match in parent");
		}
	}

}
