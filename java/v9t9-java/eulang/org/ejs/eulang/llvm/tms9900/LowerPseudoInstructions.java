/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.*;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.TypeEngine.Alignment;
import org.ejs.eulang.TypeEngine.Target;
import org.ejs.eulang.llvm.tms9900.asm.CompareOperand;
import org.ejs.eulang.llvm.tms9900.asm.CompositePieceOperand;
import org.ejs.eulang.llvm.tms9900.asm.ISymbolOperand;
import org.ejs.eulang.llvm.tms9900.asm.RegTempOperand;
import org.ejs.eulang.llvm.tms9900.asm.SymbolLabelOperand;
import org.ejs.eulang.llvm.tms9900.asm.TupleTempOperand;
import org.ejs.eulang.symbols.ISymbol;
import org.ejs.eulang.types.LLAggregateType;
import org.ejs.eulang.types.LLType;

import static v9t9.engine.cpu.InstructionTable.*;
import static org.ejs.eulang.llvm.tms9900.InstrSelection.*;

import v9t9.tools.asm.assembler.operand.hl.AddrOperand;
import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;
import v9t9.tools.asm.assembler.operand.hl.NumberOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIncOperand;
import v9t9.tools.asm.assembler.operand.hl.RegIndOperand;
import v9t9.tools.asm.assembler.operand.hl.RegOffsOperand;

/**
 * Change pseudo-instructions into actual instructions.  This should be run after
 * one initial peephole phase (which might remove them entirely) and before
 * register allocation or coloring, since it may introduce temp registers for 
 * holding immediates or for looping.
 * <p>
 * This phase will likely increase code size and register pressure.
 * @author ejs
 *
 */
public class LowerPseudoInstructions extends AbstractCodeModificationVisitor {
	protected boolean changedBlocks;
	
	public LowerPseudoInstructions() {
	}
	
