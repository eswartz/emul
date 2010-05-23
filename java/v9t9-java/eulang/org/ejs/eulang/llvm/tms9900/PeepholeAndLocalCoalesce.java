/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.*;

import org.ejs.eulang.symbols.ISymbol;

import static v9t9.engine.cpu.InstructionTable.*;
import static org.ejs.eulang.llvm.tms9900.InstrSelection.*;

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * Iterate blocks and peephole instruction patterns introduced during instruction
 * selection.  Also, eliminate register copies -- such as copying memory
 * into a reg temp and then placing the value back -- when they are not needed.
 * @author ejs
 *
 */
public class PeepholeAndLocalCoalesce extends CodeVisitor {

	private Map<ILocal, AssemblerOperand> localValues;
	private boolean changed;
	private Locals locals;
	private TreeMap<Integer, AsmInstruction> instrMap;
	private Routine routine;

	public PeepholeAndLocalCoalesce() {
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#getWalk()
	 */
	@Override
	public Walk getWalk() {
		return Walk.DOMINATOR_PATHS;
	}

	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#enterRoutine(org.ejs.eulang.llvm.tms9900.Routine)
	 */
	@Override
	public boolean enterRoutine(Routine routine) {
		this.routine = routine;
		this.locals = routine.getLocals();

		changed = false;
		instrMap = new TreeMap<Integer, AsmInstruction>();
		for (Block block : routine.getBlocks())
			for (AsmInstruction instr : block.getInstrs()) {
				assert !instrMap.containsKey(instr.getNumber());
				instrMap.put(instr.getNumber(), instr);
			}
		return super.enterRoutine(routine);
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#exitRoutine(org.ejs.eulang.llvm.tms9900.Routine)
	 */
	@Override
	public void exitRoutine(Routine directive) {
		super.exitRoutine(directive);
	}
	/**
	 * @return the changed
	 */
	public boolean isChanged() {
		return changed;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#enterBlock(org.ejs.eulang.llvm.tms9900.Block)
	 */
	@Override
	public boolean enterBlock(Block block) {
		
		if (block.getIdom() == null) {
			localValues = new HashMap<ILocal, AssemblerOperand>();
		}
		
		List<AsmInstruction> instrs = block.getInstrs();
		int idx = 0;
		while (idx < instrs.size()) {
			AsmInstruction inst = instrs.get(idx);
			boolean applied = false;
			
			switch (inst.getInst()) {
			case Imov:
				if (coalesceLoadOpStore(instrs, inst, idx)) {
					applied = true;
				}
				break;
			}
			
			if (applied) {
				changed = true;
				continue;
			}
			idx++;
		}			
		return false;
	}

	/**
	 * Convert a MOV from memory/local to a temp, operation on that temp, and MOV back to the memory/local
	 * into an operation on the memory/local directly. 
	 * @param instrs
	 * @param inst
	 * @return
	 */
	private boolean coalesceLoadOpStore(List<AsmInstruction> instrs,
			AsmInstruction mov, int index) {
		if (instrs.size() >= index + 3) {
			// the temp for the copy
			ILocal tmpLocal = getTargetLocal(mov);
			if (tmpLocal == null)
				return false;
			
			AsmInstruction opinst = getNextUse(tmpLocal, mov);
			if (opinst == null)
				return false;
			
			// src is the stored local
			AssemblerOperand src = mov.getOp1();
			AssemblerOperand dst = mov.getOp2();

			if (opinst != null && dst.equals(opinst.getDestOp())) {
				AsmInstruction mov2 = getNextUse(tmpLocal, opinst);
				if (mov2 != null && dst.equals(mov2.getSrcOp()) && src.equals(mov2.getDestOp())) {
					if (getNextUse(tmpLocal, mov2) == null) {
						instrs.remove(mov);
						instrs.remove(mov2);
						opinst.setDestOp(src);
						return true;
					}
				}
			}

		}
		return false;
	}

	private AsmInstruction getNextUse(ILocal local, AsmInstruction inst) {
		BitSet uses = local.getUses();

		int next = uses.nextSetBit(inst.getNumber() + 1);
		AsmInstruction nextInst = instrMap.get(next);
		
		return nextInst;
	}

	private ILocal getTargetLocal(AsmInstruction inst) {
		for (ISymbol sym : inst.getTargets()) {
			ILocal local = locals.getLocal(sym);
			if (local != null)
				return local;
		}
		return null;
	}
	private ILocal getSourceLocal(AsmInstruction inst) {
		for (ISymbol sym : inst.getSources()) {
			ILocal local = locals.getLocal(sym);
			if (local != null)
				return local;
		}
		return null;
	}
}
