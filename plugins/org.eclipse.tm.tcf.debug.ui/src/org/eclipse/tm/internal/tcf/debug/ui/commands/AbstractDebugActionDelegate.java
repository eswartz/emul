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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.commands.IDebugCommandHandler;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;

public abstract class AbstractDebugActionDelegate extends AbstractActionDelegate {

    private final Class<?> cmd_class;

    protected AbstractDebugActionDelegate(Class<?> cmd_class) {
        this.cmd_class = cmd_class;
    }

    @Override
    protected void selectionChanged() {
        TCFNode n = getSelectedNode();
        if (n == null) {
            getAction().setEnabled(false);
            return;
        }
        IDebugCommandHandler cmd = (IDebugCommandHandler)n.getAdapter(cmd_class);
        if (cmd == null) {
            getAction().setEnabled(false);
            return;
        }
        final Object[] selection = new Object[]{ n };
        cmd.canExecute(new IEnabledStateRequest() {

            IStatus status;
            boolean enabled = false;

            public void setStatus(IStatus status) {
                this.status = status;
            }

            public boolean isCanceled() {
                return false;
            }

            public IStatus getStatus() {
                return status;
            }

            public void done() {
                getAction().setEnabled(enabled);
            }

            public void cancel() {
            }

            public Object[] getElements() {
                return selection;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }
        });
    }

    @Override
    protected void run() {
        TCFNode n = getSelectedNode();
        if (n == null) return;
        IDebugCommandHandler cmd = (IDebugCommandHandler)n.getAdapter(cmd_class);
        if (cmd == null) return;
        final Object[] selection = new Object[]{ n };
        cmd.execute(new IDebugCommandRequest() {

            IStatus status;

            public void setStatus(IStatus status) {
                this.status = status;
            }

            public IStatus getStatus() {
                return status;
            }

            public void done() {
                if (status != null && !status.isOK()) {
                    final Shell shell = getWindow().getShell();
                    shell.getDisplay().asyncExec(new Runnable() {
                        public void run() {
                            MessageBox mb = new MessageBox(shell, SWT.ICON_ERROR | SWT.OK);
                            mb.setText("Cannot execute debugger command");
                            mb.setMessage(TCFModel.getErrorMessage(status.getException(), true));
                            mb.open();
                        }
                    });
                }
            }

            public void cancel() {
            }

            public boolean isCanceled() {
                return false;
            }

            public Object[] getElements() {
                return selection;
            }
        });
    }
}
