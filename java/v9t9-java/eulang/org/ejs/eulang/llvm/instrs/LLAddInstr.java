/**
 * 
 */
package org.ejs.eulang.llvm.instrs;

import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.llvm.types.LLType;

/**
 * @author ejs
 *
 */
public class LLAddInstr extends LLBinaryInstr {

	/**
	 * @param name
	 * @param type
	 * @param ops
	 */
	public LLAddInstr(LLType type, LLOperand[] ops) {
		super("add", type, ops);
	}

}
