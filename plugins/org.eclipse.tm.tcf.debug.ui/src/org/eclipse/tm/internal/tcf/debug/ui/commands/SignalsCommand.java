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

import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeStackFrame;

public class SignalsCommand extends AbstractActionDelegate {

    
    protected void selectionChanged() {
        boolean e = false;
        TCFNode n = getSelectedNode();
        if (n instanceof TCFNodeExecContext) e = true;
        if (n instanceof TCFNodeStackFrame) e = true;
        getAction().setEnabled(e);
    }

    protected void run() {
        TCFNode n = getSelectedNode();
        if (n instanceof TCFNodeStackFrame || n instanceof TCFNodeExecContext) {
            new SignalsDialog(getWindow().getShell(), n).open();
        }
    }
}
