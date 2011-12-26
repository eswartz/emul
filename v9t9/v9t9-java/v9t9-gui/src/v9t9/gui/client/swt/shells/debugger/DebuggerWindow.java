/**
 * 
 */
package v9t9.gui.client.swt.shells.debugger;

import java.util.Timer;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.bars.ImageBar;
import v9t9.gui.client.swt.shells.IToolShellFactory;

/**
 * @author Ed
 *
 */
public class DebuggerWindow extends Composite {

	private SashForm horizSash;
	/*private*/ final IMachine machine;
	/*private*/ CpuViewer cpuViewer;
	private SashForm vertSash;
	private RegisterViews regViewer;
	public static final String DEBUGGER_TOOL_ID = "debugger";

	public DebuggerWindow(Shell parent, int style, IMachine machine, Timer timer) {
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

	/**
	 * @param machine2
	 * @param buttonBar
	 * @param timer 
	 * @return
	 */
	public static IToolShellFactory getToolShellFactory(final IMachine machine,
			final ImageBar buttonBar, final Timer timer) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "DebuggerWindowBounds";
				behavior.dismissOnClickOutside = false;
				behavior.centerOverControl = buttonBar;
			}
			public Control createContents(Shell shell) {
				return new DebuggerWindow(shell, SWT.NONE, machine, timer);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}
}
