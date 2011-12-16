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

import java.io.OutputStream;

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IStreams;
import org.eclipse.tm.tcf.util.TCFTask;

public class TCFTerminalOutputStream extends OutputStream {

    private final IStreams streams;
    private boolean connected = true;
    private boolean write_eof;
    String os_id;

    public TCFTerminalOutputStream(final IStreams streams, final String os_id) throws IOException{
        if (streams == null) throw new IOException("istream is null");//$NON-NLS-1$
        this.streams = streams;
        this.os_id = os_id;
        write_eof = false;
    }

    @Override
    public synchronized void write(final byte b[], final int off, final int len) throws IOException {
        /* If eof is written, we can't write anything into the stream */
        if (!connected || write_eof)
            throw new IOException("stream is not connected or write_eof already!");//$NON-NLS-1$
        try {
            new TCFTask<Object>() {
                public void run() {
                    streams.write(os_id, b, off, len, new IStreams.DoneWrite() {
                        public void doneWrite(IToken token, Exception error) {
                            if (error != null) error(error);
                            done(this);
                        }
                    });

                }
            }.getIO();
        }
        catch (Exception e)
        {
            throw new IOException(e.getMessage());//$NON-NLS-1$
        }
    }

    @Override
    public synchronized void write(int b) throws IOException {

        try {
            final byte[] buf = new byte[1];
            buf[0] = (byte)b;
            this.write(buf, 0, 1);
        }
        catch(IOException ioe) {
            throw new IOException(ioe.getMessage());//$NON-NLS-1$
        }

    }

    /* close must be called --Need to reconsider it in the future*/
    public void close() throws IOException {
        if (!connected)
            return;
        try {
            new TCFTask<Object>() {
                public void run() {
                    streams.eos(os_id, new IStreams.DoneEOS() {
                        public void doneEOS(IToken token, Exception error) {
                            write_eof = true;
                            done(this);
                        }
                    });
                }
            }.getIO();
            new TCFTask<Object>() {
                public void run() {
                    streams.disconnect(os_id, new IStreams.DoneDisconnect() {
                        public void doneDisconnect(IToken token, Exception error) {
                            connected = false;
                            done(this);
                        }
                    });

                }
            }.getIO();
        }
        catch(Exception e) {
            throw new IOException(e.getMessage());   //$NON-NLS-1$
        }
    }

}
