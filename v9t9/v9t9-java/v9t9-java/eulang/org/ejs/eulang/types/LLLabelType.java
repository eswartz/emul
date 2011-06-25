/**
 * 
 */
package org.ejs.eulang.types;

import org.ejs.eulang.TypeEngine;

/**
 * @author ejs
 *
 */
public class LLLabelType extends BaseLLType {

	public LLLabelType() {
		super("__label", 0, "label", BasicType.LABEL, null);
	}
	/*
	@Override
	public boolean isComplete() {
		return true;
	}
	*/

	/* (non-Javadoc)
	 * @see org.ejs.eulang.types.LLType#updateTypes(org.ejs.eulang.TypeEngine, org.ejs.eulang.types.LLType[])
	 */
	@Override
	public LLType updateTypes(TypeEngine typeEngine, LLType[] type) {
		return this;
	}
}
