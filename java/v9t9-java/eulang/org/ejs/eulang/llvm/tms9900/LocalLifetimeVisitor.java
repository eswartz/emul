/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.HashMap;
import java.util.Map;

import org.ejs.eulang.symbols.ISymbol;

/**
 * This assigns a unique number to each {@link AsmInstruction} and finds the
 * usage statistics for each local, including identifying whether in its usage
 * it is an expr or var temp.  This also deletes unused locals.
 * @author ejs
 *
 */
public class LocalLifetimeVisitor extends CodeVisitor {
	private Map<ILocal, Block> firstBlockRefs = new HashMap<ILocal, Block>();
	private Map<ILocal, AsmInstruction> tempDefs = new HashMap<ILocal, AsmInstruction>();

	private StackFrame stackFrame;

	private Block block;

	public LocalLifetimeVisitor() {
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#getWalk()
	 */
	@Override
	public Walk getWalk() {
		return Walk.LINEAR;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterCode(org.ejs.eulang.llvm.directives.LLDefineDirective)
	 */
	@Override
	public boolean enterRoutine(Routine routine) {
		this.stackFrame = routine.getStackFrame();
		
		tempDefs.clear();
		firstBlockRefs.clear();
		
		for (ILocal local : stackFrame.getAllLocals()) {
			local.getDefs().clear();
			local.getUses().clear();
			local.setExprTemp(true);
			local.setSingleBlock(true);
		}
		return super.enterRoutine(routine);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#exitCode(org.ejs.eulang.llvm.directives.LLDefineDirective)
	 */
	@Override
	public void exitRoutine(Routine routine) {
		for (ILocal local : stackFrame.getAllLocals()) {
			if (local.getDefs().isEmpty() && local.getUses().isEmpty()) {
				stackFrame.removeLocal(local);
			}
		}
		super.exitRoutine(routine);
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#enterBlock(org.ejs.eulang.llvm.tms9900.Block)
	 */
	@Override
	public boolean enterBlock(Block block) {
		this.block = block;
		return super.enterBlock(block);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#enterInstr(org.ejs.eulang.llvm.LLBlock, org.ejs.eulang.llvm.instrs.LLInstr)
	 */
	@Override
	public boolean enterInstr(Block block, AsmInstruction instr) {
		if (instr.getInst() == InstrSelection.Pprolog) {
			for (ILocal local : stackFrame.getArgumentLocals()) {
				addLocalDef(instr, local);
			}
		}
		else if (instr.getInst() == InstrSelection.Pepilog) {
			for (ILocal local : stackFrame.getAllLocals()) {
				if (local.isOutgoing()) {
					addLocalUse(instr, local);
				}
			}			
		}
		return true;
	}
	
	/**
	 * @param instr
	 * @param local
	 */
	private void addLocalDef(AsmInstruction instr, ILocal local) {
		boolean isFirst = local.getDefs().isEmpty();
		if (isFirst) {
			local.setInit(instr.getNumber());
			firstBlockRefs.put(local, block);
		} else {
			if (firstBlockRefs.containsKey(local) && firstBlockRefs.get(local) != block) {
				local.setSingleBlock(false);
			}
			// due to 2-op nature, we can read and write in same instruction; 
			// this is not considered a kill 
			if (!local.getUses().get(instr.getNumber()))
				local.setExprTemp(false);
		}
		local.getDefs().set(instr.getNumber());
	}

	private void addLocalUse(AsmInstruction instr, ILocal local) {
		boolean isFirst = local.getUses().isEmpty();
		local.getUses().set(instr.getNumber());
		if (isFirst)
			firstBlockRefs.put(local, block);
		else if (firstBlockRefs.containsKey(local) && firstBlockRefs.get(local) != block)
			local.setSingleBlock(false);
	}
	

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#handleSource(v9t9.tools.asm.assembler.AsmInstruction, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public void handleSource(AsmInstruction instr, ISymbol sym) {
		ILocal local = getLocal(sym);
		if (local == null)
			return;

		addLocalUse(instr, local);
		
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#handleTarget(v9t9.tools.asm.assembler.AsmInstruction, org.ejs.eulang.symbols.ISymbol)
	 */
	@Override
	public void handleTarget(AsmInstruction instr, ISymbol sym) {
		ILocal local = getLocal(sym);
		if (local == null)
			return;

		addLocalDef(instr, local);
	}
	
	/**
	 * @param sym
	 * @return
	 */
	private ILocal getLocal(ISymbol sym) {
		return stackFrame.getLocal(sym);
	}
}
