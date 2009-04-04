/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Martin Oberhuber (Wind River) - [269682] Get port from RSE Property
 *     Uwe Stieber (Wind River) - [271227] Fix compiler warnings in org.eclipse.tm.tcf.rse
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.BasicConnectorService;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.core.ChannelTCP;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.tcf.services.ISysMonitor;


public class TCFConnectorService extends BasicConnectorService {

    private IChannel channel;
    private Throwable channel_error;
    private final List<Runnable> state_change = new ArrayList<Runnable>();

    public TCFConnectorService(IHost host, int port) {
        super("TCF", "Target Communication Framework", host, port); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected void internalConnect(IProgressMonitor monitor) throws Exception {
        assert !Protocol.isDispatchThread();
        final Exception[] res = new Exception[1];
        monitor.beginTask("Connecting " + getHostName(), 1); //$NON-NLS-1$
        synchronized (res) {
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    connectTCFChannel(res);
                }
            });
            res.wait();
        }
        monitor.done();
        if (res[0] != null) throw res[0];
    }

    @Override
    protected void internalDisconnect(IProgressMonitor monitor) throws Exception {
        assert !Protocol.isDispatchThread();
        final Exception[] res = new Exception[1];
        monitor.beginTask("Disconnecting " + getHostName(), 1); //$NON-NLS-1$
        synchronized (res) {
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    disconnectTCFChannel(res);
                }
            });
            res.wait();
        }
        monitor.done();
        if (res[0] != null) throw res[0];
    }

    public boolean isConnected() {
        final boolean res[] = new boolean[1];
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
               res[0] = channel != null && channel.getState() == IChannel.STATE_OPEN;
            }
        });
        return res[0];
    }

    private void connectTCFChannel(final Exception[] res) {
        if (channel != null) {
            switch (channel.getState()) {
            case IChannel.STATE_OPEN:
            case IChannel.STATE_CLOSED:
                synchronized (res) {
                    if (channel_error instanceof Exception) res[0] = (Exception)channel_error;
                    else if (channel_error != null) res[0] = new Exception(channel_error);
                    else res[0] = null;
                    res.notify();
                }
                return;
            }
        }
        if (channel == null) {
            final String host = getHostName().toLowerCase();
            int tmpPort = getConnectPort();
            if (tmpPort <= 0) {
                //Default fallback
                tmpPort = TCFConnectorServiceManager.TCF_PORT;
            }
            final int port = tmpPort;
            IPeer peer = null;
            String ports = Integer.toString(port);
            ILocator locator = Protocol.getLocator();
            for (Iterator<IPeer> i = locator.getPeers().values().iterator(); i.hasNext();) {
                IPeer p = i.next();
                Map<String, String> attrs = p.getAttributes();
                if ("TCP".equals(attrs.get(IPeer.ATTR_TRANSPORT_NAME)) && //$NON-NLS-1$
                        host.equalsIgnoreCase(attrs.get(IPeer.ATTR_IP_HOST)) &&
                        ports.equals(attrs.get(IPeer.ATTR_IP_PORT))) {
                    peer = p;
                    break;
                }
            }
            if (peer == null) {
                Map<String, String> attrs = new HashMap<String, String>();
                attrs.put(IPeer.ATTR_ID, "RSE:" + host + ":" + port); //$NON-NLS-1$ //$NON-NLS-2$
                attrs.put(IPeer.ATTR_NAME, getName());
                attrs.put(IPeer.ATTR_TRANSPORT_NAME, "TCP"); //$NON-NLS-1$
                attrs.put(IPeer.ATTR_IP_HOST, host);
                attrs.put(IPeer.ATTR_IP_PORT, ports);
                peer = new AbstractPeer(attrs) {
                    @Override
                    public IChannel openChannel() {
                        return new ChannelTCP(this, host, port);
                    }
                };
            }
            channel = peer.openChannel();
            channel.addChannelListener(new IChannel.IChannelListener() {

                public void onChannelOpened() {
                    onConnected();
                }

                public void congestionLevel(int level) {
                }

                public void onChannelClosed(Throwable error) {
                    channel.removeChannelListener(this);
                    onDisconnected(error);
                }

            });
            assert channel.getState() == IChannel.STATE_OPENNING;
        }
        state_change.add(new Runnable() {
            public void run() {
                connectTCFChannel(res);
            }
        });
    }

    private void disconnectTCFChannel(final Exception[] res) {
        if (channel == null || channel.getState() == IChannel.STATE_CLOSED) {
            synchronized (res) {
                res[0] = null;
                res.notify();
            }
            return;
        }
        if (channel.getState() == IChannel.STATE_OPEN) channel.close();
        state_change.add(new Runnable() {
            public void run() {
                disconnectTCFChannel(res);
            }
        });
    }

    private void onConnected() {
        assert channel != null;
        if (state_change.isEmpty()) return;
        Runnable[] r = state_change.toArray(new Runnable[state_change.size()]);
        state_change.clear();
        for (int i = 0; i < r.length; i++) r[i].run();
    }

    private void onDisconnected(Throwable error) {
        assert channel != null;
        channel_error = error;
        if (state_change.isEmpty()) {
            fireCommunicationsEvent(CommunicationsEvent.CONNECTION_ERROR);
        }
        else {
            Runnable[] r = state_change.toArray(new Runnable[state_change.size()]);
            state_change.clear();
            for (int i = 0; i < r.length; i++) r[i].run();
        }
        channel = null;
        channel_error = null;
    }

    public ISysMonitor getSysMonitorService() {
        if (channel == null || channel.getState() != IChannel.STATE_OPEN) throw new Error("Not connected"); //$NON-NLS-1$
        ISysMonitor m = channel.getRemoteService(ISysMonitor.class);
        if (m == null) throw new Error("Remote peer does not support SysMonitor service"); //$NON-NLS-1$
        return m;
    }

    public IFileSystem getFileSystemService() {
        if (channel == null || channel.getState() != IChannel.STATE_OPEN) throw new Error("Not connected"); //$NON-NLS-1$
        IFileSystem m = channel.getRemoteService(IFileSystem.class);
        if (m == null) throw new Error("Remote peer does not support FileSystem service"); //$NON-NLS-1$
        return m;
    }
}
