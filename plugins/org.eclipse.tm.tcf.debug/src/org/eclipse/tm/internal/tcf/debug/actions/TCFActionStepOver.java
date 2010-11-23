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
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.tm.tcf.services.ILineNumbers;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IStackTrace;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tm.tcf.util.TCFDataCache;

public abstract class TCFActionStepOver extends TCFAction implements IRunControl.RunControlListener {

    private boolean step_line;
    private final boolean step_back;
    private final IRunControl rc = launch.getService(IRunControl.class);
    private final IBreakpoints bps = launch.getService(IBreakpoints.class);

    private IRunControl.RunControlContext ctx;
    private TCFDataCache<TCFContextState> state;
    private TCFDataCache<TCFSourceRef> line_info;
    private TCFSourceRef source_ref;
    private BigInteger pc0;
    private BigInteger pc1;
    private int step_cnt;
    private Map<String,Object> bp;

    protected boolean exited;

    public TCFActionStepOver(TCFLaunch launch, IRunControl.RunControlContext ctx, boolean step_line, boolean step_back) {
        super(launch);
        this.ctx = ctx;
        this.step_line = step_line;
        this.step_back = step_back;
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
            Throwable error = state.getError();
            if (error == null) error = new Exception("Context is not suspended");
            exit(error);
            return;
        }
        if (step_cnt > 0) {
            boolean ok = false;
            String pc = state.getData().suspend_pc;
            String reason = state.getData().suspend_reason;
            if (IRunControl.REASON_STEP.equals(reason) || isMyBreakpoint(pc, reason)) {
                ok = true;
            }
            else if (IRunControl.REASON_BREAKPOINT.equals(reason) && pc0 != null && pc1 != null) {
                BigInteger x = new BigInteger(pc);
                ok = x.compareTo(pc0) >= 0 && x.compareTo(pc1) < 0;
            }
            if (!ok) {
                exit(null, reason);
                return;
            }
        }
        int mode = 0;
        if (!step_line) mode = step_back ? IRunControl.RM_REVERSE_STEP_OVER : IRunControl.RM_STEP_OVER;
        else mode = step_back ? IRunControl.RM_REVERSE_STEP_OVER_LINE : IRunControl.RM_STEP_OVER_LINE;
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
        TCFDataCache<?> stack_trace = getStackTrace();
        if (!stack_trace.validate(this)) return;
        if (step_line && source_ref == null) {
            line_info = getLineInfo();
            if (!line_info.validate(this)) return;
            source_ref = line_info.getData();
            if (source_ref == null) {
                step_line = false;
                Protocol.invokeLater(this);
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
        int fno = getStackFrameIndex();
        if (fno > 0) {
            mode = step_back ? IRunControl.RM_REVERSE_STEP_OUT : IRunControl.RM_STEP_OUT;
            if (ctx.canResume(mode)) {
                ctx.resume(mode, 1, new IRunControl.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error != null) exit(error);
                    }
                });
                return;
            }
            mode = step_back ? IRunControl.RM_REVERSE_RESUME : IRunControl.RM_RESUME;
            if (bps != null && ctx.canResume(mode)) {
                if (bp == null) {
                    TCFDataCache<IStackTrace.StackTraceContext> frame = getStackFrame();
                    if (!frame.validate(this)) return;
                    Number addr = frame.getData().getInstructionAddress();
                    if (addr == null) {
                        exit(new Exception("Unknown PC address"));
                        return;
                    }
                    String id = "Step." + ctx.getID();
                    bp = new HashMap<String,Object>();
                    bp.put(IBreakpoints.PROP_ID, id);
                    bp.put(IBreakpoints.PROP_LOCATION, addr.toString());
                    bp.put(IBreakpoints.PROP_CONDITION, "$thread==\"" + ctx.getID() + "\"");
                    bp.put(IBreakpoints.PROP_ENABLED, Boolean.TRUE);
                    bps.add(bp, new IBreakpoints.DoneCommand() {
                        public void doneCommand(IToken token, Exception error) {
                            if (error != null) exit(error);
                        }
                    });
                }
                ctx.resume(mode, 1, new IRunControl.DoneCommand() {
                    public void doneCommand(IToken token, Exception error) {
                        if (error != null) exit(error);
                    }
                });
                step_cnt++;
                return;
            }
            exit(new Exception("Step over is not supported"));
            return;
        }
        if (bp != null) {
            bps.remove(new String[]{ (String)bp.get(IBreakpoints.PROP_ID) }, new IBreakpoints.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error != null) exit(error);
                }
            });
            bp = null;
        }
        BigInteger pc = new BigInteger(state.getData().suspend_pc);
        if (step_cnt > 0) {
            if (pc0 == null && pc1 == null || state.getData().suspend_pc == null) {
                exit(null);
                return;
            }
            assert step_line;
            if (pc.compareTo(pc0) < 0 || pc.compareTo(pc1) >= 0) {
                if (!line_info.validate(this)) return;
                TCFSourceRef ref = line_info.getData();
                if (ref == null || ref.area == null) {
                    if (fno < 0) {
                        exit(null);
                        return;
                    }
                    // No line info for current PC, continue stepping
                }
                else if (isSameLine(source_ref.area, ref.area)) {
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
        step_cnt++;
        mode = step_back ? IRunControl.RM_REVERSE_STEP_OVER : IRunControl.RM_STEP_OVER;
        if (ctx.canResume(mode)) {
            ctx.resume(mode, 1, new IRunControl.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error != null) exit(error);
                }
            });
            return;
        }
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
        exit(new Exception("Step over is not supported"));
    }

    protected void exit(Throwable error) {
        exit(error, "Step Over");
    }

    protected void exit(Throwable error, String reason) {
        if (exited) return;
        if (bp != null) {
            bps.remove(new String[]{ (String)bp.get(IBreakpoints.PROP_ID) }, new IBreakpoints.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                }
            });
        }
        rc.removeListener(this);
        exited = true;
        setActionResult(ctx.getID(), reason);
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

    public void contextSuspended(String context, String pc, String reason, Map<String, Object> params) {
        if (!context.equals(ctx.getID())) return;
        Protocol.invokeLater(this);
    }

    private boolean isSameLine(ILineNumbers.CodeArea x, ILineNumbers.CodeArea y) {
        if (x == null || y == null) return false;
        if (x.start_line != y.start_line) return false;
        if (x.directory != y.directory && (x.directory == null || !x.directory.equals(y.directory))) return false;
        if (x.file != y.file && (x.file == null || !x.file.equals(y.file))) return false;
        return true;
    }

    private boolean isMyBreakpoint(String pc, String reason) {
        if (bp == null) return false;
        if (pc == null) return false;
        if (!IRunControl.REASON_BREAKPOINT.equals(reason)) return false;
        BigInteger x = new BigInteger(pc);
        BigInteger y = new BigInteger((String)bp.get(IBreakpoints.PROP_LOCATION));
        return x.equals(y);
    }
}
