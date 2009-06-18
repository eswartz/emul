/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.tests;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.tm.tcf.services.IDiagnostics;
import org.eclipse.tm.tcf.services.IExpressions;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IStackTrace;

class TestExpressions implements ITCFTest,
    IRunControl.RunControlListener, IExpressions.ExpressionsListener, IBreakpoints.BreakpointsListener {
    
    private final TCFTestSuite test_suite;
    private final IDiagnostics diag;
    private final IExpressions expr;
    private final IStackTrace stk;
    private final IRunControl rc;
    private final IBreakpoints bp;
    
    private String bp_id;
    private boolean bp_ok;
    private IDiagnostics.ISymbol sym_func3;
    private String process_id;
    private String thread_id;
    private boolean process_exited;
    private boolean test_done;
    private IRunControl.RunControlContext thread_ctx;
    private String suspended_pc;
    private boolean waiting_suspend;
    private String[] stack_trace;
    private String[] local_vars;
    private final Map<String,IExpressions.Expression> expr_ctx =
        new HashMap<String,IExpressions.Expression>();
    private final Map<String,IExpressions.Value> expr_val =
        new HashMap<String,IExpressions.Value>();
    
    private static String[] test_expressions = {
        "func2_local1 == func2_local1",
        "func2_local1 != func2_local2",
        "1.34 == 1.34",
        "1.34 != 1.35",
        "1 ? 1 : 0",
        "!func2_local1 ? 0 : 1",
        "(0 || 0) == 0",
        "(0 || func2_local1) == 1",
        "(func2_local1 || 0) == 1",
        "(func2_local1 || func2_local1) == 1",
        "(0 && 0) == 0",
        "(0 && func2_local1) == 0",
        "(func2_local1 && 0) == 0",
        "(func2_local1 && func2_local1) == 1",
        "(func2_local1 | func2_local2) == 3",
        "(func2_local1 & func2_local2) == 0",
        "(func2_local1 ^ func2_local2) == 3",
        "(func2_local1 < func2_local2)",
        "(func2_local1 <= func2_local2)",
        "!(func2_local1 > func2_local2)",
        "!(func2_local1 >= func2_local2)",
        "(func2_local1 < 1.1)",
        "(func2_local1 <= 1.1)",
        "!(func2_local1 > 1.1)",
        "!(func2_local1 >= 1.1)",
        "(func2_local2 << 2) == 8",
        "(func2_local2 >> 1) == 1",
        "+func2_local2 == 2",
        "-func2_local2 == -2",
        "((func2_local1 + func2_local2) * 2 - 2) / 2 == 2",
        "func2_local3.f_struct->f_struct->f_struct == &func2_local3"
    };
    
    TestExpressions(TCFTestSuite test_suite, IChannel channel) {
        this.test_suite = test_suite;
        diag = channel.getRemoteService(IDiagnostics.class);
        expr = channel.getRemoteService(IExpressions.class);
        stk = channel.getRemoteService(IStackTrace.class);
        rc = channel.getRemoteService(IRunControl.class);
        bp = channel.getRemoteService(IBreakpoints.class);
    }

    public void start() {
        if (diag == null || expr == null || stk == null || rc == null || bp == null) {
            test_suite.done(this, null);
        }
        else {
            expr.addListener(this);
            rc.addListener(this);
            bp.addListener(this);
            diag.getTestList(new IDiagnostics.DoneGetTestList() {
                public void doneGetTestList(IToken token, Throwable error, String[] list) {
                    assert test_suite.isActive(TestExpressions.this);
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        for (int i = 0; i < list.length; i++) {
                            if (list[i].equals("RCBP1")) {
                                runTest();
                                return;
                            }
                        }
                        exit(null);
                    }
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private void runTest() {
        if (bp_id == null) {
            bp.set(null, new IBreakpoints.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        bp_id = "TestExpressionsBP";
                        runTest();
                    }
                }
            });
            return;
        }
        if (!bp_ok) {
            Map<String,Object> m = new HashMap<String,Object>();
            m.put(IBreakpoints.PROP_ID, bp_id);
            m.put(IBreakpoints.PROP_ENABLED, Boolean.TRUE);
            m.put(IBreakpoints.PROP_LOCATION, "tcf_test_func3");
            bp.set(new Map[]{ m }, new IBreakpoints.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        bp_ok = true;
                        runTest();
                    }
                }
            });
            return;
        }
        if (process_id == null) {
            diag.runTest("RCBP1", new IDiagnostics.DoneRunTest() {
                public void doneRunTest(IToken token, Throwable error, String id) {
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        process_id = id;
                        runTest();
                    }
                }
            });
            return;
        }
        if (thread_id == null) {
            rc.getChildren(process_id, new IRunControl.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception error, String[] ids) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (ids == null || ids.length == 0) {
                        exit(new Exception("Test process has no threads"));
                    }
                    else {
                        thread_id = ids[0];
                        runTest();
                    }
                }
            });
            return;
        }
        if (thread_ctx == null) {
            rc.getContext(thread_id, new IRunControl.DoneGetContext() {
                public void doneGetContext(IToken token, Exception error, IRunControl.RunControlContext ctx) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (ctx == null || !ctx.hasState()) {
                        exit(new Exception("Invalid thread context"));
                    }
                    else {
                        thread_ctx = ctx;
                        runTest();
                    }
                }
            });
            return;
        }
        if (suspended_pc == null) {
            thread_ctx.getState(new IRunControl.DoneGetState() {
                public void doneGetState(IToken token, Exception error,
                        boolean suspended, String pc, String reason,
                        Map<String,Object> params) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (suspended) {
                        suspended_pc = pc;
                        runTest();
                    }
                    else {
                        waiting_suspend = true;
                    }
                }
            });
            return;
        }
        if (sym_func3 == null) {
            diag.getSymbol(process_id, "tcf_test_func3", new IDiagnostics.DoneGetSymbol() {
                public void doneGetSymbol(IToken token, Throwable error, IDiagnostics.ISymbol symbol) {
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        sym_func3 = symbol;
                        runTest();
                    }
                }
            });
            return;
        }
        BigInteger pc0 = new BigInteger(sym_func3.getValue().toString());
        BigInteger pc1 = new BigInteger(suspended_pc);
        if (!pc0.equals(pc1)) {
            suspended_pc = null;
            waiting_suspend = true;
            thread_ctx.resume(IRunControl.RM_RESUME, 1, new IRunControl.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error != null) exit(error);
                }
            });
            return;
        }
        if (stack_trace == null) {
            stk.getChildren(thread_id, new IStackTrace.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        stack_trace = context_ids;
                        runTest();
                    }
                }
            });
            return;
        }
        if (local_vars == null) {
            expr.getChildren(stack_trace[stack_trace.length - 2], new IExpressions.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                    if (error != null) {
                        // Need to continue tests even if local variables info is not available.
                        // TODO: need to distinguish absence of debug info from other errors.
                        local_vars = new String[0];
                        runTest();
                    }
                    else {
                        local_vars = context_ids;
                        runTest();
                    }
                }
            });
            return;
        }
        for (final String id : local_vars) {
            if (expr_ctx.get(id) == null) {
                expr.getContext(id, new IExpressions.DoneGetContext() {
                    public void doneGetContext(IToken token, Exception error, IExpressions.Expression ctx) {
                        if (error != null) {
                            exit(error);
                        }
                        else {
                            expr_ctx.put(id, ctx);
                            runTest();
                        }
                    }
                });
                return;
            }
        }
        for (final String id : test_expressions) {
            if (local_vars.length == 0 && id.indexOf("local") >= 0) continue;
            if (expr_ctx.get(id) == null) {
                expr.create(stack_trace[stack_trace.length - 2], null, id, new IExpressions.DoneCreate() {
                    public void doneCreate(IToken token, Exception error, IExpressions.Expression ctx) {
                        if (error != null) {
                            exit(error);
                        }
                        else {
                            expr_ctx.put(id, ctx);
                            runTest();
                        }
                    }
                });
                return;
            }
        }
        for (final String id : local_vars) {
            if (expr_val.get(id) == null) {
                expr.evaluate(id, new IExpressions.DoneEvaluate() {
                    public void doneEvaluate(IToken token, Exception error, IExpressions.Value ctx) {
                        if (error != null) {
                            exit(error);
                        }
                        else {
                            expr_val.put(id, ctx);
                            runTest();
                        }
                    }
                });
                return;
            }
        }
        for (final String id : expr_ctx.keySet()) {
            if (expr_val.get(id) == null) {
                expr.evaluate(expr_ctx.get(id).getID(), new IExpressions.DoneEvaluate() {
                    public void doneEvaluate(IToken token, Exception error, IExpressions.Value ctx) {
                        if (error != null) {
                            exit(error);
                        }
                        else {
                            expr_val.put(id, ctx);
                            byte[] arr = ctx.getValue();
                            boolean b = false;
                            for (byte x : arr) {
                                if (x != 0) b = true;
                            }
                            if (!b) exit(new Exception("Invalid value of expression \"" + id + "\""));
                            runTest();
                        }
                    }
                });
                return;
            }
        }
        test_done = true;
        diag.cancelTest(process_id, new IDiagnostics.DoneCancelTest() {
            public void doneCancelTest(IToken token, Throwable error) {
                exit(error);
            }
        });
    }
    
    private void exit(Throwable x) {
        if (!test_suite.isActive(this)) return;
        expr.removeListener(this);
        bp.removeListener(this);
        rc.removeListener(this);
        test_suite.done(this, x);
    }

    //--------------------------- Run Control listener ---------------------------//
    
    public void containerResumed(String[] context_ids) {
    }

    public void containerSuspended(String context, String pc, String reason,
            Map<String,Object> params, String[] suspended_ids) {
        for (String id : suspended_ids) {
            assert id != null;
            contextSuspended(id, null, null, null);
        }
    }

    public void contextAdded(IRunControl.RunControlContext[] contexts) {
    }

    public void contextChanged(IRunControl.RunControlContext[] contexts) {
    }

    public void contextException(String context, String msg) {
    }

    public void contextRemoved(String[] context_ids) {
        for (String id : context_ids) {
            if (id.equals(process_id)) {
                process_exited = true;
                if (!test_done) exit(new Exception("Test process exited too soon"));
            }
        }
    }

    public void contextResumed(String context) {
    }

    public void contextSuspended(String context, String pc, String reason, Map<String,Object> params) {
        if (context.equals(thread_id)) {
            suspended_pc = pc;
            if (waiting_suspend) {
                waiting_suspend = false;
                runTest();
            }
        }
    }

    //--------------------------- Expressions listener ---------------------------//

    public void valueChanged(String id) {
    }

    //--------------------------- Breakpoints listener ---------------------------//

    @SuppressWarnings("unchecked")
    public void breakpointStatusChanged(String id, Map<String,Object> status) {
        if (id.equals(bp_id) && process_id != null && !process_exited) {
            String s = (String)status.get(IBreakpoints.STATUS_ERROR);
            if (s != null) exit(new Exception("Invalid BP status: " + s));
            Collection<Map<String,Object>> list = (Collection<Map<String,Object>>)status.get(IBreakpoints.STATUS_INSTANCES);
            if (list == null) return;
            String err = null;
            for (Map<String,Object> map : list) {
                String ctx = (String)map.get(IBreakpoints.INSTANCE_CONTEXT);
                if (process_id.equals(ctx) && map.get(IBreakpoints.INSTANCE_ERROR) != null)
                    err = (String)map.get(IBreakpoints.INSTANCE_ERROR);
            }
            if (err != null) exit(new Exception("Invalid BP status: " + err));
        }
    }

    public void contextAdded(Map<String,Object>[] bps) {
    }

    public void contextChanged(Map<String,Object>[] bps) {
    }
}
