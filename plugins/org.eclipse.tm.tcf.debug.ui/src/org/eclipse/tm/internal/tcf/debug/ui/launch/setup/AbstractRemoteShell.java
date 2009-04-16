/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
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

    public synchronized void write(String s) {
        out.print(s);
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
                    if ((char)ch == s[i].charAt(pos[i])) {
                        pos[i]++;
                        if (pos[i] == s[i].length()) return i;
                    }
                    else if (ch == s[i].charAt(0)) {
                        pos[i] = 1;
                        if (pos[i] == s[i].length()) return i;
                    }
                    else {
                        pos[i] = 0;
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
