/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.TypeEngine;

/**
 * @author ejs
 *
 */
public class LLUnknownAggregateType extends BaseLLAggregateType {

	public LLUnknownAggregateType() {
		super(null, 0, null, BasicType.DATA, null, true);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#updateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.types.LLType[])
	 */
	@Override
	public LLType updateTypes(TypeEngine typeEngine, LLType[] type) {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.BaseLLAggregateType#isMoreComplete(org.ejs.eulang.types.LLType)
	 */
	@Override
	public boolean isMoreComplete(LLType otherType) {
		return otherType == null;
	}
}
