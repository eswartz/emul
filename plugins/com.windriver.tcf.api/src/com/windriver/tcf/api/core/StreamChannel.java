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
package com.windriver.tcf.api.core;

import java.io.IOException;

import com.windriver.tcf.api.protocol.IPeer;

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
