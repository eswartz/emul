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
package org.eclipse.tm.internal.tcf.debug.tests;

import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.ISysMonitor;
import org.eclipse.tm.tcf.services.ISysMonitor.SysMonitorContext;

class TestSysMonitor implements ITCFTest {

    private final TCFTestSuite test_suite;
    private final ISysMonitor sys_mon;
    private final HashMap<String,ISysMonitor.SysMonitorContext> procs =
        new HashMap<String,ISysMonitor.SysMonitorContext>();

    TestSysMonitor(TCFTestSuite test_suite, IChannel channel) {
        this.test_suite = test_suite;
        sys_mon = channel.getRemoteService(ISysMonitor.class);
    }

    public void start() {
        if (sys_mon == null) {
            test_suite.done(this, null);
        }
        else {
            sys_mon.getChildren(null, new ISysMonitor.DoneGetChildren() {
                public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                    if (error != null) {
                        exit(error);
                    }
                    else if (context_ids == null || context_ids.length == 0) {
                        exit(new Exception("ISysMonitor.getChildren(null) returned empty list"));
                    }
                    else {
                        final HashSet<IToken> cmds = new HashSet<IToken>();
                        for (final String id : context_ids) {
                            cmds.add(sys_mon.getContext(id, new ISysMonitor.DoneGetContext() {
                                public void doneGetContext(IToken token, Exception error, SysMonitorContext context) {
                                    cmds.remove(token);
                                    if (error != null) {
                                        // Some errors are expected, like "Access Denied"
                                        if (!(error instanceof IErrorReport)) {
                                            exit(error);
                                            return;
                                        }
                                    }
                                    else {
                                        procs.put(id, context);
                                    }
                                    if (cmds.isEmpty()) getEnvironment();
                                }
                            }));
                        }
                    }
                }
            });
        }
    }

    private void getEnvironment() {
        final HashSet<IToken> cmds = new HashSet<IToken>();
        for (final String id : procs.keySet()) {
            cmds.add(sys_mon.getEnvironment(id, new ISysMonitor.DoneGetEnvironment() {
                public void doneGetEnvironment(IToken token, Exception error, String[] environment) {
                    cmds.remove(token);
                    if (error != null) {
                        // Some errors are expected, like "Access Denied"
                        if (!(error instanceof IErrorReport)) {
                            exit(error);
                            return;
                        }
                    }
                    if (cmds.isEmpty()) getCommandLine();
                }
            }));
        }
    }

    private void getCommandLine() {
        final HashSet<IToken> cmds = new HashSet<IToken>();
        for (final String id : procs.keySet()) {
            cmds.add(sys_mon.getCommandLine(id, new ISysMonitor.DoneGetCommandLine() {
                public void doneGetCommandLine(IToken token, Exception error, String[] cmd_line) {
                    cmds.remove(token);
                    if (error != null) {
                        // Some errors are expected, like "Access Denied"
                        if (!(error instanceof IErrorReport)) {
                            exit(error);
                            return;
                        }
                    }
                    if (cmds.isEmpty()) exit(null);
                }
            }));
        }
    }

    private void exit(Throwable x) {
        if (!test_suite.isActive(this)) return;
        test_suite.done(this, x);
    }
}
