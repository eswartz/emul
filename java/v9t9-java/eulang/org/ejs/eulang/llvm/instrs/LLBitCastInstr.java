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
public class LLBitCastInstr extends LLAssignInstr {

	private LLType toType;

	/**
	 * @param temp
	 * @param valueType
	 * @param value
	 * @param rEFPTR
	 */
	public LLBitCastInstr(LLOperand temp, LLType fromType, LLOperand value,
			LLType toType) {
		super("bitcast", temp, fromType, value);
		this.toType = toType;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLBaseInstr#appendInstrString(java.lang.StringBuilder)
	 */
	@Override
	protected void appendInstrString(StringBuilder sb) {
		super.appendInstrString(sb);
		sb.append(" to " + toType.toString());
	}

}
