/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.llvm.ops.LLOperand;

/**
 * @author ejs
 *
 */
public class LLPhiInstr extends LLTypedInstr {
	private LLOperand result;

	public LLPhiInstr(LLOperand result, LLOperand... phiOps) {
		super("phi", result.getType(), phiOps);
		this.result = result;
	}

	/**
	 * @return the result
	 */
	public LLOperand getResult() {
		return result;
	}
}
