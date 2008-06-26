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

    public ChannelTCP(IPeer peer, final String host, final int port) {
        super(peer);
        Thread thread = new Thread() {
            public void run() {
                try {
                    socket = new Socket(host, port);
                    socket.setTcpNoDelay(true);
                    inp = new BufferedInputStream(socket.getInputStream());
                    out = new BufferedOutputStream(socket.getOutputStream());
                    /* Uncomment for testing of buffers.
                    inp = new BufferedInputStream(new FilterInputStream(socket.getInputStream()) {
                        public int read() throws IOException {
                            System.out.println("Inp 1");
                            return in.read();
                        }
                        public int read(byte b[]) throws IOException {
                            int n = in.read(b);
                            System.out.println("Inp " + n);
                            return n;
                        }
                        public int read(byte b[], int off, int len) throws IOException {
                            int n = in.read(b, off, len);
                            System.out.println("Inp " + n);
                            return n;
                        }
                    });
                    out = new BufferedOutputStream(new FilterOutputStream(socket.getOutputStream()){
                        public void write(int b) throws IOException {
                            System.out.println("Out 1");
                            out.write(b);
                        }
                        public void write(byte b[]) throws IOException {
                            System.out.println("Out " + b.length);
                            out.write(b);
                        }
                        public void write(byte b[], int off, int len) throws IOException {
                            System.out.println("Out " + len);
                            out.write(b, off, len);
                        }
                    });
                    */
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
