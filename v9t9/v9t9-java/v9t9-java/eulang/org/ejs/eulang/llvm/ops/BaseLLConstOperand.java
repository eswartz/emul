/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public abstract class BaseLLConstOperand extends BaseLLOperand {

	public BaseLLConstOperand(LLType type) {
		super(type);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.LLOperand#isConstant()
	 */
	@Override
	public boolean isConstant() {
		return true;
	}

}
