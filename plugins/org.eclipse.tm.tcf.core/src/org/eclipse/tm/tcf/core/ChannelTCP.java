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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * ChannelTCP is a IChannel implementation that works on top of TCP sockets as a transport.
 */
public class ChannelTCP extends StreamChannel {

    private Socket socket;
    private InputStream inp;
    private OutputStream out;
    private boolean closed;

    public ChannelTCP(IPeer remote_peer, final String host, final int port) {
        super(remote_peer);
        Thread thread = new Thread() {
            public void run() {
                try {
                    socket = new Socket(host, port);
                    socket.setTcpNoDelay(true);
                    inp = new BufferedInputStream(socket.getInputStream());
                    out = new BufferedOutputStream(socket.getOutputStream());
                    Protocol.invokeLater(new Runnable() {
                        public void run() {
                            ChannelTCP.this.start();
                        }
                    });
                }
                catch (final IOException x) {
                    Protocol.invokeLater(new Runnable() {
                        public void run() {
                            ChannelTCP.this.terminate(x);
                        }
                    });
                }
            }
        };
        thread.setName("TCF Socket Connect");
        thread.start();
    }
    
    public ChannelTCP(IPeer local_peer, IPeer remote_peer, Socket socket) throws IOException {
        super(local_peer, remote_peer);
        this.socket = socket;
        socket.setTcpNoDelay(true);
        inp = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());
        start();
    }

    @Override
    protected final int get() throws IOException {
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
    protected final void put(int b) throws IOException {
        assert b >= 0 && b <= 0xff;
        if (closed) return;
        out.write(b);
    }

    @Override
    protected final void flush() throws IOException {
        if (closed) return;
        out.flush();
    }

    @Override
    protected void stop() throws IOException {
        closed = true;
        socket.close();
        out.close();
        inp.close();
    }
}
