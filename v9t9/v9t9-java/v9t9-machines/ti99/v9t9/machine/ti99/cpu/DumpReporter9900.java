/*
  DumpReporter9900.java

  (c) 2009-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.cpu;

import java.io.PrintWriter;

import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;
import ejs.base.utils.HexUtils;


import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ChangeBlock;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IInstructionListener;
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
	public boolean preExecute(ChangeBlock block) {
		return true;
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IInstructionListener#executed(v9t9.common.cpu.ChangeBlock)
	 */
	@Override
	public void executed(ChangeBlock block) {
		PrintWriter dump = Logging.getLog(dumpSetting);
		if (dump == null)
			return;
		RawInstruction ins = ((ChangeBlock9900) block).inst;
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
