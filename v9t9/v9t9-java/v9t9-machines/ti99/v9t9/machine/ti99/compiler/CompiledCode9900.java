/*
  CompiledCode9900.java

  (c) 2011 Edward Swartz

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
