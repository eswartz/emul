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
public class LLLoadInstr extends LLAssignInstr {

	/**
	 * @param name
	 * @param ops
	 */
	public LLLoadInstr(LLOperand ret, LLType type, LLOperand... ops) {
		super("load", ret, type, ops);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLBaseInstr#appendOptionString(java.lang.StringBuilder)
	 */
	@Override
	protected void appendOptionString(StringBuilder sb) {
		sb.append(getType().getLLVMName()).append("* ");
		//sb.append(getType());
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLBaseInstr#appendOperandString(java.lang.StringBuilder, int, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	protected void appendOperandString(StringBuilder sb, int idx, LLOperand op) {
		super.appendOperandString(sb, idx, op);
	}
}
