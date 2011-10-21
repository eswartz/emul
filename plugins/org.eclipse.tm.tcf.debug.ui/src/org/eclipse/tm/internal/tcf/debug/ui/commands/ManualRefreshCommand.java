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

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.action.IAction;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPartSite;

public class ManualRefreshCommand extends AbstractActionDelegate {

    // TODO: Automatic, Manual, Breakpoint Hit
    // org.eclipse.cdt.dsf.ui/src/org/eclipse/cdt/dsf/debug/internal/ui/viewmodel/actions/messages.properties
    // No update policies for current selection

    @Override
    protected void selectionChanged() {
        final IAction action = getAction();
        final TCFNode node = getRootNode();
        if (node == null) {
            action.setEnabled(false);
            action.setChecked(false);
        }
        else {
            new TCFTask<Object>(node.getChannel()) {
                public void run() {
                    IViewPart part = getView();
                    action.setEnabled(true);
                    action.setChecked(node.getModel().isLocked(part));
                    done(null);
                }
            }.getE();
        }
    }

    @Override
    protected void run() {
        final TCFNode node = getRootNode();
        if (node == null) return;
        new TCFTask<Object>(node.getChannel()) {
            public void run() {
                IViewPart part = getView();
                if (getAction().isChecked()) {
                    node.getModel().setLock(part);
                }
                else {
                    node.getModel().clearLock(part);
                }
                done(null);
            }
        }.getE();
    }

    private TCFNode getRootNode() {
        IViewPart view = getView();
        if (view == null) return null;
        IWorkbenchPartSite site = view.getSite();
        if (site == null || IDebugUIConstants.ID_DEBUG_VIEW.equals(site.getId())) {
            return null;
        }
        if (view instanceof IDebugView) {
            Object input = ((IDebugView)view).getViewer().getInput();
            if (input instanceof TCFNode) return (TCFNode)input;
        }
        return null;
    }
}
