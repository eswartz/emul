/**
 * 
 */
package org.ejs.eulang.llvm.tms9900.asm;

import v9t9.tools.asm.assembler.operand.hl.NumberOperand;

/**
 * @author ejs
 *
 */
public class NumOperand extends NumberOperand implements AsmOperand {

	/**
	 * @param i
	 */
	public NumOperand(int i) {
		super(i);
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.asm.AsmOperand#isConst()
	 */
	@Override
	public boolean isConst() {
		return true;
	}
}
