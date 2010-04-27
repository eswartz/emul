/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.TypeEngine;

/**
 * This is a placeholder type that fills in leaf nodes of types in generic invocation
 * contexts.
 * @author ejs
 *
 */
public class LLGenericType extends BaseLLType {
	
	public LLGenericType(String name) {
		super(name, 0, null, BasicType.GENERIC, null);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#toString()
	 */
	@Override
	public String toString() {
		return name;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#isComplete()
	 */
	@Override
	public boolean isComplete() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#isMoreComplete(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isMoreComplete(LLType type) {
		return type == null;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#isGeneric()
	 */
	@Override
	public boolean isGeneric() {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLType#substitute(org.ejs.eulang.TypeEngine, org.ejs.eulang.types.LLType, org.ejs.eulang.types.LLType)
	 */
	@Override
	public LLType substitute(TypeEngine typeEngine, LLType fromType,
			LLType toType) {
		// generics support replacing all generics if the fromType has no name
		if (fromType instanceof LLGenericType && ((LLGenericType) fromType).getName() == null)
			return toType;
		return super.substitute(typeEngine, fromType, toType);
	}
}
