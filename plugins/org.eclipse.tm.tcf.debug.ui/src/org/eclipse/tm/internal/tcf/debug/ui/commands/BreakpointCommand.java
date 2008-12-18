/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.commands;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetExtension;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tm.internal.tcf.debug.model.TCFBreakpoint;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.ui.IWorkbenchPart;


public class BreakpointCommand implements IToggleBreakpointsTargetExtension {
    
    public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
        Object obj = ((IStructuredSelection)selection).getFirstElement();
        if (obj instanceof TCFNode) {
            final TCFNode node = (TCFNode)obj;
            if (node == null) return false;
            final boolean[] res = new boolean[1];
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    res[0] = node.getAddress() != null;
                }
            });
            return res[0];
        }
        else {
            return false;
        }
    }

    public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        Object obj = ((IStructuredSelection)selection).getFirstElement();
        if (obj instanceof TCFNode) {
            final TCFNode node = (TCFNode)obj;
            if (node == null) return;
            final CoreException[] res = new CoreException[1];
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    try {
                        BigInteger addr = node.getAddress();
                        if (addr == null) return;
                        Map<String,Object> m = new HashMap<String,Object>();
                        m.put(IBreakpoints.PROP_ENABLED, Boolean.TRUE);
                        m.put(IBreakpoints.PROP_LOCATION, addr.toString());
                        new TCFBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), m);
                    }
                    catch (CoreException x) {
                        res[0] = x;
                    }
                }
            });
            if (res[0] != null) throw res[0];
        }
    }

    public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
        return false;
    }

    public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        // TODO: breakpoint command: toggle line breakpoint
    }

    public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
        return false;
    }

    public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
    }

    public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
        return false;
    }

    public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        // TODO: breakpoint command: toggle watchpoint
    }
}
