/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
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

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;

import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * ChannelTCP is a IChannel implementation that works on top of TCP sockets as a transport.
 */
public class ChannelTCP extends StreamChannel {

    private Socket socket;
    private InputStream inp;
    private OutputStream out;
    private boolean started;
    private boolean closed;

    private static SSLContext ssl_context;

    public static void setSSLContext(SSLContext ssl_context) {
        ChannelTCP.ssl_context = ssl_context;
    }

    public ChannelTCP(IPeer remote_peer, final String host, final int port, final boolean ssl) {
        super(remote_peer);
        Thread thread = new Thread() {
            public void run() {
                try {
                    if (ssl) {
                        if (ssl_context == null) throw new Exception("SSL context is not set");
                        socket = ssl_context.getSocketFactory().createSocket(host, port);
                    }
                    else {
                        socket = new Socket(host, port);
                    }
                    socket.setTcpNoDelay(true);
                    socket.setKeepAlive(true);
                    if (ssl) ((SSLSocket)socket).startHandshake();
                    inp = new BufferedInputStream(socket.getInputStream());
                    out = new BufferedOutputStream(socket.getOutputStream());
                    onSocketConnected(null);
                }
                catch (final Exception x) {
                    onSocketConnected(x);
                }
            }
        };
        thread.setName("TCF Socket Connect");
        thread.start();
    }

    public ChannelTCP(IPeer remote_peer, String host, int port) {
        this(remote_peer, host, port, false);
    }

    public ChannelTCP(IPeer local_peer, IPeer remote_peer, Socket socket) throws IOException {
        super(local_peer, remote_peer);
        this.socket = socket;
        socket.setTcpNoDelay(true);
        inp = new BufferedInputStream(socket.getInputStream());
        out = new BufferedOutputStream(socket.getOutputStream());
        onSocketConnected(null);
    }

    private void onSocketConnected(final Throwable x) {
        Protocol.invokeLater(new Runnable() {
            public void run() {
                if (x != null) terminate(x);
                if (closed) {
                    try {
                        if (socket != null) {
                            socket.close();
                            if (out != null) out.close();
                            if (inp != null) inp.close();
                        }
                    }
                    catch (IOException y) {
                        Protocol.log("Cannot close socket", y);
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
    protected final void put(byte[] buf) throws IOException {
        if (closed) return;
        out.write(buf);
    }

    @Override
    protected final void flush() throws IOException {
        if (closed) return;
        out.flush();
    }

    @Override
    protected void stop() throws IOException {
        closed = true;
        if (started) {
            socket.close();
            out.close();
            inp.close();
        }
    }
}
