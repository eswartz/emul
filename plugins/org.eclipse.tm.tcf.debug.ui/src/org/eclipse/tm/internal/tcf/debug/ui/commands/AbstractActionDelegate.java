/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.commands;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Event;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public abstract class AbstractActionDelegate
implements IViewActionDelegate, IActionDelegate2, IWorkbenchWindowActionDelegate, IObjectActionDelegate {

    private IAction action;
    private IViewPart view;
    private IWorkbenchWindow window;
    private ISelection selection;

    public void init(IAction action) {
        this.action = action;
    }

    public void init(IViewPart view) {
        this.view = view;
    }

    public void init(IWorkbenchWindow window) {
        this.window = window;
    }

    public void dispose() {
        action = null;
        view = null;
        window = null;
    }

    public void setActivePart(IAction action, IWorkbenchPart part) {
        this.action = action;
        view = null;
        if (part instanceof IViewPart) view = (IViewPart)part;
        window = part.getSite().getWorkbenchWindow();
    }

    public void run(IAction action) {
        IAction action0 = this.action;
        try {
            this.action = action;
            run();
        }
        finally {
            this.action = action0;
        }
    }

    public void runWithEvent(IAction action, Event event) {
        run(action);
    }

    public void selectionChanged(IAction action, ISelection selection) {
        this.selection = selection;
        IAction action0 = this.action;
        try {
            this.action = action;
            selectionChanged();
        }
        finally {
            this.action = action0;
        }
    }

    public IAction getAction() {
        return action;
    }

    public IViewPart getView() {
        return view;
    }

    public IWorkbenchWindow getWindow() {
        if (view != null) return view.getSite().getWorkbenchWindow();
        if (window != null) return window;
        return null;
    }

    public ISelection getSelection() {
        return selection;
    }

    public TCFNode getSelectedNode() {
        if (selection instanceof IStructuredSelection) {
            final Object o = ((IStructuredSelection)selection).getFirstElement();
            if (o instanceof TCFNode) return (TCFNode)o;
            if (o instanceof TCFLaunch) {
                return new TCFTask<TCFNode>() {
                    public void run() {
                        TCFLaunch launch = (TCFLaunch)o;
                        TCFModel model = Activator.getModelManager().getModel(launch);
                        if (model != null) {
                            done(model.getRootNode());
                        }
                        else {
                            done(null);
                        }
                    }
                }.getE();
            }
        }
        return null;
    }

    protected abstract void selectionChanged();

    protected abstract void run();
}
