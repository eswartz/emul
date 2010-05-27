/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.launch.setup;

import java.io.*;

class TimeOutInputStream extends FilterInputStream {

    private byte buf[];
    private int buf_inp = 0;
    private int buf_out = 0;
    private boolean eof = false;
    private IOException err;
    private InputStream inp;
    private long time = 0;
    private Thread thread;

    private class Prefetch extends Thread {

        public void run() {
            try {
                for (;;) {
                    int rd = inp.read();
                    if (rd < 0) break;
                    synchronized (TimeOutInputStream.this) {
                        int new_inp = (buf_inp + 1) % buf.length;
                        while (new_inp == buf_out) TimeOutInputStream.this.wait();
                        buf[buf_inp] = (byte)rd;
                        buf_inp = new_inp;
                        TimeOutInputStream.this.notify();
                    }
                }
            }
            catch (InterruptedException x) {
                err = new InterruptedIOException();
            }
            catch (InterruptedIOException x) {
                err = x;
            }
            catch (IOException x) {
                err = x;
            }
            finally {
                synchronized (TimeOutInputStream.this) {
                    eof = true;
                    TimeOutInputStream.this.notify();
                }
            }
        }
    }

    TimeOutInputStream(InputStream in, long time) {
        this(in, 0x1000, time);
    }

    TimeOutInputStream(InputStream in, int size, long time) {
        super(in);
        inp = in;
        buf = new byte[size];
        this.time = time;
        thread = new Prefetch();
        thread.start();
    }

    public void setTimeOut(long time) {
        this.time = time;
    }

    public synchronized int read() throws IOException {
        try {
            int cnt = 0;
            while (buf_inp == buf_out) {
                if (err != null) throw new IOException(err.getMessage());
                if (eof) return -1;
                if (time != 0) {
                    if (cnt > 0) throw new IOException("Time Out");
                    wait(time);
                    cnt++;
                }
                else {
                    wait();
                }
            }
            int res = buf[buf_out] & 0xff;
            buf_out = (buf_out + 1) % buf.length;
            notify();
            return res;
        }
        catch (InterruptedException x) {
            throw new InterruptedIOException();
        }
    }

    public synchronized int read(byte b[], int off, int len) throws IOException {
        boolean nfy = false;
        try {
            int res = 0;
            while (res < len) {
                int cnt = 0;
                while (buf_inp == buf_out) {
                    if (res > 0) return res;
                    if (err != null) throw new IOException(err.getMessage());
                    if (eof) return -1;
                    if (nfy) {
                        notify();
                        nfy = false;
                    }
                    if (time != 0) {
                        if (cnt > 0) throw new IOException("Time Out");
                        wait(time);
                        cnt++;
                    }
                    else {
                        wait();
                    }
                }
                b[off++] = buf[buf_out];
                buf_out = (buf_out + 1) % buf.length;
                nfy = true;
                res++;
            }
            return res;
        }
        catch (InterruptedException x) {
            throw new InterruptedIOException();
        }
        finally {
            if (nfy) notify();
        }
    }

    public synchronized int available() throws IOException {
        return (buf_inp - buf_out + buf.length) % buf.length;
    }

    public synchronized void close() throws IOException {
        super.close();
        if (thread != null) {
            try {
                thread.interrupt();
                while (!eof) wait();
                thread = null;
            }
            catch (InterruptedException x) {
                throw new InterruptedIOException();
            }
        }
    }
}
