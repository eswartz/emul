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

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;

class TelnetClient extends AbstractRemoteShell {

    private Socket socket;
    private boolean logged;

    TelnetClient(InetAddress address, int port,
            String user, String password) throws Exception {
        socket = new Socket(address, port);
        socket.setTcpNoDelay(true);
        InputStream ist = new BufferedInputStream(socket.getInputStream());
        OutputStream ost = new BufferedOutputStream(socket.getOutputStream());
        ist = new TelnetInputStream(ist, ost, true, PROMPT);
        ist = new TimeOutInputStream(ist, 512, 30000);
        out = new PrintWriter(ost, true);
        inp = new BufferedReader(new InputStreamReader(ist, "UTF-8"));
        expect("ogin: ");
        write(user + "\n");
        expect("assword: ");
        write(password + "\n");
        int i = expect(new String[]{"incorrect", PROMPT, "$ ", "# ", "> "});
        if (i == 0) {
            close();
            throw new Exception("Login incorrect");
        }
        logged = true;
        write("export PS1=\"" + PROMPT + "\"\n");
        expect(PROMPT + "\"\n");
        waitPrompt();
    }

    public synchronized void close() throws IOException {
        if (socket == null) return;
        if (logged) {
            write("exit\n");
            logged = false;
        }
        out.close();
        try {
            char[] buf = new char[0x100];
            while (inp.read(buf) >= 0) {}
        }
        catch (SocketException x) {
            if (!x.getMessage().startsWith("Socket closed")) throw x;
        }
        inp.close();
        socket.close();
        socket = null;
        inp = null;
        out = null;
    }

    public void finalize() throws Throwable {
        close();
        super.finalize();
    }
}
