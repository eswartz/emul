/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.debug.tcf.ui.commands;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.IStepOverHandler;

import com.windriver.debug.tcf.ui.TCFUI;
import com.windriver.debug.tcf.ui.model.TCFModel;
import com.windriver.debug.tcf.ui.model.TCFNode;
import com.windriver.debug.tcf.ui.model.TCFRunnable;
import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.services.IRunControl;

public class StepOverCommand implements IStepOverHandler {

    private final TCFModel model;

    public StepOverCommand(TCFModel model) {
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
                        if (ctx == null || !ctx.canResume(IRunControl.RM_STEP_OVER)) {
                            node = node.getParent();
                        }
                        else {
                            if (node.isSuspended()) res = true;
                            node = null;
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
                        if (ctx == null || !ctx.canResume(IRunControl.RM_STEP_OVER)) {
                            node = node.getParent();
                        }
                        else {
                            set.add(ctx);
                            node = null;
                        }
                    }
                }
                final Set<IToken> cmds = new HashSet<IToken>();
                for (Iterator<IRunControl.RunControlContext> i = set.iterator(); i.hasNext();) {
                    IRunControl.RunControlContext ctx = i.next();
                    cmds.add(ctx.resume(IRunControl.RM_STEP_OVER, 1, new IRunControl.DoneCommand() {
                        public void doneCommand(IToken token, Exception error) {
                            assert cmds.contains(token);
                            cmds.remove(token);
                            if (error != null) {
                                monitor.setStatus(new Status(IStatus.ERROR,
                                        TCFUI.PLUGIN_ID, IStatus.OK, "Cannot step into", error));
                            }
                            if (cmds.isEmpty()) done();
                        }
                    }));
                }
            }
        };
        return true;
    }
}
