/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import java.io.PrintWriter;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.runtime.InstructionListener;
import v9t9.engine.cpu.BaseInstructionWorkBlock;
import v9t9.engine.cpu.InstructionWorkBlockF99;
import v9t9.engine.cpu.MachineOperandF99;
import v9t9.engine.cpu.RawInstruction;
import v9t9.engine.memory.MemoryEntry;

/**
 * @author ejs
 *
 */
public class DumpFullReporterF99 implements InstructionListener {

	private final CpuF99 cpu;
	private final PrintWriter dump;

	/**
	 * 
	 */
	public DumpFullReporterF99(CpuF99 cpu, PrintWriter dump) {
		this.cpu = cpu;
		this.dump = dump;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(BaseInstructionWorkBlock before_, BaseInstructionWorkBlock after_) {
		PrintWriter dumpfull = dump != null ? dump : Executor.getDumpfull();
		if (dumpfull == null) return;
		InstructionWorkBlockF99 before = (InstructionWorkBlockF99) before_;
		InstructionWorkBlockF99 after = (InstructionWorkBlockF99) after_;
		dumpFullStart(before, before.inst, dumpfull);
		dumpFullMid(before, 
				(MachineOperandF99)before.inst.getOp1(), 
				dumpfull);
		dumpFullEnd(after, before.cycles, 
				(MachineOperandF99)after.inst.getOp1(), 
				dumpfull);
	}

	private void dumpFullStart(InstructionWorkBlockF99 iinstructionWorkBlock,
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
	private void dumpFullMid(InstructionWorkBlockF99 block,
			MachineOperandF99 mop1,
			PrintWriter dumpfull) {
		String str;
		/*
		if (mop1 != null 
				&& mop1.type != MachineOperand.OP_NONE
		        && mop1.dest != Operand.OP_DEST_KILLED) {
		    str = mop1.valueString(block.ea1, block.val1);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		*/
		dumpfull.print(" || ");
	}
	private void dumpFullEnd(InstructionWorkBlockF99 block, 
			int origCycleCount, MachineOperandF99 mop1, PrintWriter dumpfull) {
		String str;
		/*
		if (mop1 != null 
				&& mop1.type != MachineOperand.OP_NONE) {
		    str = mop1.valueString(block.ea1, block.val1);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		*/
		dumpfull.print(   
		        " sp=" + Integer.toHexString(cpu.getSP() & 0xffff).toUpperCase()
		        + " rp=" + Integer.toHexString(cpu.getRSP() & 0xffff).toUpperCase()
		        + " sr="
		        + Integer.toHexString(cpu.getST() & 0xffff).toUpperCase()        
		);
		
		int cycles = cpu.getCurrentCycleCount() - origCycleCount;
		dumpfull.print(" @ " + cycles);
		dumpfull.println();
		dumpfull.flush();
	}

}
