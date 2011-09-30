/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.debugger;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import v9t9.emulator.clients.builtin.swt.debugger.CpuViewer.ICpuTracker;
import v9t9.emulator.common.Machine;

/**
 * View CPU and VDP registers
 * @author ejs
 *
 */
public class RegisterViews extends SashForm implements ICpuTracker {

	private RegisterViewer cpuRegisterViewer;
	private RegisterViewer vdpRegisterViewer;
	
	public RegisterViews(Composite parent, int style, final Machine machine) {
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
