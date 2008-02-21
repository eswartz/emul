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
package org.eclipse.tm.internal.tcf.debug.ui.commands;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.ITerminateHandler;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFRunnable;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IRunControl;


public class TerminateCommand implements ITerminateHandler {

    private final TCFModel model;

    public TerminateCommand(TCFModel model) {
        this.model = model;
    }

    public void canExecute(final IEnabledStateRequest monitor) {
        new TCFRunnable(model.getDisplay(), monitor) {
            public void run() {
                Object[] elements = monitor.getElements();
                boolean res = false;
                for (int i = 0; i < elements.length; i++) {
                    TCFNode node = null;
                    if (elements[i] instanceof TCFNode) node = (TCFNode)elements[i];
                    else node = model.getRootNode();
                    while (node != null && !node.isDisposed()) {
                        if (!node.validateNode(this)) return;
                        IRunControl.RunControlContext ctx = node.getRunContext();
                        if (ctx != null && ctx.canTerminate()) {
                            res = true;
                            node = null;
                        }
                        else {
                            node = node.getParent();
                            if (node == null && model.getLaunch().canTerminate()) res = true;
                        }
                    }
                }
                monitor.setEnabled(res);
                monitor.setStatus(Status.OK_STATUS);
                done();
            }
        };
    }

    public boolean execute(final IDebugCommandRequest monitor) {
        new TCFRunnable(model.getDisplay(), monitor) {
            public void run() {
                Object[] elements = monitor.getElements();
                Set<IRunControl.RunControlContext> set = new HashSet<IRunControl.RunControlContext>();
                for (int i = 0; i < elements.length; i++) {
                    TCFNode node = null;
                    if (elements[i] instanceof TCFNode) node = (TCFNode)elements[i];
                    else node = model.getRootNode();
                    while (node != null && !node.isDisposed()) {
                        if (!node.validateNode(this)) return;
                        IRunControl.RunControlContext ctx = node.getRunContext();
                        if (ctx != null && ctx.canTerminate()) {
                            set.add(ctx);
                            node = null;
                        }
                        else {
                            node = node.getParent();
                            if (node == null) set.add(null);
                        }
                    }
                }
                final Set<IToken> cmds = new HashSet<IToken>();
                for (Iterator<IRunControl.RunControlContext> i = set.iterator(); i.hasNext();) {
                    IRunControl.RunControlContext ctx = i.next();
                    if (ctx == null) {
                        cmds.add(null);
                        model.getLaunch().terminate(new Runnable() {
                            public void run() {
                                assert cmds.contains(null);
                                cmds.remove(null);
                                if (cmds.isEmpty()) done();
                            }
                        });
                    }
                    else {
                        cmds.add(ctx.terminate(new IRunControl.DoneCommand() {
                            public void doneCommand(IToken token, Exception error) {
                                assert cmds.contains(token);
                                cmds.remove(token);
                                if (error != null) {
                                    monitor.setStatus(new Status(IStatus.ERROR,
                                            Activator.PLUGIN_ID, IStatus.OK, "Cannot resume", error));
                                }
                                if (cmds.isEmpty()) done();
                            }
                        }));
                    }
                }
            }
        };
        return false;
    }
}
