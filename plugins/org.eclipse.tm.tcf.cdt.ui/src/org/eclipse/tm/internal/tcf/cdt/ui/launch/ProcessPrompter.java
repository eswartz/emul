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
package org.eclipse.tm.internal.tcf.cdt.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStatusHandler;
import org.eclipse.jface.window.Window;
import org.eclipse.tm.internal.tcf.cdt.launch.ContextSelection;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate;
import org.eclipse.ui.PlatformUI;

public class ProcessPrompter implements IStatusHandler {

    public Object handleStatus(IStatus status, Object source) throws CoreException {
        ILaunchConfiguration config = (ILaunchConfiguration) source;
        String peerId = config.getAttribute(TCFLaunchDelegate.ATTR_PEER_ID, (String) null);
        String contextId = config.getAttribute("attach_to_process", (String) null);
        if (peerId == null || contextId == null) {
            ContextSelection selection = new ContextSelection(peerId, contextId);
            ProcessSelectionDialog diag = new ProcessSelectionDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow());
            diag.setSelection(selection);
            if (diag.open() == Window.OK) {
                return diag.getSelection();
            }
        }
        return null;
    }

}
