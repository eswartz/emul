/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public abstract class BaseLLConstOperand implements LLOperand {

	protected final LLType type;

	public BaseLLConstOperand(LLType type) {
		this.type = type;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.LLOperand#getType()
	 */
	@Override
	public LLType getType() {
		return type;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.LLOperand#isConstant()
	 */
	@Override
	public boolean isConstant() {
		return true;
	}

}
