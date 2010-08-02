/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import java.io.PrintWriter;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.hardware.TI99Machine;
import v9t9.emulator.runtime.InstructionListener;
import v9t9.engine.cpu.Instruction9900;
import v9t9.engine.cpu.InstructionWorkBlock;

/**
 * @author ejs
 *
 */
public class DumpReporterMFP201 implements InstructionListener {
	private final CpuMFP201 cpu;

	/**
	 * 
	 */
	public DumpReporterMFP201(CpuMFP201 cpu) {
		this.cpu = cpu;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(InstructionWorkBlock before, InstructionWorkBlock after) {
		PrintWriter dump = Executor.getDump();
		if (dump == null)
			return;
		Instruction9900 ins = before.inst;
	    dump.println(HexUtils.toHex4(ins.pc) 
	            + " "
	            + HexUtils.toHex4(cpu.getStatus().flatten())
	            + " "
	            + HexUtils.toHex4(cpu.getStack())
	    );
		dump.flush();

	}

}
