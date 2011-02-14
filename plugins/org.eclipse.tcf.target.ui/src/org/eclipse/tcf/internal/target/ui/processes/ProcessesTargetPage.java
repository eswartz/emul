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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tcf.target.ui.ITargetPage;

public class ProcessesTargetPage implements ITargetPage {

	private TreeViewer viewer;
	
	@Override
	public Composite createPage(Composite parent, ITarget target) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(2, false));
		
		viewer = new TreeViewer(composite, SWT.BORDER);
		viewer.setContentProvider(new ProcessesContentProvider());
		viewer.setLabelProvider(new ProcessesLabelProvider());
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setInput(target);

		Composite buttons = new Composite(composite, SWT.NONE);
		buttons.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false));
		buttons.setLayout(new GridLayout(1, false));
		
		Button refresh = new Button(buttons, SWT.PUSH);
		refresh.setLayoutData(new GridData());
		refresh.setText("Refresh");
		
		return composite;
	}

	@Override
	public String getPageText() {
		return "Processes";
	}

}
