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
package org.eclipse.tm.internal.tcf.debug.actions;

import java.math.BigInteger;
import java.util.Map;

import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IStackTrace;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tm.tcf.util.TCFDataCache;

public abstract class TCFActionStepInto extends TCFAction implements IRunControl.RunControlListener {

    private final boolean src_step;
    private final IRunControl rc = launch.getService(IRunControl.class);

    private IRunControl.RunControlContext ctx;
    private TCFDataCache<TCFContextState> state;
    private TCFDataCache<TCFSourceRef> line_info;
    private TCFSourceRef source_ref;
    private BigInteger pc0;
    private BigInteger pc1;
    private int step_cnt;

    protected boolean exited;

    public TCFActionStepInto(TCFLaunch launch, IRunControl.RunControlContext ctx, boolean src_step) {
        super(launch, ctx.getID());
        this.ctx = ctx;
        this.src_step = src_step;
    }

    protected abstract TCFDataCache<TCFContextState> getContextState();
    protected abstract TCFDataCache<TCFSourceRef> getLineInfo();
    protected abstract TCFDataCache<?> getStackTrace();
    protected abstract TCFDataCache<IStackTrace.StackTraceContext> getStackFrame();
    protected abstract int getStackFrameIndex();

    public void run() {
        if (exited) return;
        try {
            runAction();
        }
        catch (Throwable x) {
            exit(x);
        }
    }

    private void runAction() {
        if (state == null) {
            rc.addListener(this);
            state = getContextState();
            if (state == null) {
                exit(new Exception("Invalid context ID"));
                return;
            }
        }
        if (!state.validate(this)) return;
        if (state.getData() == null || !state.getData().is_suspended) {
            exit(new Exception("Context is not suspended"));
            return;
        }
        if (ctx.canResume(src_step ? IRunControl.RM_STEP_INTO_LINE : IRunControl.RM_STEP_INTO)) {
            if (step_cnt > 0) {
                exit(null);
                return;
            }
            ctx.resume(src_step ? IRunControl.RM_STEP_INTO_LINE : IRunControl.RM_STEP_INTO, 1, new IRunControl.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error != null) exit(error);
                }
            });
            step_cnt++;
            return;
        }
        if (!src_step) {
            exit(new Exception("Step into is not supported"));
            return;
        }
        TCFDataCache<?> stack_trace = getStackTrace();
        if (!stack_trace.validate(this)) return;
        if (stack_trace.getData() == null) {
            exit(stack_trace.getError());
            return;
        }
        if (source_ref == null) {
            line_info = getLineInfo();
            if (!line_info.validate(this)) return;
            source_ref = line_info.getData();
            if (source_ref == null) {
                exit(new Exception("Line info not available"));
                return;
            }
            if (source_ref.error != null) {
                exit(source_ref.error);
                return;
            }
            ILineNumbers.CodeArea area = source_ref.area;
            if (area != null) {
                if (area.start_address instanceof BigInteger) pc0 = (BigInteger)area.start_address;
                else if (area.start_address != null) pc0 = new BigInteger(area.start_address.toString());
                if (area.end_address instanceof BigInteger) pc1 = (BigInteger)area.end_address;
                else if (area.end_address != null) pc1 = new BigInteger(area.end_address.toString());
            }
        }
        if (getStackFrameIndex() != 0) {
            // Stepped out of selected function
            exit(null);
            return;
        }
        if (step_cnt > 0) {
            TCFDataCache<IStackTrace.StackTraceContext> frame = getStackFrame();
            if (!frame.validate(this)) return;
            Number addr = frame.getData().getInstructionAddress();
            BigInteger pc = addr instanceof BigInteger ? (BigInteger)addr : new BigInteger(addr.toString());
            if (pc == null || pc0 == null || pc1 == null) {
                exit(null);
                return;
            }
            if (pc.compareTo(pc0) < 0 || pc.compareTo(pc1) >= 0) {
                if (!line_info.validate(this)) return;
                TCFSourceRef ref = line_info.getData();
                if (ref != null && ref.area != null) {
                    if (isSameLine(source_ref.area, ref.area)) {
                        source_ref = ref;
                        ILineNumbers.CodeArea area = source_ref.area;
                        if (area.start_address instanceof BigInteger) pc0 = (BigInteger)area.start_address;
                        else if (area.start_address != null) pc0 = new BigInteger(area.start_address.toString());
                        if (area.end_address instanceof BigInteger) pc1 = (BigInteger)area.end_address;
                        else if (area.end_address != null) pc1 = new BigInteger(area.end_address.toString());
                    }
                    else {
                        exit(null);
                        return;
                    }
                }
            }
        }
        step_cnt++;
        if (ctx.canResume(IRunControl.RM_STEP_INTO)) {
            ctx.resume(IRunControl.RM_STEP_INTO, 1, new IRunControl.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error != null) exit(error);
                }
            });
        }
        else {
            exit(new Exception("Step into is not supported"));
        }
    }

    private boolean isSameLine(ILineNumbers.CodeArea x, ILineNumbers.CodeArea y) {
        if (x == null || y == null) return false;
        if (x.start_line != y.start_line) return false;
        if (x.directory != y.directory && (x.directory == null || !x.directory.equals(y.directory))) return false;
        if (x.file != y.file && (x.file == null || !x.file.equals(y.file))) return false;
        return true;
    }

    protected void exit(Throwable error) {
        exit(error, "Step Into");
    }

    protected void exit(Throwable error, String reason) {
        if (exited) return;
        rc.removeListener(this);
        exited = true;
        done(reason);
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

    public void contextSuspended(String context, String pc, String reason, Map<String,Object> params) {
        if (!context.equals(ctx.getID())) return;
        if (IRunControl.REASON_STEP.equals(reason)) {
            Protocol.invokeLater(this);
        }
        else {
            exit(null, reason);
        }
    }
}
