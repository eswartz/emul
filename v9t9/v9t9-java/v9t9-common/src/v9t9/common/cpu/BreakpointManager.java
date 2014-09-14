/*
  BreakpointManager.java

  (c) 2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.common.cpu;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import ejs.base.properties.IProperty;
import ejs.base.utils.ListenerList;
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
	private Set<IBreakpoint> tempDisableSet = new HashSet<IBreakpoint>();
	private ListenerList<IBreakpointListener> listeners = new ListenerList<IBreakpointListener>();
	
	public BreakpointManager(IMachine machine) {
		this.machine = machine;
		debugging = Settings.get(machine, ICpu.settingDebugging);
	}
	
	/* (non-Javadoc)
	 * @see v9t9.common.cpu.IInstructionListener#preExecute(v9t9.common.cpu.InstructionWorkBlock)
	 */
	@Override
	public synchronized boolean preExecute(ChangeBlock block) {
		IBreakpoint bp = pcToBps.get(block.getPC());
		if (bp == null)
			return true;
		
		if (tempDisableSet.remove(bp))
			return true;
		
		boolean continueRunning = bp.execute(machine.getCpu().getState());
		
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
	 * @see v9t9.common.cpu.IInstructionListener#executed(v9t9.common.cpu.ChangeBlock)
	 */
	@Override
	public void executed(ChangeBlock block) {
		
	}

	public synchronized IBreakpoint addBreakpoint(final IBreakpoint bp) {
		IBreakpoint origBp = pcToBps.get(bp.getPc());
		if (origBp != null) {
			removeBreakpoint(origBp);
		}

		pcToBps.put(bp.getPc(), bp);
		bps.add(bp);
		
		if (bps.size() == 1) {
			machine.getExecutor().addInstructionListener(this);
		}

		listeners.fire(new ListenerList.IFire<IBreakpointListener>() {
			/* (non-Javadoc)
			 * @see ejs.base.utils.ListenerList.IFire#fire(java.lang.Object)
			 */
			@Override
			public void fire(IBreakpointListener listener) {
				listener.breakpointChanged(bp, true);
			}
		});

		return bp;
	}

	public synchronized void removeBreakpoint(final IBreakpoint bp) {
		bps.remove(bp);
		pcToBps.remove(bp.getPc());
		
		if (bps.isEmpty()) {
			machine.getExecutor().removeInstructionListener(this);
		}
		
		listeners.fire(new ListenerList.IFire<IBreakpointListener>() {
			/* (non-Javadoc)
			 * @see ejs.base.utils.ListenerList.IFire#fire(java.lang.Object)
			 */
			@Override
			public void fire(IBreakpointListener listener) {
				listener.breakpointChanged(bp, false);
			}
		});

	}

	/**
	 * @return
	 */
	public boolean isEmpty() {
		return bps.isEmpty();
	}

	/**
	 * @param pc
	 * @return
	 */
	public IBreakpoint findBreakpoint(int pc) {
		return pcToBps.get(pc);
	}

	/**
	 * @param pc
	 */
	public void tempDisableBreakpoint(int pc) {
		IBreakpoint bp = findBreakpoint(pc);
		if (bp != null) {
			tempDisableSet.add(bp);
		}
	}
	
	public void addListener(IBreakpointListener listener) {
		listeners.add(listener);
	}
	public void removeListener(IBreakpointListener listener) {
		listeners.remove(listener);
	}

}
