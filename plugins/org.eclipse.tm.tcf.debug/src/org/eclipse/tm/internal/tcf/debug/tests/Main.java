/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.tests;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.protocol.IEventQueue;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * This class is user to run TCF test suite from command line.
 */
public class Main {

    private static class EventQueue extends Thread implements IEventQueue {

        private final LinkedList<Runnable> queue = new LinkedList<Runnable>();

        EventQueue() {
            setName("TCF Event Dispatch");
            start();
        }

        public void run() {
            try {
                while (true) {
                    Runnable r = null;
                    synchronized (this) {
                        while (queue.isEmpty()) wait();
                        r = queue.removeFirst();
                    }
                    try {
                        r.run();
                    }
                    catch (Throwable x) {
                        System.err.println("Error dispatching TCF event:");
                        x.printStackTrace();
                    }
                }
            }
            catch (Throwable x) {
                x.printStackTrace();
                System.exit(1);
            }
        }

        public synchronized int getCongestion() {
            int n = queue.size() - 100;
            if (n > 100) n = 100;
            return n;
        }

        public synchronized void invokeLater(Runnable runnable) {
            queue.add(runnable);
            notify();
        }

        public boolean isDispatchThread() {
            return Thread.currentThread() == this;
        }
    }

    private static class RemotePeer extends AbstractPeer {

        public RemotePeer(Map<String,String> attrs) {
            super(attrs);
        }
    }

    private static IPeer getPeer(String s) {
        Map<String,String> map = new HashMap<String,String>();
        int len = s.length();
        int i = 0;
        while (i < len) {
            int i0 = i;
            while (i < len && s.charAt(i) != '=' && s.charAt(i) != 0) i++;
            int i1 = i;
            if (i < len && s.charAt(i) == '=') i++;
            int i2 = i;
            while (i < len && s.charAt(i) != ':') i++;
            int i3 = i;
            if (i < len && s.charAt(i) == ':') i++;
            String key = s.substring(i0, i1);
            String val = s.substring(i2, i3);
            map.put(key, val);
        }
        String id = map.get(IPeer.ATTR_ID);
        if (id == null) throw new Error("Invalid peer info: no ID");
        IPeer peer = Protocol.getLocator().getPeers().get(id);
        if (peer instanceof RemotePeer) {
            ((RemotePeer)peer).updateAttributes(map);
        }
        else {
            peer = new RemotePeer(map);
        }
        return peer;
    }

    private static void runTestSuite(IPeer peer) {
        TCFTestSuite.TestListener listener = new TCFTestSuite.TestListener() {

            public void done(Collection<Throwable> errors) {
                if (errors == null || errors.isEmpty()) {
                    System.out.println("No errors detected.");
                    System.exit(0);
                }
                for (Throwable x : errors) {
                    x.printStackTrace(System.out);
                }
                System.exit(3);
            }

            public void progress(String label, int done, int total) {
                if (label != null) System.out.println(label);
            }

        };
        try {
            new TCFTestSuite(peer, listener, null);
        }
        catch (Throwable x) {
            System.err.println("Cannot start test suite:");
            x.printStackTrace();
            System.exit(2);
        }
    }

    /**
     * Command line should contain peer description string, for example:
     * "ID=Test:TransportName=TCP:Host=127.0.0.1:Port=1534"
     */
    public static void main(final String[] args) {
        if (args.length != 1) {
            System.err.println("Missing command line argument - peer identification string");
            System.exit(4);
        }
        Protocol.setEventQueue(new EventQueue());
        Protocol.invokeLater(new Runnable() {
            public void run() {
                runTestSuite(getPeer(args[0]));
            }
        });
    }
}
