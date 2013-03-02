/*
  DumpReporterF99b.java

  (c) 2010-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.f99b.cpu;

import java.io.PrintWriter;

import ejs.base.properties.IProperty;
import ejs.base.settings.Logging;
import ejs.base.utils.HexUtils;


import v9t9.common.asm.RawInstruction;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IInstructionListener;
import v9t9.common.cpu.InstructionWorkBlock;
import v9t9.common.settings.Settings;

/**
 * @author ejs
 *
 */
public class DumpReporterF99b implements IInstructionListener {
	private final CpuF99b cpu;
	private IProperty dumpSetting;

	/**
	 * 
	 */
	public DumpReporterF99b(CpuF99b cpu) {
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
	    dump.println(HexUtils.toHex4(ins.pc) 
	            + " "
	            + HexUtils.toHex4(((CpuStateF99b)cpu.getState()).getSP())
	            + " "
	            + HexUtils.toHex4(((CpuStateF99b)cpu.getState()).getRP())
	    );
		dump.flush();

	}

}
