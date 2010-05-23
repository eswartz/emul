/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.symbols.ISymbol;

/**
 * This assigns a unique number to each {@link AsmInstruction} and finds the
 * usage statistics for each expr and var temp.
 * @author ejs
 *
 */
public class LocalLifetimeVisitor extends CodeVisitor {
	private Map<ILocal, AsmInstruction> tempDefs = new HashMap<ILocal, AsmInstruction>();

	private final Locals locals;

	private Routine routine;

	private Block block;

	public LocalLifetimeVisitor(Locals locals) {
		this.locals = locals;
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
		this.routine = routine;
		tempDefs.clear();
		return super.enterRoutine(routine);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.LLCodeVisitor#exitCode(org.ejs.eulang.llvm.directives.LLDefineDirective)
	 */
	@Override
	public void exitRoutine(Routine routine) {
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
			for (ILocal local : locals.getArgumentLocals()) {
				local.setInit(new Pair<Block, AsmInstruction>(block, instr));
			}
		}
		return true;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#handleSource(v9t9.tools.asm.assembler.AsmInstruction, v9t9.tools.asm.assembler.operand.hl.AssemblerOperand)
	 */
	@Override
	public void handleSource(AsmInstruction instr, ISymbol sym) {
		ILocal local = getLocal(sym);
		if (local == null)
			return;
		
		Map<Block, List<AsmInstruction>> uses = local.getInstUses();
		List<AsmInstruction> list = uses.get(block);
		if (list == null) {
			list = new ArrayList<AsmInstruction>();
			uses.put(block, list);
		}
		if (!list.contains(instr))
			list.add(instr);
		
		super.handleSource(instr, sym);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#handleTarget(v9t9.tools.asm.assembler.AsmInstruction, org.ejs.eulang.symbols.ISymbol)
	 */
	@Override
	public void handleTarget(AsmInstruction instr, ISymbol sym) {
		ILocal local = getLocal(sym);
		if (local == null)
			return;
		if (local.getInit() == null) {
			local.setInit(new Pair<Block, AsmInstruction>(block, instr));
		}
		super.handleTarget(instr, sym);
	}
	/**
	 * @param sym
	 * @return
	 */
	private ILocal getLocal(ISymbol sym) {
		return locals.getLocal(sym);
	}
}
