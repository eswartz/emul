/*
  BreakpointManager.java

  (c) 2012 Edward Swartz

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
package v9t9.common.cpu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ejs.base.properties.IProperty;

import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;

/**
 * @author ejs
 *
 */
public class BreakpointManager implements IInstructionListener {

	private List<IBreakpoint> bps = new ArrayList<IBreakpoint>();
	private Map<Integer, IBreakpoint> pcToBps = new HashMap<Integer, IBreakpoint>();
	private final IMachine machine;
	private IProperty debugging;
	
	public BreakpointManager(IMachine machine) {
		this.machine = machine;
		debugging = Settings.get(machine, ICpu.settingDebugging);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IInstructionListener#preExecute(v9t9.common.cpu.InstructionWorkBlock)
	 */
	@Override
	public synchronized boolean preExecute(InstructionWorkBlock before) {
		IBreakpoint bp = pcToBps.get(before.inst.pc & 0xffff);
		if (bp == null)
			return true;
		
		boolean continueRunning = bp.execute(before.cpu);
		
		if (!continueRunning) {
			machine.setPaused(true);
			debugging.setBoolean(true);
		}
		
		if (bp.isCompleted()) {
			removeBreakpoint(bp);
		}
		return continueRunning;
	}

	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IInstructionListener#executed(v9t9.common.cpu.InstructionWorkBlock, v9t9.common.cpu.InstructionWorkBlock)
	 */
	@Override
	public void executed(InstructionWorkBlock before, InstructionWorkBlock after) {
	}

	public synchronized IBreakpoint addBreakpoint(IBreakpoint bp) {
		IBreakpoint origBp = pcToBps.get(bp.getPc());
		if (origBp != null) {
			removeBreakpoint(origBp);
		}

		pcToBps.put(bp.getPc(), bp);
		bps.add(bp);
		
		if (bps.size() == 1) {
			machine.getExecutor().addInstructionListener(this);
		}
		
		return bp;
	}

	public synchronized void removeBreakpoint(IBreakpoint bp) {
		bps.remove(bp);
		pcToBps.remove(bp.getPc());
		
		if (bps.isEmpty()) {
			machine.getExecutor().removeInstructionListener(this);
		}

	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return bps.isEmpty();
	}

}
