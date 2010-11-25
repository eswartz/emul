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
import org.eclipse.debug.core.commands.IResumeHandler;
import org.eclipse.tm.internal.tcf.debug.actions.TCFAction;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IRunControl;


public class ResumeCommand extends StepCommand implements IResumeHandler {

    public ResumeCommand(TCFModel model) {
        super(model);
    }

    @Override
    protected boolean canExecute(IRunControl.RunControlContext ctx) {
        if (ctx == null) return false;
        if (ctx.canResume(IRunControl.RM_RESUME)) return true;
        return false;
    }

    @Override
    protected void execute(final IDebugCommandRequest monitor,
            final IRunControl.RunControlContext ctx, boolean src_step, final Runnable done) {
        new TCFAction(model.getLaunch()) {
            public void run() {
                ctx.resume(IRunControl.RM_RESUME, 1, new IRunControl.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error != null && model.getChannel().getState() == IChannel.STATE_OPEN) {
                            if (error instanceof IErrorReport) {
                                IErrorReport r = (IErrorReport)error;
                                if (r.getErrorCode() == IErrorReport.TCF_ERROR_ALREADY_RUNNING) {
                                    done();
                                    return;
                                }
                            }
                            monitor.setStatus(new Status(IStatus.ERROR,
                                    Activator.PLUGIN_ID, IStatus.OK, "Cannot resume: " + error.getLocalizedMessage(), error));
                        }
                        done();
                    }
                });
            }
            public void done() {
                super.done();
                setActionResult(ctx.getID(), null);
                done.run();
            }
        };
    }
}
