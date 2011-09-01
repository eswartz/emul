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
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.model.TCFSourceRef;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tm.tcf.util.TCFDataCache;

public abstract class TCFActionStepInto extends TCFAction implements IRunControl.RunControlListener {

    private boolean step_line;
    private boolean step_back;
    private final IRunControl rc = launch.getService(IRunControl.class);

    private IRunControl.RunControlContext ctx;
    private TCFDataCache<TCFContextState> state;
    private TCFSourceRef source_ref;
    private BigInteger pc0;
    private BigInteger pc1;
    private int step_cnt;
    private boolean second_step_back;
    private boolean final_step;

    protected boolean exited;

    public TCFActionStepInto(TCFLaunch launch, IRunControl.RunControlContext ctx, boolean step_line, boolean back_step) {
        super(launch, ctx.getID());
        this.ctx = ctx;
        this.step_line = step_line;
        this.step_back = back_step;
    }

    protected abstract TCFDataCache<TCFContextState> getContextState();
    protected abstract TCFDataCache<TCFSourceRef> getLineInfo();
    protected abstract TCFDataCache<?> getStackTrace();

    public void run() {
        if (exited) return;
        try {
            runAction();
        }
        catch (Throwable x) {
            exit(x);
        }
    }

    private void setSourceRef(TCFSourceRef ref) {
        ILineNumbers.CodeArea area = ref.area;
        if (area != null) {
            pc0 = JSON.toBigInteger(area.start_address);
            pc1 = JSON.toBigInteger(area.end_address);
        }
        else {
            pc0 = null;
            pc1 = null;
        }
        source_ref = ref;
    }

    private void runAction() {
        if (aborted) {
            exit(null);
            return;
        }
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
            Throwable error = state.getError();
            if (error == null) error = new Exception("Context is not suspended");
            exit(error);
            return;
        }
        if (step_cnt > 0) {
            String reason = state.getData().suspend_reason;
            if (!IRunControl.REASON_STEP.equals(reason)) {
                exit(null, reason);
                return;
            }
        }
        int mode = 0;
        if (!step_line) mode = step_back ? IRunControl.RM_REVERSE_STEP_INTO : IRunControl.RM_STEP_INTO;
        else mode = step_back ? IRunControl.RM_REVERSE_STEP_INTO_LINE : IRunControl.RM_STEP_INTO_LINE;
        if (ctx.canResume(mode)) {
            if (step_cnt > 0) {
                exit(null);
                return;
            }
            ctx.resume(mode, 1, new IRunControl.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error != null) exit(error);
                }
            });
            step_cnt++;
            return;
        }
        if (!step_line) {
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
            TCFDataCache<TCFSourceRef> line_info = getLineInfo();
            if (!line_info.validate(this)) return;
            TCFSourceRef ref = line_info.getData();
            if (ref == null) {
                step_line = false;
                Protocol.invokeLater(this);
                return;
            }
            if (ref.error != null) {
                exit(ref.error);
                return;
            }
            setSourceRef(ref);
        }
        BigInteger pc = new BigInteger(state.getData().suspend_pc);
        if (step_cnt > 0) {
            if (pc == null || pc0 == null || pc1 == null) {
                exit(null);
                return;
            }
            if (pc.compareTo(pc0) < 0 || pc.compareTo(pc1) >= 0) {
                TCFDataCache<TCFSourceRef> line_info = getLineInfo();
                if (!line_info.validate(this)) return;
                TCFSourceRef ref = line_info.getData();
                if (ref == null || ref.area == null) {
                    exit(null);
                }
                else if (isSameLine(source_ref.area, ref.area)) {
                    setSourceRef(ref);
                }
                else if (step_back && !second_step_back) {
                    // After step back we stop at last instruction of previous line.
                    // Do second step back into line to skip that line.
                    second_step_back = true;
                    setSourceRef(ref);
                }
                else if (step_back && !final_step) {
                    // After second step back we have stepped one instruction more then needed.
                    // Do final step forward to correct that.
                    final_step = true;
                    step_back = false;
                    setSourceRef(ref);
                }
                else {
                    exit(null);
                    return;
                }
            }
        }
        step_cnt++;
        mode = step_back ? IRunControl.RM_REVERSE_STEP_INTO_RANGE : IRunControl.RM_STEP_INTO_RANGE;
        if (ctx.canResume(mode) &&
                pc != null && pc0 != null && pc1 != null &&
                pc.compareTo(pc0) >= 0 && pc.compareTo(pc1) < 0) {
            HashMap<String,Object> args = new HashMap<String,Object>();
            args.put(IRunControl.RP_RANGE_START, pc0);
            args.put(IRunControl.RP_RANGE_END, pc1);
            ctx.resume(mode, 1, args, new IRunControl.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error != null) exit(error);
                }
            });
            return;
        }
        mode = step_back ? IRunControl.RM_REVERSE_STEP_INTO : IRunControl.RM_STEP_INTO;
        if (ctx.canResume(mode)) {
            ctx.resume(mode, 1, new IRunControl.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error != null) exit(error);
                }
            });
            return;
        }
        exit(new Exception("Step into is not supported"));
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
        if (error == null) setActionResult(getContextID(), reason);
        else launch.removeContextActions(getContextID());
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

    public void contextSuspended(String context, String pc, String reason, Map<String,Object> params) {
        if (!context.equals(ctx.getID())) return;
        Protocol.invokeLater(this);
    }
}
