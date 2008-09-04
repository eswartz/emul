/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.services.local;

import org.eclipse.tm.internal.tcf.core.Token;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IDiagnostics;


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
                            null, new String[0]}));
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
