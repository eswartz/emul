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
import java.io.InputStream;
import java.util.Iterator;
import java.util.LinkedList;

import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.tcf.services.IFileSystem.IFileHandle;

/**
 * TCFFileInputStream is high performance InputStream implementation over TCF FileSystem service.
 * The class uses read-ahead buffers to achieve maximum throughput.
 * 
 * @noextend This class is not intended to be subclassed by clients.
 */
public final class TCFFileInputStream extends InputStream {
    
    private static final int MAX_READ_AHEAD = 8;
    
    private static class Buffer {
        
        final long offset;

        IToken token;
        byte[] buf;
        boolean eof;

        Buffer(long offset) {
            this.offset = offset;
        }
        
        @Override
        public String toString() {
            return "[" + offset + ":" + (buf == null ? "null" : Integer.toString(buf.length)) + "]";
        }
    }
    
    private final IFileHandle handle;
    private final IFileSystem fs;
    private final int buf_size;
    private long mark = 0;
    private long offset = 0;
    private Buffer buf;
    private boolean closed = false;
    
    private boolean suspend_read_ahead;
    private Runnable waiting_client;
    private final LinkedList<Buffer> read_ahead_buffers = new LinkedList<Buffer>();
    
    public TCFFileInputStream(IFileHandle handle) {
        this(handle, 0x1000);
    }

    public TCFFileInputStream(IFileHandle handle, int buf_size) {
        this.handle = handle;
        this.fs = handle.getService();
        this.buf_size = buf_size;
    }
    
    private void startReadAhead(Buffer prv) {
        if (suspend_read_ahead) return;
        if (read_ahead_buffers.size() > 0) {
            prv = read_ahead_buffers.getLast();
        }
        if (prv.eof) return;
        long pos = prv.offset + (prv.buf == null ? buf_size : prv.buf.length);
        while (read_ahead_buffers.size() < MAX_READ_AHEAD) {
            final Buffer buf = new Buffer(pos);
            buf.token = fs.read(handle, pos, buf_size, new IFileSystem.DoneRead() {
                public void doneRead(IToken token, FileSystemException error,
                        byte[] data, boolean eof) {
                    assert buf.token == token;
                    assert read_ahead_buffers.contains(buf);
                    buf.token = null;
                    if (error != null) {
                        suspend_read_ahead = true;
                        read_ahead_buffers.remove(buf);
                    }
                    else if (data.length != buf_size) {
                        buf.buf = data;
                        buf.eof = eof;
                        if (!eof) suspend_read_ahead = true;
                    }
                    else {
                        buf.buf = data;
                        buf.eof = eof;
                        startReadAhead(buf);
                    }
                    if (waiting_client != null) {
                        Protocol.invokeLater(waiting_client);
                        waiting_client = null;
                    }
                }
            });
            read_ahead_buffers.add(buf);
            pos += buf_size;
        }
    }
    
    private boolean stopReadAhead(Runnable done) {
        suspend_read_ahead = true;
        for (Iterator<Buffer> i = read_ahead_buffers.iterator(); i.hasNext();) {
            Buffer buf = i.next();
            if (buf.token == null || buf.token.cancel()) i.remove();
        }
        if (read_ahead_buffers.size() > 0) {
            assert waiting_client == null;
            waiting_client = done;
            return false;
        }
        return true;
    }

    @Override
    public synchronized int read() throws IOException {
        if (closed) throw new IOException("Stream is closed");
        while (buf == null || buf.offset > offset || buf.offset + buf.buf.length <= offset) {
            if (buf != null && buf.eof) return -1;
            buf = new TCFTask<Buffer>() {
                public void run() {
                    assert waiting_client == null;
                    while (read_ahead_buffers.size() > 0) {
                        Buffer buf = read_ahead_buffers.getFirst();
                        if (buf.offset == offset) {
                            if (buf.token != null) {
                                waiting_client = this;
                            }
                            else {
                                startReadAhead(buf);
                                read_ahead_buffers.remove(buf);
                                done(buf);
                            }
                            return;
                        }
                        suspend_read_ahead = true;
                        if (buf.token != null && buf.token.cancel()) buf.token = null;
                        if (buf.token != null) {
                            waiting_client = this;
                            return;
                        }
                        read_ahead_buffers.remove(buf);
                    }
                    fs.read(handle, offset, buf_size, new IFileSystem.DoneRead() {
                        public void doneRead(IToken token, FileSystemException error,
                                byte[] data, boolean eof) {
                            if (error != null) {
                                error(error);
                                return;
                            }
                            assert data != null && data.length <= buf_size;
                            Buffer buf = new Buffer(offset);
                            buf.buf = data;
                            buf.eof = eof;
                            if (!eof) {
                                suspend_read_ahead = false;
                                startReadAhead(buf);
                            }
                            done(buf);
                        }
                    });
                }
            }.getIO();
            assert buf.token == null;
        }
        int ofs = (int)(offset++ - buf.offset);
        return buf.buf[ofs] & 0xff;
    }

    @Override
    public synchronized int read(final byte arr[], final int off, final int len) throws IOException {
        if (closed) throw new IOException("Stream is closed");
        if (arr == null) throw new NullPointerException();
        if (off < 0 || len < 0 || len > arr.length - off) throw new IndexOutOfBoundsException();
        int pos = 0;
        while (pos < len) {
            if (buf != null && buf.offset <= offset && buf.offset + buf.buf.length > offset) {
                int buf_pos = (int)(offset - buf.offset);
                int buf_len = buf.buf.length - buf_pos;
                int n = len - pos < buf_len ? len - pos : buf_len;
                System.arraycopy(buf.buf, buf_pos, arr, off + pos, n);
                pos += n;
                offset += n;
            }
            else {
                int c = read();
                if (c == -1) {
                    if (pos == 0) return -1;
                    break;
                }
                arr[off + pos++] = (byte)c;
            }
        }
        return pos;
    }
    
    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void reset() throws IOException {
        if (closed) throw new IOException("Stream is closed");
        offset = mark;
        if (buf != null && buf.offset <= offset && buf.offset + buf.buf.length > offset) return;
        new TCFTask<Object>() {
            public void run() {
                if (!stopReadAhead(this)) return;
                done(this);
            }
        }.getIO();
        buf = null;
    }

    @Override
    public synchronized void mark(int readlimit) {
        mark = offset;
    }

    @Override
    public synchronized void close() throws IOException {
        if (closed) return;
        new TCFTask<Object>() {
            public void run() {
                if (!stopReadAhead(this)) return;
                assert read_ahead_buffers.isEmpty();
                fs.close(handle, new IFileSystem.DoneClose() {
                    public void doneClose(IToken token, FileSystemException error) {
                        if (error != null) error(error);
                        else done(this);
                    }
                });
            }
        }.getIO();
        closed = true;
        buf = null;
    }
}