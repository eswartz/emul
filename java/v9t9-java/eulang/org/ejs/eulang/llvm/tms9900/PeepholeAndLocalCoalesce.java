/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.*;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.TypeEngine.Target;
import org.ejs.eulang.llvm.tms9900.asm.CompositePieceOperand;
import org.ejs.eulang.llvm.tms9900.asm.AsmOperand;
import org.ejs.eulang.llvm.tms9900.asm.ISymbolOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.ejs.eulang.llvm.tms9900.asm.StackLocalOperand;
import org.ejs.eulang.llvm.tms9900.asm.SymbolOperand;
import org.ejs.eulang.llvm.tms9900.asm.TupleTempOperand;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;

import static v9t9.engine.cpu.InstructionTable.*;
import static org.ejs.eulang.llvm.tms9900.InstrSelection.*;

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.Instruction.Effects;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.BinaryOperand;
import v9t9.tools.asm.assembler.operand.hl.ConstPoolRefOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;

/**
 * Iterate blocks and peephole instruction patterns introduced during instruction
 * selection.  Also, propagate known constants through the instructions, to remove
 * unnecessary copies or loads.  Also, eliminate register copies -- such as copying memory
 * into a reg temp and then placing the value back -- when they are not needed.
 * <p>
 * @author ejs
 *
 */
public class PeepholeAndLocalCoalesce extends AbstractCodeModificationVisitor {

	/** hold known values of locals when they hold numbers or stack values */
	protected Map<ILocal, AssemblerOperand> localValues;
	/** hold known values of memory when they hold numbers */
	protected Map<AssemblerOperand, NumberOperand> memNumberValues;
	/** hold known values of memory when they hold register values */
	protected Map<AssemblerOperand, RegTempOperand> memRegisterValues;
	protected HashMap<AssemblerOperand, Integer> memOpRefs;

