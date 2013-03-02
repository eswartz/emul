/*
  DumpReporter9900.java

  (c) 2009-2012 Edward Swartz

  This program is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  This program is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with this program; if not, write to the Free Software
  Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
  02111-1307, USA.
 */
package v9t9.machine.ti99.cpu;

import java.io.PrintWriter;

import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;
import ejs.base.utils.HexUtils;


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
	private IProperty dumpSetting;

	/**
	 * 
	 */
	public DumpReporter9900(Cpu9900 cpu) {
		this.cpu = cpu;
		dumpSetting = Settings.get(cpu, ICpu.settingDumpInstructions);
	}
	

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IInstructionListener#preExecute(v9t9.common.cpu.InstructionWorkBlock)
	 */
	@Override
	public boolean preExecute(InstructionWorkBlock before) {
		return true;
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
