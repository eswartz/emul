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

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.CDIDebugModel;
import org.eclipse.cdt.debug.internal.ui.actions.AddWatchpointDialog;
import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.handlers.HandlerUtil;

@SuppressWarnings("restriction")
public class AddWatchpointHandler extends AbstractHandler {
    public Object execute(ExecutionEvent event) throws ExecutionException {
        ISelection selection = HandlerUtil.getCurrentSelection(event);
        String expr = null;
        if (selection instanceof ITextSelection) {
                expr = ((ITextSelection) selection).getText();
        }
        AddWatchpointDialog dlg = new AddWatchpointDialog(HandlerUtil.getActiveShell(event), null);
        dlg.setExpression(expr);
        if (dlg.open() == Window.OK) {
            addWatchpoint(dlg.getWriteAccess(), dlg.getReadAccess(), dlg.getExpression(), dlg.getMemorySpace(), dlg.getRange());
        }
        return null;
    }

    private void addWatchpoint(boolean write, boolean read, String expression, String memorySpace, BigInteger range) {
        IResource resource = ResourcesPlugin.getWorkspace().getRoot();
        try {
            CDIDebugModel.createWatchpoint("", resource, write, read, expression, memorySpace, range, true, 0, "", true); //$NON-NLS-1$
        }
        catch(CoreException ce) {
            CDebugUIPlugin.errorDialog("Cannot add watchpoint.", ce);
        }
    }
}
