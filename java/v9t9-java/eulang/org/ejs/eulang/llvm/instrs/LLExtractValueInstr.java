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
public class LLExtractValueInstr extends LLAssignInstr {
	public LLExtractValueInstr(LLOperand temp, LLType type, LLOperand... valueAndIdxOps) {
		super("extractvalue", temp, type, valueAndIdxOps);
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLBaseInstr#appendOperandString(java.lang.StringBuilder, int, org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	protected void appendOperandString(StringBuilder sb, int idx, LLOperand op) {
		super.appendOperandString(sb, idx, op);
	}


}
