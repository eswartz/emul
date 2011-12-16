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
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

import org.eclipse.jsch.core.IJSchService;
import org.eclipse.jsch.ui.UserInfoPrompter;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.Session;

class SSHClient extends AbstractRemoteShell {

    private final Session session;
    private final Channel channel;

    private static class Pipe {

        final PipedOutputStream out = new PipedOutputStream();
        final PipedInputStream inp = new PipedInputStream();

        Pipe() throws IOException {
            inp.connect(out);
        }
    }

    SSHClient(final Shell parent, String host, String user, final String password) throws Exception {
        BundleContext ctx = Activator.getDefault().getBundle().getBundleContext();
        ServiceReference ref = ctx.getServiceReference(IJSchService.class.getName());
        IJSchService s = (IJSchService)ctx.getService(ref);
        session = s.createSession(host, 22, user);
        session.setPassword(password);
        new UserInfoPrompter(session);
        session.connect(30000);
        channel = session.openChannel("shell");
        Pipe inp = new Pipe();
        Pipe out = new Pipe();
        channel.setInputStream(inp.inp);
        channel.setOutputStream(out.out);
        this.out = new PrintWriter(inp.out, true);
        this.inp = new BufferedReader(new InputStreamReader(new TimeOutInputStream(out.inp, 512, 60000), "UTF-8"));
        channel.connect(30000);
        write("export PS1=\"" + PROMPT + "\"\n");
        expect(PROMPT + "\"\n");
        waitPrompt();
    }

    public void close() throws IOException {
        channel.disconnect();
        session.disconnect();
    }
}
