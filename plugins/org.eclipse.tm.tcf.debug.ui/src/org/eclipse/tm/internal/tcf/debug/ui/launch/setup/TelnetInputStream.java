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
import java.net.*;
import java.util.ArrayList;

class TelnetInputStream extends FilterInputStream {

    public interface TelnetTraceListener {
        void command(String s);
    }

    private final boolean echo;
    private final String prompt;

    private final InputStream inp;
    private final OutputStream out;
    private final boolean mode_rem[] = new boolean[256];
    private final int mode_cnt[] = new int[256];
    private final Reader reader = new Reader();
    private final byte buf[] = new byte[512];
    private int buf_inp = 0;
    private int buf_out = 0;
    private boolean eof;
    private IOException err;
    private boolean closed;

    private final ArrayList<TelnetTraceListener> trace_listeners = new ArrayList<TelnetTraceListener>();

    private static final int
        cm_IAC      = 255,
        cm_WILL     = 251,
        cm_WONT     = 252,
        cm_DO       = 253,
        cm_DONT     = 254,
        cm_SB       = 250,
        cm_SE       = 240,
        cm_DataMark = 242;

    private static final int
        co_ECHO                 = 1,
        co_SUPPRESS_GO_AHEAD    = 3,
        co_STATUS               = 5,
        co_TERMINAL_TYPE        = 24,
        co_NAWS                 = 31, // Negotiate About Window Size
        co_TERMINAL_SPEED       = 32,
        co_TOGGLE_FLOW_CONTROL  = 33,
        co_X_DISPLAY_LOCATION   = 35,
        co_ENVIRON              = 36,
        co_NEW_ENVIRON          = 39;

    @SuppressWarnings("unused")
    private static final int
        sp_VAR                  = 0,
        sp_VALUE                = 1,
        sp_ESC                  = 2,
        sp_USERVAR              = 3;

    @SuppressWarnings("unused")
    private static final int
        ac_IS                   = 0,
        ac_SEND                 = 1,
        ac_INFO                 = 2;

    private class Reader extends Thread {

        private void logCommand(int cmd, int opt) {
            String s = "" + cmd;
            switch (cmd) {
            case cm_WILL:
                s = "WILL";
                break;
            case cm_WONT:
                s = "WONT";
                break;
            case cm_DO:
                s = "DO";
                break;
            case cm_DONT:
                s = "DONT";
                break;
            case cm_SB:
                s = "SB";
                break;
            }
            s += " ";
            switch (opt) {
            case co_ECHO:
                s += "ECHO";
                break;
            case co_SUPPRESS_GO_AHEAD:
                s += "SUPPRESS_GO_AHEAD";
                break;
            case co_STATUS:
                s += "STATUS";
                break;
            case co_TERMINAL_TYPE:
                s += "TERMINAL_TYPE";
                break;
            case co_NAWS:
                s += "NAWS";
                break;
            case co_TERMINAL_SPEED:
                s += "TERMINAL_SPEED";
                break;
            case co_TOGGLE_FLOW_CONTROL:
                s += "TOGGLE_FLOW_CONTROL";
                break;
            case co_X_DISPLAY_LOCATION:
                s += "X_DISPLAY_LOCATION";
                break;
            case co_ENVIRON:
                s += "ENVIRON";
                break;
            case co_NEW_ENVIRON:
                s += "NEW_ENVIRON";
                break;
            default:
                s += opt;
                break;
            }
            for (TelnetTraceListener l : trace_listeners) l.command(s);
        }

        private int read_ch() throws IOException {
            try {
                return inp.read();
            }
            catch (SocketException x) {
                String s = x.getMessage();
                if (s.startsWith("Socket closed")) return -1;
                if (s.startsWith("socket closed")) return -1;
                throw x;
            }
        }

