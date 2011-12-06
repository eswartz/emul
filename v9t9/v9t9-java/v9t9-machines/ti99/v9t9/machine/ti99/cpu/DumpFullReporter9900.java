/**
 * 
 */
package v9t9.machine.ti99.cpu;

import java.io.PrintWriter;


import v9t9.base.properties.IProperty;
import v9t9.base.settings.Logging;
import v9t9.base.utils.HexUtils;
import v9t9.common.asm.IMachineOperand;
import v9t9.common.asm.IOperand;
import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.memory.IMemoryEntry;
import v9t9.common.settings.Settings;

/**
 * @author ejs
 *
 */
public class DumpFullReporter9900 implements IInstructionListener {

	private final Cpu9900 cpu;
	private IProperty dumpSetting;

	/**
	 * 
	 */
	public DumpFullReporter9900(Cpu9900 cpu) {
		this.cpu = cpu;
		dumpSetting = Settings.get(cpu, ICpu.settingDumpFullInstructions);

	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(InstructionWorkBlock before_, InstructionWorkBlock after_) {
		PrintWriter dumpfull = Logging.getLog(dumpSetting);
		if (dumpfull == null) return;
		
		InstructionWorkBlock9900 before = (InstructionWorkBlock9900) before_;
		InstructionWorkBlock9900 after = (InstructionWorkBlock9900) after_;
		dumpFullStart(before, before.inst, dumpfull);
		dumpFullMid(before, (MachineOperand9900)before.inst.getOp1(), (MachineOperand9900)before.inst.getOp2(), dumpfull);
		dumpFullEnd(after, before.cycles, (MachineOperand9900)after.inst.getOp1(), (MachineOperand9900)after.inst.getOp2(), dumpfull);
	}

	public void dumpFullStart(InstructionWorkBlock9900 iinstructionWorkBlock,
			RawInstruction ins, PrintWriter dumpfull) {
		IMemoryEntry entry = iinstructionWorkBlock.domain.getEntryAt(ins.pc);
		String name = null;
		if (entry != null) 
			name = entry.lookupSymbol((short) ins.pc);
		if (name != null)
			dumpfull.println('"' + name + "\" ");
		dumpfull.print(HexUtils.toHex4(ins.pc) + ": "
		        + ins.toString() + " ==> ");
	}
	private void dumpFullMid(InstructionWorkBlock9900 iinstructionWorkBlock,
			MachineOperand9900 mop1, MachineOperand9900 mop2,
			PrintWriter dumpfull) {
		String str;
		if (mop1.type != IMachineOperand.OP_NONE
		        && mop1.dest != IOperand.OP_DEST_KILLED) {
		    str = mop1.valueString(iinstructionWorkBlock.ea1, iinstructionWorkBlock.val1);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		if (mop2.type != IMachineOperand.OP_NONE
		        && mop2.dest != IOperand.OP_DEST_KILLED) {
		    str = mop2.valueString(iinstructionWorkBlock.ea2, iinstructionWorkBlock.val2);
		    if (str != null) {
				dumpfull.print("op2=" + str);
			}
		}
		dumpfull.print(" || ");
	}
	public void dumpFullEnd(InstructionWorkBlock9900 iinstructionWorkBlock, 
			int origCycleCount, MachineOperand9900 mop1,
			MachineOperand9900 mop2, PrintWriter dumpfull) {
		String str;
		if (mop1.type != IMachineOperand.OP_NONE
		        && (mop1.dest != IOperand.OP_DEST_FALSE
		        		|| mop1.type == MachineOperand9900.OP_INC)) {
		    str = mop1.valueString(iinstructionWorkBlock.ea1, iinstructionWorkBlock.val1);
		    if (str != null) {
		        dumpfull.print("op1=" + str + " ");
		    }
		}
		if (mop2.type != IMachineOperand.OP_NONE
				&& (mop2.dest != IOperand.OP_DEST_FALSE
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
