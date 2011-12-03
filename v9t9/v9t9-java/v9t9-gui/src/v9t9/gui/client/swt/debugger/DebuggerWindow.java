/**
 * 
 */
package v9t9.gui.client.swt.debugger;

import java.util.Timer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

import v9t9.engine.machine.Machine;

/**
 * @author Ed
 *
 */
public class DebuggerWindow extends Composite {

	private SashForm horizSash;
	/*private*/ final Machine machine;
	/*private*/ CpuViewer cpuViewer;
	private SashForm vertSash;
	private RegisterViews regViewer;

	public DebuggerWindow(Shell parent, int style, Machine machine, Timer timer) {
		super(parent, style);
		this.machine = machine;
		GridLayoutFactory.fillDefaults().applyTo(this);
		
		parent.setText("V9t9 Debugger");
		
		
		horizSash = new SashForm(this, SWT.HORIZONTAL);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(horizSash);
		
		SashForm innerSash = new SashForm(horizSash, SWT.VERTICAL);
		
		cpuViewer = new CpuViewer(innerSash, SWT.BORDER, machine, timer);
		regViewer = new RegisterViews(innerSash, SWT.BORDER, machine);

		GridDataFactory.fillDefaults().grab(true, true).applyTo(cpuViewer);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(regViewer);

		cpuViewer.setTracker(regViewer);
		
		vertSash = new SashForm(horizSash, SWT.VERTICAL);
		
		new MemoryViewer(vertSash, SWT.BORDER, machine.getMemory(), timer);
		new MemoryViewer(vertSash, SWT.BORDER, machine.getMemory(), timer);
		new MemoryViewer(vertSash, SWT.BORDER, machine.getMemory(), timer);
	}
}