        public void run() {
            try {
                synchronized (out) {
                    out.write(cm_IAC);
                    out.write(echo ? cm_DO : cm_DONT);
                    out.write(co_ECHO);
                    out.flush();
                }
                for (;;) {
                    int rd = read_ch();
                    if (rd < 0) break;
                    if (rd == cm_IAC) {
                        int cm = read_ch();
                        if (cm < 0) break;
                        if (cm != cm_IAC) {
                            int co = read_ch();
                            if (co < 0) break;
                            if (co == cm_DataMark) continue;
                            logCommand(cm, co);
                            synchronized (out) {
                                if (mode_cnt[co] >= 5) continue;
                                switch (cm) {
                                case cm_WILL:
                                    mode_rem[co] = true;
                                    break;
                                case cm_WONT:
                                    mode_rem[co] = false;
                                    break;
                                case cm_DO:
                                    out.write(cm_IAC);
                                    if (co == co_SUPPRESS_GO_AHEAD) {
                                        out.write(cm_WILL);
                                    }
                                    else if (co == co_NEW_ENVIRON && prompt != null) {
                                        out.write(cm_WILL);
                                    }
                                    else {
                                        out.write(cm_WONT);
                                    }
                                    out.write(co);
                                    break;
                                case cm_DONT:
                                    out.write(cm_IAC);
                                    out.write(cm_WONT);
                                    out.write(co);
                                    break;
                                case cm_SB:
                                    int ac = read_ch();
                                    if (ac < 0) break;
                                    if (ac == ac_SEND) {
                                        if (co == co_NEW_ENVIRON) {
                                            out.write(cm_IAC);
                                            out.write(cm_SB);
                                            out.write(co_NEW_ENVIRON);
                                            out.write(ac_IS);
                                            if (prompt != null) {
                                                out.write(sp_VAR);
                                                out.write('P');
                                                out.write('S');
                                                out.write('1');
                                                out.write(sp_VALUE);
                                                for (int k = 0; k < prompt.length(); k++) out.write(prompt.charAt(k));
                                            }
                                            out.write(cm_IAC);
                                            out.write(cm_SE);
                                        }
                                    }
                                    int c0 = 0;
                                    for (;;) {
                                        int c1 = read_ch();
                                        if (c0 == cm_IAC && c1 == cm_SE) break;
                                        if (c0 == cm_IAC && c1 == cm_IAC) c1 = 0;
                                        c0 = c1;
                                    }
                                    break;
                                default:
                                    throw new IOException("Invalid command: " + cm);
                                }
                                out.flush();
                                mode_cnt[co]++;
                            }
                            continue;
                        }
                    }
                    synchronized (TelnetInputStream.this) {
                        int new_inp = (buf_inp + 1) % buf.length;
                        while (new_inp == buf_out) TelnetInputStream.this.wait();
                        buf[buf_inp] = (byte)rd;
                        buf_inp = new_inp;
                        TelnetInputStream.this.notify();
                    }
                }
            }
            catch (InterruptedException x) {
                err = new InterruptedIOException();
            }
            catch (IOException x) {
                if (!closed) err = x;
            }
            finally {
                synchronized (TelnetInputStream.this) {
                    eof = true;
                    TelnetInputStream.this.notify();
                }
            }
        }
    }

    TelnetInputStream(InputStream inp, OutputStream out, boolean echo, String prompt) {
        super(inp);
        if (!(inp instanceof BufferedInputStream)) inp = new BufferedInputStream(inp);
        this.inp = inp;
        this.out = out;
        this.echo = echo;
        this.prompt = prompt;
        reader.start();
    }

    public synchronized void addTraceListener(TelnetTraceListener l) {
        trace_listeners.add(l);
    }

    public synchronized void removeTraceListener(TelnetTraceListener l) {
        trace_listeners.remove(l);
    }

    public synchronized int read() throws IOException {
        try {
            while (buf_out == buf_inp) {
                if (err != null) throw new IOException(err.getMessage());
                if (eof) return -1;
                wait();
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
            int cnt = 0;
            while (cnt < len) {
                while (buf_out == buf_inp) {
                    if (cnt > 0) return cnt;
                    if (err != null) throw new IOException(err.getMessage());
                    if (eof) return -1;
                    if (nfy) {
                        notify();
                        nfy = false;
                    }
                    wait();
                }
                b[off++] = buf[buf_out];
                buf_out = (buf_out + 1) % buf.length;
                nfy = true;
                cnt++;
            }
            return cnt;
        }
        catch (InterruptedException x) {
            throw new InterruptedIOException();
        }
        finally {
            if (nfy) notify();
        }
    }

    public synchronized int available() throws IOException {
        return (buf_inp + buf.length - buf_out) % buf.length;
    }

    public synchronized void close() throws IOException {
        closed = true;
        super.close();
    }
}
