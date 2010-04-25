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
public class LLInsertValueInstr extends LLAssignInstr {
	private final int idx;
	private final LLOperand elt;
	private final LLType eltType;

	public LLInsertValueInstr(LLOperand temp, LLType type, LLOperand val, LLType eltType, LLOperand elt, int idx) {
		super("insertvalue", temp, type, val);
		this.eltType = eltType;
		this.elt = elt;
		this.idx = idx;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLAssignInstr#appendInstrString(java.lang.StringBuilder)
	 */
	@Override
	protected void appendInstrString(StringBuilder sb) {
		super.appendInstrString(sb);
		sb.append(", ").append(eltType.getLLVMName()).append(" ").append(elt).append(", ").append(idx);
	}
}
