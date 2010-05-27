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
package org.eclipse.tm.internal.tcf.debug.launch;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.tm.internal.tcf.debug.Activator;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.tcf.util.TCFTask;
import org.osgi.framework.Bundle;

/**
 * This class checks that TCF Agent is running on the local host,
 * and starts a new instance of the agent if it cannot be located.
 */
public class TCFLocalAgent {

    private static final String
        AGENT_HOST = "127.0.0.1",
        AGENT_PORT = "1534";

    private static Process agent;
    private static boolean destroed;

    private static String getAgentFileName() {
        String os = System.getProperty("os.name");
        String arch = System.getProperty("os.arch");
        String fnm = "agent";
        if (arch.equals("x86")) arch = "i386";
        if (arch.equals("i686")) arch = "i386";
        if (os.startsWith("Windows")) {
            os = "Windows";
            fnm = "agent.exe";
        }
        if (os.equals("Linux")) os = "GNU/Linux";
        return "agent/" + os + "/" + arch + "/" + fnm;
    }

    static synchronized String runLocalAgent() throws CoreException {
        if (destroed) return null;
        String id = getLocalAgentID();
        if (id != null) return id;
        if (agent != null) {
            agent.destroy();
            agent = null;
        }
        Path fnm = new Path(getAgentFileName());
        try {
            Bundle bundle = Platform.getBundle(Activator.PLUGIN_ID);
            URL url = FileLocator.find(bundle, fnm, null);
            if (url != null) {
                URLConnection ucn = url.openConnection();
                ucn.setRequestProperty("Method", "HEAD");
                ucn.connect();
                long mtime = ucn.getLastModified();
                File f = Activator.getDefault().getStateLocation().append(fnm).toFile();
                if (!f.exists() || mtime != f.lastModified()) {
                    f.getParentFile().mkdirs();
                    InputStream inp = url.openStream();
                    OutputStream out = new FileOutputStream(f);
                    byte[] buf = new byte[0x1000];
                    for (;;) {
                        int len = inp.read(buf);
                        if (len < 0) break;
                        out.write(buf, 0, len);
                    }
                    out.close();
                    inp.close();
                    if (!"exe".equals(fnm.getFileExtension())) {
                        String[] cmd = {
                                "chmod",
                                "a+x",
                                f.getAbsolutePath()
                        };
                        Runtime.getRuntime().exec(cmd).waitFor();
                    }
                    f.setLastModified(mtime);
                }
                String[] cmd = {
                        f.getAbsolutePath(),
                        "-s",
                        "TCP:" + AGENT_HOST + ":" + AGENT_PORT
                };
                final Process prs = agent = Runtime.getRuntime().exec(cmd);
                final TCFTask<String> waiting = waitAgentReady();
                Thread t = new Thread() {
                    public void run() {
                        try {
                            final int n = prs.waitFor();
                            if (n != 0) {
                                Protocol.invokeLater(new Runnable() {
                                    public void run() {
                                        if (waiting.isDone()) return;
                                        waiting.error(new IOException("TCF Agent exited with code " + n));
                                    }
                                });
                            }
                            synchronized (TCFLocalAgent.class) {
                                if (agent == prs) {
                                    if (n != 0 && !destroed) {
                                        Activator.log("TCF Agent exited with code " + n, null);
                                    }
                                    agent = null;
                                }
                            }
                        }
                        catch (InterruptedException x) {
                            Activator.log("TCF Agent Monitor interrupted", x);
                        }
                    }
                };
                t.setDaemon(true);
                t.setName("TCF Agent Monitor");
                t.start();
                return waiting.getIO();
            }
        }
        catch (Throwable x) {
            agent = null;
            throw new CoreException(new Status(IStatus.ERROR,
                    Activator.PLUGIN_ID, 0,
                    "Cannot start local TCF agent.",
                    x));
        }
        throw new CoreException(new Status(IStatus.ERROR,
                Activator.PLUGIN_ID, 0,
                "Cannot start local TCF agent: file not available:\n" + fnm,
                null));
    }

    private static boolean isLocalAgent(IPeer p) {
        String host = p.getAttributes().get(IPeer.ATTR_IP_HOST);
        String port = p.getAttributes().get(IPeer.ATTR_IP_PORT);
        return AGENT_HOST.equals(host) && AGENT_PORT.equals(port);
    }

    public static synchronized String getLocalAgentID() {
        return new TCFTask<String>() {
            public void run() {
                final ILocator locator = Protocol.getLocator();
                for (IPeer p : locator.getPeers().values()) {
                    if (isLocalAgent(p)) {
                        done(p.getID());
                        return;
                    }
                }
                done(null);
            }
        }.getE();
    }

    private static TCFTask<String> waitAgentReady() {
        return new TCFTask<String>() {
            public void run() {
                final ILocator locator = Protocol.getLocator();
                for (IPeer p : locator.getPeers().values()) {
                    if (isLocalAgent(p)) {
                        done(p.getID());
                        return;
                    }
                }
                final ILocator.LocatorListener listener = new ILocator.LocatorListener() {
                    public void peerAdded(IPeer p) {
                        if (!isDone() && isLocalAgent(p)) {
                            done(p.getID());
                            locator.removeListener(this);
                        }
                    }
                    public void peerChanged(IPeer peer) {
                    }
                    public void peerHeartBeat(String id) {
                    }
                    public void peerRemoved(String id) {
                    }
                };
                locator.addListener(listener);
                Protocol.invokeLater(30000, new Runnable() {
                    public void run() {
                        if (!isDone()) {
                            error(new Exception("Timeout waiting for TCF Agent to start"));
                            locator.removeListener(listener);
                        }
                    }
                });
            }
        };
    }

    public static synchronized void destroy() {
        if (agent != null) {
            destroed = true;
            agent.destroy();
        }
    }
}
