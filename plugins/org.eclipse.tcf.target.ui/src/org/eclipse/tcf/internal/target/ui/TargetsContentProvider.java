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
package org.eclipse.tcf.internal.target.ui;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tcf.target.core.ITargetListener;
import org.eclipse.tcf.target.core.ITargetManager;
import org.eclipse.tcf.target.core.TargetEvent;

/**
 * Provides navigator content for Targets.
 * 
 * @author Doug Schaefer
 */
public class TargetsContentProvider implements ITreeContentProvider, ITargetListener {

	private TreeViewer viewer;
	private Object input;
	private final ITargetManager targetManager;

	public TargetsContentProvider() {
		targetManager = Activator.getService(ITargetManager.class);
		targetManager.addTargetListener(this);
	}

	@Override
	public void handleEvent(final TargetEvent event) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				if (viewer != null) {
					switch (event.getEventType()) {
					case ADDED:
					case DELETED:
						viewer.refresh();
						break;
					case LAUNCHED:
						viewer.refresh(event.getTarget());
						break;
					}
				}	
			}
		});
	}
	
	@Override
	public void dispose() {
		targetManager.removeTargetListener(this);
		viewer = null;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = (TreeViewer)viewer;
		this.input = newInput;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return targetManager.getTargets();
	}

	@Override
	public synchronized Object[] getChildren(Object parentElement) {
		if (parentElement == input)
			return targetManager.getTargets();
		return new Object[0];
	}

	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element == input)
			return true;
		else if (element instanceof ITarget)
			return false;
		return false;
	}

}
