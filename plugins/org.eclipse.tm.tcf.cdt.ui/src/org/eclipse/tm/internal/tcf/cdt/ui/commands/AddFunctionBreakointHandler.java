/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui.commands;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.core.model.ICBreakpointType;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Command handler to add a CDT function breakpoint on an arbitrary symbol.
 */
public class AddFunctionBreakointHandler extends AbstractHandler {

    private static class AddFunctionBreakpointDialog extends InputDialog {
        public AddFunctionBreakpointDialog(Shell parentShell, String initialValue) {
            super(parentShell, "Add Function Breakpoint", "Enter symbol:", initialValue, null);
        }
    }

    public Object execute(ExecutionEvent event) throws ExecutionException {
        Shell shell = HandlerUtil.getActiveShell(event);
        AddFunctionBreakpointDialog dialog = new AddFunctionBreakpointDialog(shell, "");
        if (dialog.open() == Window.OK) {
            final String symbol = dialog.getValue();
            Job job = new WorkspaceJob("Create Function Breakpoint") {
                @Override
                public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
                    IResource resource = ResourcesPlugin.getWorkspace().getRoot();
                    CDIDebugModel.createFunctionBreakpoint(
                            null,
                            resource,
                            ICBreakpointType.REGULAR,
                            symbol,
                            -1,
                            -1,
                            -1,
                            true,
                            -1,
                            null,
                            true);
                    return Status.OK_STATUS;
                }
            };
            job.setSystem(true);
            job.setPriority(Job.SHORT);
            job.schedule();
        }
        return null;
    }

}
