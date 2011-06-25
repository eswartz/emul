/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import java.io.PrintWriter;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.runtime.InstructionListener;
import v9t9.engine.cpu.BaseInstructionWorkBlock;
import v9t9.engine.cpu.InstructionWorkBlock;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.MachineOperandMFP201;
import v9t9.engine.cpu.Operand;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryEntry;

/**
 * @author ejs
 *
 */
public class DumpFullReporterMFP201 implements InstructionListener {

	private final CpuMFP201 cpu;

	/**
	 * 
	 */
	public DumpFullReporterMFP201(CpuMFP201 cpu) {
		this.cpu = cpu;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(BaseInstructionWorkBlock before_, BaseInstructionWorkBlock after_) {
		PrintWriter dumpfull = Executor.getDumpfull();
		if (dumpfull == null) return;
		InstructionWorkBlock before = (InstructionWorkBlock) before_;
		InstructionWorkBlock after = (InstructionWorkBlock) after_;
		dumpFullStart(before, before.inst, dumpfull);
		dumpFullMid(before, 
				(MachineOperandMFP201)before.inst.getOp1(), 
				(MachineOperandMFP201)before.inst.getOp2(), 
				(MachineOperandMFP201)before.inst.getOp3(),
				dumpfull);
		dumpFullEnd(after, before.cycles, 
				(MachineOperandMFP201)after.inst.getOp1(), 
				(MachineOperandMFP201)after.inst.getOp2(), 
				(MachineOperandMFP201)after.inst.getOp3(), 
				dumpfull);
	}

	private void dumpFullStart(InstructionWorkBlock iinstructionWorkBlock,
			RawInstruction ins, PrintWriter dumpfull) {
		MemoryEntry entry = iinstructionWorkBlock.domain.getEntryAt(ins.pc);
		String name = null;
		if (entry != null) 
			name = entry.lookupSymbol((short) ins.pc);
		if (name != null)
			dumpfull.println('"' + name + "\" ");
		dumpfull.print(HexUtils.toHex4(ins.pc) + ": "
		        + ins.toString() + " ==> ");
	}
	private void dumpFullMid(InstructionWorkBlock block,
			MachineOperandMFP201 mop1, MachineOperandMFP201 mop2, MachineOperandMFP201 mop3,
			PrintWriter dumpfull) {
		String str;
		if (mop1 != null 
				&& mop1.type != MachineOperand.OP_NONE
		        && mop1.dest != Operand.OP_DEST_KILLED) {
		    str = mop1.valueString(block.ea1, block.val1);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		if (mop2 != null 
				&& mop2.type != MachineOperand.OP_NONE
		        && mop2.dest != Operand.OP_DEST_KILLED) {
		    str = mop2.valueString(block.ea2, block.val2);
		    if (str != null) {
				dumpfull.print("op2=" + str + " ");
			}
		}
		if (mop3 != null 
				&& mop3.type != MachineOperand.OP_NONE
				&& mop3.dest != Operand.OP_DEST_KILLED) {
			str = mop3.valueString(block.ea3, block.val3);
			if (str != null) {
				dumpfull.print("op3=" + str);
			}
		}
		dumpfull.print(" || ");
	}
	private void dumpFullEnd(InstructionWorkBlock block, 
			int origCycleCount, MachineOperandMFP201 mop1,
			MachineOperandMFP201 mop2, MachineOperandMFP201 mop3, PrintWriter dumpfull) {
		String str;
		if (mop1 != null 
				&& mop1.type != MachineOperand.OP_NONE
		        && (mop1.dest != Operand.OP_DEST_FALSE
		        		|| mop1.type == MachineOperandMFP201.OP_INC
		        		|| mop1.type == MachineOperandMFP201.OP_DEC)) {
		    str = mop1.valueString(block.ea1, block.val1);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		if (mop2 != null && mop2.type != MachineOperand.OP_NONE
				&& (mop2.dest != Operand.OP_DEST_FALSE
		        		|| mop2.type == MachineOperandMFP201.OP_INC
		        		|| mop2.type == MachineOperandMFP201.OP_DEC)) {
		    str = mop2.valueString(block.ea2, block.val2);
		    if (str != null) {
				dumpfull.print("op2=" + str + " ");
			}
		}
		if (mop3 != null && mop3.type != MachineOperand.OP_NONE
				&& (mop3.dest != Operand.OP_DEST_FALSE
						|| mop3.type == MachineOperandMFP201.OP_INC
						|| mop3.type == MachineOperandMFP201.OP_DEC)) {
			str = mop3.valueString(block.ea3, block.val3);
			if (str != null) {
				dumpfull.print("op3=" + str + " ");
			}
		}
		dumpfull.print(  "sr="
		        + Integer.toHexString(cpu.getST() & 0xffff)
		                .toUpperCase() + " sp="
		        + Integer.toHexString(cpu.getSP() & 0xffff).toUpperCase());
		
		int cycles = cpu.getCurrentCycleCount() - origCycleCount;
		dumpfull.print(" @ " + cycles);
		dumpfull.println();
		dumpfull.flush();
	}

}
