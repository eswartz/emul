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
public class LLRefType extends BaseLLType {

	public LLRefType(LLType baseType, int ptrBits, int refCntBits) {
		super(baseType.getName() + "$ref", ptrBits, 
				 "{ " + (baseType != null ? baseType.getLLVMType() : "") + "*, i" + refCntBits + "}*",
				 BasicType.REF, baseType);
		if (baseType instanceof LLRefType)
			throw new IllegalArgumentException();
	}
	
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return subType != null;
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
		if (newSub != subType)
			return typeEngine.getRefType(newSub);
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
			return typeEngine.getRefType(newSub);
		else
			return this;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#updateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.types.LLType[])
	 */
	@Override
	public LLType updateTypes(TypeEngine typeEngine, LLType[] type) {
		return typeEngine.getRefType(type[0]);
	}
}
