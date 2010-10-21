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
package org.eclipse.tcf.internal.target.ui.actions;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.tcf.internal.target.ui.Activator;
import org.eclipse.tcf.internal.target.ui.TargetLaunchShortcut;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.ui.actions.BaseSelectionListenerAction;

public class AttachDebuggerAction extends BaseSelectionListenerAction {

	public AttachDebuggerAction() {
		super("Attach Debugger");
	}
	
	private class LauncherJob extends Job {
		public LauncherJob() {
			super("Attaching Debugger");
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			new TargetLaunchShortcut().launch(getStructuredSelection(), "debug");
			return Status.OK_STATUS;
		}
	}
	
	@Override
	public void run() {
		// Automate creating a Launch that connects to this target
		Object element = getStructuredSelection().getFirstElement();
		if (element instanceof ITarget) {
			new LauncherJob().schedule();
		}
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		ImageDescriptor desc = Activator.getImageDescriptor("icons/debug_exc.gif");
		if (desc != null)
			return desc;
		return super.getImageDescriptor();
	}
	
}
