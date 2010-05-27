/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeStackFrame;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IWorkbenchPart;


public class BreakpointCommand implements IToggleBreakpointsTargetExtension {

    public boolean canToggleBreakpoints(IWorkbenchPart part, ISelection selection) {
        if (selection.isEmpty()) return false;
        final Object obj = ((IStructuredSelection)selection).getFirstElement();
        return new TCFTask<Boolean>() {
            public void run() {
                TCFDataCache<BigInteger> addr_cache = null;
                if (obj instanceof TCFNodeExecContext) addr_cache = ((TCFNodeExecContext)obj).getAddress();
                if (obj instanceof TCFNodeStackFrame) addr_cache = ((TCFNodeStackFrame)obj).getAddress();
                if (addr_cache != null) {
                    if (!addr_cache.validate(this)) return;
                    done(addr_cache.getData() != null);
                }
                else {
                    done(false);
                }
            }
        }.getE();
    }

    public void toggleBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
        if (selection.isEmpty()) return;
        final Object obj = ((IStructuredSelection)selection).getFirstElement();
        CoreException x = new TCFTask<CoreException>() {
            public void run() {
                try {
                    TCFDataCache<BigInteger> addr_cache = null;
                    if (obj instanceof TCFNodeExecContext) addr_cache = ((TCFNodeExecContext)obj).getAddress();
                    if (obj instanceof TCFNodeStackFrame) addr_cache = ((TCFNodeStackFrame)obj).getAddress();
                    if (addr_cache != null) {
                        if (!addr_cache.validate(this)) return;
                        BigInteger addr = addr_cache.getData();
                        if (addr != null) {
                            Map<String,Object> m = new HashMap<String,Object>();
                            m.put(IBreakpoints.PROP_ENABLED, Boolean.TRUE);
                            m.put(IBreakpoints.PROP_LOCATION, addr.toString());
                            new TCFBreakpoint(ResourcesPlugin.getWorkspace().getRoot(), m);
                        }
                    }
                    done(null);
                }
                catch (CoreException x) {
                   done(x);
                }
            }
        }.getE();
        if (x != null) throw x;
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
