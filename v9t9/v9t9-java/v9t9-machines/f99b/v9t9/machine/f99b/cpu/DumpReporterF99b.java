/**
 * 
 */
package v9t9.machine.f99b.cpu;

import java.io.PrintWriter;


import v9t9.base.utils.HexUtils;
import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.engine.cpu.Executor;
import v9t9.engine.cpu.InstructionListener;

/**
 * @author ejs
 *
 */
public class DumpReporterF99b implements InstructionListener {
	private final CpuF99b cpu;

	/**
	 * 
	 */
	public DumpReporterF99b(CpuF99b cpu) {
		this.cpu = cpu;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(InstructionWorkBlock before, InstructionWorkBlock after) {
		PrintWriter dump = Executor.getDump();
		if (dump == null)
			return;
		RawInstruction ins = before.inst;
	    dump.println(HexUtils.toHex4(ins.pc) 
	            + " "
	            + HexUtils.toHex4(((CpuStateF99b)cpu.getState()).getSP())
	            + " "
	            + HexUtils.toHex4(((CpuStateF99b)cpu.getState()).getRP())
	    );
		dump.flush();

	}

}