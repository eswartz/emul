/*
  ROMSetupDialog.java

  (c) 2012-2013 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.files.IPathFileLocator.IPathChangeListener;
import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.SwtWindow;

/**
 * This tool shell lets the user configure paths for ROMs.
 * @author ejs
 *
 */
public class ROMSetupDialog extends Composite {
	public static final String ROM_SETUP_TOOL_ID = "rom.setup";

	public static ROMSetupDialog createDialog(Shell shell, final IMachine machine,
			final SwtWindow window) {
		 return new ROMSetupDialog(shell, machine, window);
	}

	public static IToolShellFactory getToolShellFactory(final IMachine machine, final SwtWindow window) {
		return new IToolShellFactory() {
			Behavior behavior = new Behavior();
			{
				behavior.boundsPref = "SetupWindowBounds";
				behavior.centering = Centering.CENTER;
				behavior.dismissOnClickOutside = false;
			}
			public Control createContents(Shell shell) {
				return new ROMSetupDialog(shell, machine, window);
			}
			public Behavior getBehavior() {
				return behavior;
			}
		};
	}

	private IMachine machine;

	private IPathChangeListener pathListener;

	protected boolean romsChanged;

	private boolean wasPaused;
	
	public ROMSetupDialog(Shell shell, IMachine machine_, SwtWindow window) {
		super(shell, SWT.NONE);
		this.machine = machine_;
		wasPaused = machine.setPaused(true);

		shell.setText("V9t9 Setup");
		
		Composite composite = this;
		
		GridLayoutFactory.fillDefaults().margins(6, 6).applyTo(composite);
		
		PathSetupComposite pathSetup = new PathSetupComposite(composite, machine, window);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(pathSetup);
		
		pathListener = new IPathChangeListener() {
			
			@Override
			public void pathsChanged() {
				romsChanged = true;
			}
		};
		machine.getRomPathFileLocator().addListener(pathListener);
		
		composite.addDisposeListener(new DisposeListener() {
			
			@Override
			public void widgetDisposed(DisposeEvent e) {
				machine.getRomPathFileLocator().removeListener(pathListener);
				if (romsChanged) {
					machine.getMemoryModel().loadMemory(machine.getEventNotifier());
					machine.reset();
					machine.setPaused(false);
				}
				else {
					machine.setPaused(wasPaused);
				}
			}
		});
		
		pathSetup.refresh();
	}

}
