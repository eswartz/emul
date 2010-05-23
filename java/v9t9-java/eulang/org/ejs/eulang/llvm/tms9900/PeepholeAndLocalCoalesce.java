/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.*;

import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.ejs.eulang.llvm.tms9900.asm.StackLocalOperand;
import org.ejs.eulang.symbols.ISymbol;

import static v9t9.engine.cpu.InstructionTable.*;
import static org.ejs.eulang.llvm.tms9900.InstrSelection.*;

import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
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
	
	/**
	 * @param asmInstruction
	 * @param from
	 * @param to
	 */
	private void replaceUses(AsmInstruction asmInstruction, AssemblerOperand from, AssemblerOperand to, ILocal fromLocal, ILocal toLocal) {
		assert asmInstruction != null;
		AssemblerOperand[] ops = asmInstruction.getOps();
		System.out.print("From " + asmInstruction + " -- > ");
		for (int idx = 0; idx < ops.length; idx++) {
			AssemblerOperand newOp = ops[idx].replaceOperand(from, to);
			if (newOp != ops[idx]) {
				if (fromLocal != null) {
					if (ops[idx].equals(asmInstruction.getSrcOp()))
						fromLocal.getUses().clear(asmInstruction.getNumber());
					else if (ops[idx].equals(asmInstruction.getDestOp()))
						fromLocal.getDefs().clear(asmInstruction.getNumber());
				}
				if (toLocal != null) {
					if (ops[idx].equals(asmInstruction.getSrcOp()))
						toLocal.getUses().set(asmInstruction.getNumber());
					else if (ops[idx].equals(asmInstruction.getDestOp()))
						toLocal.getDefs().set(asmInstruction.getNumber());
				}
				asmInstruction.setOp(idx + 1, newOp);
			}
		}
		System.out.println(asmInstruction);
	}

	/**
	 * @param instrs 
	 * @param inst
	 */
	private void removeInst(List<AsmInstruction> instrs, AsmInstruction inst) {
		System.out.println("Deleting " + inst);
		
		for (ISymbol sym : inst.getSources()) {
			ILocal local = locals.getLocal(sym);
			if (local != null) {
				local.getUses().clear(inst.getNumber());
				local.getDefs().clear(inst.getNumber());
			}
		}
		for (ISymbol sym : inst.getTargets()) {
			ILocal local = locals.getLocal(sym);
			if (local != null) {
				local.getUses().clear(inst.getNumber());
				local.getDefs().clear(inst.getNumber());
			}
		}
		instrs.remove(inst);
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
		for (Block block : routine.getBlocks()) {
			for (AsmInstruction instr : block.getInstrs()) {
				assert !instrMap.containsKey(instr.getNumber());
				instrMap.put(instr.getNumber(), instr);
			}
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
	
	/** Tell if any changes occurred.  This means that instructions were deleted from
	 * blocks, but instruction numbers were not re-calculated.
	 * @return
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
				if (coalesceLoadOpStore(instrs, inst)) {
					applied = true;
				}
				break;
			}

			if (!applied && combineStackAddressOperands(instrs, inst)) {
				applied = true;
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
	 * @param mov last MOV
	 * @return
	 */
	private boolean coalesceLoadOpStore(List<AsmInstruction> instrs,
			AsmInstruction mov) {
		// the temp for the copy must be a register
		ILocal tmpLocal = getSourceLocal(mov);
		if (!(tmpLocal instanceof RegisterLocal))
			return false;
		
		// the temp for the source must be a register if we want to substitute;
		// and avoid physical regs until coloring time to avoid conflicts
		ILocal origLocal = getTargetLocal(mov);
		if (!(origLocal instanceof RegisterLocal) || ((RegisterLocal) origLocal).isPhysReg())
			return false;
		
		// see if the temp is only defined once and this is its last use
		if (!tmpLocal.isExprTemp() || !tmpLocal.isSingleBlock()
				|| tmpLocal.getUses().nextSetBit(mov.getNumber() + 1) >= 0)
			return false;
		
		// and see if the definition is a read from the same target
		AsmInstruction def = instrMap.get(tmpLocal.getInit());
		assert def != null;
		
		if (!mov.getOp2().equals(def.getSrcOp()))
			return false;
		
		// okay, replace all uses of the source with the target
		AssemblerOperand fromOp = new RegTempOperand(tmpLocal.getType(), (RegisterLocal) tmpLocal);
		AssemblerOperand toOp = new RegTempOperand(origLocal.getType(), (RegisterLocal) origLocal);
	
		System.out.println("In " + mov.getNumber() +":  Replacing " + fromOp + " with " + toOp);
		
		for (int use = tmpLocal.getUses().nextSetBit(tmpLocal.getInit()); use >= 0; use = tmpLocal.getUses().nextSetBit(use + 1)) {
			replaceUses(instrMap.get(use), fromOp, toOp, tmpLocal, origLocal);
		}
	
		// and delete the moves
		removeInst(instrs, mov);
		
		if (def.getInst() == Imov)
			removeInst(instrs, def);
		
		return true;
	}
	

	/**
	 * In an instruction that uses an address operand using a register defined
	 * from LEA of a stack operand, convert to AddrOffsOperand. 
	 * @param instrs
	 * @param inst 
	 * @return
	 */
	private boolean combineStackAddressOperands(List<AsmInstruction> instrs,
			AsmInstruction inst) {
		
		boolean changed = false;
		for (ISymbol sym : inst.getSources()) {
			ILocal addrLocal = locals.getLocal(sym);
			if (addrLocal == null)
				continue;
			
			// the temp for the copy must be a register
			if (!(addrLocal instanceof RegisterLocal))
				continue;

			// see if the definition is an LEA on a stack local
			AsmInstruction def = instrMap.get(addrLocal.getInit());
			assert def != null;
			
			if (def.getInst() != Plea)
				continue;

			if (!(def.getOp1() instanceof AddrOperand))
				continue;
			
			// and just be sure we're talking about a stack local...
			// else we want to replace LEA with STWP/AI
			ILocal origLocal = getSourceLocal(def);
			if (!(origLocal instanceof StackLocal)) 
				continue;
			
			// see if the temp is only defined once and this is its last use
			if (!addrLocal.isExprTemp() || !addrLocal.isSingleBlock()
					|| addrLocal.getUses().nextSetBit(inst.getNumber() + 1) >= 0)
				continue;
		
			// okay, replace all uses of the source with the target
			AssemblerOperand fromOp = new RegTempOperand(addrLocal.getType(), (RegisterLocal) addrLocal);
			AssemblerOperand toOp = new StackLocalOperand(origLocal.getType(), (StackLocal) origLocal);
	
			System.out.println("In " + inst.getNumber() +":  Replacing " + fromOp + " with " + toOp);
			
			changed = true;
			for (int use = addrLocal.getUses().nextSetBit(addrLocal.getInit()); use >= 0; use = addrLocal.getUses().nextSetBit(use + 1)) {
				replaceUses(instrMap.get(use), fromOp, toOp, addrLocal, null);
			}
			
			// and delete the LEA
			removeInst(instrs, def);
		}
		
		return changed;
	}

}
