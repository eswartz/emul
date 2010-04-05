/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.LLType;

/**
 * @author ejs
 *
 */
public class LLBranchInstr extends LLTypedInstr {

	/**
	 * @param name
	 * @param type
	 * @param ops
	 */
	public LLBranchInstr(LLType type, LLOperand... ops) {
		super("br", type, ops);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLBaseInstr#appendOperandString(java.lang.StringBuilder, int, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	protected void appendOperandString(StringBuilder sb, int idx, LLOperand op) {
		if (idx != 0)
			sb.append("label ");
		super.appendOperandString(sb, idx, op);
	}
}
