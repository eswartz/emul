/*
  EmulatorRnDBar.java

  (c) 2011-2012 Edward Swartz

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
package v9t9.gui.client.swt.bars;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

import v9t9.common.cpu.ICpu;
import v9t9.common.machine.IMachine;
import v9t9.common.settings.Settings;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.bars.IImageBar.IPaintOffsetListener;
import v9t9.gui.client.swt.shells.CpuMetricsCanvas;
import v9t9.gui.client.swt.shells.SettingsDialog;
import v9t9.gui.client.swt.shells.SpeechDialog;
import v9t9.gui.client.swt.shells.debugger.DebuggerWindow;
import ejs.base.properties.IProperty;
import ejs.base.properties.IPropertyListener;

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
	private Canvas cpuMetricsCanvas;
	

	
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
			int[] colors, float[] points, int style) {
		super(window, imageProvider, parent, machine, colors, points, style);
		
		boolean isHorizontal = (style & SWT.HORIZONTAL) != 0;
		
		if ((style & SWT.HORIZONTAL) != 0) {
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
		

		ImageBarChild cpuMetricsCanvasHolder = new ImageBarChild(buttonBar, 0);
		cpuMetricsCanvas = new CpuMetricsCanvas(cpuMetricsCanvasHolder, 
				SWT.BORDER | (isHorizontal ? SWT.HORIZONTAL : SWT.VERTICAL), 
				machine.getCpuMetrics(), true);
		GridDataFactory.fillDefaults()
			.align(isHorizontal ? SWT.LEFT : SWT.FILL, isHorizontal ? SWT.FILL : SWT.BOTTOM)
			//.grab(false, false)
			.indent(4, 4).exclude(true).applyTo(cpuMetricsCanvas);
		
		buttonBar.addPaintOffsetListener(new IPaintOffsetListener() {
			
			@Override
			public void offsetChanged(Point pt) {
				cpuMetricsCanvas.setLocation(pt);
			}
		});

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

//		createButton(IconConsts.DEBUGGER,
//			"Create debugger window", new SelectionAdapter() {
//				@Override
//				public void widgetSelected(SelectionEvent e) {
//					swtWindow.toggleToolShell(DebuggerWindow.DEBUGGER_TOOL_ID, 
//							DebuggerWindow.getToolShellFactory(machine, buttonBar, swtWindow.getToolUiTimer()));
//				}
//			}
//		);
		
		createToggleStateButton(ICpu.settingDebugging,
				IconConsts.DEBUGGER, 
				IconConsts.CHECKMARK_OVERLAY, "Toggle debugging");
		
		
		IProperty debugging = Settings.get(machine, ICpu.settingDebugging);
		debugging.addListenerAndFire(new IPropertyListener() {
			
			@Override
			public void propertyChanged(final IProperty property) {
				getButtonBar().getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (property.getBoolean())
							swtWindow.showToolShell(DebuggerWindow.DEBUGGER_TOOL_ID, 
									DebuggerWindow.getToolShellFactory(machine, buttonBar, swtWindow.getToolUiTimer()));
						else
							swtWindow.closeToolShell(DebuggerWindow.DEBUGGER_TOOL_ID);
					}
				});
			}
		});

		
		createButton(IconConsts.SPEECH, "Speech options", 
			new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e) {
					swtWindow.toggleToolShell(SpeechDialog.SPEECH_DIALOG_TOOL_ID, 
							SpeechDialog.getToolShellFactory(buttonBar, machine, swtWindow));
				}
		});
		
		createButton(IconConsts.SETTINGS, "Settings", 
				new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e) {
						swtWindow.toggleToolShell(SettingsDialog.SETTINGS_DIALOG_TOOL_ID, 
								SettingsDialog.getToolShellFactory(buttonBar, machine, swtWindow));
					}
			});
			
		
	}
	
	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.bars.BaseEmulatorBar#dispose()
	 */
	@Override
	public void dispose() {
		super.dispose();
		
		cpuMetricsCanvas.dispose();

		
	}

	
}
