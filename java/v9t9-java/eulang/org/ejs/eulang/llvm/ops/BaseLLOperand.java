/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public abstract class BaseLLOperand implements LLOperand {

	protected final LLType type;

	public BaseLLOperand(LLType type) {
		this.type = type;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.LLOperand#getType()
	 */
	@Override
	public LLType getType() {
		return type;
	}

}
