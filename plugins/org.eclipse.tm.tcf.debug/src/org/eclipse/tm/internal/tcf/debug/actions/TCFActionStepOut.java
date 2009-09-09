package org.eclipse.tm.internal.tcf.debug.actions;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IStackTrace;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;
import org.eclipse.tm.tcf.util.TCFDataCache;

public abstract class TCFActionStepOut extends TCFAction implements IRunControl.RunControlListener {

    private final IRunControl rc = launch.getService(IRunControl.class);
    private final IBreakpoints bps = launch.getService(IBreakpoints.class);

    private IRunControl.RunControlContext ctx;
    private TCFDataCache<TCFContextState> state;
    private int step_cnt;
    private Map<String,Object> bp;

    protected boolean exited;

    public TCFActionStepOut(TCFLaunch launch, IRunControl.RunControlContext ctx) {
        super(launch, ctx.getID());
        this.ctx = ctx;
    }

    protected abstract TCFDataCache<TCFContextState> getContextState();
    protected abstract TCFDataCache<?> getStackTrace();
    protected abstract TCFDataCache<IStackTrace.StackTraceContext> getStackFrame();
    protected abstract int getStackFrameIndex();

    public void run() {
        if (exited) return;
        if (state == null) {
            rc.addListener(this);
            state = getContextState();
            if (state == null) {
                exit(new Exception("Invalid context ID"));
                return;
            }
        }
        if (!state.validate(this)) return;
        if (!state.getData().is_suspended) {
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
        TCFDataCache<?> stack_trace = getStackTrace();
        if (!stack_trace.validate(this)) return;
        if (getStackFrameIndex() < 0) {
            // Stepped out of selected function
            exit(null);
        }
        else if (bps != null && ctx.canResume(IRunControl.RM_RESUME)) {
            if (bp == null) {
                TCFDataCache<IStackTrace.StackTraceContext> frame = getStackFrame();
                if (!frame.validate(this)) return;
                Number addr = frame.getData().getReturnAddress();
                if (addr == null) {
                    exit(new Exception("Unknown stack frame return address"));
                    return;
                }
                String id = "Step." + ctx.getID();
                launch.addContextActionBreakpoint(id, "Step");
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

    protected void exit(Throwable error) {
        if (exited) return;
        if (bp != null) {
            bps.remove(new String[]{ (String)bp.get(IBreakpoints.PROP_ID) }, new IBreakpoints.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                }
            });
        }
        rc.removeListener(this);
        exited = true;
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
        exit(null);
    }
}
