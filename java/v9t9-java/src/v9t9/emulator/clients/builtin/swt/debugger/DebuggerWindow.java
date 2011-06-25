/**
 * 
 */
package v9t9.emulator.clients.builtin.swt.debugger;

import java.util.Timer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;

import v9t9.emulator.common.Machine;

/**
 * @author Ed
 *
 */
public class DebuggerWindow extends Composite {

	private SashForm horizSash;
	/*private*/ final Machine machine;
	/*private*/ CpuViewer cpuViewer;
	private SashForm vertSash;

	public DebuggerWindow(Composite parent, int style, Machine machine, Timer timer) {
		super(parent, style);
		this.machine = machine;
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		horizSash = new SashForm(this, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(horizSash);
		
		cpuViewer = new CpuViewer(horizSash, SWT.BORDER, machine, timer);
		
		vertSash = new SashForm(horizSash, SWT.VERTICAL);
		
		new MemoryViewer(vertSash, SWT.BORDER, machine.getMemory(), timer);
		new MemoryViewer(vertSash, SWT.BORDER, machine.getMemory(), timer);
		new MemoryViewer(vertSash, SWT.BORDER, machine.getMemory(), timer);
	}
}
