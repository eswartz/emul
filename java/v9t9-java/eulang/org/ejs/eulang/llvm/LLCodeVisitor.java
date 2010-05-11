/**
 * 
 */
package org.ejs.eulang.llvm;

import org.ejs.eulang.llvm.directives.LLBaseDirective;
import org.ejs.eulang.llvm.directives.LLDefineDirective;
import org.ejs.eulang.llvm.instrs.LLInstr;
import org.ejs.eulang.llvm.ops.LLOperand;

/**
 * @author ejs
 *
 */
public class LLCodeVisitor implements ILLCodeVisitor {

	@Override
	public boolean enterBlock(LLBlock block) {
		return true;
	}

	@Override
	public boolean enterCode(LLDefineDirective directive) {
		return true;
	}

	@Override
	public boolean enterInstr(LLBlock block, LLInstr instr) {
		return true;
	}

	@Override
	public boolean enterOperand(LLInstr instr, int num, LLOperand operand) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeVisitor#exitBlock(org.ejs.eulang.llvm.LLBlock)
	 */
	@Override
	public void exitBlock(LLBlock block) {

	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeVisitor#exitCode(org.ejs.eulang.llvm.directives.LLBaseDirective)
	 */
	@Override
	public void exitCode(LLDefineDirective directive) {

	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeVisitor#exitInstr(org.ejs.eulang.llvm.instrs.LLInstr)
	 */
	@Override
	public void exitInstr(LLBlock block, LLInstr instr) {

	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.ILLCodeVisitor#exitOperand(org.ejs.eulang.llvm.ops.LLOperand)
	 */
	@Override
	public void exitOperand(LLInstr instr, int num, LLOperand operand) {
	}

}
