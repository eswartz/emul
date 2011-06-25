/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.types.LLType;


/**
 * @author ejs
 *
 */
public class LLNullOp extends BaseLLOperand {

	public LLNullOp(LLType type) {
		super(type);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "null";
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ops.BaseLLOperand#isConstant()
	 */
	@Override
	public boolean isConstant() {
		return true;
	}
	
}
