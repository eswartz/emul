/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.*;

import org.ejs.coffee.core.utils.Pair;
import org.ejs.eulang.llvm.tms9900.asm.AsmOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.ejs.eulang.llvm.tms9900.asm.SymbolOperand;
import org.ejs.eulang.types.BasicType;
import org.ejs.eulang.types.LLType;

import static org.ejs.eulang.llvm.tms9900.InstrSelection.*;

import v9t9.engine.cpu.Inst9900;
import v9t9.tools.asm.assembler.IInstructionFactory;
import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;

/**
 * Iterate blocks and peephole instruction patterns introduced during instruction
 * selection.  Also, propagate known constants through the instructions, to remove
 * unnecessary copies or loads.  Also, eliminate register copies -- such as copying memory
 * into a reg temp and then placing the value back -- when they are not needed.
 * <p>
 * @author ejs
 *
 */
public class PeepholeAndLocalCoalesce9900 extends PeepholeAndLocalCoalesce {

	public PeepholeAndLocalCoalesce9900(IInstructionFactory instructionFactory) {
		super(instructionFactory);
	}
	/**
	 * @param inst
	 * @return
	 */
	@Override
	protected boolean applyTargetValuePropagation(AsmInstruction inst) {
		boolean applied = false;
		if (replaceConstant(inst))
			applied = true;
		else 
		if (replaceMemoryReadWithConstant(inst))
			applied = true;
		return applied;
	}
	/**
	 * @param inst
	 * @return
	 */
	@Override
	protected boolean applyTargetPeephole(AsmInstruction inst) {
		boolean applied = false;
		switch (inst.getInst()) {
		case Inst9900.Imov:
		case Inst9900.Imovb:
			if (coalesceLoadOpStore(inst)) {
				applied = true;
			} 
			else if (coalesceCopy(inst)) {
				applied = true;
			} 
			//else if (coalesceCopy2(inst)) {
			//	applied = true;
			//} 
			else if (replaceConstant(inst)) {
				applied = true;
			}
			break;
			
		case Inst9900.Is:
		case Inst9900.Isb:
			if (replaceZeroSubWithNegate(inst)) {
				applied = true;
			}
			break;
			
		case Inst9900.Isla:
		case Inst9900.Isrl:
		case Inst9900.Isra:
		case Inst9900.Isrc:
			if (removeDeadShift(inst)) {
				applied = true;
			}
			break;
		}
		return applied;
	}
	/**
	 * @param inst
	 * @return
	 */
	private boolean removeDeadShift(AsmInstruction inst) {
		if (inst.getOp2() instanceof NumberOperand &&
				(((NumberOperand) inst.getOp2()).getValue() & 0xf) == 0) {
			System.out.println(here());
			removeInst(inst);
			return true;
		}
		return false;
	}
	

