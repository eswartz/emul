/*
  RegisterViews.java

  (c) 2011-2016 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.debugger;

import org.eclipse.swt.widgets.Composite;

import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.shells.debugger.CpuViewer.ICpuTracker;

/**
 * View CPU, VDP, GPL registers
 * @author ejs
 *
 */
public class RegisterViews implements ICpuTracker {

	private RegisterViewer cpuRegisterViewer;
	private RegisterViewer vdpRegisterViewer;
	private RegisterViewer gplRegisterViewer;
	
	public RegisterViews(Composite parent, int style, final IMachine machine) {
		IRegisterProvider cpuRegs = new CpuRegisterProvider(machine);
		IRegisterProvider vdpRegs = new VdpRegisterProvider(machine);
		IRegisterProvider gplRegs = new GplRegisterProvider(machine);
		
		cpuRegisterViewer = new RegisterViewer(parent, machine, cpuRegs, 4);
		vdpRegisterViewer = new RegisterViewer(parent, machine, vdpRegs, 
				vdpRegs.getRegisterCount() < 16 ? 4 : 12);
		gplRegisterViewer = new RegisterViewer(parent, machine, gplRegs, 4);  
	}


	public void updateForInstruction() {
		cpuRegisterViewer.update();
		vdpRegisterViewer.update();
		gplRegisterViewer.update();
	}

}
