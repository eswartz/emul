/**
 * 
 */
package v9t9.tools.asm.assembler.transform;

import java.util.List;
import java.util.ListIterator;

import v9t9.engine.cpu.IInstruction;
import v9t9.engine.cpu.InstructionTable;
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
			
			if (llInst.getInst() == InstructionTable.Idelete) {
				System.err.println("Changed " + inst.toInfoString());
				iterator.remove();
				changed = true;
			}
		}
		return changed;
	}

	private boolean convertInstruction(LLInstruction llInst) {
		if (llInst.getInst() == InstructionTable.Ili) {
			LLOperand op2 = llInst.getOp2();
			int immed = op2.getImmediate();
			if (immed == 0) {
				llInst.setInst(InstructionTable.Iclr);
				llInst.setOp2(null);
				return true;
			} else if (immed == -1) {
				llInst.setInst(InstructionTable.Iseto);
				llInst.setOp2(null);
				return true;
			}
		}
		else if (llInst.getInst() == InstructionTable.Iai) {
			LLOperand op2 = llInst.getOp2();
			int immed = op2.getImmediate();
			if (immed == 0) {
				llInst.setInst(InstructionTable.Idelete);
				return true;
			} else if (immed == 1) {
				llInst.setInst(InstructionTable.Iinc);
				llInst.setOp2(null);
				return true;
			} else if (immed == -1) {
				llInst.setInst(InstructionTable.Idec);
				llInst.setOp2(null);
				return true;
			} else if (immed == 2) {
				llInst.setInst(InstructionTable.Iinct);
				llInst.setOp2(null);
				return true;
			} else if (immed == -2) {
				llInst.setInst(InstructionTable.Idect);
				llInst.setOp2(null);
				return true;
			}
		}
		return false;
	}
}
