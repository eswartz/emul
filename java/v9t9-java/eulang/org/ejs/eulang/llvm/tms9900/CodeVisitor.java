/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;


import java.util.HashSet;
import java.util.Set;

import org.ejs.eulang.symbols.ISymbol;

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author ejs
 *
 */
public abstract class CodeVisitor implements ICodeVisitor {
	public CodeVisitor() {
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#enterBlock(org.ejs.eulang.llvm.tms9900.Block)
	 */
	@Override
	public boolean enterBlock(Block block) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#enterCode(org.ejs.eulang.llvm.tms9900.Routine)
	 */
	@Override
	public boolean enterRoutine(Routine routine) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#enterInstr(org.ejs.eulang.llvm.tms9900.Block, v9t9.tools.asm.assembler.AsmInstruction)
	 */
	@Override
	public boolean enterInstr(Block block, AsmInstruction instr) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#enterOperand(v9t9.tools.asm.assembler.AsmInstruction, int, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public boolean enterOperand(AsmInstruction instr, int num,
			AssemblerOperand operand) {
		return true;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#exitBlock(org.ejs.eulang.llvm.tms9900.Block)
	 */
	@Override
	public void exitBlock(Block block) {
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#exitCode(org.ejs.eulang.llvm.tms9900.Routine)
	 */
	@Override
	public void exitRoutine(Routine directive) {
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#exitInstr(org.ejs.eulang.llvm.tms9900.Block, v9t9.tools.asm.assembler.AsmInstruction)
	 */
	@Override
	public void exitInstr(Block block, AsmInstruction instr) {
		
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#exitOperand(v9t9.tools.asm.assembler.AsmInstruction, int, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public void exitOperand(AsmInstruction instr, int num,
			AssemblerOperand operand) {
		
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#handleSource(v9t9.tools.asm.assembler.AsmInstruction, org.ejs.eulang.symbols.ISymbol)
	 */
	@Override
	public void handleSource(AsmInstruction instr, ISymbol source) {
		
	}
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#handleTarget(v9t9.tools.asm.assembler.AsmInstruction, org.ejs.eulang.symbols.ISymbol)
	 */
	@Override
	public void handleTarget(AsmInstruction instr, ISymbol target) {
		
	}
	
}
