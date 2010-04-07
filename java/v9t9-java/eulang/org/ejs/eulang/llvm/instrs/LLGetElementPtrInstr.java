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
public class LLGetElementPtrInstr extends LLAssignInstr {

	private LLType toType;

	public LLGetElementPtrInstr(LLOperand temp, LLType type, LLOperand... valueAndTypeIdxOps) {
		super("getelementptr", temp, type, valueAndTypeIdxOps);
	}

}
