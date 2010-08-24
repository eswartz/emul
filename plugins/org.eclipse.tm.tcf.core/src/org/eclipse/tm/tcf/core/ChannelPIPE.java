/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.tcf.core;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * ChannelPIPE is a IChannel implementation that works on top of named pipes as a transport.
 */
public class ChannelPIPE extends StreamChannel {

    private InputStream inp;
    private OutputStream out;
    private boolean started;
    private boolean closed;

    public ChannelPIPE(IPeer remote_peer, String name) {
        super(remote_peer);
        try {
            inp = new BufferedInputStream(new FileInputStream(name));
            byte[] buf = new byte[0x400];
            int rd = inp.read(buf);
            if (rd <= 0 || buf[rd - 1] != 0) throw new Exception("Invalid remote peer responce");
            out = new BufferedOutputStream(new FileOutputStream(new String(buf, 0, rd - 1, "UTF-8")));
            onConnected(null);
        }
        catch (Exception x) {
            onConnected(x);
        }
    }

    private void onConnected(final Throwable x) {
        Protocol.invokeLater(new Runnable() {
            public void run() {
                if (x != null) {
                    terminate(x);
                    closed = true;
                }
                if (closed) {
                    try {
                        if (out != null) out.close();
                        if (inp != null) inp.close();
                    }
                    catch (IOException y) {
                        Protocol.log("Cannot close pipe", y);
                    }
                }
                else {
                    started = true;
                    start();
                }
            }
        });
    }

    @Override
    protected int get() throws IOException {
        try {
            if (closed) return -1;
            return inp.read();
        }
        catch (SocketException x) {
            if (closed) return -1;
            throw x;
        }
    }

    @Override
    protected void put(int b) throws IOException {
        assert b >= 0 && b <= 0xff;
        if (closed) return;
        out.write(b);
    }

    @Override
    protected final void put(byte[] buf) throws IOException {
        if (closed) return;
        out.write(buf);
    }

    @Override
    protected void flush() throws IOException {
        if (closed) return;
        out.flush();
    }

    @Override
    protected void stop() throws IOException {
        closed = true;
        if (started) {
            out.close();
            inp.close();
        }
    }
}
