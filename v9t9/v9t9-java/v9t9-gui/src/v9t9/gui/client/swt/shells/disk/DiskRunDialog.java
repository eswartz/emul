/*
  DiskBrowseDialog.java

  (c) 2011-2012 Edward Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.shells.disk;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import v9t9.common.files.Catalog;
import v9t9.common.files.IFileExecutor;
import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.fileimport.FileExecutorComposite;

public class DiskRunDialog extends Dialog {
	private final Catalog catalog;
	private final IMachine machine;
	private FileExecutorComposite execComp;
	private int drive;
	{
	}

	public DiskRunDialog(Shell parentShell,
			IMachine machine,
			int drive,
			Catalog catalog) {
		super(parentShell);
		this.machine = machine;
		this.drive = drive;
		this.catalog = catalog;
		setShellStyle(getShellStyle() | SWT.RESIZE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.window.Window#configureShell(org.eclipse.swt.widgets.Shell)
	 */
	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Run Programs on " + catalog.deviceName);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		Composite composite = (Composite) super.createDialogArea(parent);
		
		execComp = new FileExecutorComposite(composite, machine);
		GridDataFactory.fillDefaults().grab(true,true).applyTo(execComp);
		
		execComp.updateExecs(drive, catalog, false);
		
		return composite;
	}
	
	public IFileExecutor getFileExecutor() {
		return execComp.getFileExecutor();
	}

}