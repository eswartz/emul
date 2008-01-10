/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.tcf.api.internal.services.local;

import com.windriver.tcf.api.internal.core.Token;
import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.IToken;
import com.windriver.tcf.api.protocol.JSON;
import com.windriver.tcf.api.protocol.Protocol;
import com.windriver.tcf.api.services.IDiagnostics;

public class DiagnosticsService implements IDiagnostics {

    private final IChannel channel;
    
    private class CommandServer implements IChannel.ICommandServer {
        
        public void command(IToken token, String name, byte[] data) {
            try {
                if (name.equals("echo")) {
                    channel.sendResult(token, data);
                }
                else if (name.equals("getTestList")) {
                    channel.sendResult(token, JSON.toJSONSequence(new Object[]{
                            new Integer(0), null, new String[0]}));
                }
                else {
                    channel.terminate(new Exception("Illegal command: " + name));
                }
            }
            catch (Throwable x) {
                channel.terminate(x);
            }
        }
    }

    public DiagnosticsService(IChannel channel) {
        this.channel = channel;
        channel.addCommandServer(this, new CommandServer());
    }

    public String getName() {
        return NAME;
    }

    public IToken echo(final String s, final DoneEcho done) {
        final IToken token = new Token();
        Protocol.invokeLater(new Runnable() {
            public void run() {
                done.doneEcho(token, null, s);
            }
        });
        return token;
    }

    public IToken getTestList(final DoneGetTestList done) {
        final IToken token = new Token();
        Protocol.invokeLater(new Runnable() {
            public void run() {
                done.doneGetTestList(token, null, new String[0]);
            }
        });
        return token;
    }

    public IToken runTest(final String s, final DoneRunTest done) {
        final IToken token = new Token();
        Protocol.invokeLater(new Runnable() {
            public void run() {
                done.doneRunTest(token, new Exception("Test suite not found: " + s), null);
            }
        });
        return token;
    }

    public IToken cancelTest(String context_id, final DoneCancelTest done) {
        final IToken token = new Token();
        Protocol.invokeLater(new Runnable() {
            public void run() {
                done.doneCancelTest(token, null);
            }
        });
        return token;
    }

    public IToken getSymbol(String context_id, String symbol_name, final DoneGetSymbol done) {
        final IToken token = new Token();
        Protocol.invokeLater(new Runnable() {
            public void run() {
                done.doneGetSymbol(token, new Exception("Invalid context"), null);
            }
        });
        return token;
    }
}
