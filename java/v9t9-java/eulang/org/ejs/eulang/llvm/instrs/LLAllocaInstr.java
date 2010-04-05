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
public class LLAllocaInstr extends LLAssignInstr {

	/**
	 * @param name
	 * @param ops
	 */
	public LLAllocaInstr(LLOperand ret, LLType type, LLOperand... ops) {
		super("alloca", ret, type, ops);
	}

}
