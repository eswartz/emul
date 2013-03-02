/*
  RegisterViews.java

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
package v9t9.gui.client.swt.shells.debugger;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.shells.debugger.CpuViewer.ICpuTracker;

/**
 * View CPU and VDP registers
 * @author ejs
 *
 */
public class RegisterViews extends SashForm implements ICpuTracker {

	private RegisterViewer cpuRegisterViewer;
	private RegisterViewer vdpRegisterViewer;
	
	public RegisterViews(Composite parent, int style, final IMachine machine) {
		super(parent, style | SWT.VERTICAL);
		
		setLayout(new GridLayout());

		IRegisterProvider cpuRegs = new CpuRegisterProvider(machine);
		IRegisterProvider vdpRegs = new VdpRegisterProvider(machine);
		
		cpuRegisterViewer = new RegisterViewer(this, machine, cpuRegs, 4);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(cpuRegisterViewer);
		vdpRegisterViewer = new RegisterViewer(this, machine, vdpRegs, 
				vdpRegs.getRegisterCount() < 16 ? 4 : 12);
		GridDataFactory.swtDefaults().grab(true, false).align(SWT.FILL, SWT.TOP).applyTo(vdpRegisterViewer);
	}


	public void updateForInstruction() {
		cpuRegisterViewer.update();
		vdpRegisterViewer.update();
	}

}
