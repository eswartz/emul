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
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.auth.x500.X500Principal;

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

    public ChannelTCP(IPeer remote_peer, final String host, final int port, final boolean ssl) {
        super(remote_peer);
        Thread thread = new Thread() {
            public void run() {
                try {
                    if (ssl) {
                        SSLContext context = SSLContext.getInstance("TLS");
                        X509TrustManager tm = new X509TrustManager() {
                            public void checkClientTrusted(X509Certificate[] chain, String auth_type) throws CertificateException {
                                throw new CertificateException();
                            }
                            public void checkServerTrusted(X509Certificate[] chain, String auth_type) throws CertificateException {
                                if ("RSA".equals(auth_type) && chain != null && chain.length >= 1) {
                                    X500Principal issuer = chain[0].getIssuerX500Principal();
                                    if (issuer.getName().equals("CN=TCF")) return;
                                }
                                throw new CertificateException();
                            }
                            public X509Certificate[] getAcceptedIssuers() {
                                return null;
                            }
                        };
                        context.init(null, new TrustManager[] { tm }, null);
                        socket = context.getSocketFactory().createSocket(host, port);
                    }
                    else {
                        socket = new Socket(host, port);
                    }
                    socket.setTcpNoDelay(true);
                    socket.setKeepAlive(true);
                    if (ssl) ((SSLSocket)socket).startHandshake();
                    inp = new BufferedInputStream(socket.getInputStream());
                    out = new BufferedOutputStream(socket.getOutputStream());
                    Protocol.invokeLater(new Runnable() {
                        public void run() {
                            ChannelTCP.this.start();
                        }
                    });
                }
                catch (final Exception x) {
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
    
    public ChannelTCP(IPeer remote_peer, String host, int port) {
        this(remote_peer, host, port, false);
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