	public PeepholeAndLocalCoalesce() {
	}
	protected void replaceUses(AsmInstruction asmInstruction, AssemblerOperand from, AssemblerOperand to, ILocal fromLocal, ILocal toLocal) {
		super.replaceUses(asmInstruction, from, to);
		updateLocalValues(from, to, fromLocal, toLocal);
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
		if (!super.enterRoutine(routine))
			return false;
		
		memOpRefs = new HashMap<AssemblerOperand, Integer>();
		for (Block block : routine.getBlocks()) {
			for (AsmInstruction instr : block.getInstrs()) {
				for (AssemblerOperand op : instr.getOps()) {
					if (op.isMemory()) {
						Integer cnt = memOpRefs.get(op);
						if (cnt == null) cnt = 1;
						else cnt = cnt + 1;
						memOpRefs.put(op, cnt);
					}
				}
			}
		}
		return true;
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
		if (block.getIdom() == null || /*block.getIdom().getDominatedChildren().size() > 1*/ block.pred().size() > 1) {
			localValues = new HashMap<ILocal, AssemblerOperand>();
			memNumberValues = new HashMap<AssemblerOperand, NumberOperand>();
			memRegisterValues = new HashMap<AssemblerOperand, RegTempOperand>();
		}
		
		List<AsmInstruction> instrs = block.getInstrs();
		int idx = 0;
		while (idx < instrs.size()) {
			AsmInstruction inst = instrs.get(idx);
			System.out.println("?" + inst.getAnnotatedString().substring(1));
			
			boolean applied = false;
			
			// apply instruction-specific peepholes
			switch (inst.getInst()) {
			case Imov:
			case Imovb:
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
				
			case Is:
			case Isb:
				if (replaceZeroSubWithNegate(inst)) {
					applied = true;
				}
				break;
				
			case Pcopy:
				if (coalesceCopy(inst)) {
					applied = true;
				} 
				break;
				
			case Pjcc:
				if (replaceIsetWithStatusJump(inst)) {
					applied = true;
				}
				break;
			}

			// apply value-specific peepholes
			if (!applied) {
				if (combineAddressOperands(inst)) {
					applied = true;
				}
				else if (removeStackToRegisterCopies(inst)) {
					applied = true;
				}
				else if (replaceMemoryWithRegisterRead(inst)) {
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
						for (Map.Entry<AssemblerOperand, RegTempOperand> entry : memRegisterValues.entrySet()) {
							System.out.println("\t" + entry.getKey() + " =\t" + entry.getValue());
						}
						for (Map.Entry<AssemblerOperand, NumberOperand> entry : memNumberValues.entrySet()) {
							System.out.println("\t" + entry.getKey() + " =\t" + entry.getValue());
						}
						System.out.println();
						
						if (replaceConstant(inst))
							applied = true;
						else 
						if (replaceMemoryReadWithConstant(inst))
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
		AssemblerOperand destOp = inst.getDestOp();
		
		// see if the source has a well-known value
		ILocal srcLocal = getSourceLocal(inst);
		AssemblerOperand src = null; 
		AssemblerOperand simplestAddr = getSimplestAddr(inst.getSrcOp());
		src = memRegisterValues.get(simplestAddr);
		if (src == null) {
			src = memNumberValues.get(simplestAddr);
			if (src == null) {
				if (srcLocal != null)
					src = localValues.get(srcLocal);
			}
		}
		
		boolean changedValues = false;
		boolean gotStackValue = false;
		
		// look for well-known instructions
		switch (inst.getInst()) {
		case Ili:
			if (inst.getOp2() instanceof NumberOperand)
				src = inst.getOp2();
			break;
		case Iai:
			if (src instanceof NumberOperand && inst.getOp2() instanceof NumberOperand) {
				int l = ((NumberOperand) src).getValue() ;
				int r = ((NumberOperand) inst.getOp2()).getValue() ;
				src = new NumberOperand( l + r );
			} else {
				src = null;
			}
			break;
		case Iandi:
			if (src instanceof NumberOperand && inst.getOp2() instanceof NumberOperand) {
				int l = ((NumberOperand) src).getValue() ;
				int r = ((NumberOperand) inst.getOp2()).getValue() ;
				src = new NumberOperand( l & r );
			} else {
				src = null;
			}
			break;
		case Iori:
			if (src instanceof NumberOperand && inst.getOp2() instanceof NumberOperand) {
				int l = ((NumberOperand) src).getValue() ;
				int r = ((NumberOperand) inst.getOp2()).getValue() ;
				src = new NumberOperand( l | r );
			} else {
				src = null;
			}
			break;
			
		case Isla:
		case Isra:
		case Isrl:
			if (src instanceof NumberOperand && inst.getOp2() instanceof NumberOperand) {
				int l = ((NumberOperand) src).getValue() ;
				int r = ((NumberOperand) inst.getOp2()).getValue() ;
				if (inst.getInst() == Isla)
					src = new NumberOperand( ( l << r ) & 0xffff );
				else if (inst.getInst() == Isra)
					src = new NumberOperand( ( l >> r ) & 0xffff );
				else /*if (inst.getInst() == Isrl)*/
					src = new NumberOperand( ( l >>> r ) & 0xffff );
			} else {
				src = null;
			}
			break;
		
		case Imov:
		case Imovb:
		case Pcopy:
			if (getSourceLocal(inst) instanceof StackLocal) {
				src = inst.getOp1();
				gotStackValue = storeMemoryValue(inst.getOp2(), inst.getOp1());
			} else if (inst.getOp1() instanceof ConstPoolRefOperand) {
				src = new NumberOperand(((ConstPoolRefOperand) inst.getOp1()).getValue());
			} else if (inst.getOp1().isMemory()) {
				gotStackValue = storeMemoryValue(inst.getOp1(), inst.getOp2());
			} else if (inst.getOp2().isMemory()) {
				gotStackValue = storeMemoryValue(inst.getOp2(), inst.getOp1());
			}
			break;
		case Ibl:
		case Iblwp:
			memNumberValues.clear();
			memRegisterValues.clear();
			src = null;
			changedValues = true;
			break;
		default:
			// dunno what to do here
			src = null;
		}
		
		ILocal targetLocal = getTargetLocal(inst);

		if (isSingleRegister(targetLocal) && (destOp == null || destOp.isRegister())) {
			if (src != null) {
				localValues.put(targetLocal, src);
				changedValues = true;
			} else {
				if (localValues.containsKey(targetLocal)) {
					localValues.remove(targetLocal);
					changedValues = true;
				} 
				if (!gotStackValue) {
					memNumberValues.values().remove(destOp);
					memRegisterValues.values().remove(destOp);
				}
			}
		} 
		
		return changedValues || gotStackValue;
		
	}

	class TupleValueStorer extends OperandDepthFirstVisitor {

		private boolean recorded;
		private final AddrOperand baseAddr;
		
		public TupleValueStorer(TypeEngine typeEngine, Target target, AddrOperand op) {
			super(typeEngine, target);
			this.baseAddr = op;
		}
		@Override
		protected void handleOperand(AssemblerOperand operand, LLType type,
				int byteOffset) {
			if (operand instanceof NumberOperand && type.getBits() <= 8) {
				operand = new NumberOperand((((NumberOperand) operand).getValue() << 8) & 0xff00);
				type = typeEngine.BYTE;	// allow BOOL/BYTE interchange
			}
			AssemblerOperand subop = new CompositePieceOperand(new NumberOperand(byteOffset), 
					baseAddr.getAddr(), type);
			recorded |= storeMemoryValue(subop, operand);
		}
		public boolean isRecorded() {
			return recorded;
		}
		
	}
	/**
	 * @param op
	 * @param simplestValue
	 * @return
	 */
	private boolean storeMemoryValue(AssemblerOperand op,
			AssemblerOperand val) {
		if (!op.isMemory())
			return false;
		op = getSimplestAddr(op);
		val = getSimplestValue(val);
		if (val instanceof NumberOperand) {
			memNumberValues.put(op, (NumberOperand) val);
			return true;
		} else if (val instanceof RegTempOperand) {
			memRegisterValues.put(getSimplestAddr(op), (RegTempOperand) val);
			return true;
		} else if (val instanceof TupleTempOperand) {
			if (op instanceof AddrOperand && ((AddrOperand) op).getAddr() instanceof StackLocalOperand) {
				StackLocal local = ((StackLocalOperand)(((AddrOperand) op).getAddr())).getLocal();
				//return storeMemoryValue(op, val, local.getType());
				TupleValueStorer storer = new TupleValueStorer(typeEngine, Target.STACK, (AddrOperand) op);
				storer.accept(val, local.getType(), 0);
				return storer.isRecorded();
			}
			return false;
		} else {
			return false;
		}
	}

	/*
	private boolean storeMemoryValue(AssemblerOperand op, AssemblerOperand val,
			LLType type) {
		if (val instanceof TupleTempOperand) {
			if (op instanceof AddrOperand && ((AddrOperand) op).getAddr() instanceof StackLocalOperand) {
				AssemblerOperand[] components = ((TupleTempOperand) val).getComponents();
				Alignment align = routine.getDefinition().getTypeEngine().new Alignment(Target.STACK);
				if (type instanceof LLAggregateType) {
					LLType[] types = ((LLAggregateType) type).getTypes();
					assert types.length == components.length;
					
					for (int i = 0; i < components.length; i++) {
						LLType compType = types[i];
						int offs = align.alignAndAdd(compType);
						assert offs % 8 == 0;
						AssemblerOperand subval = components[i];
						if (subval instanceof NumberOperand && compType.getBits() <= 8) {
							subval = new NumberOperand((((NumberOperand) subval).getValue() << 8) & 0xff00);
							compType = typeEngine.BYTE;	// allow BOOL/BYTE interchange
						}
						AssemblerOperand subop = new CompositePieceOperand(new NumberOperand(offs / 8), 
								((AddrOperand) op).getAddr(), compType);
						storeMemoryValue(subop, subval, types[i]);
					}
				} else if (type instanceof LLArrayType) {
					LLArrayType arrayType = (LLArrayType) type;
					
					for (int i = 0; i < components.length; i++) {
						int offs = align.alignAndAdd(arrayType.getSubType());
						assert offs % 8 == 0;
						AssemblerOperand subval = components[i];
						AssemblerOperand subop = new CompositePieceOperand(new NumberOperand(offs / 8), 
								((AddrOperand) op).getAddr(), arrayType.getSubType());
						storeMemoryValue(subop, subval, arrayType.getSubType());
					}
				} else {
					assert false;
				}
			}
			return true;
		} else {
			return storeMemoryValue(op, val);
		}
	}
	*/
	
	private AssemblerOperand getSimplestValue(AssemblerOperand op) {
		if (op instanceof NumberOperand)
			return op;
		AssemblerOperand val;
		val = memNumberValues.get(op);
		if (val != null)
			return val;
		val = memRegisterValues.get(op);
		if (val != null)
			return val;
		if (op.isRegister()) {
			ILocal local = getReffedLocal(op);
			if (isSingleRegister(local)) {
				val = localValues.get(local);
				if (val != null)
					return val;
			}
		}
		return op;
	}

	private AssemblerOperand getSimplestAddr(AssemblerOperand op) {
		/*
		if (op == null || !op.isMemory())
			return op;
		
		if (op instanceof AddrOperand) {
			AssemblerOperand addr = ((AddrOperand) op).getAddr();
			if (addr.isRegister()) {
				ILocal local = getReffedLocal(op);
				if (isSingleRegister(local)) {
					AssemblerOperand val = localValues.get(local);
					if (val != null) {
						op = op.replaceOperand(addr, val);
					}
				}
			}
		}
		*/
		return op;
	}

	/**
	 * Convert a MOV from memory/local to a temp, operation on that temp, and MOV back to the memory/local
	 * into an operation on the memory/local directly. 
	 * @param mov last MOV
	 * @return
	 */
	private boolean coalesceLoadOpStore(AsmInstruction mov) {
		if (mov.isPartialWrite())
			return false;
		
		// the temp for the copy must be a register
		ILocal tmpLocal = getSourceLocal(mov);
		if (!isSingleRegister(tmpLocal))
			return false;
		
		// the temp for the source must be a register if we want to substitute;
		// and avoid physical regs until coloring time to avoid conflicts
		if (mov.getOp2().isMemory())
			return false;
		ILocal origLocal = getTargetLocal(mov);
		if (!isSingleRegister(origLocal) || ((RegisterLocal) origLocal).isPhysReg())
			return false;
		
		// see if the temp is only defined once and this is its last use
		if (!tmpLocal.isExprTemp() || !tmpLocal.isSingleBlock()
				|| tmpLocal.getUses().nextSetBit(mov.getNumber() + 1) >= 0)
			return false;
		
		// and see if the definition is a read from the same target
		AsmInstruction def = instrMap.get(tmpLocal.getInit());
		assert def != null;
		
		if (def.isPartialWrite())
			return false;
		
		if (!mov.getOp1().equals(def.getDestOp()))
			return false;
		
		// okay, replace all uses of the source with the target
		AssemblerOperand fromOp = new RegTempOperand((RegisterLocal) tmpLocal);
		AssemblerOperand toOp = new RegTempOperand((RegisterLocal) origLocal);
	
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
		if (mov.getOp2().equals(def.getSrcOp()) && !dependsOnStatus(mov))
			removeInst(def);
		
		if (mov.getOp1().equals(mov.getOp2()) && !dependsOnStatus(mov))
			removeInst(mov);
		
		// maintain forced reg
		if (((RegisterLocal)tmpLocal).isPhysReg()) {
			((RegisterLocal)origLocal).setVr(((RegisterLocal) tmpLocal).getVr());
		}
		
		return changed;
	}
	
	/**
	 * Convert a COPY from a temp to another place, when that temp is only used once.
	 * @param inst last COPY
	 * @return
	 */
	private boolean coalesceCopy(AsmInstruction inst) {
		if (inst.isPartialWrite())
			return false;
		
		// the temp for the copy must be a register
		ILocal tmpLocal = getSourceLocal(inst);
		if (tmpLocal == null)
			return false;
		
		// see if the temp is only defined once and this is its last use
		if (!tmpLocal.isExprTemp() || !tmpLocal.isSingleBlock()
				|| tmpLocal.getDefs().cardinality() != 1
				|| tmpLocal.getUses().nextSetBit(inst.getNumber() + 1) >= 0)
			return false;

		// and see if the definition is a read from the same target
		AsmInstruction def = instrMap.get(tmpLocal.getInit());
		assert def != null;
		
		if (def.isPartialWrite())
			return false;
		
		ILocal origLocal = getSourceLocal(def);
		if (!inst.getOp1().equals(def.getDestOp()) || !(def.getSrcOp().isMemory() || def.getSrcOp().isRegister()))
			return false;
		
		// okay, replace all uses of the source with the target
		AssemblerOperand fromOp = inst.getOp1();
		AssemblerOperand toOp = def.getSrcOp();
		
		if (fromOp.equals(toOp))
			return false;

		// make sure the inst allows the replaced operand 
		for (int use = tmpLocal.getUses().nextSetBit(tmpLocal.getInit()); use >= 0; use = tmpLocal.getUses().nextSetBit(use + 1)) {
			AsmInstruction useInst = instrMap.get(use);
			if (useInst.getOp1() != null) {
				if (useInst.getOp1().equals(fromOp) && !useInst.supportsOp(1, toOp))
					return false;
				if (useInst.getOp2() != null) {
					if (useInst.getOp2().equals(fromOp) && !useInst.supportsOp(2, toOp))
						return false;
				}
			}
		}


		System.out.println(here() + "In " + inst.getNumber() +":  Replacing " + fromOp + " with " + toOp);
		boolean changed = false;
		
		for (int use = tmpLocal.getUses().nextSetBit(tmpLocal.getInit()); use >= 0; use = tmpLocal.getUses().nextSetBit(use + 1)) {
			replaceUses(instrMap.get(use), fromOp, toOp, tmpLocal, origLocal);
			changed = true;
		}
		
		// and delete the moves
		if (def.getOp1().equals(def.getOp2()) && !dependsOnStatus(def))
			removeInst(def);
			
		return changed;
	}
	
	/**
	 * In an instruction that uses an address operand using a register defined
	 * from LEA of a stack operand or LI of a symbol, convert to a direct reference 
	 * @param inst 
	 * @return
	 */
	private boolean combineAddressOperands(AsmInstruction inst) {
		
		boolean changed = false;
		
		for (AssemblerOperand srcOp : inst.getOps()) {
			if (!(srcOp instanceof AddrOperand) && !(srcOp instanceof RegIndOperand))
				continue;
			ILocal addrLocal = getReffedLocal(srcOp);
			if (addrLocal == null)
				continue;
			
			// the temp for the copy must be a register
			if (!isSingleRegister(addrLocal))
				continue;

			// see if the definition is an LEA on a stack local
			AsmInstruction def = instrMap.get(addrLocal.getInit());
			assert def != null;
			
			if (addrLocal.getDefs().cardinality() > 1)
				continue;
			
			AssemblerOperand toOp;
			AssemblerOperand fromOp;
			AssemblerOperand offset = null;

			ILocal origLocal = null;
			
			if (def.getInst() == Plea) {
				// and just be sure we're talking about a stack local...
				// else we want to replace LEA with STWP/AI
				if (!(def.getOp1() instanceof AddrOperand))
					continue;
				origLocal = getReffedLocal(def.getOp1());
				if (origLocal == null || !(origLocal instanceof StackLocal) && origLocal.getType().getBasicType() != BasicType.POINTER) 
					continue;
				
				toOp = ((AddrOperand)def.getOp1()).getAddr(); // new StackLocalOperand(origLocal.getType(), (StackLocal) origLocal);
				fromOp = def.getOp2();
				offset = getOperandOffset(def.getOp1());
			}
			else if (def.getInst() == Ili) {
				// and just be sure we're talking about a stack local...
				// else we want to replace LEA with STWP/AI
				if (!(def.getOp2() instanceof SymbolOperand))
					continue;
				
				toOp = def.getOp2();
				fromOp = def.getOp1();
				offset = getOperandOffset(def.getOp2());
			}
			else
				continue;
			
			// see if the temp is only defined once and this is its last use
			if (!addrLocal.isExprTemp() || !addrLocal.isSingleBlock()
					|| addrLocal.getUses().nextSetBit(inst.getNumber() + 1) >= 0)
				continue;
		
			// okay, replace all uses of the source with the target
			
			System.out.println(here() + "In " + inst.getNumber() +":  Replacing " + fromOp + " with " + toOp);
			
			changed = true;
			for (int use = addrLocal.getUses().nextSetBit(addrLocal.getInit()); use >= 0; use = addrLocal.getUses().nextSetBit(use + 1)) {
				replaceAddrUses(instrMap.get(use), (RegTempOperand) fromOp, toOp, offset, addrLocal, origLocal);
			}
			
			// and delete the LEA/LI
			removeInst(def);
		}
		
		return changed;
	}
	private AssemblerOperand getOperandOffset(AssemblerOperand op) {
		AssemblerOperand offset = null;
		if (op instanceof CompositePieceOperand)
			offset = ((CompositePieceOperand) op).getOffset();
		else if (op instanceof RegOffsOperand)
			offset = ((RegOffsOperand) op).getAddr();
		else if (op instanceof BinaryOperand) {
			if (((BinaryOperand) op).getKind() == '+')
				offset = ((BinaryOperand) op).getRight();
			else
				assert false;
		}
		else if (op instanceof RegIndOperand || op instanceof AddrOperand) {
			// ok
		}
		else if (op.isMemory())
			assert false;
		return offset;
	}

	/**
	 * @param asmInstruction
	 * @param from
	 * @param toOp
	 */
	private void replaceAddrUses(AsmInstruction asmInstruction, RegTempOperand from, AssemblerOperand toOp, AssemblerOperand offset, ILocal fromLocal, ILocal toLocal) {
		assert asmInstruction != null;
		AssemblerOperand[] ops = asmInstruction.getOps();
		emitChangePrefix(asmInstruction);
		
		LLType theType = typeEngine.getIntType(asmInstruction.getInst() == Imovb ? 8 : 16);
		for (int idx = 0; idx < ops.length; idx++) {
			
			AssemblerOperand op = ops[idx];
			AssemblerOperand newOp = op;
			
			// MOV @>0002(vr), ...
			AssemblerOperand origOffset = null;
			
			if (op instanceof CompositePieceOperand) {
				origOffset = ((CompositePieceOperand) op).getOffset();
				op = ((CompositePieceOperand) op).getAddr();
			} else if (op instanceof RegOffsOperand) {
				origOffset = ((RegOffsOperand) op).getAddr();
				op = ((RegOffsOperand) op).getReg();
			} else if (op instanceof RegIndOperand) {
				op = ((RegIndOperand) op).getReg();
			}
			
			if (op.equals(from)) {
				AssemblerOperand newOffset = addOffsets(offset, origOffset);
				if (newOffset == null)
					newOp = InstrSelection.ensurePiecewiseAccess(new AddrOperand(toOp), theType);
				else {
					newOp = new CompositePieceOperand(newOffset, toOp, theType);
				}
			}
			
			if (newOp != op) {
				//updateLocalUsage(asmInstruction, fromLocal, null, op);
				//if (toLocal != null)
				//	toLocal.getUses().set(asmInstruction.getNumber());
				
				// update op
				updateOperandUsage(asmInstruction, op, false);
				newOp = InstrSelection.ensurePiecewiseAccess(newOp, theType);
				asmInstruction.setOp(idx + 1, newOp);
				updateOperandUsage(asmInstruction, newOp, true);
			}
		}
		updateLocalValues(from, toOp, fromLocal, toLocal);

		emitChangeSuffix(asmInstruction);
	}

	/**
	 * @param offset
	 * @param offset2
	 * @return
	 */
	private AssemblerOperand addOffsets(AssemblerOperand off1, AssemblerOperand off2) {
		if (off1 == null)
			return off2;
		if (off2 == null)
			return off1;
		
		if (off2 instanceof ISymbolOperand) {
			AssemblerOperand t = off1;
			off1 = off2;
			off2 = t;
		}
		if (off1 instanceof NumberOperand && off2 instanceof NumberOperand) {
			return new NumberOperand(((NumberOperand) off1).getValue() + ((NumberOperand) off2).getValue());
		}
		if (off1 instanceof ISymbolOperand && off2 instanceof NumberOperand) {
			return new BinaryOperand('+', off1, off2);
		}
		assert false;
		return null;
	}

	private void updateLocalValues(AssemblerOperand from, AssemblerOperand to,
			ILocal fromLocal, ILocal toLocal) {
		if (localValues.containsKey(fromLocal)) {
			if (toLocal != null)
				localValues.put(toLocal, localValues.get(fromLocal));
			localValues.remove(fromLocal);
		}
		
		// update stored numbers
		from = getSimplestAddr(from);
		NumberOperand fromVal = memNumberValues.get(from);
		if (fromVal != null && to.isMemory())
			memNumberValues.put(to, fromVal);
		for (Iterator<Map.Entry<AssemblerOperand, RegTempOperand>> iter = memRegisterValues.entrySet().iterator(); iter.hasNext();) {
			Map.Entry<AssemblerOperand, RegTempOperand> entry = iter.next();
			if (entry.getValue().equals(from)) {
				if (to instanceof RegTempOperand)
					entry.setValue((RegTempOperand) to);
				else
					iter.remove();
			}
			else if (fromLocal != null && fromLocal.equals(getReffedLocal(entry.getKey()))) {
				// don't update
				iter.remove();
			}
		}
	}

	/**
	 * In an instruction reads (only) a temp which is copied from a well-known memory location, 
	 * and that memory location is only read once, just replace the memory operand directly, if allowed.
	 * @param inst 
	 * @return
	 */
	private boolean removeStackToRegisterCopies(AsmInstruction inst) {
		
		if (inst.getOps().length != 2)
			return false;
		
		// look for register temp used by value (e.g. not inside address)
		ILocal tmpLocal = getSourceLocal(inst);
		if (!isSingleRegister(tmpLocal))
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
		
		Integer cnt = memOpRefs.get(mem);
		if (cnt != null && cnt > 2) 
			return false;
		
		// uh... 
		if (mem.equals(inst.getDestOp()))
			return false;

		// okay, replace all uses of the source with the target
		AssemblerOperand fromOp = inst.getSrcOp();
		AssemblerOperand toOp = mem;

		System.out.println(here() + "In " + inst.getNumber() +":  Replacing " + fromOp + " with " + toOp);
		
		replaceUses(inst, fromOp, toOp, tmpLocal, getReffedLocal(toOp));
			
		// and delete the definition
		if (!dependsOnStatus(def))
			removeInst(def);
		
		return true;
	}
	/**
	 * If using a stack value, see if we have a copy in a register.
	 * @param inst 
	 * @return
	 */
	private boolean replaceMemoryWithRegisterRead(AsmInstruction inst) {
		if (inst.getInst() == Plea)
			return false;
		
		AssemblerOperand srcOp = inst.getSrcOp();
		if (srcOp == null || !srcOp.isMemory())
			return false;
		
		ILocal tmpLocal = getSourceLocal(inst);
		if (tmpLocal == null) 
			return false;
		
		// do we know the value?
		AssemblerOperand destOp = inst.getDestOp();
		AssemblerOperand valOp;
		
		srcOp = getSimplestAddr(srcOp);
		valOp = memRegisterValues.get(srcOp);
		if (valOp == null || valOp.equals(srcOp)) {
			for (Map.Entry<ILocal, AssemblerOperand> known : localValues.entrySet()) {
				if (inst.getOp1().equals(known.getValue()) && isSingleRegister(known.getKey())) {
					valOp = new RegTempOperand((RegisterLocal) known.getKey());
					if (!valOp.equals(destOp))
						break;
					valOp = null;
				}
			}
		}
		if (valOp == null)
			return false;
		
		if (valOp instanceof NumberOperand)
			return false;
		
		// TODO: should we only do this if it doesn't extend the lifetime?
		
		AssemblerOperand fromOp = srcOp;
		AssemblerOperand toOp = valOp;

		System.out.println(here() + "In " + inst.getNumber() +":  Replacing " + fromOp + " with " + toOp);

		replaceUses(inst, fromOp, toOp, tmpLocal, null);

		// be sure to notify we're extending the lifetime  
		Set<ISymbol> copySyms = new HashSet<ISymbol>();
		AsmInstruction.getSymbolRefs(copySyms, toOp);

		for (ISymbol sym : copySyms) {
			ILocal local = stackFrame.getLocal(sym);
			if (local != null)
				local.getUses().set(inst.getNumber());
		}
		return true;
	}

	/**
	 * If we are loading a constant value into a register from memory, replace with LI
	 * @param instrs
	 * @param inst last MOV
	 * @return
	 */
	private boolean replaceMemoryReadWithConstant(AsmInstruction inst) {
		
		AssemblerOperand valOp = memNumberValues.get(inst.getSrcOp());
		
		if (!(valOp instanceof NumberOperand))
			return false;
		
		// allow phys registers here
		ILocal origLocal = getTargetLocal(inst);
		if (!isSingleRegister(origLocal))
			return false;
		AssemblerOperand destOp = inst.getDestOp();
		if (destOp == null || !destOp.isRegister() || !(destOp instanceof AsmOperand))
			return false;
		
		AssemblerOperand fromOp = inst.getSrcOp();
		AssemblerOperand toOp = valOp;
		
		// make sure the inst allows the replaced operand 
		int newInst;
		switch (inst.getInst()) {
		case Imov:
		case Imovb:
		case Pcopy:
			newInst = Ili;
			break;
		case Ia:
		case Iab:
			if (!inst.getOp2().isRegister())
				return false;
			newInst = Iai;
			break;
		case Isoc:
		case Isocb:
			if (!inst.getOp2().isRegister())
				return false;
			newInst = Iori;
			break;
		case Iszc:
		case Iszcb:
			if (!inst.getOp2().isRegister())
				return false;
			newInst = Iandi;
			break;
		default:
			return false;
		}

		System.out.println(here() + "In " + inst.getNumber() +":  Replacing " + fromOp + " with " + toOp);
		
		replaceUses(inst, fromOp, toOp, null, null);

		
		//if (inst.getInst() == Imovb)
		//if (inst.getInst() == Imovb)
		//	valOp = new NumOperand(this.routine.getDefinition().getTypeEngine().INT, (((NumberOperand) valOp).getValue() << 8) & 0xff00);
		
		inst.setInst(newInst);
		inst.setOp1(destOp);
		inst.setOp2(valOp);
		
		System.out.println("with\t" + inst);
		
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
		if (tmpLocal == null || !isSingleRegister(tmpLocal)) 
			return false;
		
		AssemblerOperand valOp = localValues.get(tmpLocal);
		if (valOp == null)
			valOp = memNumberValues.get(getSimplestAddr(inst.getSrcOp()));
		
		if (!(valOp instanceof NumberOperand))
			return false;
		
		// allow phys registers here
		ILocal origLocal = getTargetLocal(inst);
		if (!isSingleRegister(origLocal))
			return false;
		AssemblerOperand destOp = inst.getDestOp();
		if (destOp == null || !destOp.isRegister())
			return false;
		
		// substitute only if we gain anything by it:  if the tmp is used
		// elsewhere after this copy, don't replace or else we waste code
		
		// remove the tmp if it's not used elsewhere
		boolean singleUse = tmpLocal.getUses().cardinality() == 1;
		if (inst.getInst() == Imov || inst.getInst() == Imovb) {
			if (!singleUse)
				return false;
		} else {
			// else, only replace in an inst where we're modifying a value
			if (!origLocal.equals(tmpLocal))
				return false;
		}
		
		AsmInstruction initInstr = instrMap.get(tmpLocal.getInit());
		if (singleUse && !dependsOnStatus(initInstr))
			removeInst(initInstr);
		
		// but, if another register holds this value, use the reg
		for (Map.Entry<ILocal, AssemblerOperand> entry : localValues.entrySet()) {
			if (entry.getKey() instanceof RegisterLocal
					&& !entry.getKey().equals(origLocal) 
					&& !entry.getKey().equals(tmpLocal) 
					&& entry.getValue().equals(valOp)) {

				AssemblerOperand fromOp = inst.getSrcOp();
				AssemblerOperand toOp = new RegTempOperand((RegisterLocal) entry.getKey());
				
				System.out.println(here() + "In " + inst.getNumber() +":  Replacing " + fromOp + " with " + toOp);
				
				// make dummy replacement just to take care of the def/use accounting
				replaceUses(inst, fromOp, toOp, tmpLocal, entry.getKey());

				System.out.println("with\t" + inst);
				
				return true;
			}
		}
		
		AssemblerOperand fromOp = inst.getSrcOp();
		AssemblerOperand toOp = valOp;
		
		System.out.println(here() + "In " + inst.getNumber() +":  Replacing " + fromOp + " with " + toOp);
		
		// make dummy replacement just to take care of the def/use accounting
		//replaceUses(inst, fromOp, toOp, tmpLocal, null);

		updateOperandUsage(inst, inst.getOp1(), false);
		updateOperandUsage(inst, inst.getOp2(), false);
		inst.setInst(Ili);
		inst.setOp1(destOp);
		inst.setOp2(valOp);
		updateOperandUsage(inst, destOp, true);
		updateOperandUsage(inst, valOp, true);
		
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
		if (dependsOnStatus(inst))
			return false;
		
		// be sure this isn't an unknown memory write
		AssemblerOperand destOp = inst.getDestOp();
		if (destOp == null)
			return false;
		
		if (destOp.isMemory()) {
			if (destOp instanceof StackLocalOperand ||
					(destOp instanceof AddrOperand && ((AddrOperand) destOp).getAddr() instanceof StackLocalOperand)) {
				// okay, since this is known
			} else {
				return false;
			}
		}
		
		ISymbol[] targets = inst.getTargets();
		if (targets.length == 0)
			return false;
		
		for (ISymbol sym : targets) {
			ILocal local = stackFrame.getLocal(sym);
			if (local == null)
				return false;		// not a local
			
			// see if this is its last use
			if (!local.isSingleBlock())
				return false;
			
			int nextUse = local.getUses().nextSetBit(inst.getNumber() + 1);
			int nextDef = local.getDefs().nextSetBit(inst.getNumber() + 1);
			if (nextDef >= 0) {
				// if not defined in the same block, ignore
				if (instrBlockMap.get(inst.getNumber()) != instrBlockMap.get(nextDef))
					return false;
			}
			
			if (nextUse >= 0 && !(nextDef >= 0 && nextUse > nextDef)) {
				return false;
			}
		}
		
		System.out.println(here());
		removeInst(inst);
		
		// forget the source
		for (ISymbol sym : targets) {
			ILocal local = stackFrame.getLocal(sym);
			if (local.getInit() == inst.getNumber())
				localValues.remove(local);
		}

		return true;
	}
	
	/**
	 * Change JCC to use a compare operand instead of a temp, if the bool value
	 * is not used.
	 * @param inst
	 * @return
	 */
	private boolean replaceIsetWithStatusJump(AsmInstruction inst) {

		// Note: we don't flatten JCC here, because 
		// we can't convert all kinds of jumps into instructions (no JGT+EQ or JLT+EQ),
		// meaning we'd have to introduce new jumps and break blocks into little pieces.
		// Finally, we may want to modify the jumps again later to handle short/long
		// jumping.
		// All that should be done when flattening, not here.
		
		// the first one should be the variable
		ILocal local = getSourceLocal(inst);
		if (local == null)
			return false;
		
		if (local.getUses().cardinality() > 1)
			return false;
		
		AsmInstruction def = instrMap.get(local.getInit());
		assert def != null;
		
		ISymbol statusSym = getStatusSymbol();
		
		// did this boolean came from ISET?
		if (def.getInst() == Piset) {
			// assume (!) the previous instruction generated the CC
			List<AsmInstruction> list = instrBlockMap.get(inst.getNumber()).getInstrs();
			AsmInstruction ccinst = list.get(list.indexOf(def) - 1);
			
			// comparison defines status reg now
			assert ccinst.getTargets().length == 0;
			ccinst.setImplicitTargets(new ISymbol[] { statusSym });
			
			// jump uses the status
			inst.setOp1(def.getOp1());
			inst.setImplicitSources(new ISymbol[] { statusSym, ((ISymbolOperand) inst.getOp2()).getSymbol(), 
					((ISymbolOperand) inst.getOp3()).getSymbol() });
	
			// no more ISET
			removeInst(def);
			
			return true;
		}
		
		return false;
		
	}

	/**
	 * Convert the LLVM-required "clr" + "s" back to NEG. 
	 * @param mov last S
	 * @return
	 */
	private boolean replaceZeroSubWithNegate(AsmInstruction sub) {
		// the temp for the copy must be a register
		ILocal tmpLocal = getTargetLocal(sub);
		if (!isSingleRegister(tmpLocal) || !(sub.getOp2().isRegister()))
			return false;

		// find def, which should be previous
		AsmInstruction clr = instrMap.get(tmpLocal.getInit());
		assert clr != null;
		if (clr.getInst() != Iclr)
			return false;
		
		int nextDef = tmpLocal.getDefs().nextSetBit(tmpLocal.getInit() + 1); 
		if (tmpLocal.getUses().nextSetBit(tmpLocal.getInit() + 1) != sub.getNumber()
				|| (nextDef != -1 && nextDef < sub.getNumber()))
			return false;

		System.out.println(here() + "In " + sub.getNumber() +":  Replacing CLR/S with NEG");

		emitChangePrefix(clr);
		
		updateOperandUsage(clr, clr.getOp1(), false);
		updateOperandUsage(clr, sub.getOp1(), false);
		
		clr.setInst(sub.getInst() == Isb ? Imovb : Imov);
		clr.setOp2(clr.getOp1());
		clr.setOp1(sub.getOp1());
		
		updateOperandUsage(clr, clr.getOp1(), true);
		updateOperandUsage(clr, sub.getOp1(), true);
		

		emitChangeSuffix(clr);

		emitChangePrefix(sub);
		
		updateOperandUsage(sub, sub.getOp1(), false);
		updateOperandUsage(sub, sub.getOp2(), false);
		
		sub.setInst(Ineg);
		sub.setOp1(sub.getOp2());
		sub.setOp2(null);
		
		updateOperandUsage(sub, sub.getOp1(), true);
		
		emitChangeSuffix(sub);
		
		return true;
	}
}
