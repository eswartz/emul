/**
 * 
 */
package org.ejs.eulang.ast.impl;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.IAstArrayType;
import org.ejs.eulang.ast.IAstIntLitExpr;
import org.ejs.eulang.ast.IAstLitExpr;
import org.ejs.eulang.ast.IAstNode;
import org.ejs.eulang.ast.IAstType;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLArrayType;
import org.ejs.eulang.types.LLType;
import org.ejs.eulang.types.TypeException;


/**
 * @author ejs
 *
 */
public class AstArrayType extends AstTypedExpr implements IAstArrayType {

	private IAstType baseType;
	private IAstTypedExpr countExpr;

	public AstArrayType(TypeEngine typeEngine, IAstType baseType, IAstTypedExpr countExpr) throws TypeException {
		setBaseType(baseType);
		setCount(countExpr);
		initType(typeEngine);
	}

	protected AstArrayType(IAstType baseType, IAstTypedExpr countExpr, LLType type) {
		setBaseType(baseType);
		setCount(countExpr);
		setType(type);
	}
	
	/**
	 * @param typeEngine 
	 * @throws TypeException 
	 * 
	 */
	protected void initType(TypeEngine typeEngine) throws TypeException {
		int count = 0;
		IAstTypedExpr countVal = null;
		if (countExpr != null) {
			countExpr.simplify(typeEngine);
			countVal = countExpr;
			if (countVal instanceof IAstLitExpr) {
				if (!(countVal instanceof IAstIntLitExpr)) 
					throw new TypeException(countExpr, "array size must be integral");
				count = (int) ((IAstIntLitExpr) countVal).getValue();
				countVal = null;
			}
		}
		LLArrayType data = typeEngine.getArrayType(baseType.getType(), count, countVal);  
		setType(data);
		
		if (countExpr != null)
			countExpr.setType(typeEngine.INT);
				
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#copy()
	 */
	@Override
	public IAstArrayType copy() {
		return fixup(this, new AstArrayType(doCopy(baseType), doCopy(countExpr), getType()));
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + ((baseType == null) ? 0 : baseType.hashCode());
		result = prime * result + ((countExpr == null) ? 0 : countExpr.hashCode());
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
		AstArrayType other = (AstArrayType) obj;
		if (baseType == null) {
			if (other.baseType != null)
				return false;
		} else if (!baseType.equals(other.baseType))
			return false;
		if (countExpr == null) {
			if (other.countExpr != null)
				return false;
		} else if (!countExpr.equals(other.countExpr))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see v9t9.tools.ast.expr.impl.AstNode#toString()
	 */
	@Override
	public String toString() {
		return baseType.toString() + "[" + (countExpr != null ? countExpr.toString() : "") + "]";
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstType#getChildren()
	 */
	@Override
	public IAstNode[] getChildren() {
		if (countExpr != null)
			return new IAstNode[] { baseType, countExpr };
		else
			return new IAstNode[] { baseType };
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstNode#replaceChildren(org.ejs.eulang.ast.IAstNode[])
	 */
	@Override
	public void replaceChild(IAstNode existing, IAstNode another) {
		if (existing == countExpr)
			setCount((IAstTypedExpr) another);
		else if (existing == baseType)
			setBaseType((IAstType) another);
		else
			throw new IllegalArgumentException();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstType#setSymbol(org.ejs.eulang.ast.IAstSymbolExpr)
	 */
	@Override
	public void setCount(IAstTypedExpr symbolExpr) {
		this.countExpr = reparent(this.countExpr, symbolExpr);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstType#getSymbol()
	 */
	@Override
	public IAstTypedExpr getCount() {
		return countExpr;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.IAstArrayType#setBaseType(org.ejs.eulang.ast.IAstType)
	 */
	@Override
	public void setBaseType(IAstType typeExpr) {
		this.baseType = reparent(this.baseType, typeExpr);
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
			int count = 0;
			IAstTypedExpr countVal = null;
			if (countExpr != null) {
				if (countExpr.simplify(typeEngine)) {
					changed = true;
				}
				countVal = countExpr;
				if (countVal instanceof IAstLitExpr) {
					if (!(countVal instanceof IAstIntLitExpr)) 
						throw new TypeException(countExpr, "array size must be integral");
					count = (int) ((IAstIntLitExpr) countVal).getValue();
					countVal = null;
				}
			}
			LLArrayType data = typeEngine.getArrayType(baseType.getType(), count, countVal);  
			changed |= updateType(this, data);
		}
		
		if (countExpr != null)
			changed |= updateType(countExpr, typeEngine.INT);
		
		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.ast.impl.AstNode#validateChildTypes(org.ejs.eulang.TypeEngine)
	 */
	@Override
	public void validateChildTypes(TypeEngine typeEngine) throws TypeException {
		if (baseType.getType() != null && baseType.getType().isComplete()) {
			if (!baseType.getType().matchesExactly(getType().getSubType()))
				throw new TypeException(countExpr, "array element type does not match in parent");
		}
		if (countExpr != null) {
			if (countExpr.getType() != null && countExpr.getType().isComplete()) {
				if (countExpr.getType().getBasicType() != BasicType.INTEGRAL) {
					throw new TypeException(countExpr, "array size must be integral");
				}
			}	
		}
			
	}

}