	public boolean changedBlocks() {
		return changedBlocks;
	}
	
	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.ICodeVisitor#getWalk()
	 */
	@Override
	public Walk getWalk() {
		return Walk.DOMINATOR_PATHS;
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
			System.out.println(inst);
			
			boolean applied = false;
			
			switch (inst.getInst()) {
			case Pjcc:
				if (ensureStatusForJump(inst)) {
					applied = true;
				}
				break;
			case Pcopy:
				if (expandCopy(inst)) {
					applied = true;
				}
				break;
			}

			if (applied) {
				changed = true;
				System.out.println();
				throw new Terminate();		// start over due to new instructions
			}
			idx++;
			
		}			
		return false;
	}

	/**
	 * Change JCC to use explicit status setters and comparisons (if needed) to
	 * establish status flags jumping on boolean variables. We don't fully
	 * flatten, though -- instructions do not exist to represent every possible
	 * condition.  Also, we maintain the LLVM pattern that we have both the
	 * true and false targets present.
	 * 
	 * @param inst
	 * @return
	 */
	private boolean ensureStatusForJump(AsmInstruction inst) {
		if (inst.getOp1() instanceof CompareOperand)
			return false;
		
		AssemblerOperand bool = inst.getOp1();
		
		ISymbol statusSym = getStatusSymbol();
		
		// add CB x,x to test 
		AsmInstruction ccinst = AsmInstruction.create(Icb, bool, bool);
		ccinst.setImplicitTargets(new ISymbol[] { statusSym });
		
		instrBlockMap.get(inst.getNumber()).addInstBefore(ccinst, inst);
		
		System.out.println(here() + " Added " + ccinst);
		
		// jump uses the status
		inst.setOp1(new CompareOperand("ne"));
		inst.setImplicitSources(new ISymbol[] { statusSym, ((ISymbolOperand) inst.getOp2()).getSymbol(), 
				((ISymbolOperand) inst.getOp3()).getSymbol() });
	
		return true;
	}
	
	/**
	 * Expand a COPY instruction.  If it deals with constant values
	 * and is big enough, make an external data block for it, and
	 * copy its contents in using a loop.  Otherwise, copy it piecewise.  
	 * <pre>
	 		
	basic copy loop:
	
	4/6		LEA addr1, t1		e.g. MOV SP, t1 / AI t1, >20  (28 cycles) or LI t1, symbol (14 cycles)	 
	4/6		LEA addr2, t2
	4		LI t3, size					// 14 cycles
		loop:
	2		MOV *t1+, *t2+				// 14 + 6 + 6 cycles
	2		DECT T3						// 10
	2		JGT loop					// 8
	
	min: 18, max: 22		plus possibility of spilling 3 registers
	cycles: 42 + 44*K
	
	basic copy loop (global-local):
	
	2		MOV SP, t1					// 14 cycles
	4		AI t1, K 					// 14 cycles
	2		CLR t3						// 10 cycles
		loop:
	4		MOV @global(t3), *t1+		// 14 + 8 + 6 cycles
	2		INCT T3						// 10 cycles
	4		CI T3, N					// 14 cycles
	2		JL loop						// 8 cycles
	
	min: 20					plus possibility of spilling 2 registers
	cycles:  38 + 60*K
	
	piecewise-copy (global-global):
	
	6		MOV @global1(t1), @global2(t2)
	6		MOV @global1+2(t1), @global2+2(t2)
	6		MOV @global1+4(t1), @global2+4(t2)		// 14 + 8 + 8 = 30 each 
	
	piecewise-copy (local-local):
	
	6		MOV @K(SP), @L(SP)
	6		MOV @K+2(SP), @L+2(SP)
	6		MOV @K+4(SP), @L+4(SP)
	
	piecewise-copy (global-local):
	
	6		MOV @global, @K(SP)
	6		MOV @global+2, @K+2(SP)
	6		MOV @global+4, @K+4(SP)			
	
	external copy routine:
	
	4/6		LEA addr1, R0		e.g. MOV SP, t1 / AI t1, >20  or LI t1, symbol	 
	4/6		LEA addr2, R1
	4		LI R2, size
	4		BL @copyThis
	
	min size: 16, max size: 20		plus register spilling costs
	 </pre>
	 * Piecewise:  30 * N<br>
	 * Loop: 42 + 44*N<br>  
	 * <p>
	 * Best:  30*N >= 42 + 44*N<br>
	 * 		  -42 >= 14*N<br>
	 *			N <= -3  (duh: piecewise is always faster)<br> 
	 * <p>
	 * See where it gets twice as slow:<br>
	 * 		  30*N*2 >= 42+44*N<br>
	 * 		  16*N >= 42<br>
	 * 	      N >= ~3<br>
	 * <p>
	 * Given this, it seems like piecewise copying is size-optimal for 3 iters (6 bytes) 
	 * but always speed-optimal.  Any loop will be slower, and the first loop we generate
	 * (for 4 iters) will be twice as slow!     
	 * @param inst
	 * @return
	 */
	private boolean expandCopy(AsmInstruction inst) {
		LLType type = inst.getType();
		
		if (type == null)
			assert false;

		/** # of ops */
		final int THRESHOLD = 4;
		
		AssemblerOperand from = inst.getOp1();
		
		// 16-bit copies
		int work = type.getBits() / 8 / 2;
		
		boolean isTuple = from instanceof TupleTempOperand;
		boolean isConstTuple = isTuple && ((TupleTempOperand) from).isConst();
		if (isConstTuple) {
			work = calculateConstTupleWork((TupleTempOperand) from);
		}
		
		if (work >= THRESHOLD && (!isTuple || isConstTuple)) {
			if (isConstTuple) {
				from = makeInternalData(inst.getOp1());
			}
			expandCopyLoop(type, inst, from);
		} else {
			if (isTuple) {
				expandCopyTuplePiecewise(type, inst, (TupleTempOperand) from);
			} else {
				expandCopyPiecewise(type, inst, from);
			}
		}
	
		removeInst(inst);
		
		return true;
	}
	
	/**
	 * Figure out how much work is needed to copy the tuple (which
	 * consists of constant values).  We need to copy each piece
	 * in a type-safe way to maximize optimization. 
	 * @param from
	 * @return approximate number of operations
	 */
	private int calculateConstTupleWork(TupleTempOperand from) {
		LLType[] types = ((LLAggregateType) from).getTypes();
		int sum = 0;
		for (LLType sub : types) {
			if (sub.getBits() <= 16)
				sum += 2;						// LI and MOV
			else 
				sum += sub.getBits() / 16;		// maybe judge MORE because these are new copy insts
		}
		
		return sum;
	}
	
	/**
	 * @param op1
	 * @return
	 */
	private AssemblerOperand makeInternalData(AssemblerOperand op1) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * <pre>
	 basic copy loop:
	
	4/6		LEA addr1, t1		e.g. MOV SP, t1 / AI t1, >20  (28 cycles) or LI t1, symbol (14 cycles)	 
	4/6		LEA addr2, t2
	4		LI t3, size					// 14 cycles
		loop:
	2		MOV *t1+, *t2+				// 14 + 6 + 6 cycles
	2		DECT T3						// 10
	2		JGT loop					// 8
	</pre>
	 */
	private void expandCopyLoop(LLType type, AsmInstruction inst, AssemblerOperand from) {
		assert from instanceof RegIndOperand || from instanceof AddrOperand;
		AssemblerOperand to = inst.getOp2();
		assert to instanceof RegIndOperand || to instanceof AddrOperand || to instanceof RegOffsOperand;
		
		TypeEngine typeEngine = routine.getDefinition().getTypeEngine();
		
		Block block = instrBlockMap.get(inst.getNumber());
		
		// insert LEAs
		AsmInstruction lp;
		
		ILocal t1Local = locals.allocateTemp(typeEngine.getPointerType(typeEngine.INT));
		AssemblerOperand t1 = new RegTempOperand((RegisterLocal) t1Local);
		lp = AsmInstruction.create(Plea, from, t1);
		block.addInstBefore(lp, inst);
		System.out.println(here() +" " + lp);
		
		ILocal t2Local = locals.allocateTemp(typeEngine.getPointerType(typeEngine.INT));
		AssemblerOperand t2 = new RegTempOperand((RegisterLocal) t2Local);
		lp = AsmInstruction.create(Plea, to, t2);
		block.addInstBefore(lp, inst);
		System.out.println(here() +" " + lp);
		
		// get size of copy
		ILocal t3Local = locals.allocateTemp(typeEngine.INT);
		AssemblerOperand t3 = new RegTempOperand((RegisterLocal) t3Local);
		lp = AsmInstruction.create(Ili, t3, new NumberOperand(type.getBits() / 8));
		block.addInstBefore(lp, inst);
		System.out.println(here() +" " + lp);
		
		// make block for copy loop
		ISymbol labelSym = locals.getScope().addTemporary(".copy");
		labelSym.setType(typeEngine.LABEL);
		Block loop = new Block(labelSym);
		routine.addBlock(loop);
		System.out.println(here() +" added " + loop);
		
		ISymbol afterSym = locals.getScope().addTemporary(block.getLabel().getName());
		afterSym.setType(typeEngine.LABEL);
		
		Block after = routine.splitBlockAt(block, inst, afterSym);
		System.out.println(here() +" added " + after);
		
		instrBlockMap.put(inst.getNumber(), after);
		
		lp = AsmInstruction.create(Ijmp, new SymbolLabelOperand(labelSym));
		block.addInst(lp);
		System.out.println(here() +" " + lp);
		
		// add new stuff to 'loop'
		
		lp = AsmInstruction.create(Imov, new RegIncOperand(t1), new RegIncOperand(t2));
		loop.addInst(lp);
		System.out.println(here() +" " + lp);
		lp = AsmInstruction.create(Idect, t3);
		lp.setImplicitTargets(new ISymbol[] { getStatusSymbol() });
		loop.addInst(lp);
		System.out.println(here() +" " + lp);
		lp = AsmInstruction.create(Pjcc, 
				new CompareOperand(type.getBits() / 8 < 32768 ? "sgt" : "ugt"),		
				new SymbolLabelOperand(labelSym),
				new SymbolLabelOperand(afterSym));
		lp.setImplicitSources(new ISymbol[] { getStatusSymbol(), labelSym, afterSym });
		loop.addInst(lp);
		System.out.println(here() +" " + lp);
		
		changedBlocks = true;
	}
	
	/**
	 * @param type 
	 * @param inst
	 */
	private void expandCopyTuplePiecewise(LLType type, AsmInstruction inst, TupleTempOperand fromTuple) {
		AssemblerOperand to = inst.getOp2();
		assert to instanceof RegIndOperand || to instanceof AddrOperand;
		
		TypeEngine typeEngine = routine.getDefinition().getTypeEngine();
		
		AsmInstruction last = inst;
		Block block = instrBlockMap.get(inst.getNumber());
		
		Alignment align = typeEngine.new Alignment(Target.STACK);
		
		AssemblerOperand[] components = fromTuple.getComponents();
		LLType[] types = inst.getType().getTypes(); // fromTuple.getType().getTypes();
		
		AssemblerOperand toBase = InstrSelection.ensurePiecewiseAccess(to, null);
		
		for (int i = 0; i < components.length; i++) {
			int offs = align.alignAndAdd(types[i]);
			assert offs % 8 == 0;
			
			to = toBase.addOffset(offs / 8);
			if (to instanceof CompositePieceOperand)
				((CompositePieceOperand) to).setType(types[i]);
			
			AssemblerOperand from = components[i];
			
			int ins = Imov;
			if (types[i].getBits() > 16)
				ins = Pcopy;
			else if (types[i].getBits() <= 8)
				ins = Imovb;
			
			AsmInstruction copy;
			if (from instanceof NumberOperand && ins != Pcopy) {
				// oops, const copy
				ins = Ili; 
				to = new RegTempOperand((RegisterLocal) locals.allocateTemp(types[i]));
				if (types[i].getBits() <= 8)
					from = new NumberOperand((((NumberOperand) from).getValue() << 8) & 0xff00);
				copy = AsmInstruction.create(ins, to, from);
			} else {
				copy = AsmInstruction.create(ins, from, to);
			}
			System.out.println(here() + " adding " + copy);
			block.addInstAfter(last, copy);
			last = copy;
		}
	}
	
	private void expandCopyPiecewise(LLType type, AsmInstruction inst, AssemblerOperand from) {
		assert from instanceof RegIndOperand || from instanceof AddrOperand;
		AssemblerOperand to = inst.getOp2();
		assert to instanceof RegIndOperand || to instanceof AddrOperand;
		
		to = InstrSelection.ensurePiecewiseAccess(to, null);
		from = InstrSelection.ensurePiecewiseAccess(from, null);
		
		TypeEngine typeEngine = routine.getDefinition().getTypeEngine();
		
		AsmInstruction last = inst;
		Block block = instrBlockMap.get(inst.getNumber());
		
		for (int i = 0; i < type.getBits(); i += 16) {
			int left = type.getBits() - i;
			int use = Math.min(typeEngine.INT.getBits(), left);
			LLType theType = typeEngine.getIntType(use);
			int ins = Imov;
			if (use <= 8) {
				ins = Imovb;
			}
			
			if (to instanceof CompositePieceOperand)
				((CompositePieceOperand) to).setType(theType);
			if (from instanceof CompositePieceOperand)
				((CompositePieceOperand) from).setType(theType);
			
			AsmInstruction copy = AsmInstruction.create(ins, from, to);
			block.addInstAfter(last, copy);
			last = copy;
			
			from = from.addOffset(use / 8);
			to = to.addOffset(use / 8);
		}
	}

}
