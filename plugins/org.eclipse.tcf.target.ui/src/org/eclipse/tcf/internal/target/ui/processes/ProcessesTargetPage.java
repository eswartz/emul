/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.target.ui.processes;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.tcf.internal.target.ui.processes.ProcessesContentProvider.RootNode;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tcf.target.ui.ITargetPage;

public class ProcessesTargetPage implements ITargetPage {

	private TreeViewer viewer;
	
	@Override
	public Composite createPage(Composite parent, final ITarget target) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		
		Composite buttons = new Composite(composite, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		buttons.setLayout(new GridLayout(2, false));
		
		Label label = new Label(buttons, SWT.NONE);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false));
		label.setText("Processes running on the target");
		
		Button refresh = new Button(buttons, SWT.PUSH);
		refresh.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		refresh.setText("Refresh");
		refresh.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// Force a refresh of the data
				target.getLocalProperties().remove(RootNode.propertyName);
				viewer.refresh();
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		});
		
		viewer = new TreeViewer(composite, SWT.BORDER);
		viewer.setContentProvider(new ProcessesContentProvider(false));
		viewer.setLabelProvider(new ProcessesLabelProvider());
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setInput(target);

		return composite;
	}

	@Override
	public String getPageText() {
		return "Processes";
	}

}
