/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.*;

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * Iterate blocks and peephole instruction patterns introduced during instruction
 * selection.  Also, eliminate register copies -- such as copying memory
 * into a reg temp and then placing the value back -- when they are not needed.
 * @author ejs
 *
 */
public class PeepholeAndLocalCoalesce extends CodeVisitor {

	private final Routine routine;
	private Map<ILocal, AssemblerOperand> localValues;

	public PeepholeAndLocalCoalesce(Routine routine) {
		this.routine = routine;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#getWalk()
	 */
	@Override
	public Walk getWalk() {
		return Walk.SUCCESSOR;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#enterRoutine(org.ejs.eulang.llvm.tms9900.Routine)
	 */
	@Override
	public boolean enterRoutine(Routine routine) {
		localValues = new HashMap<ILocal, AssemblerOperand>();
		localValues = new HashMap<ILocal, AssemblerOperand>();
		return super.enterRoutine(routine);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#enterBlock(org.ejs.eulang.llvm.tms9900.Block)
	 */
	@Override
	public boolean enterBlock(Block block) {
		
		List<AsmInstruction> instrs = block.getInstrs();
		int idx = 0;
		while (idx < instrs.size()) {
			AsmInstruction inst = instrs.get(idx);
			idx++;
		}			
		return false;
	}
}
