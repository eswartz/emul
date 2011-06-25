/**
 * 
 */
package org.ejs.eulang.llvm.tms9900;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

import org.ejs.eulang.TypeEngine;
import org.ejs.eulang.llvm.LLModule;
import org.ejs.eulang.symbols.ISymbol;

import v9t9.tools.asm.assembler.operand.hl.AssemblerOperand;

/**
 * @author ejs
 *
 */
public abstract class AbstractCodeModificationVisitor extends CodeVisitor {
	protected boolean changed;
	protected Routine routine;

	protected StackFrame stackFrame;
	protected TreeMap<Integer, AsmInstruction> instrMap;
	protected TreeMap<Integer, Block> instrBlockMap;
	protected TypeEngine typeEngine;
	protected LLModule module;
	private HashSet<ILocal> origLocals;

	public AbstractCodeModificationVisitor() {
	}
	

	protected void updateLocalUsage(AsmInstruction asmInstruction,
			ILocal fromLocal, ILocal toLocal, AssemblerOperand op) {
		// update usage
		if (fromLocal != null) {
			if (op.equals(asmInstruction.getSrcOp()))
				fromLocal.getUses().clear(asmInstruction.getNumber());
			else if (op.equals(asmInstruction.getDestOp()))
				fromLocal.getDefs().clear(asmInstruction.getNumber());
		}
		if (toLocal != null) {
			if (op.equals(asmInstruction.getSrcOp()))
				toLocal.getUses().set(asmInstruction.getNumber());
			else if (op.equals(asmInstruction.getDestOp())) {
				toLocal.getDefs().set(asmInstruction.getNumber());
				if (asmInstruction.getNumber() < toLocal.getInit())
					toLocal.setInit(asmInstruction.getNumber());
			}
		}
	}

	
	/**
	 * @param asmInstruction
	 * @param from
	 * @param to
	 */
	protected void replaceUses(AsmInstruction asmInstruction, AssemblerOperand from, AssemblerOperand to) {
		assert asmInstruction != null;
		AssemblerOperand[] ops = asmInstruction.getOps();
		emitChangePrefix(asmInstruction);
		for (int idx = 0; idx < ops.length; idx++) {
			AssemblerOperand newOp = ops[idx].replaceOperand(from, to);
			if (newOp != ops[idx]) {
				//updateLocalUsage(asmInstruction, fromLocal, toLocal, ops[idx]);
				updateOperandUsage(asmInstruction, ops[idx], false);

				// update op
				asmInstruction.setOp(idx + 1, newOp);
				
				updateOperandUsage(asmInstruction, newOp, true);
			}
		}
		emitChangeSuffix(asmInstruction);
	}

	protected void emitChangePrefix(AsmInstruction asmInstruction) {
		System.out.println("From\t" + asmInstruction);
		gatherUsageInfo(asmInstruction);
	}


	protected void gatherUsageInfo(AsmInstruction asmInstruction) {
		origLocals = new HashSet<ILocal>();
		
		for (ISymbol sym : asmInstruction.getTargets()) {
			ILocal local = stackFrame.getLocal(sym);
			if (local != null) {
				origLocals.add(local);
			}
		}
		for (ISymbol sym : asmInstruction.getSources()) {
			ILocal local = stackFrame.getLocal(sym);
			if (local != null) {
				origLocals.add(local);
			}
		}
	}

	/**
	 * @param asmInstruction
	 * @param origLocals2 
	 */
	protected void emitUsageInfo(AsmInstruction asmInstruction) {
		Set<ILocal> seen = new HashSet<ILocal>();
		for (ISymbol sym : asmInstruction.getTargets()) {
			ILocal local = stackFrame.getLocal(sym);
			if (local != null) {
				if (!origLocals.contains(local)) {
					System.out.println("\t\t+" + local.getName() + "\tdefs: " + local.getDefs());
				} else {
					seen.add(local);
				}
			}
		}
		for (ISymbol sym : asmInstruction.getSources()) {
			ILocal local = stackFrame.getLocal(sym);
			if (local != null) {
				if (!origLocals.contains(local)) {
					System.out.println("\t\t+" + local.getName() + "\tuses: " + local.getUses());
				} else {
					seen.add(local);
				}
			}
		}
		origLocals.removeAll(seen);
		for (ILocal local : origLocals) {
			System.out.println("\t\t-" + local.getName() + "\tuses: " + local.getUses());
			System.out.println("\t\t-" + local.getName() + "\tdefs: " + local.getDefs());
		}
	}


