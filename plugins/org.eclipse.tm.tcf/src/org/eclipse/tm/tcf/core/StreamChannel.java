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
package org.eclipse.tm.tcf.core;

import java.io.IOException;

import org.eclipse.tm.tcf.protocol.IPeer;

/**
 * Abstract implementation of IChannel interface for stream oriented transport protocols.
 *
 * StreamChannel implements communication link connecting two end points (peers).
 * The channel asynchronously transmits messages: commands, results and events.
 * 
 * StreamChannel uses escape sequences to represent End-Of-Message and End-Of-Stream markers.
 * 
 * Clients can subclass StreamChannel to support particular stream oriented transport (wire) protocol.
 * Also, see ChannelTCP for a concrete IChannel implementation that works on top of TCP sockets as a transport.
 */
public abstract class StreamChannel extends AbstractChannel {

    public static final int ESC = 3;

    public StreamChannel(IPeer peer) {
        super(peer);
    }

    protected abstract int get() throws IOException;
    protected abstract void put(int n) throws IOException;

    @Override
    protected final int read() throws IOException {
        int res = get();
        if (res < 0) return EOS;
        assert res >= 0 && res <= 0xff;
        if (res != ESC) return res;
        int n = get();
        switch (n) {
        case 0: return ESC;
        case 1: return EOM;
        case 2: return EOS;
        default:
            if (n < 0) return EOS;
            assert false;
            return 0;
        }
    }

    @Override
    protected final void write(int n) throws IOException {
        switch (n) {
        case ESC: put(ESC); put(0); break;
        case EOM: put(ESC); put(1); break;
        case EOS: put(ESC); put(2); break;
        default:
            assert n >= 0 && n <= 0xff;
            put(n);
        }
    }

    @Override
    protected void write(byte[] buf) throws IOException {
        for (int i = 0; i < buf.length; i++) {
            int n = buf[i] & 0xff;
            put(n);
            if (n == ESC) put(0);
        }
    }
}
