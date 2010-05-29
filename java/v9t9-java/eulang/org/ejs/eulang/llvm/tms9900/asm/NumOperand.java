/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import org.ejs.eulang.types.LLType;

import v9t9.tools.asm.assembler.operand.hl.NumberOperand;

/**
 * @author ejs
 *
 */
public class NumOperand extends NumberOperand implements AsmOperand {

	private LLType type;

	/**
	 * @param i
	 */
	public NumOperand(LLType type, int i) {
		super(i);
		this.type = type;
	}


	public LLType getType() {
		return type;
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.asm.AsmOperand#setType(org.ejs.eulang.types.LLType)
	 */
	@Override
	public void setType(LLType type) {
		this.type = type;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.asm.AsmOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return true;
	}
}