	protected void emitChangeSuffix(AsmInstruction asmInstruction) {
		System.out.println("to\t" + asmInstruction);
		emitUsageInfo(asmInstruction);
	}

	/**
	 * @param key
	 * @return
	 */
	protected ILocal getReffedLocal(AssemblerOperand key) {
		Set<ISymbol> syms = new HashSet<ISymbol>(); 
		AsmInstruction.getSymbolRefs(syms, key);
		for (ISymbol sym : syms) {
			ILocal local = stackFrame.getLocal(sym);
			if (local != null)
				return local;
		}
		return null;
	}


	protected void updateOperandUsage(AsmInstruction asmInstruction, AssemblerOperand op, boolean adding) {
		if (op == null)
			return;
		
		Set<ISymbol> syms = new HashSet<ISymbol>(); 
		AsmInstruction.getSymbolRefs(syms, op);
		
		for (ISymbol sym : asmInstruction.getSources()) {
			if (syms.contains(sym)) {
				ILocal local = stackFrame.getLocal(sym);
				if (local != null) {
					if (adding)
						local.getUses().set(asmInstruction.getNumber());
					else
						local.getUses().clear(asmInstruction.getNumber());
				}
			}
		}
	
		for (ISymbol sym : asmInstruction.getTargets()) {
			if (syms.contains(sym)) {
				ILocal local = stackFrame.getLocal(sym);
				if (local != null) {
					if (adding)
						local.getDefs().set(asmInstruction.getNumber());
					else
						local.getDefs().clear(asmInstruction.getNumber());
				}
			}
		}
	}

	/**
	 * @param instrs 
	 * @param inst
	 */
	protected void removeInst(AsmInstruction inst) {
		System.out.println("Deleting " + inst);
		
		gatherUsageInfo(inst);
		
		for (ISymbol sym : inst.getSources()) {
			ILocal local = stackFrame.getLocal(sym);
			if (local != null) {
				local.getUses().clear(inst.getNumber());
				local.getDefs().clear(inst.getNumber());
			}
		}
		for (ISymbol sym : inst.getTargets()) {
			ILocal local = stackFrame.getLocal(sym);
			if (local != null) {
				local.getUses().clear(inst.getNumber());
				local.getDefs().clear(inst.getNumber());
			}
		}
		Block block = instrBlockMap.get(inst.getNumber());
		block.getInstrs().remove(inst);

		inst.setOp1(null);
		inst.setOp2(null);
		inst.setOp3(null);
		inst.setInst(0);
		
		emitUsageInfo(inst);
	}

	protected ILocal getTargetLocal(AsmInstruction inst) {
		for (ISymbol sym : inst.getTargets()) {
			ILocal local = stackFrame.getLocal(sym);
			if (local != null)
				return local;
		}
		return null;
	}

	protected boolean isSingleRegister(ILocal local) {
		return local instanceof RegisterLocal && !((RegisterLocal) local).isRegPair();
	}

	/**
	 * @return
	 */
	protected String here() {
		Exception e = new Exception();
		e.fillInStackTrace();
		String func = e.getStackTrace()[1].getMethodName();
		return "[" + func + "] ";
	}

	protected ILocal getSourceLocal(AsmInstruction inst) {
		for (ISymbol sym : inst.getSources()) {
			ILocal local = stackFrame.getLocal(sym);
			if (local != null)
				return local;
		}
		return null;
	}

	/** Tell if any changes occurred.  This means that instructions were deleted from
	 * blocks, but instruction numbers were not re-calculated.
	 * @return
	 */
	public boolean isChanged() {
		return changed;
	}


	/* (non-Javadoc)
	 * @see org.ejs.eulang.llvm.tms9900.CodeVisitor#enterRoutine(org.ejs.eulang.llvm.tms9900.Routine)
	 */
	@Override
	public boolean enterRoutine(Routine routine) {
		this.routine = routine;
		this.stackFrame = routine.getStackFrame();
		this.typeEngine = routine.getDefinition().getTypeEngine();
		this.module = routine.getDefinition().getModule();
		
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
	
	protected ISymbol getStatusSymbol() {
		return routine.getDefinition().getTarget().
			getStatusRegister(routine.getDefinition().getModule().getModuleScope());
	}

	protected boolean dependsOnStatus(AsmInstruction inst) {
		ISymbol status = getStatusSymbol(); 
		if (inst.getEffects().stReads != 0)
			return true;
		for (ISymbol sym : inst.getSources()) {
			if (sym.equals(status))
				return true;
		}
		for (ISymbol sym : inst.getTargets()) {
			if (sym.equals(status))
				return true;
		}
		return false;
	}
}
