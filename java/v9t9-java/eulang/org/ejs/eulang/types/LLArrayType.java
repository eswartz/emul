/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Collections;
import java.util.Map;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.ast.DumpAST;
import org.ejs.eulang.ast.IAstTypedExpr;
import org.ejs.eulang.ast.impl.AstNode;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;

/**
 * @author ejs
 *
 */
public class LLArrayType extends BaseLLType {

	private final int arrayCount;
	private final IAstTypedExpr dynamicSizeExpr;


	public LLArrayType(LLType baseType, int arrayCount, IAstTypedExpr dynamicSizeExpr) {
		super((baseType != null ? baseType.getName() : "<unknown>") + (dynamicSizeExpr != null ? "$dyn" : "x" + arrayCount), 
				baseType != null ? baseType.getBits() * arrayCount : 0, 
				 baseType != null && baseType.isComplete() ? ("[ " + arrayCount + " x " + (baseType != null ? baseType.getLLVMType() : "") + " ]") : null,
				 BasicType.ARRAY, baseType);
		this.arrayCount = arrayCount;
		this.dynamicSizeExpr = dynamicSizeExpr;
	}
	
	
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + arrayCount;
		result = prime * result
				+ ((dynamicSizeExpr == null) ? 0 : dynamicSizeExpr.hashCode());
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
		LLArrayType other = (LLArrayType) obj;
		if (arrayCount != other.arrayCount)
			return false;
		if (dynamicSizeExpr == null) {
			if (other.dynamicSizeExpr != null)
				return false;
		} else if (!dynamicSizeExpr.equals(other.dynamicSizeExpr))
			return false;
		return true;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#toString()
	 */
	@Override
	public String toString() {
		String str = super.toString();
		if (dynamicSizeExpr != null) {
			str += " { dynamic: " + DumpAST.dumpString(dynamicSizeExpr) + " }";
		}
		return str;
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	/*
	@Override
	public boolean isComplete() {
		return subType != null;
	}
	*/
	
	/**
	 * @return the arraySize
	 */
	public int getArrayCount() {
		return arrayCount;
	}

	public boolean isValidArrayIndex(int index) {
		if (isInitSized())
			return true;
		else
			return index < arrayCount;
	}


	/**
	 * @return
	 */
	public IAstTypedExpr getDynamicSizeExpr() {
		return dynamicSizeExpr;
	}



	/**
	 * @return
	 */
	public boolean isInitSized() {
		return arrayCount == 0 && dynamicSizeExpr == null;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#substitute(org.ejs.eulang.TypeEngine, java.lang.String, org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType substitute(TypeEngine typeEngine, LLType fromType, LLType toType) {
		if (fromType == null || fromType.equals(this))
			return toType;
		if (subType == null)
			return this;
		LLType newSub = subType.substitute(typeEngine, fromType, toType);
		IAstTypedExpr newDynamicSizeExpr = dynamicSizeExpr;
		if (newDynamicSizeExpr != null) {
			newDynamicSizeExpr = (IAstTypedExpr) dynamicSizeExpr.copy(null);
			boolean changed = AstNode.replaceTypesInTree(typeEngine, newDynamicSizeExpr, Collections.singletonMap(fromType, toType));
			if (!changed) {
				newDynamicSizeExpr = dynamicSizeExpr;
			}
		}
		if (newSub != subType || dynamicSizeExpr != newDynamicSizeExpr)
			return typeEngine.getArrayType(newSub, getArrayCount(), getDynamicSizeExpr());
		else
			return this;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#substitute(org.ejs.eulang.TypeEngine, org.ejs.eulang.symbols.IScope, java.util.Map)
	 */
	@Override
	public LLType substitute(TypeEngine typeEngine, IScope origScope,
			Map<Integer, ISymbol> symbolMap) {
		if (subType == null)
			return this;
		LLType newSub = subType.substitute(typeEngine, origScope, symbolMap);
		
		IAstTypedExpr newDynamicSizeExpr = dynamicSizeExpr;
		if (newDynamicSizeExpr != null) {
			newDynamicSizeExpr = (IAstTypedExpr) dynamicSizeExpr.copy(null);
			boolean changed = AstNode.replaceSymbols(typeEngine, newDynamicSizeExpr, origScope, symbolMap);
			if (!changed) {
				newDynamicSizeExpr = dynamicSizeExpr;
			}
		}
		
		if (newSub != subType || dynamicSizeExpr != newDynamicSizeExpr)
			return typeEngine.getArrayType(newSub, getArrayCount(), getDynamicSizeExpr());
		else
			return this;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#updateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.types.LLType[])
	 */
	@Override
	public LLType updateTypes(TypeEngine typeEngine, LLType[] type) {
		return typeEngine.getArrayType(type[0], arrayCount, dynamicSizeExpr);
	}

}
