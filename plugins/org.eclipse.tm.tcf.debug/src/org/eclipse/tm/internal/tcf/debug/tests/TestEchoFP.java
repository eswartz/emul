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

    TestEchoFP(TCFTestSuite test_suite, IChannel channel) {
        this.test_suite = test_suite;
        diag = channel.getRemoteService(IDiagnostics.class);
    }

    public void start() {
        if (diag == null) {
            test_suite.done(this, null);
        }
        else {
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
        }
        else if (msgs.isEmpty()){
            test_suite.done(this, null);
        }
    }
}
