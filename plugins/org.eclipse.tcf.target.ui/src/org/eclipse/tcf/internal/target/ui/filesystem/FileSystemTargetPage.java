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
package org.eclipse.tcf.internal.target.ui.filesystem;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tcf.target.ui.ITargetPage;

public class FileSystemTargetPage implements ITargetPage {

	private TreeViewer viewer;
	
	@Override
	public Composite createPage(Composite parent, ITarget target) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		composite.setLayout(layout);

		viewer = new TreeViewer(composite, SWT.BORDER);
		viewer.setContentProvider(new FileSystemContentProvider(false));
		viewer.setLabelProvider(new FileSystemLabelProvider());		
		viewer.getControl().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		viewer.setInput(target);
		
		return composite;
	}
	
	@Override
	public String getPageText() {
		return "File System";
	}
	
}
