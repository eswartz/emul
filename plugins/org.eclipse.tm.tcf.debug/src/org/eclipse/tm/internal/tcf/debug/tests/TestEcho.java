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

import java.util.LinkedList;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IErrorReport;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IDiagnostics;

class TestEcho implements ITCFTest, IDiagnostics.DoneEcho {

    private final TCFTestSuite test_suite;
    private final IDiagnostics diag;
    private final LinkedList<String> msgs = new LinkedList<String>();
    private int count = 0;
    private long start_time;

    TestEcho(TCFTestSuite test_suite, IChannel channel) {
        this.test_suite = test_suite;
        diag = channel.getRemoteService(IDiagnostics.class);
    }

    public void start() {
        if (diag == null) {
            test_suite.done(this, null);
        }
        else {
            diag.not_implemented_command(new IDiagnostics.DoneNotImplementedCommand() {

                public void doneNotImplementedCommand(IToken token, Throwable error) {
                    if (!(error instanceof IErrorReport)) {
                        test_suite.done(TestEcho.this, new Exception("Invalid responce to unimplemented command"));
                        return;
                    }
                    if (((IErrorReport)error).getErrorCode() != IErrorReport.TCF_ERROR_INV_COMMAND) {
                        test_suite.done(TestEcho.this, new Exception("Invalid error code in responce to unimplemented command"));
                        return;
                    }
                    start_time = System.currentTimeMillis();
                    for (int i = 0; i < 32; i++) sendMessage();
                }
            });
        }
    }

    private void sendMessage() {
        StringBuffer buf = new StringBuffer();
        buf.append(Integer.toHexString(count));
        for (int i = 0; i < 64; i++) {
            buf.append('-');
            buf.append((char)(0x400 * i + count));
        }
        String s =  buf.toString();
        msgs.add(s);
        diag.echo(s, this);
        count++;
    }

    public void doneEcho(IToken token, Throwable error, String b) {
        String s = msgs.removeFirst();
        if (!test_suite.isActive(this)) return;
        if (error != null) {
            test_suite.done(this, error);
        }
        else if (!s.equals(b)) {
            test_suite.done(this, new Exception("Echo test failed: " + s + " != " + b));
        }
        else if (count < 0x400) {
            sendMessage();
            // Don't run the test much longer then 4 seconds
            if (count % 0x10 == 0 && System.currentTimeMillis() - start_time >= 4000) {
                count = 0x400;
            }
        }
        else if (msgs.isEmpty()){
            test_suite.done(this, null);
        }
    }
}