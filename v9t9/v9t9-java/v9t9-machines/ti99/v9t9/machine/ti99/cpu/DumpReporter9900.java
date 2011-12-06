/**
 * 
 */
package v9t9.machine.ti99.cpu;

import java.io.PrintWriter;


import v9t9.base.settings.Logging;
import v9t9.base.settings.SettingProperty;
import v9t9.base.utils.HexUtils;
import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.settings.Settings;
import v9t9.machine.ti99.machine.TI99Machine;

/**
 * @author ejs
 *
 */
public class DumpReporter9900 implements IInstructionListener {
	private final Cpu9900 cpu;
	private SettingProperty dumpSetting;

	/**
	 * 
	 */
	public DumpReporter9900(Cpu9900 cpu) {
		this.cpu = cpu;
		dumpSetting = Settings.get(cpu, ICpu.settingDumpInstructions);
	}
	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.InstructionListener#executed(v9t9.engine.cpu.InstructionAction.Block, v9t9.engine.cpu.InstructionAction.Block)
	 */
	public void executed(InstructionWorkBlock before, InstructionWorkBlock after) {
		PrintWriter dump = Logging.getLog(dumpSetting);
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
