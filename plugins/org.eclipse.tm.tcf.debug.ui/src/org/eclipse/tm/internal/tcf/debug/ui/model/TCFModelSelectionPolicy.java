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
package org.eclipse.tm.internal.tcf.debug.ui.model;

import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelSelectionPolicy;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tm.tcf.util.TCFTask;

class TCFModelSelectionPolicy implements IModelSelectionPolicy {
    
    private final TCFModel model;
    
    TCFModelSelectionPolicy(TCFModel model) {
        this.model = model;
    }
    
    public boolean contains(ISelection selection, IPresentationContext context) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection)selection;
            Object e = ss.getFirstElement();
            if (e instanceof TCFNode) {
                TCFNode n = (TCFNode)e;
                return !n.isDisposed() && n.getModel() == model;
            }
        }
        return false;
    }

    public boolean isSticky(ISelection selection, IPresentationContext context) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection)selection;
            Object e = ss.getFirstElement();
            if (e instanceof TCFNode) return isSuspended((TCFNode)e);
        }
        return false;
    }
    
    private boolean isSuspended(final TCFNode node) {
        return new TCFTask<Boolean>() {
            public void run() {
                TCFNode n = node;
                while (n != null && !n.isDisposed()) {
                    if (n instanceof TCFNodeExecContext) {
                        if (!n.validateNode(this)) return;
                        if (((TCFNodeExecContext)n).isSuspended()) {
                            done(Boolean.TRUE);
                            return;
                        }
                    }
                    n = n.parent;
                }
                done(Boolean.FALSE);
            }
        }.getE();
    }

    public boolean overrides(ISelection existing, ISelection candidate, IPresentationContext context) {
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(context.getId())) {  
            if (existing instanceof IStructuredSelection && candidate instanceof IStructuredSelection) {
                Object el_existing = ((IStructuredSelection)existing).getFirstElement();
                Object el_candidate = ((IStructuredSelection)candidate).getFirstElement();
                if (el_existing == null) return true;
                if (el_existing == el_candidate) return true;
                if (el_existing instanceof TCFNode && el_candidate instanceof TCFNode) {
                    if (el_existing instanceof TCFNodeStackFrame && el_candidate instanceof TCFNodeStackFrame) {
                        TCFNodeStackFrame curr = (TCFNodeStackFrame)el_existing;
                        TCFNodeStackFrame next = (TCFNodeStackFrame)el_candidate;
                        return curr.parent == next.parent || !isSuspended(curr);
                    }
                    return !isSuspended((TCFNode)el_existing);
                }
            }
        }
        return true;
    }

    public ISelection replaceInvalidSelection(ISelection invalid_selection, ISelection new_selection) {
        return new_selection;
    }
}
