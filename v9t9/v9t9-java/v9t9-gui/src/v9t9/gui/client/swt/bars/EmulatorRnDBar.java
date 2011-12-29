/**
 * Dec 27, 2011
 */
package v9t9.gui.client.swt.bars;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import v9t9.common.cpu.ICpu;
import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.shells.debugger.DebuggerWindow;

/**
 * This is the bar of command buttons and status indicators for
 * use when developing the emulator or being a coder in general.
 * The bar is present only when the user explicitly enables it,
 * since it has some confusing or obscure commands inside.
 * 
 * @author ejs
 *
 */
public class EmulatorRnDBar extends BaseEmulatorBar  {
	/**
	 * @param parent 
	 * @param isHorizontal 
	 * @param parent
	 * @param style
	 * @param focusRestorer
	 * @param smoothResize
	 */
	public EmulatorRnDBar(final SwtWindow window, ImageProvider imageProvider, Composite parent, 
			final IMachine machine,
			int[] colors, float[] points, boolean isHorizontal) {
		super(window, imageProvider, parent, machine, colors, points, isHorizontal);
		
		if (isHorizontal) {
			GridData gd = ((GridData) buttonBar.getLayoutData());
			gd.verticalSpan = 2;
		}
		
		buttonBar.addControlListener(new ControlAdapter() {
			@Override
			public void controlMoved(ControlEvent e) {
				swtWindow.recenterToolShells();
			}
			@Override
			public void controlResized(ControlEvent e) {
				swtWindow.recenterToolShells();
				buttonBar.setMaxIconSize(Math.max(16, Math.min(48, swtWindow.getShell().getSize().y / 8)));
			}
		});
		
		buttonBar.getDisplay().addFilter(SWT.MouseUp, new Listener() {
			
			public void handleEvent(Event event) {
				if (!(event.widget instanceof Control))
					return;
				if (((Control) event.widget).getShell() != swtWindow.getShell())
					return;
				if (event.button == 1) {
					Point pt = ((Control)event.widget).toDisplay(event.x, event.y);
					swtWindow.handleClickOutsideToolWindow(pt);
				}
			}
		});
		
		buttonBar.setMaxIconSize(48);
		buttonBar.setMinIconSize(16);

		createButton(IconConsts.INTERRUPT, "Send a non-maskable interrupt",
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						machine.getCpu().nmi();
					}
				});

		createToggleStateButton(ICpu.settingDumpFullInstructions,
				IconConsts.CPU_LOGGING, 
				IconConsts.CHECKMARK_OVERLAY, "Toggle CPU logging");

		createButton(IconConsts.DEBUGGER,
				"Create debugger window", new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						swtWindow.toggleToolShell(DebuggerWindow.DEBUGGER_TOOL_ID, 
								DebuggerWindow.getToolShellFactory(machine, buttonBar, swtWindow.getToolUiTimer()));
					}
			}
		);
		

	}

}
