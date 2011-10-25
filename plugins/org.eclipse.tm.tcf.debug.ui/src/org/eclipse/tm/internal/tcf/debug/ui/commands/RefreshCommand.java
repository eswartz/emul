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
package org.eclipse.tm.internal.tcf.debug.ui.commands;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.debug.ui.memory.IMemoryRenderingSite;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartSite;

public class RefreshCommand extends AbstractActionDelegate {

    @Override
    protected void selectionChanged() {
        getAction().setEnabled(getRootNode() != null);
    }

    @Override
    protected void run() {
        final TCFNode node = getRootNode();
        if (node == null) return;
        new TCFTask<Object>(node.getChannel()) {
            public void run() {
                IViewPart part = getView();
                TCFModel model = node.getModel();
                TCFNode ref_node = node;
                if (part instanceof IMemoryRenderingSite) {
                    // Search memory node
                    TCFDataCache<TCFNodeExecContext> mem_cache = model.searchMemoryContext(node);
                    if (mem_cache == null) {
                        done(null);
                        return;
                    }
                    if (!mem_cache.validate(this)) return;
                    ref_node = mem_cache.getData();
                }
                if (ref_node != null) {
                    ref_node.refresh(part);
                    if (model.clearLock(part)) {
                        model.setLock(part);
                    }
                }
                done(null);
            }
        }.getE();
    }

    private TCFNode getRootNode() {
        IViewPart view = getView();
        if (view == null) return null;
        IWorkbenchPartSite site = view.getSite();
        if (site != null && IDebugUIConstants.ID_DEBUG_VIEW.equals(site.getId())) {
            TCFNode n = getSelectedNode();
            if (n == null) return null;
            return n.getModel().getRootNode();
        }
        if (site != null && IDebugUIConstants.ID_MEMORY_VIEW.equals(site.getId())) {
            ISelection selection = DebugUITools.getDebugContextManager().getContextService(
                    site.getWorkbenchWindow()).getActiveContext();
            if (selection instanceof IStructuredSelection) {
                Object obj = ((IStructuredSelection)selection).getFirstElement();
                if (obj instanceof TCFNode) return (TCFNode)obj;
            }
        }
        if (view instanceof IDebugView) {
            Object input = ((IDebugView)view).getViewer().getInput();
            if (input instanceof TCFNode) return (TCFNode)input;
        }
        return null;
    }
}
