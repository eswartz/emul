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
package org.eclipse.tcf.internal.target.ui.properties;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tcf.internal.target.core.LocalTarget;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Page for viewing and setting the Local Target connection properties.
 * 
 * @author Doug Schaefer
 */
public class LocalPropertyPage extends PropertyPage implements IWorkbenchPropertyPage {

	private Text hostText;
	private Text portText;

	@Override
	protected Control createContents(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		comp.setLayout(new GridLayout(2, false));

		LocalTarget target = (LocalTarget)getElement();
		
		Label hostLabel = new Label(comp, SWT.NONE);
		hostLabel.setText("Host:");
		
		hostText = new Text(comp, SWT.BORDER);
		hostText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		hostText.setText(target.getHost());
		hostText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setValid(validHost());
			}
		});
		
		Label portLabel = new Label(comp, SWT.NONE);
		portLabel.setText("Port:");
		
		portText = new Text(comp, SWT.BORDER);
		portText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		portText.setText(target.getPort());
		portText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				setValid(validPort());
			}
		});
		
		return comp;
	}

	private boolean validHost() {
		// valid if field isn't empty
		return !hostText.getText().isEmpty();
	}
	
	private boolean validPort() {
		// valid if text is a number
		String port = portText.getText();
		if (port.isEmpty()) {
			return false;
		} else {
			try {
				Integer.parseInt(portText.getText());
				return true;
			} catch (NumberFormatException ex) {
				return false;
			}
		}
	}
	
	@Override
	public String getErrorMessage() {
		if (!validHost())
			return "Host name is invalid";
		
		if (!validPort())
			return "Port number is invalid";
		
		return null;
	}
	
	@Override
	public boolean performOk() {
		LocalTarget target = (LocalTarget)getElement();
		target.setHost(hostText.getText().trim());
		target.setPort(portText.getText().trim());
		return super.performOk();
	}
	
}
