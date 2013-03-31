/*
  Simplifier9900.java

  (c) 2008-2011 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.tools.asm.transform;

import java.util.List;
import java.util.ListIterator;

import v9t9.common.asm.IInstruction;
import v9t9.common.asm.InstTableCommon;
import v9t9.machine.ti99.cpu.Inst9900;
import v9t9.tools.asm.LLInstruction;
import v9t9.tools.asm.operand.ll.LLOperand;
import v9t9.tools.asm.operand.ll.LLRegIndOperand;
import v9t9.tools.asm.operand.ll.LLRegOffsOperand;

/**
 * @author Ed
 *
 */
public class Simplifier9900 {

	private final List<IInstruction> insts;

	public Simplifier9900(List<IInstruction> insts) {
		this.insts = insts;
	}

	public boolean run() {
		boolean changed = false;
		for (ListIterator<IInstruction> iterator = insts.listIterator();
			iterator.hasNext();) {
			IInstruction inst = iterator.next();
			
			if (!(inst instanceof LLInstruction))
				continue;
			
			LLInstruction llInst = (LLInstruction) inst;
			
			changed |= simplifyInstructionOperands(llInst);
			changed |= convertInstruction(llInst);
			
			if (llInst.getInst() == InstTableCommon.Idelete) {
				System.err.println("Changed " + inst.toInfoString());
				iterator.remove();
				changed = true;
			}
		}
		return changed;
	}

	private boolean simplifyInstructionOperands(LLInstruction llInst) {
		boolean changed = false;
		LLOperand op;
		op = reduceOperand(llInst.getOp1());
		if (op != llInst.getOp1()) {
			llInst.setOp1(op);
			changed = true;
		}
		op = reduceOperand(llInst.getOp2());
		if (op != llInst.getOp2()) {
			llInst.setOp2(op);
			changed = true;
		}
		
		return changed;
	}
	private LLOperand reduceOperand(LLOperand op) {
		if (!(op instanceof LLRegOffsOperand))
			return op;
		LLRegOffsOperand offs = (LLRegOffsOperand) op;
		if (offs.getOffset() == 0)
			return new LLRegIndOperand(offs.getRegister());
		return op;
	}

	private boolean convertInstruction(LLInstruction llInst) {
		if (llInst.getInst() == Inst9900.Ili) {
			LLOperand op2 = llInst.getOp2();
			int immed = op2.getImmediate();
			if (immed == 0) {
				llInst.setInst(Inst9900.Iclr);
				llInst.setOp2(null);
				return true;
			} else if (immed == -1) {
				llInst.setInst(Inst9900.Iseto);
				llInst.setOp2(null);
				return true;
			}
		}
		else if (llInst.getInst() == Inst9900.Iai) {
			LLOperand op2 = llInst.getOp2();
			int immed = op2.getImmediate();
			if (immed == 0) {
				llInst.setInst(InstTableCommon.Idelete);
				return true;
			} else if (immed == 1) {
				llInst.setInst(Inst9900.Iinc);
				llInst.setOp2(null);
				return true;
			} else if (immed == -1) {
				llInst.setInst(Inst9900.Idec);
				llInst.setOp2(null);
				return true;
			} else if (immed == 2) {
				llInst.setInst(Inst9900.Iinct);
				llInst.setOp2(null);
				return true;
			} else if (immed == -2) {
				llInst.setInst(Inst9900.Idect);
				llInst.setOp2(null);
				return true;
			}
		}
		return false;
	}
}
