/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import org.ejs.eulang.llvm.LLBlock;
import org.ejs.eulang.llvm.LLCodeVisitor;
import org.ejs.eulang.llvm.instrs.LLInstr;

/**
 * @author ejs
 *
 */
public class RenumberInstructionsVisitor extends LLCodeVisitor {
	private int number;

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterInstr(org.ejs.eulang.llvm.LLBlock, org.ejs.eulang.llvm.instrs.LLInstr)
	 */
	@Override
	public boolean enterInstr(LLBlock block, LLInstr instr) {
		instr.setNumber(number++);
		return false;
	}
}
