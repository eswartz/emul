/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.*;
import java.util.Map.Entry;

import org.ejs.eulang.llvm.tms9900.asm.ISymbolOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.ejs.eulang.llvm.tms9900.asm.StackLocalOperand;
import org.ejs.eulang.symbols.ISymbol;

import static v9t9.engine.cpu.InstructionTable.*;
import static org.ejs.eulang.llvm.tms9900.InstrSelection.*;

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.Instruction.Effects;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.ConstPoolRefOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;

/**
 * Iterate blocks and peephole instruction patterns introduced during instruction
 * selection.  Also, eliminate register copies -- such as copying memory
 * into a reg temp and then placing the value back -- when they are not needed.
 * @author ejs
 *
 */
public class PeepholeAndLocalCoalesce extends CodeVisitor {

	/** hold known values of locals when they hold numbers or stack values */
	private Map<ILocal, AssemblerOperand> localValues;
	/** hold known values of locals when they hold numbers or stack values */
	private Map<AssemblerOperand, AssemblerOperand> stackValues;
	private boolean changed;
	private Locals locals;
	private TreeMap<Integer, AsmInstruction> instrMap;
	private TreeMap<Integer, Block> instrBlockMap;

	public PeepholeAndLocalCoalesce() {
	}
	
	/**
	 * @param asmInstruction
	 * @param from
	 * @param to
	 */
	@SuppressWarnings("unchecked")
	private void replaceUses(AsmInstruction asmInstruction, AssemblerOperand from, AssemblerOperand to, ILocal fromLocal, ILocal toLocal) {
		assert asmInstruction != null;
		AssemblerOperand[] ops = asmInstruction.getOps();
		System.out.print("From\t" + asmInstruction + "\n-- >\t");
		for (int idx = 0; idx < ops.length; idx++) {
			AssemblerOperand newOp = ops[idx].replaceOperand(from, to);
			if (newOp != ops[idx]) {
				// update usage
				if (fromLocal != null) {
					if (ops[idx].equals(asmInstruction.getSrcOp()))
						fromLocal.getUses().clear(asmInstruction.getNumber());
					else if (ops[idx].equals(asmInstruction.getDestOp()))
						fromLocal.getDefs().clear(asmInstruction.getNumber());
				}
				if (toLocal != null) {
					if (ops[idx].equals(asmInstruction.getSrcOp()))
						toLocal.getUses().set(asmInstruction.getNumber());
					else if (ops[idx].equals(asmInstruction.getDestOp())) {
						toLocal.getDefs().set(asmInstruction.getNumber());
						if (asmInstruction.getNumber() < toLocal.getInit())
							toLocal.setInit(asmInstruction.getNumber());
					}
				}

				// update op
				asmInstruction.setOp(idx + 1, newOp);
			}
		}
		boolean isKnown = false;
		if (localValues.containsKey(fromLocal)) {
			if (toLocal != null)
				localValues.put(toLocal, localValues.get(fromLocal));
			isKnown = true;
		}
		Object[] entryArray = stackValues.entrySet().toArray();
		for (Object o : entryArray) {
			Map.Entry<AssemblerOperand, AssemblerOperand> entry = (Entry<AssemblerOperand, AssemblerOperand>) o;
			if (entry.getValue().equals(from)) {
				if (isKnown)
					entry.setValue(to);
				else
					stackValues.remove(entry.getKey());
			}
		}
		
		System.out.println(asmInstruction);
	}

