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
public class LLStoreInstr extends LLTypedInstr {

	/**
	 * @param name
	 * @param ops
	 */
	public LLStoreInstr(LLType type, LLOperand... ops) {
		super("store", type, ops);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLBaseInstr#appendOperandString(java.lang.StringBuilder, int, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	protected void appendOperandString(StringBuilder sb, int idx, LLOperand op) {
		if (idx == 1)
			//sb.append(getType()).append(' ').append(op); 
			sb.append(getType().getLLVMName() + "* ").append(op); 
		else
			super.appendOperandString(sb, idx, op);
	}
}
