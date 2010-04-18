/**
 * 
 */
package org.ejs.eulang.llvm.ops;

import org.ejs.eulang.types.LLType;


/**
 * @author ejs
 *
 */
public class LLUndefOp extends BaseLLOperand {

	public LLUndefOp(LLType type) {
		super(type);
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return "undef";
	}
}
