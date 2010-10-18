/**
 * 
 */
package v9t9.emulator.runtime.cpu;

import java.io.PrintWriter;

import org.ejs.coffee.core.utils.HexUtils;

import v9t9.emulator.hardware.TI99Machine;
import v9t9.emulator.runtime.InstructionListener;
import v9t9.engine.cpu.BaseInstructionWorkBlock;
import v9t9.engine.cpu.RawInstruction;

/**
 * @author ejs
 *
 */
public class DumpReporter9900 implements InstructionListener {
	private final Cpu9900 cpu;

	/**
	 * 
	 */
	public DumpReporter9900(Cpu9900 cpu) {
		this.cpu = cpu;
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(BaseInstructionWorkBlock before, BaseInstructionWorkBlock after) {
		PrintWriter dump = Executor.getDump();
		if (dump == null)
			return;
		RawInstruction ins = before.inst;
		if (cpu.getMachine() instanceof TI99Machine) {
		    TI99Machine ti = (TI99Machine) cpu.getMachine();
		    dump.println(HexUtils.toHex4(ins.pc) 
		            + " "
		            //+ Utils.toHex4(cpu.getWP())
		            //+ " "
		            + HexUtils.toHex4(cpu.getST())
		            + " "
		            + HexUtils.toHex4(ti.getVdpMmio().getAddr())
		            + " "
		            + HexUtils.toHex4(ti.getGplMmio().getAddr()));
		} else {
		    dump.println(HexUtils.toHex4(ins.pc) 
		            + " "
		            + HexUtils.toHex4(cpu.getST())
		    );
		    
		}
		dump.flush();

	}

}
