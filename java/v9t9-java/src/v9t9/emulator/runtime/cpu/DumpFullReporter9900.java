/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import java.io.PrintWriter;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.runtime.InstructionListener;
import v9t9.engine.cpu.InstructionWorkBlock;
import v9t9.engine.cpu.MachineOperand;
import v9t9.engine.cpu.MachineOperand9900;
import v9t9.engine.cpu.Operand;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryEntry;

/**
 * @author ejs
 *
 */
public class DumpFullReporter9900 implements InstructionListener {

	private final Cpu9900 cpu;

	/**
	 * 
	 */
	public DumpFullReporter9900(Cpu9900 cpu) {
		this.cpu = cpu;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(InstructionWorkBlock before, InstructionWorkBlock after) {
		PrintWriter dumpfull = Executor.getDumpfull();
		if (dumpfull == null) return;
		dumpFullStart(before, before.inst, dumpfull);
		dumpFullMid(before, (MachineOperand9900)before.inst.getOp1(), (MachineOperand9900)before.inst.getOp2(), dumpfull);
		dumpFullEnd(after, before.cycles, (MachineOperand9900)after.inst.getOp1(), (MachineOperand9900)after.inst.getOp2(), dumpfull);
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
	private void dumpFullMid(InstructionWorkBlock iinstructionWorkBlock,
			MachineOperand9900 mop1, MachineOperand9900 mop2,
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
			int origCycleCount, MachineOperand9900 mop1,
			MachineOperand9900 mop2, PrintWriter dumpfull) {
		String str;
		if (mop1.type != MachineOperand.OP_NONE
		        && (mop1.dest != Operand.OP_DEST_FALSE
		        		|| mop1.type == MachineOperand9900.OP_INC)) {
		    str = mop1.valueString(iinstructionWorkBlock.ea1, iinstructionWorkBlock.val1);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		if (mop2.type != MachineOperand.OP_NONE
				&& (mop2.dest != Operand.OP_DEST_FALSE
		        		|| mop2.type == MachineOperand9900.OP_INC)) {
		    str = mop2.valueString(iinstructionWorkBlock.ea2, iinstructionWorkBlock.val2);
		    if (str != null) {
				dumpfull.print("op2=" + str + " ");
			}
		}
		dumpfull.print("st="
		        + Integer.toHexString(cpu.getST() & 0xffff)
		                .toUpperCase() + " wp="
		        + Integer.toHexString(((Cpu9900) cpu).getWP() & 0xffff).toUpperCase());
		
		int cycles = cpu.getCurrentCycleCount() - origCycleCount;
		dumpfull.print(" @ " + cycles);
		dumpfull.println();
		dumpfull.flush();
	}

}
