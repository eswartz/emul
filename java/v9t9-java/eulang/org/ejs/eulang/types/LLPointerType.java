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
		super(name, bits, baseType.getLLVMType() + "*", BasicType.POINTER, baseType);
		
	}
	public LLPointerType(int bits, LLType baseType) {
		super(baseType.getName() != null ? baseType.getName() + "$p" : 
			fixLLVMName(baseType.getLLVMName()) + "$p", 
				bits, baseType.getLLVMType() + "*", BasicType.POINTER, baseType);
		
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return subType != null && subType.isComplete();
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#substitute(org.ejs.eulang.TypeEngine, java.lang.String, org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType substitute(TypeEngine typeEngine, LLType fromType, LLType toType) {
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
}
