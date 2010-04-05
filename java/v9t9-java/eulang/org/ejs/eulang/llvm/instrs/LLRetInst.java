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
public class LLRetInst extends LLTypedInstr {

	/**
	 * @param name
	 * @param type
	 * @param ops
	 */
	public LLRetInst(LLType type, LLOperand... ops) {
		super("ret", type, ops);
	}

}
