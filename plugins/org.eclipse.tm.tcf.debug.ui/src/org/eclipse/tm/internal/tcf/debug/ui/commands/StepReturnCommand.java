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

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IStepReturnHandler;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeStackFrame;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;


public class StepReturnCommand extends StepCommand implements IStepReturnHandler {

    private static final long TIMEOUT = 10000;
    
    private static class StepStateMachine extends TCFModel.ContextAction implements IRunControl.RunControlListener {
        
        private final IDebugCommandRequest monitor;
        private final Runnable done;
        private final IRunControl rc = model.getLaunch().getService(IRunControl.class);
        private final IBreakpoints bps = model.getLaunch().getService(IBreakpoints.class);
        
        private IRunControl.RunControlContext ctx;
        private TCFNodeExecContext node;
        private TCFNodeStackFrame frame;
        private int step_cnt;
        private Map<String,Object> bp;
        private boolean started;
        private boolean exited;
        
        StepStateMachine(TCFModel model, IDebugCommandRequest monitor,
                IRunControl.RunControlContext ctx, Runnable done) {
            super(model, ctx.getID());
            this.monitor = monitor;
            this.ctx = ctx;
            this.done = done;
        }
        
        public void run() {
            assert !exited;
            if (!started) {
                started = true;
                rc.addListener(this);
                node = (TCFNodeExecContext)model.getNode(ctx.getID());
                if (node == null) {
                    exit(new Exception("Invalid context ID"));
                    return;
                }
                if (!ctx.canResume(IRunControl.RM_STEP_OUT)) {
                    model.invokeLater(TIMEOUT, new Runnable() {
                        public void run() {
                            exit(new Exception("Time out"));
                        }
                    });
                }
            }
            if (!node.validateNode(this)) return;
            if (!node.isSuspended()) {
                exit(new Exception("Context is not suspended"));
                return;
            }
            if (ctx.canResume(IRunControl.RM_STEP_OUT)) {
                if (step_cnt > 0) return;
                ctx.resume(IRunControl.RM_STEP_OUT, 1, new IRunControl.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        exit(error);
                    }
                });
                step_cnt++;
                return;
            }
            if (frame == null) {
                frame = node.getTopFrame();
                if (frame == null) {
                    exit(new Exception("Cannot get top stack frame"));
                    return;
                }
            }
            if (!frame.validateNode(this)) return;
            if (frame.getFrameNo() < 0) {
                // Stepped out of selected function
                exit(null);
            }
            else if (bps != null && ctx.canResume(IRunControl.RM_RESUME)) {
                if (bp == null) {
                    BigInteger addr = frame.getReturnAddress();
                    if (addr == null) {
                        exit(new Exception("Unknown stack frame return address"));
                        return;
                    }
                    bp = new HashMap<String,Object>();
                    bp.put(IBreakpoints.PROP_ID, "Step" + System.currentTimeMillis());
                    bp.put(IBreakpoints.PROP_LOCATION, addr.toString());
                    bp.put(IBreakpoints.PROP_CONDITION, "$thread==\"" + ctx.getID() + "\"");
                    bp.put(IBreakpoints.PROP_ENABLED, Boolean.TRUE);
                    bps.add(bp, new IBreakpoints.DoneCommand() {
                        public void doneCommand(IToken token, Exception error) {
                            if (error != null) exit(error);
                        }
                    });
                }
                ctx.resume(IRunControl.RM_RESUME, 1, new IRunControl.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error != null) exit(error);
                    }
                });
            }
            else {
                exit(new Exception("Step out is not supported"));
            }
        }
        
        private void exit(Throwable error) {
            assert started;
            if (exited) return;
            if (bp != null) {
                bps.remove(new String[]{ (String)bp.get(IBreakpoints.PROP_ID) }, new IBreakpoints.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                    }
                });
            }
            rc.removeListener(this);
            exited = true;
            if (error != null) {
                monitor.setStatus(new Status(IStatus.ERROR,
                        Activator.PLUGIN_ID, IStatus.OK, "Cannot step", error));
            }
            done.run();
            done();
        }

        public void containerResumed(String[] context_ids) {
        }

        public void containerSuspended(String context, String pc,
                String reason, Map<String, Object> params,
                String[] suspended_ids) {
            for (String id : suspended_ids) {
                if (!id.equals(context)) contextSuspended(id, null, null, null);
            }
            contextSuspended(context, pc, reason, params);
        }

        public void contextAdded(RunControlContext[] contexts) {
        }

        public void contextChanged(RunControlContext[] contexts) {
            for (RunControlContext c : contexts) {
                if (c.getID().equals(ctx.getID())) ctx = c;
            }
        }

        public void contextException(String context, String msg) {
            if (context.equals(ctx.getID())) exit(new Exception(msg));
        }

        public void contextRemoved(String[] context_ids) {
            for (String context : context_ids) {
                if (context.equals(ctx.getID())) exit(null);
            }
        }

        public void contextResumed(String context) {
        }

        public void contextSuspended(String context, String pc, String reason,
                Map<String, Object> params) {
            if (!context.equals(ctx.getID())) return;
            exit(null);
        }
    }

    public StepReturnCommand(TCFModel model) {
        super(model);
    }

    @Override
    protected boolean canExecute(IRunControl.RunControlContext ctx) {
        if (ctx == null) return false;
        if (ctx.canResume(IRunControl.RM_STEP_OUT)) return true;
        if (ctx.canResume(IRunControl.RM_RESUME) && model.getLaunch().getService(IBreakpoints.class) != null) return true;
        return false;
    }

    @Override
    protected void execute(final IDebugCommandRequest monitor, final IRunControl.RunControlContext ctx,
            boolean src_step, final Runnable done) {
        new StepStateMachine(model, monitor, ctx, done);
    }
}
