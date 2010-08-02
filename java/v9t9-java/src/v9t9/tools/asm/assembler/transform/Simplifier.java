/**
 * 
 */
package v9t9.tools.asm.assembler.transform;

import java.util.List;
import java.util.ListIterator;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.Inst9900;
import v9t9.engine.cpu.InstTableCommon;
import v9t9.tools.asm.assembler.LLInstruction;
import v9t9.tools.asm.assembler.operand.ll.LLOperand;

/**
 * @author Ed
 *
 */
public class Simplifier {

	private final List<IInstruction> insts;

	public Simplifier(List<IInstruction> insts) {
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
			
			changed |= convertInstruction(llInst);
			
			if (llInst.getInst() == InstTableCommon.Idelete) {
				System.err.println("Changed " + inst.toInfoString());
				iterator.remove();
				changed = true;
			}
		}
		return changed;
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
