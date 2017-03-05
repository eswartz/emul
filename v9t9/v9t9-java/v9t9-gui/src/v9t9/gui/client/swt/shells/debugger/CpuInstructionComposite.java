/*
  CpuInstructionComposite.java

  (c) 2012-2015 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;

import v9t9.common.cpu.ChangeBlock;
import v9t9.common.cpu.ICpu;
import v9t9.common.cpu.IExecutor;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import ejs.base.properties.IProperty;

/**
 * @author ejs
 *
 */
public abstract class CpuInstructionComposite extends Composite {

	protected static final int MAX_INST_HISTORY = 250000;
	protected IMachine machine;
	protected IProperty pauseMachine;
	protected IProperty debugging;
	protected IProperty singleStep;
	private Runnable refreshTask;
	private boolean isDirty;
	protected final List<InstRow> instHistory = new ArrayList<InstRow>();
	
	public CpuInstructionComposite(Composite parent, int style, IMachine machine) {
		super(parent, style);
		this.machine = machine;
		
		pauseMachine = Settings.get(machine, IMachine.settingPauseMachine);
		debugging = Settings.get(machine, ICpu.settingDebugging);
		singleStep = Settings.get(machine, IExecutor.settingSingleStep);

	}
	
	protected void start() {

		refreshTask = new Runnable() {
			volatile boolean busy = false;
			//volatile long nextTime;
			
			@Override
			public void run() {
				if (busy || isDisposed()) // || System.currentTimeMillis() < nextTime)
					return;
				
				busy = true;

				getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (!isDisposed() && isDirty) {
							flush();
							isDirty = false;
							//nextTime = System.currentTimeMillis() + 1000 / 10;
						}
						busy = false;
					}
				});
				
			}
		};
		machine.getFastMachineTimer().scheduleTask(refreshTask, 20);
		
		addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				machine.getFastMachineTimer().cancelTask(refreshTask);
			}
		});
	}
	
	abstract public void flush();
	
	abstract public void setupEvents();

	abstract public void go();
	
	public void executed(ChangeBlock block) {
        
		// copy block so operand effects are distinct for each
        InstRow row = new InstRow(block, false);
        
        synchronized (instHistory) {
        	if (instHistory.size() >= MAX_INST_HISTORY) {
        		instHistory.subList(0, MAX_INST_HISTORY / 2).clear();
        	}
        	InstRow last = instHistory.size() > 0 ? instHistory.get(instHistory.size() - 1) : null;
			if (last != null && last.getInst().pc == row.getInst().pc && last.isGeneric()) {
	        	instHistory.remove(last);
	        }
	        instHistory.add(row);
	        
	        isDirty = true;
        }
	}

	/**
	 * 
	 */
	public void refresh() {
		ChangeBlock block = machine.getCpu().createChangeBlock(machine.getCpu().getState().getPC());
		InstRow row = new InstRow(block, true);
		
		synchronized (instHistory) {
			InstRow last = instHistory.size() > 0 ? instHistory.get(instHistory.size() - 1) : null;
			if (last == null || last.getInst().pc != row.getInst().pc) {
				instHistory.add(row);
				isDirty = true;
			}
		}
	}

	/**
	 * 
	 */
	public void clear() {
		synchronized (instHistory) {
			instHistory.clear();
			flush();
		}
	}
}
