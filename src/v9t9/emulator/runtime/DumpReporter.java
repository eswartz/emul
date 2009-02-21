/**
 * 
 */
package v9t9.emulator.runtime;

import java.io.PrintWriter;

import v9t9.emulator.hardware.TI994A;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionWorkBlock;
import v9t9.utils.Utils;

/**
 * @author ejs
 *
 */
public class DumpReporter implements InstructionListener {
	private final Cpu cpu;

	/**
	 * 
	 */
	public DumpReporter(Cpu cpu) {
		this.cpu = cpu;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(InstructionWorkBlock before, InstructionWorkBlock after) {
		PrintWriter dump = Executor.getDump();
		if (dump == null)
			return;
		Instruction ins = before.inst;
		if (cpu.getMachine() instanceof TI994A) {
		    TI994A ti = (TI994A) cpu.getMachine();
		    dump.println(Utils.toHex4(ins.pc) 
		            + " "
		            //+ Utils.toHex4(cpu.getWP())
		            //+ " "
		            + Utils.toHex4(cpu.getStatus().flatten())
		            + " "
		            + Utils.toHex4(ti.getVdpMmio().getAddr())
		            + " "
		            + Utils.toHex4(ti.getGplMmio().getAddr()));
		} else {
		    dump.println(Utils.toHex4(ins.pc) 
		            + " "
		            + Utils.toHex4(cpu.getStatus().flatten())
		    );
		    
		}
		dump.flush();

	}

}
