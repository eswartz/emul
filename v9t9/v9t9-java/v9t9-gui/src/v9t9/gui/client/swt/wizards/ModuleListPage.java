/*
  ModuleListPage.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.wizards;

import java.io.File;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.shells.modules.ModuleListComposite;
import v9t9.gui.client.swt.shells.modules.ModuleListComposite.IStatusListener;

/**
 * Allow setting up module lists
 * @author ejs
 *
 */
public class ModuleListPage extends WizardPage implements IStatusListener {
	
	private SwtWindow window;
	private IMachine machine;
	private ModuleListComposite composite;

	public ModuleListPage(IMachine machine_, SwtWindow window) {
		super("paths");
		this.window = window;
		this.machine = machine_;

		setTitle("Manage Module List");
		setDescription("Select which detected modules are visible in the Module Selector.");
		
		setPageComplete(false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		composite = new ModuleListComposite(parent, machine, window, this);
		setControl(composite);
		
		GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
	}

	/* (non-Javadoc)
	 * @see v9t9.gui.client.swt.shells.modules.ModuleListComposite.IStatusListener#statusUpdated(org.eclipse.core.runtime.IStatus)
	 */
	@Override
	public void statusUpdated(IStatus status) {
		if (status == null)
			setMessage(null, INFORMATION);
		else
			setMessage(status.getMessage(), 
				status.getSeverity() == IStatus.INFO ? INFORMATION
						: status.getSeverity() == IStatus.WARNING ? WARNING 
							: 	/*status.getSeverity() == IStatus.ERROR*/ ERROR);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			composite.refresh();
			setPageComplete(true);
		}
			
	}

	/**
	 * 
	 */
	public File getModuleDatabase() {
		return composite != null ? composite.getModuleDatabase() : null;
	}

	/**
	 * 
	 */
	public boolean save() {
		return composite.saveModuleLists();
	}
	

}
