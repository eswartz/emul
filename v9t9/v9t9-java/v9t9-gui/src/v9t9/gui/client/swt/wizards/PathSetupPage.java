/*
  PathSetupPage.java

  (c) 2013 Ed Swartz

  All rights reserved. This program and the accompanying materials
  are made available under the terms of the Eclipse Public License v1.0
  which accompanies this distribution, and is available at
  http://www.eclipse.org/legal/epl-v10.html
 */
package v9t9.gui.client.swt.wizards;

import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.widgets.Composite;

import v9t9.common.machine.IMachine;
import v9t9.gui.client.swt.SwtWindow;
import v9t9.gui.client.swt.shells.PathSetupComposite;
import v9t9.gui.client.swt.shells.PathSetupComposite.IPathSetupListener;

/**
 * Allow selecting the paths for searching ROMs
 * @author ejs
 *
 */
public class PathSetupPage extends WizardPage {
	
	private SwtWindow window;
	private IMachine machine;
	private PathSetupComposite composite;

	public PathSetupPage(IMachine machine_, SwtWindow window) {
		super("paths");
		this.window = window;
		this.machine = machine_;

		setTitle("Setup Paths");
		setDescription("Select paths on your system which contain system ROMs or modules.");
		
		setPageComplete(false);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		composite = new PathSetupComposite(parent, machine, window);
		setControl(composite);
		composite.addListener(new IPathSetupListener() {
			
			@Override
			public void allRequiredRomsFound(boolean found) {
				setPageComplete(found || true);
				//setPageComplete(found);
			}
		});
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.DialogPage#setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (visible) {
			composite.refresh();
		}

	}
	

}
