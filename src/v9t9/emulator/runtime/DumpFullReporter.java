/**
 * 
 */
package v9t9.emulator.runtime;

import java.io.PrintWriter;

import org.ejs.emul.core.utils.HexUtils;

import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionWorkBlock;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.Operand;
import v9t9.engine.memory.MemoryEntry;

/**
 * @author ejs
 *
 */
public class DumpFullReporter implements InstructionListener {

	private final Cpu cpu;

	/**
	 * 
	 */
	public DumpFullReporter(Cpu cpu) {
		this.cpu = cpu;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(InstructionWorkBlock before, InstructionWorkBlock after) {
		PrintWriter dumpfull = Executor.getDumpfull();
		if (dumpfull == null) return;
		dumpFullStart(before, before.inst, dumpfull);
		dumpFullMid(before, (MachineOperand)before.inst.op1, (MachineOperand)before.inst.op2, dumpfull);
		dumpFullEnd(after, before.cycles, (MachineOperand)after.inst.op1, (MachineOperand)after.inst.op2, dumpfull);
	}

	private void dumpFullStart(InstructionWorkBlock iinstructionWorkBlock,
			Instruction ins, PrintWriter dumpfull) {
		MemoryEntry entry = iinstructionWorkBlock.domain.getEntryAt(ins.pc);
		String name = null;
		if (entry != null) 
			name = entry.lookupSymbol((short) ins.pc);
		if (name != null)
			dumpfull.println('"' + name + "\" ");
		dumpfull.print(HexUtils.toHex4(ins.pc) + ": "
		        + ins.toString() + " ==> ");
	}
	private void dumpFullMid(InstructionWorkBlock iinstructionWorkBlock,
			MachineOperand mop1, MachineOperand mop2,
			PrintWriter dumpfull) {
		String str;
		if (mop1.type != MachineOperand.OP_NONE
		        && mop1.dest != Operand.OP_DEST_KILLED) {
		    str = mop1.valueString(iinstructionWorkBlock.ea1, iinstructionWorkBlock.val1);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		if (mop2.type != MachineOperand.OP_NONE
		        && mop2.dest != Operand.OP_DEST_KILLED) {
		    str = mop2.valueString(iinstructionWorkBlock.ea2, iinstructionWorkBlock.val2);
		    if (str != null) {
				dumpfull.print("op2=" + str);
			}
		}
		dumpfull.print(" || ");
	}
	private void dumpFullEnd(InstructionWorkBlock iinstructionWorkBlock, 
			int origCycleCount, MachineOperand mop1,
			MachineOperand mop2, PrintWriter dumpfull) {
		String str;
		if (mop1.type != MachineOperand.OP_NONE
		        && (mop1.dest != Operand.OP_DEST_FALSE
		        		|| mop1.type == MachineOperand.OP_INC)) {
		    str = mop1.valueString(iinstructionWorkBlock.ea1, iinstructionWorkBlock.val1);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		if (mop2.type != MachineOperand.OP_NONE
				&& (mop2.dest != Operand.OP_DEST_FALSE
		        		|| mop2.type == MachineOperand.OP_INC)) {
		    str = mop2.valueString(iinstructionWorkBlock.ea2, iinstructionWorkBlock.val2);
		    if (str != null) {
				dumpfull.print("op2=" + str + " ");
			}
		}
		dumpfull.print("st="
		        + Integer.toHexString(cpu.getStatus().flatten() & 0xffff)
		                .toUpperCase() + " wp="
		        + Integer.toHexString(cpu.getWP() & 0xffff).toUpperCase());
		
		int cycles = cpu.getCurrentCycleCount() - origCycleCount;
		dumpfull.print(" @ " + cycles);
		dumpfull.println();
		dumpfull.flush();
	}

}
