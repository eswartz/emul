/*******************************************************************************
 * Copyright (c) 2007, 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleConstants;
import org.eclipse.ui.console.IConsoleManager;
import org.eclipse.ui.console.IConsoleView;
import org.eclipse.ui.console.IOConsole;
import org.eclipse.ui.console.IOConsoleInputStream;
import org.eclipse.ui.console.IOConsoleOutputStream;

class TCFConsole {
    private final TCFModel model;
    private final IOConsole console;
    private final Display display;

    private final LinkedList<Message> out_queue;

    private static class Message {
        int stream_id;
        byte[] data;
    }

    private final Thread inp_thread = new Thread() {
        public void run() {
            try {
                IOConsoleInputStream inp = console.getInputStream();
                final byte[] buf = new byte[0x100];
                for (;;) {
                    int len = inp.read(buf);
                    if (len < 0) break;
                    // TODO: Eclipse Console view has a bad habit of replacing CR with CR/LF
                    if (len == 2 && buf[0] == '\r' && buf[1] == '\n') len = 1;
                    final int n = len;
                    Protocol.invokeAndWait(new Runnable() {
                        public void run() {
                            try {
                                model.getLaunch().writeProcessInputStream(buf, 0, n);
                            }
                            catch (Exception x) {
                                model.onProcessStreamError(null, 0, x, 0);
                            }
                        }
                    });
                }
            }
            catch (Throwable x) {
                Activator.log("Cannot read console input", x);
            }
        }
    };

    private final Thread out_thread = new Thread() {
        public void run() {
            Map<Integer,IOConsoleOutputStream> out_streams =
                new HashMap<Integer,IOConsoleOutputStream>();
            try {
                for (;;) {
                    Message m = null;
                    synchronized (out_queue) {
                        while (out_queue.size() == 0) out_queue.wait();
                        m = out_queue.removeFirst();
                    }
                    if (m.data == null) break;
                    IOConsoleOutputStream stream = out_streams.get(m.stream_id);
                    if (stream == null) {
                        final int id = m.stream_id;
                        final IOConsoleOutputStream s = stream = console.newOutputStream();
                        display.syncExec(new Runnable() {
                            public void run() {
                                try {
                                    int color_id = SWT.COLOR_BLACK;
                                    switch (id) {
                                    case 1: color_id = SWT.COLOR_RED; break;
                                    case 2: color_id = SWT.COLOR_BLUE; break;
                                    case 3: color_id = SWT.COLOR_GREEN; break;
                                    }
                                    s.setColor(display.getSystemColor(color_id));
                                }
                                catch (Throwable x) {
                                    Activator.log("Cannot open console view", x);
                                }
                            }
                        });
                        out_streams.put(m.stream_id, stream);
                    }
                    stream.write(m.data, 0, m.data.length);
                }
            }
            catch (Throwable x) {
                Activator.log("Cannot write console output", x);
            }
            for (IOConsoleOutputStream stream : out_streams.values()) {
                try {
                    stream.close();
                }
                catch (IOException x) {
                    Activator.log("Cannot close console stream", x);
                }
            }
            try {
                console.getInputStream().close();
            }
            catch (IOException x) {
                Activator.log("Cannot close console stream", x);
            }
            display.syncExec(new Runnable() {
                public void run() {
                    IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
                    manager.removeConsoles(new IOConsole[]{ console });
                }
            });
        }
    };

    TCFConsole(final TCFModel model, String process_id) {
        this.model = model;
        display = model.getDisplay();
        out_queue = new LinkedList<Message>();
        console = new IOConsole("TCF " + process_id, null,
                ImageCache.getImageDescriptor(ImageCache.IMG_TCF), "UTF-8", true);
        display.asyncExec(new Runnable() {
            public void run() {
                try {
                    IConsoleManager manager = ConsolePlugin.getDefault().getConsoleManager();
                    manager.addConsoles(new IConsole[]{ console });
                    IWorkbenchWindow w = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
                    if (w == null) return;
                    IWorkbenchPage page = w.getActivePage();
                    if (page == null) return;
                    IConsoleView view = (IConsoleView)page.showView(IConsoleConstants.ID_CONSOLE_VIEW);
                    view.display(console);
                }
                catch (Throwable x) {
                    Activator.log("Cannot open console view", x);
                }
            }
        });
        inp_thread.setName("TCF Launch Console Input");
        out_thread.setName("TCF Launch Console Output");
        inp_thread.start();
        out_thread.start();
    }

    void write(final int stream_id, byte[] data) {
        if (data == null || data.length == 0) return;
        synchronized (out_queue) {
            Message m = new Message();
            m.stream_id = stream_id;
            m.data = data;
            out_queue.add(m);
            out_queue.notify();
        }
    }

    void close() {
        synchronized (out_queue) {
            out_queue.add(new Message());
            out_queue.notify();
        }
    }
}