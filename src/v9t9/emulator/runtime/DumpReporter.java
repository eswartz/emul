/**
 * 
 */
package v9t9.emulator.runtime;

import java.io.PrintWriter;

import org.ejs.emul.core.utils.HexUtils;

import v9t9.emulator.hardware.TI994A;
import v9t9.engine.cpu.Instruction;
import v9t9.engine.cpu.InstructionWorkBlock;

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
		    dump.println(HexUtils.toHex4(ins.pc) 
		            + " "
		            //+ Utils.toHex4(cpu.getWP())
		            //+ " "
		            + HexUtils.toHex4(cpu.getStatus().flatten())
		            + " "
		            + HexUtils.toHex4(ti.getVdpMmio().getAddr())
		            + " "
		            + HexUtils.toHex4(ti.getGplMmio().getAddr()));
		} else {
		    dump.println(HexUtils.toHex4(ins.pc) 
		            + " "
		            + HexUtils.toHex4(cpu.getStatus().flatten())
		    );
		    
		}
		dump.flush();

	}

}
