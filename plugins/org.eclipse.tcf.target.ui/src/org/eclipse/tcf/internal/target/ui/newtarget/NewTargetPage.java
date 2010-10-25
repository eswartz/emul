/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.target.ui.newtarget;

import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

/**
 * Wizard page to set attributes for the target launch.
 * TODO assuming SSH for now.
 * 
 * @author Doug Schaefer
 */
public class NewTargetPage extends WizardPage {
	
	private Text hostnameText;
	private Text usernameText;
	private Text passwordText;
	
	private static final String HOSTNAME = "hostname";
	private static final String USERNAME = "username";
	
	public NewTargetPage() {
		super("NewTargetPage");
		setTitle("Target Agent Launch");
		setDescription("Enter SSH login information to use when launching the target agent.");
	}
	
	@Override
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.None);
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		
		IDialogSettings settings = getDialogSettings();
		
		Label label = new Label(comp, SWT.NONE);
		label.setText("Host:");
		
		hostnameText = new Text(comp, SWT.BORDER);
		hostnameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		String hostname = settings.get(HOSTNAME);
		if (hostname != null)
			hostnameText.setText(hostname);
		
		label = new Label(comp, SWT.NONE);
		label.setText("User name:");
		
		usernameText = new Text(comp, SWT.BORDER);
		usernameText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		String username = settings.get(USERNAME);
		if (username != null)
			usernameText.setText(settings.get(USERNAME));
		
		label = new Label(comp, SWT.NONE);
		label.setText("Password:");
		
		passwordText = new Text(comp, SWT.BORDER);
		passwordText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		passwordText.setEchoChar('*');
		
		setControl(comp);
	}

	public void performFinish() {
		// Save away the host and username
		IDialogSettings settings = getDialogSettings();
		settings.put(HOSTNAME, hostnameText.getText());
		settings.put(USERNAME, usernameText.getText());
	}

}
