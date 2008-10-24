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
package org.eclipse.tm.tcf.util;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.tcf.services.IFileSystem.IFileHandle;

/**
 * TCFFileOutputStream is high performance OutputStream implementation over TCF FileSystem service.
 * The class uses write-back buffers to achieve maximum throughput.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public final class TCFFileOutputStream extends OutputStream {
    
    private static final int MAX_WRITE_BACK = 8;

    private final IFileHandle handle;
    private final IFileSystem fs;
    private final int buf_size;
    private final Set<IToken> write_commands = new HashSet<IToken>();
    private final int[] dirty = new int[1];
    private final byte[] buf;
    private int buf_pos = 0;
    private long offset = 0;
    private IOException flush_error;
    private boolean closed;

    public TCFFileOutputStream(IFileHandle handle) {
        this(handle, 0x1000);
    }
    
    public TCFFileOutputStream(IFileHandle handle, int buf_size) {
        this.handle = handle;
        this.fs = handle.getService();
        this.buf_size = buf_size;
        buf = new byte[buf_size];
    }

    @Override
    public synchronized void write(int b) throws IOException {
        if (closed) throw new IOException("Stream is closed");
        if (buf_pos == buf_size) flush();
        buf[buf_pos++] = (byte)b;
    }

    @Override
    public void write(byte b[], int off, int len) throws IOException {
        if (len == 0) return;
        if (b == null) throw new NullPointerException();
        if (off < 0 || off > b.length || len < 0 ||
                   off + len > b.length || off + len < 0)
            throw new IndexOutOfBoundsException();
        while (len > 0) {
            if (buf_pos == buf_size) flush();
            if (buf_pos == 0 && len > buf_size) {
                flush(b, off, len);
                return;
            }
            int n = buf_size - buf_pos;
            if (len < n) n = len;
            System.arraycopy(b, off, buf, buf_pos, n);
            off += n;
            len -= n;
            buf_pos += n;
        }
    }

    @Override
    public synchronized void flush() throws IOException {
        if (buf_pos == 0) return;
        flush(buf, 0, buf_pos);
        buf_pos = 0;
    }
    
    private void flush(final byte[] buf, final int off, final int len) throws IOException {
        synchronized (dirty) {
            if (flush_error != null) throw flush_error;
            while (dirty[0] >= MAX_WRITE_BACK) {
                try {
                    dirty.wait();
                }
                catch (InterruptedException e) {
                    throw new InterruptedIOException();
                }
            }
        }
        new TCFTask<Object>() {
            public void run() {
                write_commands.add(fs.write(handle, offset, buf, off, len, new IFileSystem.DoneWrite() {
                    public void doneWrite(IToken token, FileSystemException error) {
                        assert write_commands.contains(token);
                        write_commands.remove(token);
                        if (error != null) {
                            for (Iterator<IToken> i = write_commands.iterator(); i.hasNext();) {
                                if (i.next().cancel()) i.remove();
                            }
                        }
                        synchronized (dirty) {
                            if (error != null && flush_error == null) flush_error = error;
                            dirty[0] = write_commands.size();
                            dirty.notifyAll();
                        }
                    }
                }));
                synchronized (dirty) {
                    dirty[0] = write_commands.size();
                }
                done(this);
            }
        }.getIO();
        offset += len; 
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed) return;
        flush();
        synchronized (dirty) {
            while (dirty[0] > 0) {
                try {
                    dirty.wait();
                }
                catch (InterruptedException e) {
                    throw new InterruptedIOException();
                }
            }
        }
        new TCFTask<Object>() {
            public void run() {
                fs.close(handle, new IFileSystem.DoneClose() {
                    public void doneClose(IToken token, FileSystemException error) {
                        if (error != null) error(error);
                        else done(this);
                    }
                });
            }
        }.getIO();
        closed = true;
    }
}