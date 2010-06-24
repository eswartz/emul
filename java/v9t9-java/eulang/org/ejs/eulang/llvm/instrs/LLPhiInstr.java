/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.llvm.ops.LLOperand;

/**
 * @author ejs
 *
 */
public class LLPhiInstr extends LLAssignInstr {
	public LLPhiInstr(LLOperand temp, LLOperand... phiOps) {
		super("phi", temp, temp.getType(), phiOps);
	}

}
