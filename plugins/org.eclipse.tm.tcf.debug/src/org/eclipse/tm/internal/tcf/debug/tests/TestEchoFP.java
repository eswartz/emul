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

import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.Random;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IDiagnostics;

public class TestEchoFP  implements ITCFTest, IDiagnostics.DoneEchoFP {

    private final TCFTestSuite test_suite;
    private final IDiagnostics diag;
    private final LinkedList<BigDecimal> msgs = new LinkedList<BigDecimal>();
    private final Random rnd = new Random();

    private int count = 0;
    private long start_time;

    TestEchoFP(TCFTestSuite test_suite, IChannel channel) {
        this.test_suite = test_suite;
        diag = channel.getRemoteService(IDiagnostics.class);
    }

    public void start() {
        if (diag == null) {
            test_suite.done(this, null);
        }
        else {
            start_time = System.currentTimeMillis();
            for (int i = 0; i < 32; i++) sendMessage();
        }
    }

    private void sendMessage() {
        BigDecimal n = BigDecimal.valueOf(rnd.nextInt(), rnd.nextInt(61) - 30);
        msgs.add(n);
        diag.echoFP(n, this);
        count++;
    }

    private boolean cmp(double x, double y) {
        return (float)x == (float)y;
    }

    public void doneEchoFP(IToken token, Throwable error, BigDecimal b) {
        BigDecimal s = msgs.removeFirst();
        if (!test_suite.isActive(this)) return;
        if (error != null) {
            test_suite.done(this, error);
        }
        else if (!cmp(s.doubleValue(), b.doubleValue())) {
            test_suite.done(this, new Exception("EchoFP test failed: " + s + " != " + b));
        }
        else if (count < 0x800) {
            sendMessage();
            // Don't run the test much longer then 4 seconds
            if (count % 0x10 == 0 && System.currentTimeMillis() - start_time >= 4000) {
                count = 0x800;
            }
        }
        else if (msgs.isEmpty()){
            test_suite.done(this, null);
        }
    }
}
