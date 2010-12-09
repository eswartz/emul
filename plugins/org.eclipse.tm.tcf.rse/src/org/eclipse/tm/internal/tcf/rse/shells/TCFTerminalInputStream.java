/*******************************************************************************
 * Copyright (c) 2010 Intel Corporation. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Liping Ke (Intel Corp.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.shells;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IStreams;
import org.eclipse.tm.tcf.util.TCFTask;

public class TCFTerminalInputStream extends InputStream {
    private IStreams streams;
    private boolean connected = true;;/* The stream is connected or not */
    String is_id;
    private int value;
    private boolean bEof = false;;

    public TCFTerminalInputStream(final IStreams streams, final String is_id) throws IOException{
        if (streams == null)
            throw new IOException("TCP streams is null");//$NON-NLS-1$
        this.streams = streams;
        this.is_id = is_id;
    }

    /* read must be synchronized */
    @Override
    public synchronized int read() throws IOException {
        if (!connected)
            throw new IOException("istream is not connected");//$NON-NLS-1$
        if (bEof)
            return -1;
        try {
            new TCFTask<Object>() {
                public void run() {
                    streams.read(is_id, 1, new IStreams.DoneRead() {
                        public void doneRead(IToken token, Exception error, int lostSize,
                                byte[] data, boolean eos) {
                            if (error != null) {
                                error(error);
                                return;
                            }
                            bEof = eos;
                            if (data != null) {
                                value = (int)data[0];
                            }
                            else
                                value = -1;
                            done(this);
                        }
                    });
                }
            }.getIO();
        }
        catch (Exception e) {
            e.printStackTrace();//$NON-NLS-1$
            throw new IOException(e.getMessage());//$NON-NLS-1$
        }
        return value;
    }

    private static class Buffer {
        byte[] buf;
        Buffer() {
        }
    }
    private Buffer buffer;

    public synchronized int read(byte b[], final int off, final int len) throws IOException {


        if (!connected)
            throw new IOException("istream is not connected");//$NON-NLS-1$
        if (bEof) return -1;
        if (b == null) {
            throw new NullPointerException();
        } else if (off < 0 || len < 0 || len > b.length - off) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return 0;
        }
        try {
            new TCFTask<Buffer>() {
                public void run() {
                    streams.read(is_id, len, new IStreams.DoneRead() {
                        public void doneRead(IToken token, Exception error, int lostSize,
                                byte[] data, boolean eos) {
                            if (error != null) {
                                error(error);
                                return;
                            }
                            bEof = eos;
                            if (data != null) {
                                buffer = new Buffer();
                                buffer.buf = data;

                            }
                            done(buffer);
                        }
                    });
                }
            }.getIO();

            if (buffer.buf != null) {
                int length = buffer.buf.length;
                System.arraycopy(buffer.buf, 0, b, off, length);
                return length;
            }
            else if (bEof)
                return -1;
            else return 0;
        } catch (Exception ee) {
            throw new IOException(ee.getMessage());//$NON-NLS-1$
        }
    }

    public void close() throws IOException {
        if (!connected) return;
        new TCFTask<Object>() {
            public void run() {
                streams.disconnect(is_id, new IStreams.DoneDisconnect() {
                    public void doneDisconnect(IToken token, Exception error) {
                        connected = false;
                        done(this);
                    }
                });
            }
        }.getIO();
        connected = false;
    }

}
