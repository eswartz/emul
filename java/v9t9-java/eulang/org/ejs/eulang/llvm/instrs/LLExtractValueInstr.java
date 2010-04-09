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
	public LLExtractValueInstr(LLOperand temp, LLType type, LLOperand ... valueAndIdxOps) {
		super("extractvalue", temp, type, valueAndIdxOps);
	}

}
