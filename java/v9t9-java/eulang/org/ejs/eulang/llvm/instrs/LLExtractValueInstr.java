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
	private final int index;

	public LLExtractValueInstr(LLOperand temp, LLType type, LLOperand value, int index) {
		super("extractvalue", temp, type, value);
		this.index = index;
	}
	
	@Override
	protected void appendInstrString(StringBuilder sb) {
		super.appendInstrString(sb);
		sb.append(", ").append(index);
	}
	

	/**
	 * @return
	 */
	public int getIndex() {
		return index;
	}

}
