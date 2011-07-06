/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.tests;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IDiagnostics;
import org.eclipse.tm.tcf.services.IRunControl;
import org.eclipse.tm.tcf.services.IRunControl.RunControlContext;

class TestAttachTerminate implements ITCFTest, IRunControl.RunControlListener {

    private final TCFTestSuite test_suite;
    private final IDiagnostics diag;
    private final IRunControl rc;
    private final Random rnd = new Random();

    private int cnt = 0;

    private final HashMap<String,IRunControl.RunControlContext> ctx_map =
        new HashMap<String,IRunControl.RunControlContext>();
    private final HashSet<String> test_ctx_ids = new HashSet<String>();
    private final HashSet<String> suspended_ctx_ids = new HashSet<String>();
    private final HashMap<String,IToken> resume_cmds = new HashMap<String,IToken>();

    TestAttachTerminate(TCFTestSuite test_suite, IChannel channel) {
        this.test_suite = test_suite;
        diag = channel.getRemoteService(IDiagnostics.class);
        rc = channel.getRemoteService(IRunControl.class);
    }

    public void start() {
        if (diag == null || rc == null) {
            test_suite.done(this, null);
        }
        else {
            rc.addListener(this);
            diag.getTestList(new IDiagnostics.DoneGetTestList() {
                public void doneGetTestList(IToken token, Throwable error, String[] list) {
                    if (!test_suite.isActive(TestAttachTerminate.this)) return;
                    if (error != null) {
                        exit(error);
                    }
                    else if (list.length > 0) {
                        startTestContext(list[rnd.nextInt(list.length)]);
                        Protocol.invokeLater(100, new Runnable() {
                            int cnt = 0;
                            public void run() {
                                if (!test_suite.isActive(TestAttachTerminate.this)) return;
                                cnt++;
                                if (test_suite.cancel) {
                                    exit(null);
                                }
                                else if (cnt < 300) {
                                    Protocol.invokeLater(100, this);
                                    for (String id : suspended_ctx_ids) resume(id);
                                }
                                else if (test_ctx_ids.isEmpty()) {
                                    exit(new Error("Missing 'contextAdded' event"));
                                }
                                else {
                                    exit(new Error("Missing 'contextRemoved' event for " + test_ctx_ids));
                                }
                            }
                        });
                        return;
                    }
                    exit(null);
                }
            });
        }
    }

    public boolean canResume(IRunControl.RunControlContext ctx) {
        if (resume_cmds.get(ctx.getID()) != null) return false;
        String grp = ctx.getRCGroup();
        if (grp != null) {
            for (String id : resume_cmds.keySet()) {
                IRunControl.RunControlContext c = ctx_map.get(id);
                if (c == null) return false;
                if (grp.equals(c.getRCGroup())) return false;
            }
        }
        return true;
    }

    private void resume(final String id) {
        IRunControl.RunControlContext ctx = ctx_map.get(id);
        if (ctx != null && test_suite.canResume(ctx)) {
            assert resume_cmds.get(id) == null;
            resume_cmds.put(id, ctx.resume(IRunControl.RM_RESUME, 1, new IRunControl.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    assert resume_cmds.get(id) == token;
                    resume_cmds.remove(id);
                    if (error instanceof IErrorReport) {
                        int code = ((IErrorReport)error).getErrorCode();
                        if (code == IErrorReport.TCF_ERROR_ALREADY_RUNNING) return;
                        if (code == IErrorReport.TCF_ERROR_INV_CONTEXT && ctx_map.get(id) == null) return;
                    }
                    if (error != null) exit(error);
                }
            }));
        }
    }

    private void startTestContext(String test_name) {
        for (int i = 0; i < 4; i++) {
            diag.runTest(test_name, new IDiagnostics.DoneRunTest() {
                public void doneRunTest(IToken token, Throwable error, final String id) {
                    cnt--;
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        assert id != null;
                        if (ctx_map.get(id) == null) {
                            exit(new Error("Missing 'contextAdded' event for context " + id));
                        }
                        else {
                            test_ctx_ids.add(id);
                            Runnable r = new Runnable() {
                                public void run() {
                                    if (!test_suite.isActive(TestAttachTerminate.this)) return;
                                    IRunControl.RunControlContext ctx = ctx_map.get(id);
                                    if (ctx == null) return;
                                    if (!test_suite.canResume(ctx)) {
                                        Protocol.invokeLater(100, this);
                                    }
                                    else {
                                        diag.cancelTest(id, new IDiagnostics.DoneCancelTest() {
                                            public void doneCancelTest(IToken token, Throwable error) {
                                                if (error instanceof IErrorReport) {
                                                    int code = ((IErrorReport)error).getErrorCode();
                                                    if (code == IErrorReport.TCF_ERROR_INV_CONTEXT && ctx_map.get(id) == null) return;
                                                }
                                                if (error != null) exit(error);
                                            }
                                        });
                                    }
                                }
                            };
                            Protocol.invokeLater(r);
                        }
                    }
                }
            });
            cnt++;
        }
    }

    private void exit(Throwable x) {
        if (!test_suite.isActive(this)) return;
        rc.removeListener(this);
        test_suite.done(this, x);
    }

    public void containerResumed(String[] context_ids) {
        for (String id : context_ids) contextResumed(id);
    }

    public void containerSuspended(String main_context, String pc,
            String reason, Map<String, Object> params,
            String[] suspended_ids) {
        for (String context : suspended_ids) {
            assert context != null;
            contextSuspended(context, null, null, null);
        }
    }

    public void contextAdded(RunControlContext[] contexts) {
        for (RunControlContext ctx : contexts) {
            if (ctx_map.get(ctx.getID()) != null) exit(new Error("Invalid 'contextAdded' event"));
            ctx_map.put(ctx.getID(), ctx);
        }
    }

    public void contextChanged(RunControlContext[] contexts) {
        for (RunControlContext ctx : contexts) {
            if (ctx_map.get(ctx.getID()) == null) return;
            ctx_map.put(ctx.getID(), ctx);
        }
    }

    public void contextException(String context, String msg) {
    }

    public void contextRemoved(String[] context_ids) {
        for (String id : context_ids) {
            ctx_map.remove(id);
            test_ctx_ids.remove(id);
            suspended_ctx_ids.remove(id);
        }
        if (cnt == 0 && test_ctx_ids.isEmpty()) exit(null);
    }

    public void contextResumed(String context) {
        suspended_ctx_ids.remove(context);
    }

    public void contextSuspended(String context, String pc, String reason, Map<String,Object> params) {
        assert context != null;
        suspended_ctx_ids.add(context);
        resume(context);
    }
}