/*******************************************************************************
 * Copyright (c) 2008, 2011 Wind River Systems, Inc. and others.
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
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IBreakpoints;
import org.eclipse.tm.tcf.services.IDiagnostics;
import org.eclipse.tm.tcf.services.IExpressions;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IStackTrace;
import org.eclipse.tm.tcf.services.ISymbols;

class TestExpressions implements ITCFTest,
    IRunControl.RunControlListener, IExpressions.ExpressionsListener, IBreakpoints.BreakpointsListener {

    private final TCFTestSuite test_suite;
    private final RunControl test_rc;
    private final IDiagnostics diag;
    private final IExpressions expr;
    private final ISymbols syms;
    private final IStackTrace stk;
    private final IRunControl rc;
    private final IBreakpoints bp;
    private final Random rnd = new Random();

    private String test_id;
    private String bp_id;
    private boolean bp_ok;
    private IDiagnostics.ISymbol sym_func3;
    private String test_ctx_id;
    private String process_id;
    private String thread_id;
    private boolean run_to_bp_done;
    private boolean test_done;
    private boolean cancel_test_sent;
    private IRunControl.RunControlContext test_ctx;
    private IRunControl.RunControlContext thread_ctx;
    private String suspended_pc;
    private boolean waiting_suspend;
    private String[] stack_trace;
    private IStackTrace.StackTraceContext[] stack_frames;
    private String[] local_vars;
    private final Map<String,IExpressions.Expression> expr_ctx = new HashMap<String,IExpressions.Expression>();
    private final Map<String,IExpressions.Value> expr_val = new HashMap<String,IExpressions.Value>();
    private final Map<String,ISymbols.Symbol> expr_sym = new HashMap<String,ISymbols.Symbol>();
    private final Map<String,String[]> expr_chld = new HashMap<String,String[]>();
    private final Set<String> expr_to_dispose = new HashSet<String>();

    private static String[] test_expressions = {
        "func2_local1",
        "func2_local2",
        "func2_local3",
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
        "(short)(int)(long)((char *)func2_local2 + 1) == 3",
        "((func2_local1 + func2_local2) * 2 - 2) / 2 == 2",
        "func2_local3.f_struct->f_struct->f_struct == &func2_local3",
        "(char *)func2_local3.f_struct",
        "(char[4])func2_local3.f_struct",
        "&((test_struct *)0)->f_float",
        "&((struct test_struct *)0)->f_float",
        "tcf_test_func3",
        "&tcf_test_func3",
        "tcf_test_array + 10",
        "*(tcf_test_array + 10) | 1",
    };

    TestExpressions(TCFTestSuite test_suite, RunControl test_rc, IChannel channel) {
        this.test_suite = test_suite;
        this.test_rc = test_rc;
        diag = channel.getRemoteService(IDiagnostics.class);
        expr = channel.getRemoteService(IExpressions.class);
        syms = channel.getRemoteService(ISymbols.class);
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
                    if (!test_suite.isActive(TestExpressions.this)) return;
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        if (list.length > 0) {
                            test_id = list[rnd.nextInt(list.length)];
                            runTest();
                            Protocol.invokeLater(100, new Runnable() {
                                int cnt = 0;
                                public void run() {
                                    if (!test_suite.isActive(TestExpressions.this)) return;
                                    cnt++;
                                    if (test_suite.cancel) {
                                        exit(null);
                                    }
                                    else if (cnt < 600) {
                                        if (test_done && !cancel_test_sent) {
                                            test_rc.cancel(thread_id, test_ctx_id);
                                            cancel_test_sent = true;
                                        }
                                        Protocol.invokeLater(100, this);
                                    }
                                    else if (test_ctx_id == null) {
                                        exit(new Error("Timeout waiting for reply of Diagnostics.runTest command"));
                                    }
                                    else {
                                        exit(new Error("Missing 'contextRemoved' event for " + test_ctx_id));
                                    }
                                }
                            });
                            return;
                        }
                        exit(null);
                    }
                }
            });
        }
    }

    public boolean canResume(String id) {
        if (test_ctx_id != null && thread_ctx == null) return false;
        if (thread_ctx != null && !test_done) {
            assert thread_ctx.getID().equals(thread_id);
            IRunControl.RunControlContext ctx = test_rc.getContext(id);
            if (ctx == null) return false;
            String grp = ctx.getRCGroup();
            if (id.equals(thread_id) || grp != null && grp.equals(thread_ctx.getRCGroup())) {
                if (run_to_bp_done) return false;
                if (sym_func3 == null) return false;
                if (suspended_pc == null) return false;
                BigInteger pc0 = new BigInteger(sym_func3.getValue().toString());
                BigInteger pc1 = new BigInteger(suspended_pc);
                if (pc0.equals(pc1)) return false;
            }
        }
        return true;
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
        if (test_ctx_id == null) {
            diag.runTest(test_id, new IDiagnostics.DoneRunTest() {
                public void doneRunTest(IToken token, Throwable error, String id) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (id == null) {
                        exit(new Exception("Test context ID must not be null"));
                    }
                    else if (test_rc.getContext(id) == null) {
                        exit(new Exception("Missing context added event"));
                    }
                    else {
                        test_ctx_id = id;
                        runTest();
                    }
                }
            });
            return;
        }
        if (test_ctx == null) {
            rc.getContext(test_ctx_id, new IRunControl.DoneGetContext() {
                public void doneGetContext(IToken token, Exception error, IRunControl.RunControlContext ctx) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (ctx == null) {
                        exit(new Exception("Invalid test execution context"));
                    }
                    else {
                        test_ctx = ctx;
                        process_id = test_ctx.getProcessID();
                        if (test_ctx.hasState()) thread_id = test_ctx_id;
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
                    else if (ids.length != 1) {
                        exit(new Exception("Test process has too many threads"));
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
                        exit(new Exception("Cannot get context state", error));
                    }
                    else if (!suspended) {
                        waiting_suspend = true;
                    }
                    else if (pc == null || pc.length() == 0 || pc.equals("0")) {
                        exit(new Exception("Invalid context PC"));
                    }
                    else {
                        suspended_pc = pc;
                        runTest();
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
                    else if (symbol == null) {
                        exit(new Exception("Symbol must not be null: tcf_test_func3"));
                    }
                    else {
                        sym_func3 = symbol;
                        runTest();
                    }
                }
            });
            return;
        }
        if (!run_to_bp_done) {
            BigInteger pc0 = new BigInteger(sym_func3.getValue().toString());
            BigInteger pc1 = new BigInteger(suspended_pc);
            if (!pc0.equals(pc1)) {
                waiting_suspend = true;
                test_rc.resume(thread_id, IRunControl.RM_RESUME);
                return;
            }
            run_to_bp_done = true;
        }
        assert test_done || !canResume(thread_id);
        if (stack_trace == null) {
            stk.getChildren(thread_id, new IStackTrace.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (context_ids == null || context_ids.length < 2) {
                        exit(new Exception("Invalid stack trace"));
                    }
                    else {
                        stack_trace = context_ids;
                        runTest();
                    }
                }
            });
            return;
        }
        if (stack_frames == null) {
            stk.getContext(stack_trace, new IStackTrace.DoneGetContext() {
                public void doneGetContext(IToken token, Exception error, IStackTrace.StackTraceContext[] frames) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (frames == null || frames.length != stack_trace.length) {
                        exit(new Exception("Invalid stack trace"));
                    }
                    else {
                        stack_frames = frames;
                        runTest();
                    }
                }
            });
            return;
        }
        if (local_vars == null) {
            expr.getChildren(stack_trace[stack_trace.length - 2], new IExpressions.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                    if (error != null || context_ids == null) {
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
        for (final String txt : test_expressions) {
            if (local_vars.length == 0) {
                // Debug info not available
                if (txt.indexOf("local") >= 0) continue;
                if (txt.indexOf("test_struct") >= 0) continue;
                if (txt.indexOf("tcf_test_array") >= 0) continue;
            }
            if (expr_ctx.get(txt) == null) {
                expr.create(stack_trace[stack_trace.length - 2], null, txt, new IExpressions.DoneCreate() {
                    public void doneCreate(IToken token, Exception error, IExpressions.Expression ctx) {
                        if (error != null) {
                            exit(error);
                        }
                        else {
                            expr_to_dispose.add(ctx.getID());
                            expr_ctx.put(txt, ctx);
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
        if (syms != null) {
            for (final String id : expr_val.keySet()) {
                if (expr_sym.get(id) == null) {
                    IExpressions.Value v = expr_val.get(id);
                    String type_id = v.getTypeID();
                    if (type_id != null) {
                        syms.getContext(type_id, new ISymbols.DoneGetContext() {
                            public void doneGetContext(IToken token, Exception error, ISymbols.Symbol ctx) {
                                if (error != null) {
                                    exit(error);
                                }
                                else if (ctx == null) {
                                    exit(new Exception("Symbol.getContext returned null"));
                                }
                                else {
                                    expr_sym.put(id, ctx);
                                    runTest();
                                }
                            }
                        });
                        return;
                    }
                }
            }
            for (final String id : expr_sym.keySet()) {
                if (expr_chld.get(id) == null) {
                    ISymbols.Symbol sym = expr_sym.get(id);
                    syms.getChildren(sym.getID(), new ISymbols.DoneGetChildren() {
                        public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                            if (error != null) {
                                exit(error);
                            }
                            else {
                                if (context_ids == null) context_ids = new String[0];
                                expr_chld.put(id, context_ids);
                                runTest();
                            }
                        }
                    });
                    return;
                }
            }
        }
        for (final String id : expr_to_dispose) {
            expr.dispose(id, new IExpressions.DoneDispose() {
                public void doneDispose(IToken token, Exception error) {
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        expr_to_dispose.remove(id);
                        runTest();
                    }
                }
            });
            return;
        }
        test_done = true;
        test_rc.resume(thread_id, IRunControl.RM_RESUME);
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
        for (String id : context_ids) contextResumed(id);
    }

    public void containerSuspended(String context, String pc, String reason,
            Map<String,Object> params, String[] suspended_ids) {
        for (String id : suspended_ids) {
            contextSuspended(id, null, null, null);
        }
    }

    public void contextAdded(IRunControl.RunControlContext[] contexts) {
    }

    public void contextChanged(IRunControl.RunControlContext[] contexts) {
    }

    public void contextException(String context, String msg) {
        if (test_done) return;
        IRunControl.RunControlContext ctx = test_rc.getContext(context);
        if (ctx != null) {
            String p = ctx.getParentID();
            String c = ctx.getCreatorID();
            if (!test_ctx_id.equals(c) && !test_ctx_id.equals(p)) return;
        }
        exit(new Exception("Context exception: " + msg));
    }

    public void contextRemoved(String[] context_ids) {
        for (String id : context_ids) {
            if (id.equals(test_ctx_id)) {
                if (test_done) {
                    bp.set(null, new IBreakpoints.DoneCommand() {
                        public void doneCommand(IToken token, Exception error) {
                            exit(error);
                        }
                    });
                }
                else {
                    exit(new Exception("Test process exited too soon"));
                }
                return;
            }
        }
    }

    public void contextResumed(String id) {
        if (id.equals(thread_id)) {
            if (run_to_bp_done && !test_done) {
                assert thread_ctx != null;
                assert !canResume(thread_id);
                exit(new Exception("Unexpected contextResumed event: " + id));
            }
            suspended_pc = null;
        }
    }

    public void contextSuspended(String id, String pc, String reason, Map<String,Object> params) {
        assert id != null;
        if (id.equals(thread_id) && waiting_suspend) {
            suspended_pc = pc;
            waiting_suspend = false;
            runTest();
        }
    }

    //--------------------------- Expressions listener ---------------------------//

    public void valueChanged(String id) {
    }

    //--------------------------- Breakpoints listener ---------------------------//

    @SuppressWarnings("unchecked")
    public void breakpointStatusChanged(String id, Map<String,Object> status) {
        if (id.equals(bp_id) && process_id != null && !test_done) {
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
