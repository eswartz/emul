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

    private int cnt = 0;

    private final HashMap<String,IRunControl.RunControlContext> ctx_map =
        new HashMap<String,IRunControl.RunControlContext>();
    private final HashSet<String> process_ids = new HashSet<String>();

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
                    assert test_suite.isActive(TestAttachTerminate.this);
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        for (int i = 0; i < list.length; i++) {
                            if (list[i].equals("RCBP1")) {
                                startProcess();
                                Protocol.invokeLater(1000, new Runnable() {
                                    int cnt = 0;
                                    public void run() {
                                        if (!test_suite.isActive(TestAttachTerminate.this)) return;
                                        cnt++;
                                        if (cnt < 10) {
                                            Protocol.invokeLater(1000, this);
                                        }
                                        else if (test_suite.cancel) {
                                            exit(null);
                                        }
                                        else if (process_ids.isEmpty()) {
                                            exit(new Error("Missing 'contextAdded' event"));
                                        }
                                        else {
                                            exit(new Error("Missing 'contextRemoved' event for " + process_ids));
                                        }
                                    }
                                });
                                return;
                            }
                        }
                    }
                    exit(null);
                }
            });
        }
    }

    private void startProcess() {
        for (int i = 0; i < 4; i++) {
            diag.runTest("RCBP1", new IDiagnostics.DoneRunTest() {
                public void doneRunTest(IToken token, Throwable error, String context_id) {
                    cnt--;
                    if (error != null) {
                        exit(error);
                    }
                    else {
                        assert context_id != null;
                        if (ctx_map.get(context_id) == null) {
                            exit(new Error("Missing 'contextAdded' event for context " + context_id));
                        }
                        process_ids.add(context_id);
                        diag.cancelTest(context_id, new IDiagnostics.DoneCancelTest() {
                            public void doneCancelTest(IToken token, Throwable error) {
                                if (error != null) exit(error);
                            }
                        });
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
        IRunControl.RunControlContext ctx = ctx_map.get(context);
        if (ctx == null) return;
        if (process_ids.contains(ctx.getParentID())) {
            /*
            exit(new Error("Unexpected 'contextException' event for " + context + ": " + msg));
            */
        }
    }

    public void contextRemoved(String[] context_ids) {
        for (String id : context_ids) {
            ctx_map.remove(id);
            process_ids.remove(id);
        }
        if (cnt == 0 && process_ids.isEmpty()) exit(null);
    }

    public void contextResumed(String context) {
    }

    public void contextSuspended(String context, String pc, String reason, Map<String,Object> params) {
        assert context != null;
        IRunControl.RunControlContext ctx = ctx_map.get(context);
        if (ctx != null && process_ids.contains(ctx.getParentID())) {
            ctx.resume(IRunControl.RM_RESUME, 1, new IRunControl.DoneCommand() {
                public void doneCommand(IToken token, Exception error) {
                    if (error instanceof IErrorReport) {
                        int code = ((IErrorReport)error).getErrorCode();
                        if (code == IErrorReport.TCF_ERROR_ALREADY_RUNNING) return;
                        if (code == IErrorReport.TCF_ERROR_INV_CONTEXT) return;
                    }
                    if (error != null) exit(error);
                }
            });
        }
    }
}