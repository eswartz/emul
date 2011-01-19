/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.commands;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeArrayPartition;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExpression;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeModule;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeStackFrame;

public class MemoryMapCommand extends AbstractActionDelegate {

    private static boolean isValidNode(TCFNode n) {
        if (n instanceof TCFNodeLaunch) return true;
        if (n instanceof TCFNodeExecContext) return true;
        if (n instanceof TCFNodeStackFrame) return true;
        if (n instanceof TCFNodeExpression) return true;
        if (n instanceof TCFNodeArrayPartition) return true;
        if (n instanceof TCFNodeModule) return true;
        return false;
    }

    protected void selectionChanged() {
        TCFNode n = getSelectedNode();
        getAction().setEnabled(isValidNode(n));
    }

    protected void run() {
        TCFNode n = getSelectedNode();
        if (isValidNode(n)) {
            Shell shell = getWindow().getShell();
            try {
                new MemoryMapDialog(shell, n).open();
            }
            catch (Throwable x) {
                MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                mb.setText("Cannot open Memory Map dialog");
                mb.setMessage(TCFModel.getErrorMessage(x, true));
                mb.open();
            }
        }
    }
}
