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
package org.eclipse.tm.internal.tcf.cdt.ui.breakpoints;

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.cdt.debug.core.model.ICBreakpointExtension;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.tm.internal.tcf.cdt.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.model.ITCFConstants;
import org.eclipse.tm.tcf.services.IBreakpoints;

public class TCFBreakpointScopeExtension implements ICBreakpointExtension {

    static final String ATTR_CONTEXT_IDS = ITCFConstants.ID_TCF_DEBUG_MODEL + '.' + IBreakpoints.PROP_CONTEXTIDS;

    private String[] fContextIds;
    private ICBreakpoint fBreakpoint;

    public void initialize(ICBreakpoint breakpoint) throws CoreException {
        fBreakpoint = breakpoint;
        IMarker m = breakpoint.getMarker();
        if (m != null && m.exists()) {
            String contextIdAttr = m.getAttribute(ATTR_CONTEXT_IDS, null);
            if (contextIdAttr != null) {
                fContextIds = contextIdAttr.split(",\\s*");
            }
        }
    }

    public void setThreadFilter(String[] threadIds) {
        fContextIds = threadIds;
        if (fBreakpoint == null) {
            return;
        }
        IMarker m = fBreakpoint.getMarker();
        if (m == null || !m.exists()) {
            return;
        }
        String contextIdAttr = null;
        if (threadIds != null) {
            if (threadIds.length == 0) {
                // empty string is filtered out in TCFBreakpointsModel
                contextIdAttr = " ";
            } else {
                StringBuilder buf = new StringBuilder();
                for (int i=0; i < threadIds.length - 1; ++i) {
                    buf.append(threadIds[i]).append(',');
                }
                buf.append(threadIds[threadIds.length - 1]);
                contextIdAttr = buf.toString();
            }
        }
        try {
            m.setAttribute(ATTR_CONTEXT_IDS, contextIdAttr);
        }
        catch (CoreException e) {
            Activator.log(e);
        }
    }

    public String[] getThreadFilters() {
        return fContextIds;
    }

}
