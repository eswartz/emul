/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;


import org.ejs.eulang.symbols.ISymbol;

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * Scan high-level code.  Each routine is visited once, in no particular order.
 * Then, blocks are visited in an order dictated by {@link #getWalk()}.  In
 * all cases but {@link Walk#LINEAR}, some blocks may not be reachable.  Such
 * blocks are entered after the exit block, and can be identified by having
 * no predecessors.
 * 
 * @author ejs
 *
 */
public interface ICodeVisitor {

	class Terminate extends Error {
		private static final long serialVersionUID = 7914440842755205893L;
		
	}
	
	enum Walk {
		/** Go through the blocks in declaration order, once */
		LINEAR,
		/** Go through the blocks in successor order, once */
		SUCCESSOR,
		/** Go through the blocks in dominator order, once */
		DOMINATOR,
		/** Go through the blocks in dominator order, once for each path */
		DOMINATOR_PATHS,
	}

	/** 
	 * Tell how to iterate blocks. 
	 * */
	Walk getWalk();
	
	boolean enterRoutine(Routine routine);
	
	void exitRoutine(Routine directive);
	
	boolean enterBlock(Block block);
	
	void exitBlock(Block block);
	
	boolean enterInstr(Block block, AsmInstruction instr);
	
	void exitInstr(Block block, AsmInstruction instr);
	
	boolean enterOperand(AsmInstruction instr, int num, AssemblerOperand operand);
	
	void exitOperand(AsmInstruction instr, int num, AssemblerOperand operand);
	
	void handleSource(AsmInstruction instr, ISymbol source);
	void handleTarget(AsmInstruction instr, ISymbol target);
}
