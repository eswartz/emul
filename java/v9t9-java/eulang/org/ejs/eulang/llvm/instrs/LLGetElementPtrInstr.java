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
public class LLGetElementPtrInstr extends LLAssignInstr {
	public LLGetElementPtrInstr(LLOperand temp, LLType type, LLOperand... valueAndTypeIdxOps) {
		super("getelementptr", temp, type, valueAndTypeIdxOps);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLBaseInstr#appendOperandString(java.lang.StringBuilder, int, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	protected void appendOperandString(StringBuilder sb, int idx, LLOperand op) {
		if (idx > 0) 
			sb.append(op.getType()).append(' ');
		super.appendOperandString(sb, idx, op);
	}
}
