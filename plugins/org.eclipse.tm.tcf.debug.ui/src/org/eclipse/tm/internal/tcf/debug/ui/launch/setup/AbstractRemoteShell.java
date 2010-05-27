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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

abstract class AbstractRemoteShell implements IRemoteShell {

    protected boolean debug;
    protected BufferedReader inp;
    protected PrintWriter out;

    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    public synchronized void write(String s) {
        out.write(s);
    }

    public synchronized void expect(String s) throws IOException {
        expect(new String[]{ s });
    }

    public synchronized int expect(String s[]) throws IOException {
        out.flush();
        int[] pos = new int[s.length];
        for (int i = 0; i < pos.length; i++) pos[i] = 0;
        StringBuffer buf = new StringBuffer();
        try {
            for (;;) {
                int ch = inp.read();
                if (ch < 0) throw new IOException("Unexpected EOF");
                if (ch == '\r') continue;
                if (debug) System.out.write(ch);
                for (int i = 0; i < pos.length; i++) {
                    String p = s[i];
                    if (ch == p.charAt(pos[i])) {
                        pos[i]++;
                        if (pos[i] == p.length()) return i;
                    }
                    else {
                        int nps = pos[i];
                        while (nps > 0) {
                            if (ch == p.charAt(nps - 1)) {
                                int j = nps - 2;
                                int k = pos[i] - 1;
                                while (j >= 0 && p.charAt(j) == p.charAt(k)) {
                                    j--;
                                    k--;
                                }
                                if (j < 0) break;
                            }
                            nps--;
                        }
                        pos[i] = nps;
                    }
                }
                buf.append((char)ch);
                if (ch == '\n') buf.setLength(0);
            }
        }
        catch (IOException x) {
            if (buf.length() == 0) throw x;
            IOException y = new IOException("I/O error, last text received: " + buf);
            y.initCause(x);
            throw y;
        }
        finally {
            if (debug) System.out.flush();
        }
    }

    public synchronized String waitPrompt() throws IOException {
        StringBuffer res = new StringBuffer();
        StringBuffer buf = new StringBuffer();
        out.flush();
        try {
            for (;;) {
                int ch = inp.read();
                if (ch < 0) throw new IOException("Unexpected EOF");
                if (ch == '\r') continue;
                if (debug) System.out.write(ch);
                buf.append((char)ch);
                if (buf.length() == PROMPT.length() && buf.toString().equals(PROMPT)) break;
                if (ch == '\n') {
                    res.append(buf);
                    buf.setLength(0);
                }
            }
            return res.toString();
        }
        catch (IOException x) {
            if (buf.length() == 0) throw x;
            IOException y = new IOException("I/O error, last text received: " + buf);
            y.initCause(x);
            throw y;
        }
        finally {
            if (debug) System.out.flush();
        }
    }
}
