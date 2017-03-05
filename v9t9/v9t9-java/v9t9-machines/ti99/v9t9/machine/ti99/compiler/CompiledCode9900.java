/*
  CompiledCode9900.java

  (c) 2011-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.machine.ti99.compiler;

import v9t9.common.cpu.IExecutor;
import v9t9.engine.compiler.CompiledCode;
import v9t9.engine.hardware.ICruHandler;
import v9t9.engine.memory.GplMmio;
import v9t9.engine.memory.VdpMmio;
import v9t9.machine.ti99.machine.TI99Machine;

/**
 * @author ejs
 *
 */
public class CompiledCode9900 extends CompiledCode {

    // used for debugging
    protected VdpMmio vdpMmio;
    protected GplMmio gplMmio;
    protected ICruHandler cru;

	/**
	 * @param exec
	 */
	public CompiledCode9900(IExecutor exec) {
		super(exec);
		
		TI99Machine ti99Machine = (TI99Machine) exec.getCpu().getMachine();
		this.cru = ti99Machine.getCruManager();
		this.vdpMmio = ti99Machine.getVdpMmio();
		this.gplMmio = ti99Machine.getGplMmio();
	}

	/* (non-Javadoc)
	 * @see v9t9.emulator.runtime.compiler.CompiledCode#run()
	 */
	@Override
	public boolean run() {
		// TODO Auto-generated method stub
		return false;
	}

}
