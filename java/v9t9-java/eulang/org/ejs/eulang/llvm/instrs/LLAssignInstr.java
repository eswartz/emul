/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.LLType;

/**
 * Any assignment instruction like "foo = bar"
 * @author ejs
 *
 */
public abstract class LLAssignInstr extends LLTypedInstr {

	private final LLOperand result;

	/**
	 * @param name
	 * @param type
	 * @param ops
	 */
	public LLAssignInstr(String name, LLOperand result, LLType type, LLOperand... ops) {
		super(name, type, ops);
		this.result = result;
	}
	/**
	 * @return the result
	 */
	public LLOperand getResult() {
		return result;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.instrs.LLBaseInstr#appendInstrString(java.lang.StringBuilder)
	 */
	@Override
	protected void appendInstrString(StringBuilder sb) {
		if (result != null)
			sb.append(result).append(" = ");
		super.appendInstrString(sb);
	}
	
}
