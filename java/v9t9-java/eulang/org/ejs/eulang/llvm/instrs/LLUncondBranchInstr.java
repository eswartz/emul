/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.llvm.ops.LLOperand;

/**
 * @author ejs
 *
 */
public class LLUncondBranchInstr extends LLBaseInstr {

	/**
	 * @param name
	 * @param ops
	 */
	public LLUncondBranchInstr(LLOperand... ops) {
		super("br", ops);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLBaseInstr#appendOptionString(java.lang.StringBuilder)
	 */
	@Override
	protected void appendOptionString(StringBuilder sb) {
		sb.append("label ");
		super.appendOptionString(sb);
	}
}
