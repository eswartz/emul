/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import org.ejs.eulang.llvm.ops.LLOperand;
import org.ejs.eulang.types.LLType;

import v9t9.tools.asm.assembler.operand.hl.NumberOperand;

/**
 * @author ejs
 *
 */
public class NumOperand extends NumberOperand implements AsmOperand {

	private final LLOperand llOp;

	/**
	 * @param i
	 */
	public NumOperand(LLOperand llOp, int i) {
		super(i);
		this.llOp = llOp;
	}


	public org.ejs.eulang.llvm.ops.LLOperand getLLOperand() {
		return llOp;
	}
	
	public LLType getType() {
		return llOp != null ? llOp.getType() : null;
	}
	
}
