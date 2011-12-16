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

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.tm.internal.tcf.debug.actions.TCFActionStepInto;
import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeStackFrame;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.util.TCFDataCache;

public class BackIntoCommand extends StepCommand {

    private static class StepStateMachine extends TCFActionStepInto {

        private final IDebugCommandRequest monitor;
        private final Runnable done;
        private final TCFNodeExecContext node;

        StepStateMachine(TCFModel model, IDebugCommandRequest monitor,
                IRunControl.RunControlContext ctx, boolean src_step, Runnable done) {
            super(model.getLaunch(), ctx, src_step, true);
            this.monitor = monitor;
            this.done = done;
            node = (TCFNodeExecContext)model.getNode(ctx.getID());
        }

        @Override
        protected TCFDataCache<TCFContextState> getContextState() {
            if (node == null) return null;
            return node.getState();
        }

        @Override
        protected TCFDataCache<TCFSourceRef> getLineInfo() {
            TCFNodeStackFrame frame = node.getStackTrace().getTopFrame();
            if (frame == null) return null;
            return frame.getLineInfo();
        }

        @Override
        protected TCFDataCache<?> getStackTrace() {
            return node.getStackTrace();
        }

        @Override
        protected void exit(Throwable error) {
            if (exited) return;
            super.exit(error);
            if (error != null && node.getChannel().getState() == IChannel.STATE_OPEN) {
                monitor.setStatus(new Status(IStatus.ERROR,
                        Activator.PLUGIN_ID, 0, "Cannot step: " + error.getLocalizedMessage(), error));
            }
            done.run();
        }
    }

    public BackIntoCommand(TCFModel model) {
        super(model);
    }

    @Override
    protected boolean canExecute(IRunControl.RunControlContext ctx) {
        if (ctx == null) return false;
        if (ctx.canResume(IRunControl.RM_REVERSE_STEP_INTO_LINE)) return true;
        if (ctx.canResume(IRunControl.RM_REVERSE_STEP_INTO)) return true;
        return false;
    }

    @Override
    protected void execute(final IDebugCommandRequest monitor, final IRunControl.RunControlContext ctx,
            boolean src_step, final Runnable done) {
        new StepStateMachine(model, monitor, ctx, src_step, done);
    }
}
