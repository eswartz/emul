/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
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
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.IActionDelegate2;
import org.eclipse.ui.IViewActionDelegate;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class SignalsCommand implements IViewActionDelegate, IActionDelegate2, IWorkbenchWindowActionDelegate {

    private IAction action;

    public void init(IViewPart view) {
        // TODO Auto-generated method stub
        
    }

    public void run(IAction action) {
        // TODO Auto-generated method stub
        
    }

    public void selectionChanged(IAction action, ISelection selection) {
        // TODO Auto-generated method stub
        
    }

    public void dispose() {
        action = null;
    }

    public void init(IAction action) {
        this.action = action;
    }

    public void runWithEvent(IAction action, Event event) {
        run(action);
    }

    public void init(IWorkbenchWindow window) {
        // TODO Auto-generated method stub
        
    }
}
