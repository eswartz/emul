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
        StringBuffer buf = new StringBuffer();
        buf.append(Integer.toHexString(count));
        for (int i = 0; i < 64; i++) {
            buf.append('-');
            buf.append((char)(0x400 * i + count));
        }
        BigDecimal s = null;
        switch (count % 10) {
        case 0: s = BigDecimal.valueOf(0); break;
        case 1: s = BigDecimal.valueOf(-1.1); break;
        case 2: s = BigDecimal.valueOf(+1.1); break;
        case 3: s = BigDecimal.valueOf(rnd.nextInt()); break;
        default: s = new BigDecimal(rnd.nextInt() / 1000000.0); break;
        }
        msgs.add(s);
        diag.echoFP(s, this);
        count++;
    }
    
    public void doneEchoFP(IToken token, Throwable error, BigDecimal b) {
        BigDecimal s = msgs.removeFirst();
        if (!test_suite.isActive(this)) return;
        if (error != null) {
            test_suite.done(this, error);
        }
        else if (s.doubleValue() != b.doubleValue()) {
            test_suite.done(this, new Exception("EchoFP test failed: " + s.doubleValue() + " != " + b.doubleValue()));
        }
        else if (count < 0x400) {
            sendMessage();
        }
        else if (msgs.isEmpty()){
            test_suite.done(this, null);
        }
    }
}