	/**
	 * @param instrs 
	 * @param inst
	 */
	private void removeInst(AsmInstruction inst) {
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
		Block block = instrBlockMap.get(inst.getNumber());
		block.getInstrs().remove(inst);
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
		this.locals = routine.getLocals();

		changed = false;
		instrMap = new TreeMap<Integer, AsmInstruction>();
		instrBlockMap = new TreeMap<Integer, Block>();
		for (Block block : routine.getBlocks()) {
			for (AsmInstruction instr : block.getInstrs()) {
				assert !instrMap.containsKey(instr.getNumber());
				instrMap.put(instr.getNumber(), instr);
				instrBlockMap.put(instr.getNumber(), block);
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
		
		// toss away stored values on a new path or when multiple blocks come in
		if (block.getIdom() == null || block.getIdom().getDominatedChildren().size() > 1) {
			localValues = new HashMap<ILocal, AssemblerOperand>();
			stackValues = new HashMap<AssemblerOperand, AssemblerOperand>();
		}
		
		List<AsmInstruction> instrs = block.getInstrs();
		int idx = 0;
		while (idx < instrs.size()) {
			AsmInstruction inst = instrs.get(idx);
			System.out.println(inst);
			
			boolean applied = false;
			
			// apply instruction-specific peepholes
			switch (inst.getInst()) {
			case Imov:
				if (coalesceLoadOpStore(inst)) {
					applied = true;
				} 
				else if (coalesceCopy(inst)) {
					applied = true;
				} 
				else if (replaceConstant(inst)) {
					applied = true;
				}
				break;
			}

			// apply value-specific peepholes
			if (!applied) {
				if (combineStackAddressOperands(inst)) {
					applied = true;
				}
				else if (removeStackToMemoryCopies(inst)) {
					applied = true;
				}
				else if (replaceStackRead(inst)) {
					applied = true;
				}
				else if (removeDeadInst(inst)) {
					applied = true;
				} else {
					if (trackValues(inst)) {
						System.out.println("\nLocal values:");
						for (Map.Entry<ILocal, AssemblerOperand> entry : localValues.entrySet()) {
							System.out.println("\t" + entry.getKey().getName() + " =\t" + entry.getValue());
						}
						for (Map.Entry<AssemblerOperand, AssemblerOperand> entry : stackValues.entrySet()) {
							System.out.println("\t" + entry.getKey() + " =\t" + entry.getValue());
						}
						System.out.println();
						
						if (replaceConstant(inst))
							applied = true;
					}
					

				}
			}
			
			if (applied) {
				changed = true;
				System.out.println();
				continue;
			}
			idx++;
			
		}			
		return false;
	}

	/**
	 * Track register values used in the instructions, for later substitution. 
	 * @param inst current inst
	 */
	private boolean trackValues(AsmInstruction inst) {
		// look at target of instruction (either reg or stack)
		ILocal targetLocal = getTargetLocal(inst);
		if (targetLocal == null)
			return false;
		
		AssemblerOperand destOp = inst.getDestOp();
		if (!(targetLocal instanceof RegisterLocal)) {
			return false;
		}
		
		// see if the source has a well-known value
		ILocal srcLocal = getSourceLocal(inst);
		AssemblerOperand src = null; 
		src = stackValues.get(inst.getSrcOp());
		if (src == null) {
			if (srcLocal != null)
				src = localValues.get(srcLocal);
		}
		
		boolean changedValues = false;
		boolean gotStackValue = false;
		
		// look for well-known instructions
		if (inst.getInst() == Ili) {
			if (inst.getOp2() instanceof NumberOperand)
				src = inst.getOp2();
		} 
		else if (inst.getInst() == Iai) {
			if (src instanceof NumberOperand && inst.getOp2() instanceof NumberOperand)
				src = new NumberOperand( ((NumberOperand) src).getValue() + ((NumberOperand) inst.getOp2()).getValue() );
			else
				src = null;
		} 
		else if (inst.getInst() == Imov || inst.getInst() == Imovb || inst.getInst() == Pcopy) {
			if (getSourceLocal(inst) instanceof StackLocal) {
				src = inst.getOp1();
			} else if (inst.getOp1() instanceof ConstPoolRefOperand) {
				src = new NumberOperand(((ConstPoolRefOperand) inst.getOp1()).getValue());
			} else if (inst.getOp1().isMemory()) {
				stackValues.put(inst.getOp1(), inst.getOp2());
				gotStackValue = true;
			} else if (inst.getOp2().isMemory()) {
				stackValues.put(inst.getOp2(), inst.getOp1());
				gotStackValue = true;
			}
		} else {
			// dunno what to do here
			src = null;
		}
		
		if (targetLocal instanceof RegisterLocal && destOp.isRegister()) {
			if (src != null) {
				localValues.put(targetLocal, src);
				changedValues = true;
			} else {
				if (localValues.containsKey(targetLocal)) {
					localValues.remove(targetLocal);
					changedValues = true;
				} 
				if (!gotStackValue)
					stackValues.values().remove(destOp);
			}
		} 
		
		return changedValues || gotStackValue;
		
	}

	/**
	 * Convert a MOV from memory/local to a temp, operation on that temp, and MOV back to the memory/local
	 * into an operation on the memory/local directly. 
	 * @param mov last MOV
	 * @return
	 */
	private boolean coalesceLoadOpStore(AsmInstruction mov) {
		// the temp for the copy must be a register
		ILocal tmpLocal = getSourceLocal(mov);
		if (!(tmpLocal instanceof RegisterLocal))
			return false;
		
		// the temp for the source must be a register if we want to substitute;
		// and avoid physical regs until coloring time to avoid conflicts
		if (mov.getOp2().isMemory())
			return false;
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
		
		if (!mov.getOp1().equals(def.getDestOp()))
			return false;
		
		// okay, replace all uses of the source with the target
		AssemblerOperand fromOp = new RegTempOperand(tmpLocal.getType(), (RegisterLocal) tmpLocal);
		AssemblerOperand toOp = new RegTempOperand(origLocal.getType(), (RegisterLocal) origLocal);
	
		System.out.println(here() + "In " + mov.getNumber() +":  Replacing " + fromOp + " with " + toOp);
		boolean changed = false;
		
		for (int defi = tmpLocal.getDefs().nextSetBit(tmpLocal.getInit()); defi >= 0; defi = tmpLocal.getDefs().nextSetBit(defi + 1)) {
			replaceUses(instrMap.get(defi), fromOp, toOp, tmpLocal, origLocal);
			changed = true;
		}
	
		for (int use = tmpLocal.getUses().nextSetBit(tmpLocal.getInit()); use >= 0; use = tmpLocal.getUses().nextSetBit(use + 1)) {
			replaceUses(instrMap.get(use), fromOp, toOp, tmpLocal, origLocal);
			changed = true;
		}
	
		// and delete the moves
		if (mov.getOp1().equals(mov.getOp2()))
			removeInst(mov);
		
		if (mov.getOp2().equals(def.getSrcOp()))
			removeInst(def);
		
		return changed;
	}
	
	/**
	 * @return
	 */
	private String here() {
		Exception e = new Exception();
		e.fillInStackTrace();
		String func = e.getStackTrace()[1].getMethodName();
		return "[" + func + "] ";
	}

	/**
	 * Convert a MOV from a temp to another reg, when that temp is only used once.
	 * @param inst last MOV
	 * @return
	 */
	private boolean coalesceCopy(AsmInstruction inst) {
		// the temp for the copy must be a register
		ILocal tmpLocal = getSourceLocal(inst);
		if (tmpLocal == null)
			return false;
		
		ILocal origLocal = getTargetLocal(inst);
		if (origLocal == null)
			return false;
		
		// see if the temp is only defined once and this is its last use
		if (!tmpLocal.isExprTemp() || !tmpLocal.isSingleBlock()
				|| tmpLocal.getUses().nextSetBit(inst.getNumber() + 1) >= 0)
			return false;
		
		// and see if the definition is a read from the same target
		AsmInstruction def = instrMap.get(tmpLocal.getInit());
		assert def != null;
		
		origLocal = getSourceLocal(def);
		if (!inst.getOp1().equals(def.getDestOp()) || !(def.getSrcOp() instanceof ISymbolOperand))
			return false;
		
		// okay, replace all uses of the source with the target
		AssemblerOperand fromOp = inst.getOp1();
		AssemblerOperand toOp = def.getSrcOp();
		
		if (fromOp.equals(toOp))
			return false;
	
		System.out.println(here() + "In " + inst.getNumber() +":  Replacing " + fromOp + " with " + toOp);
		boolean changed = false;
		
		for (int use = tmpLocal.getUses().nextSetBit(tmpLocal.getInit()); use >= 0; use = tmpLocal.getUses().nextSetBit(use + 1)) {
			replaceUses(instrMap.get(use), fromOp, toOp, tmpLocal, origLocal);
			changed = true;
		}
	
		return changed;
	}
	
	/**
	 * In an instruction that uses an address operand using a register defined
	 * from LEA of a stack operand, convert to AddrOffsOperand. 
	 * @param inst 
	 * @return
	 */
	private boolean combineStackAddressOperands(AsmInstruction inst) {
		
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
	
			System.out.println(here() + "In " + inst.getNumber() +":  Replacing " + fromOp + " with " + toOp);
			
			changed = true;
			for (int use = addrLocal.getUses().nextSetBit(addrLocal.getInit()); use >= 0; use = addrLocal.getUses().nextSetBit(use + 1)) {
				replaceUses(instrMap.get(use), fromOp, toOp, addrLocal, null);
			}
			
			// and delete the LEA
			removeInst(def);
		}
		
		return changed;
	}


	/**
	 * In an instruction reads (only) a temp which is copied from a well-known memory location, 
	 * just replace the memory operand directly, if allowed.
	 * @param inst 
	 * @return
	 */
	private boolean removeStackToMemoryCopies(AsmInstruction inst) {
		
		if (inst.getOps().length != 2)
			return false;
		
		// look for register temp used by value (e.g. not inside address)
		ILocal tmpLocal = getSourceLocal(inst);
		if (!(tmpLocal instanceof RegisterLocal))
			return false;
		
		if (!(inst.getSrcOp() instanceof RegTempOperand))
			return false;
		
		// make sure single use of temp (else save time not reading memory twice)
		if (!tmpLocal.isExprTemp() || !tmpLocal.isSingleBlock()
				|| tmpLocal.getUses().cardinality() != 1
				|| tmpLocal.getDefs().cardinality() != 1
				)
			return false;
		
		// find if the definition is a memory read
		AsmInstruction def = instrMap.get(tmpLocal.getInit());
		assert def != null;
		
		AssemblerOperand mem = def.getSrcOp();
		if (mem == null || !mem.isMemory())
			return false;
		
		// uh... 
		if (mem.equals(inst.getDestOp()))
			return false;

		// okay, replace all uses of the source with the target
		AssemblerOperand fromOp = inst.getSrcOp();
		AssemblerOperand toOp = mem;

		System.out.println(here() + "In " + inst.getNumber() +":  Replacing " + fromOp + " with " + toOp);
		
		replaceUses(inst, fromOp, toOp, tmpLocal, null);
			
		// and delete the definition
		removeInst(def);
		
		return true;
	}
	/**
	 * If using a stack value, see if we have a copy in a register.
	 * @param inst 
	 * @return
	 */
	private boolean replaceStackRead(AsmInstruction inst) {
		if (inst.getInst() == Plea)
			return false;
		
		AssemblerOperand srcOp = inst.getSrcOp();
		if (srcOp == null || !srcOp.isMemory())
			return false;
		
		ILocal tmpLocal = getSourceLocal(inst);
		if (tmpLocal == null) 
			return false;
		
		// do we know the value?
		AssemblerOperand valOp = stackValues.get(srcOp);
		if (valOp == null || valOp.equals(inst.getOp2())) {
			for (Map.Entry<ILocal, AssemblerOperand> known : localValues.entrySet()) {
				if (inst.getOp1().equals(known.getValue()) && known.getKey() instanceof RegisterLocal) {
					valOp = new RegTempOperand(known.getKey().getType(), (RegisterLocal) known.getKey());
					if (!valOp.equals(inst.getOp2()))
						break;
					valOp = null;
				}
			}
		}
		if (valOp == null)
			return false;
			
		// should we only do this if it doesn't extend the lifetime
		
		AssemblerOperand fromOp = srcOp;
		AssemblerOperand toOp = valOp;

		System.out.println(here() + "In " + inst.getNumber() +":  Replacing " + fromOp + " with " + toOp);

		replaceUses(inst, fromOp, toOp, tmpLocal, null);

		// be sure to notify we're extending the lifetime  
		Set<ISymbol> copySyms = new HashSet<ISymbol>();
		AsmInstruction.getSymbolRefs(copySyms, toOp);

		for (ISymbol sym : copySyms) {
			ILocal local = locals.getLocal(sym);
			if (local != null)
				local.getUses().set(inst.getNumber());
		}
		return true;
	}


	/**
	 * If we are loading a constant value into a register, replace with LI
	 * @param instrs
	 * @param inst last MOV
	 * @return
	 */
	private boolean replaceConstant(AsmInstruction inst) {
		ILocal tmpLocal = getSourceLocal(inst);
		if (tmpLocal == null) 
			return false;
		
		AssemblerOperand valOp = localValues.get(tmpLocal);
		if (!(valOp instanceof NumberOperand))
			return false;
		
		// allow phys registers here
		ILocal origLocal = getTargetLocal(inst);
		if (!(origLocal instanceof RegisterLocal))
			return false;
		AssemblerOperand destOp = inst.getDestOp();
		if (destOp == null || !destOp.isRegister())
			return false;
		
		// substitute only if we gain anything by it:  if the tmp is used
		// elsewhere after this copy, don't replace or else we waste code
		
		// remove the tmp if it's not used elsewhere
		boolean singleUse = tmpLocal.getUses().cardinality() == 1;
		if ((inst.getInst() == Imov || inst.getInst() == Imovb) && !singleUse)
			 return false;
		if (singleUse)
			removeInst(instrMap.get(tmpLocal.getInit()));

		AssemblerOperand fromOp = inst.getSrcOp();
		AssemblerOperand toOp = valOp;
		
		System.out.println(here() + "In " + inst.getNumber() +":  Replacing " + fromOp + " with " + toOp);
		
		// make dummy replacement just to take care of the def/use accounting
		replaceUses(inst, fromOp, toOp, tmpLocal, null);

		inst.setInst(Ili);
		inst.setOp1(destOp);
		inst.setOp2(valOp);
		
		System.out.println("with\t" + inst);
		
		return true;
	}


	/**
	 * Remove instructions whose destinations are not used. 
	 * @param inst
	 * @return
	 */
	private boolean removeDeadInst(AsmInstruction inst) {
		
		Effects fx = inst.getEffects();
		if (((fx.reads | fx.writes) & ~(Instruction.INST_RSRC_ST | Instruction.INST_RSRC_WP | Instruction.INST_RSRC_PC)) != 0)
			return false;
		
		// be sure this isn't a memory write
		if (inst.getDestOp() == null || inst.getDestOp().isMemory())
			return false;
		
		ISymbol[] targets = inst.getTargets();
		if (targets.length == 0)
			return false;
		
		for (ISymbol sym : targets) {
			ILocal local = locals.getLocal(sym);
			if (local == null)
				return false;		// not a local
			
			// see if this is its last use
			if (!local.isSingleBlock())
				return false;
			
			int nextUse = local.getUses().nextSetBit(inst.getNumber() + 1);
			if (nextUse >= 0) {
				return false;
			}
		}
		
		System.out.println(here());
		removeInst(inst);
		
		// forget the source
		for (ISymbol sym : targets) {
			ILocal local = locals.getLocal(sym);
			if (local.getInit() == inst.getNumber())
				localValues.remove(local);
		}

		return true;
	}
}
