/**
 * 
 */
package org.ejs.eulang.types;

import java.util.Map;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.symbols.IScope;
import org.ejs.eulang.symbols.ISymbol;

/**
 * @author ejs
 *
 */
public class LLPointerType extends BaseLLType {

	
	public LLPointerType(String name, int bits, LLType baseType) {
		super(name, bits, getLLVMName(baseType), BasicType.POINTER, baseType);
		
	}
	
	private static String getLLVMName(LLType baseType) {
		//  baseType != null && baseType.isComplete() ? ((baseType.getName() != null ? "%" + baseType.getName() : baseType.getLLVMType()) + "*") : null
		if (baseType == null)
			return null;
		if (baseType.isComplete())
			return (baseType.getName() != null ? "%" + baseType.getName() : baseType.getLLVMType()) + "*";
		// exceptions for named types
		if (baseType instanceof LLDataType)
			return "%" + ((LLDataType) baseType).getName() + "*";
		return null;
	}
	public LLPointerType(int bits, LLType baseType) {
		super(baseType != null ? (baseType.getName() != null ? baseType.getName() + "$p" : 
			fixLLVMName(baseType.getLLVMName()) + "$p") : "$p", 
				bits, 
				getLLVMName(baseType), 
				BasicType.POINTER, baseType);
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	/*
	@Override
	public boolean isComplete() {
		return subType != null && subType.isComplete();
	}
	*/

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
		if (newSub != subType)
			return typeEngine.getPointerType(newSub);
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
		if (newSub != subType)
			return typeEngine.getPointerType(newSub);
		else
			return this;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#isCompatibleWith(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isCompatibleWith(LLType target) {
		return super.isCompatibleWith(target);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#updateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.types.LLType[])
	 */
	@Override
	public LLType updateTypes(TypeEngine typeEngine, LLType[] type) {
		return typeEngine.getPointerType(type[0]);
	}
}
