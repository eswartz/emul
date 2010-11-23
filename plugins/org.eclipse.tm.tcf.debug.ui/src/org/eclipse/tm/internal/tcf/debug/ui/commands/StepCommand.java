/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
import java.util.Set;

import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.commands.IDebugCommandHandler;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFRunnable;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.util.TCFDataCache;

// TODO: implement Instruction Stepping Mode user action
abstract class StepCommand implements IDebugCommandHandler {

    private static final int MAX_ACTION_CNT = 8;

    protected final TCFModel model;

    public StepCommand(TCFModel model) {
        this.model = model;
    }

    protected abstract boolean canExecute(IRunControl.RunControlContext ctx);

    protected abstract void execute(IDebugCommandRequest monitor,
            IRunControl.RunControlContext ctx, boolean src_step, Runnable done);

    private boolean getContextSet(Object[] elements, Set<IRunControl.RunControlContext> set, Runnable done) {
        for (int i = 0; i < elements.length; i++) {
            TCFNode node = null;
            if (elements[i] instanceof TCFNode) node = (TCFNode)elements[i];
            else node = model.getRootNode();
            while (node != null && !node.isDisposed()) {
                IRunControl.RunControlContext ctx = null;
                if (node instanceof TCFNodeExecContext) {
                    TCFDataCache<IRunControl.RunControlContext> cache = ((TCFNodeExecContext)node).getRunContext();
                    if (!cache.validate(done)) return false;
                    ctx = cache.getData();
                }
                if (ctx == null || !canExecute(ctx)) {
                    node = node.getParent();
                }
                else {
                    int cnt = model.getLaunch().getContextActionsCount();
                    if (cnt == 0) {
                        TCFDataCache<TCFContextState> state_cache = ((TCFNodeExecContext)node).getState();
                        if (!state_cache.validate(done)) return false;
                        TCFContextState state_data = state_cache.getData();
                        if (state_data != null && state_data.is_suspended) set.add(ctx);
                    }
                    else if (cnt < MAX_ACTION_CNT) {
                        set.add(ctx);
                    }
                    break;
                }
            }
        }
        return true;
    }

    public final void canExecute(final IEnabledStateRequest monitor) {
        new TCFRunnable(monitor) {
            public void run() {
                if (done) return;
                Set<IRunControl.RunControlContext> set = new HashSet<IRunControl.RunControlContext>();
                if (!getContextSet(monitor.getElements(), set, this)) return;
                monitor.setEnabled(set.size() > 0);
                monitor.setStatus(Status.OK_STATUS);
                done();
            }
        };
    }

    public final boolean execute(final IDebugCommandRequest monitor) {
        new TCFRunnable(monitor) {
            public void run() {
                if (done) return;
                Set<IRunControl.RunControlContext> set = new HashSet<IRunControl.RunControlContext>();
                if (!getContextSet(monitor.getElements(), set, this)) return;
                if (set.size() == 0) {
                    monitor.setStatus(Status.OK_STATUS);
                    monitor.done();
                }
                else {
                    final Set<Runnable> wait_list = new HashSet<Runnable>();
                    for (IRunControl.RunControlContext ctx : set) {
                        Runnable done = new Runnable() {
                            public void run() {
                                wait_list.remove(this);
                                if (wait_list.isEmpty()) monitor.done();
                            }
                        };
                        wait_list.add(done);
                        execute(monitor, ctx, true, done);
                    }
                }
            }
        };
        return true;
    }
}