	/**
	 * Convert the LLVM-required "clr" + "s" back to NEG. 
	 * @param mov last S
	 * @return
	 */
	protected boolean replaceZeroSubWithNegate(AsmInstruction sub) {
		// the temp for the copy must be a register
		ILocal tmpLocal = getTargetLocal(sub);
		if (!isSingleRegister(tmpLocal) || !(sub.getOp2().isRegister()))
			return false;
	
		// find def, which should be previous
		AsmInstruction clr = instrMap.get(tmpLocal.getInit());
		assert clr != null;
		if (clr.getInst() != Inst9900.Iclr)
			return false;
		
		int nextDef = tmpLocal.getDefs().nextSetBit(tmpLocal.getInit() + 1); 
		if (tmpLocal.getUses().nextSetBit(tmpLocal.getInit() + 1) != sub.getNumber()
				|| (nextDef != -1 && nextDef < sub.getNumber()))
			return false;
	
		System.out.println(here() + "In " + sub.getNumber() +":  Replacing CLR/S with NEG");
	
		emitChangePrefix(clr);
		
		updateOperandUsage(clr, clr.getOp1(), false);
		updateOperandUsage(clr, sub.getOp1(), false);
		
		clr.setInst(sub.getInst() == Inst9900.Isb ? Inst9900.Imovb : Inst9900.Imov);
		clr.setOp2(clr.getOp1());
		clr.setOp1(sub.getOp1());
		
		updateOperandUsage(clr, clr.getOp1(), true);
		updateOperandUsage(clr, sub.getOp1(), true);
		
	
		emitChangeSuffix(clr);
	
		emitChangePrefix(sub);
		
		updateOperandUsage(sub, sub.getOp1(), false);
		updateOperandUsage(sub, sub.getOp2(), false);
		
		sub.setInst(Inst9900.Ineg);
		sub.setOp1(sub.getOp2());
		sub.setOp2(null);
		
		updateOperandUsage(sub, sub.getOp1(), true);
		
		emitChangeSuffix(sub);
		
		return true;
	}

	
	/**
	 * @param inst
	 * @param src
	 * @return
	 */
	@Override
	protected Pair<Boolean, AssemblerOperand> trackTargetInstructionValues(
			AsmInstruction inst, AssemblerOperand src) {
		switch (inst.getInst()) {
		case Inst9900.Ili:
			if (inst.getOp2() instanceof NumberOperand)
				src = inst.getOp2();
			break;
		case Inst9900.Iai:
			if (src instanceof NumberOperand && inst.getOp2() instanceof NumberOperand) {
				int l = ((NumberOperand) src).getValue() ;
				int r = ((NumberOperand) inst.getOp2()).getValue() ;
				src = new NumberOperand( l + r );
			} else {
				src = null;
			}
			break;
		case Inst9900.Iandi:
			if (src instanceof NumberOperand && inst.getOp2() instanceof NumberOperand) {
				int l = ((NumberOperand) src).getValue() ;
				int r = ((NumberOperand) inst.getOp2()).getValue() ;
				src = new NumberOperand( l & r );
			} else {
				src = null;
			}
			break;
		case Inst9900.Iori:
			if (src instanceof NumberOperand && inst.getOp2() instanceof NumberOperand) {
				int l = ((NumberOperand) src).getValue() ;
				int r = ((NumberOperand) inst.getOp2()).getValue() ;
				src = new NumberOperand( l | r );
			} else {
				src = null;
			}
			break;
			
		case Inst9900.Isla:
		case Inst9900.Isra:
		case Inst9900.Isrl:
			if (src instanceof NumberOperand && inst.getOp2() instanceof NumberOperand) {
				int l = ((NumberOperand) src).getValue() ;
				int r = ((NumberOperand) inst.getOp2()).getValue() ;
				if (inst.getInst() == Inst9900.Isla)
					src = new NumberOperand( ( l << r ) & 0xffff );
				else if (inst.getInst() == Inst9900.Isra)
					src = new NumberOperand( ( l >> r ) & 0xffff );
				else /*if (inst.getInst() == Isrl)*/
					src = new NumberOperand( ( l >>> r ) & 0xffff );
			} else if (inst.getOp2() instanceof NumberOperand &&
					(((NumberOperand) inst.getOp2()).getValue() & 0xf) == 0) {
				// no-op
			} else {
				src = null;
			}
			break;
		
		default:
			return new Pair<Boolean, AssemblerOperand>(false, null);
		}
		
		return new Pair<Boolean, AssemblerOperand>(true, src);
	}
	
	@Override
	protected boolean isTargetMoveInstruction(AsmInstruction inst) {
		return inst.getInst() == Inst9900.Imov || inst.getInst() == Inst9900.Imovb;
	}
	@Override
	protected boolean isTargetCallInstruction(AsmInstruction inst) {
		return inst.getInst() == Inst9900.Ibl || inst.getInst() == Inst9900.Iblwp;
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
		case Inst9900.Imov:
		case Inst9900.Imovb:
		case Pcopy:
			newInst = Inst9900.Ili;
			break;
		case Inst9900.Ia:
		case Inst9900.Iab:
			if (!inst.getOp2().isRegister())
				return false;
			newInst = Inst9900.Iai;
			break;
		case Inst9900.Isoc:
		case Inst9900.Isocb:
			if (!inst.getOp2().isRegister())
				return false;
			newInst = Inst9900.Iori;
			break;
		case Inst9900.Iszc:
		case Inst9900.Iszcb:
			if (!inst.getOp2().isRegister())
				return false;
			newInst = Inst9900.Iandi;
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
		if (isTargetMoveInstruction(inst)) {
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
		inst.setInst(Inst9900.Ili);
		inst.setOp1(destOp);
		inst.setOp2(valOp);
		updateOperandUsage(inst, destOp, true);
		updateOperandUsage(inst, valOp, true);
		
		System.out.println("with\t" + inst);
		
		return true;
	}
	
	/**
	 * In an instruction that uses an address operand using a register defined
	 * from LEA of a stack operand or LI of a symbol, convert to a direct reference 
	 * @param inst 
	 * @return
	 */
	protected boolean combineTargetAddressOperands(AsmInstruction inst) {
		
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
			else if (def.getInst() == Inst9900.Ili) {
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
				AsmInstruction asmInstruction = instrMap.get(use);
				LLType theType = typeEngine.getIntType(asmInstruction.getInst() == Inst9900.Imovb ? 8 : 16);
				replaceAddrUses(asmInstruction, (RegTempOperand) fromOp, toOp, offset, addrLocal, origLocal, theType);
			}
			
			// and delete the LEA/LI
			if (addrLocal.getUses().isEmpty())
				removeInst(def);
		}
		
		return changed;
	}

}
