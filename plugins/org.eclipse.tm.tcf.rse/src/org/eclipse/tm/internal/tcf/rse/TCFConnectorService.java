/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Martin Oberhuber (Wind River) - [269682] Get port from RSE Property
 *     Uwe Stieber      (Wind River) - [271227] Fix compiler warnings in org.eclipse.tm.tcf.rse
 *     Anna Dushistova  (MontaVista) - [285373] TCFConnectorService should send CommunicationsEvent.BEFORE_CONNECT and CommunicationsEvent.BEFORE_DISCONNECT
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.BasicConnectorService;
import org.eclipse.rse.core.subsystems.CommunicationsEvent;
import org.eclipse.tm.tcf.core.AbstractPeer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.ILocator;
import org.eclipse.tm.tcf.services.ISysMonitor;


public class TCFConnectorService extends BasicConnectorService {

    private IChannel channel;
    private Throwable channel_error;
    private final List<Runnable> wait_list = new ArrayList<Runnable>();

    private boolean poll_timer_started;

    public TCFConnectorService(IHost host, int port) {
        super("TCF", "Target Communication Framework", host, port); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    protected void internalConnect(final IProgressMonitor monitor) throws Exception {
        assert !Protocol.isDispatchThread();
        final Exception[] res = new Exception[1];
        // Fire comm event to signal state about to change
        fireCommunicationsEvent(CommunicationsEvent.BEFORE_CONNECT);
        monitor.beginTask("Connecting " + getHostName(), 1); //$NON-NLS-1$
        synchronized (res) {
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    if (!connectTCFChannel(res, monitor)) add_to_wait_list(this);
                }
            });
            res.wait();
        }
        monitor.done();
        if (res[0] != null) throw res[0];
    }

    @Override
    protected void internalDisconnect(final IProgressMonitor monitor) throws Exception {
        assert !Protocol.isDispatchThread();
        final Exception[] res = new Exception[1];
        // Fire comm event to signal state about to change
        fireCommunicationsEvent(CommunicationsEvent.BEFORE_DISCONNECT);
        monitor.beginTask("Disconnecting " + getHostName(), 1); //$NON-NLS-1$
        synchronized (res) {
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    if (!disconnectTCFChannel(res, monitor)) add_to_wait_list(this);
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

    private void add_to_wait_list(Runnable cb) {
        wait_list.add(cb);
        if (poll_timer_started) return;
        Protocol.invokeLater(1000, new Runnable() {
            public void run() {
                poll_timer_started = false;
                run_wait_list();
            }
        });
        poll_timer_started = true;
    }

    private void run_wait_list() {
        if (wait_list.isEmpty()) return;
        Runnable[] r = wait_list.toArray(new Runnable[wait_list.size()]);
        wait_list.clear();
        for (int i = 0; i < r.length; i++) r[i].run();
    }

    private boolean connectTCFChannel(Exception[] res, IProgressMonitor monitor) {
        if (channel != null) {
            switch (channel.getState()) {
            case IChannel.STATE_OPEN:
            case IChannel.STATE_CLOSED:
                synchronized (res) {
                    if (channel_error instanceof Exception) res[0] = (Exception)channel_error;
                    else if (channel_error != null) res[0] = new Exception(channel_error);
                    else res[0] = null;
                    res.notify();
                    return true;
                }
            }
        }
        if (monitor.isCanceled()) {
            synchronized (res) {
                res[0] = new Exception("Canceled"); //$NON-NLS-1$
                if (channel != null) channel.terminate(res[0]);
                res.notify();
                return true;
            }
        }
        if (channel == null) {
            String host = getHostName().toLowerCase();
            int port = getConnectPort();
            if (port <= 0) {
                //Default fallback
                port = TCFConnectorServiceManager.TCF_PORT;
            }
            IPeer peer = null;
            String port_str = Integer.toString(port);
            ILocator locator = Protocol.getLocator();
            for (IPeer p : locator.getPeers().values()) {
                Map<String, String> attrs = p.getAttributes();
                if ("TCP".equals(attrs.get(IPeer.ATTR_TRANSPORT_NAME)) && //$NON-NLS-1$
                        host.equalsIgnoreCase(attrs.get(IPeer.ATTR_IP_HOST)) &&
                        port_str.equals(attrs.get(IPeer.ATTR_IP_PORT))) {
                    peer = p;
                    break;
                }
            }
            if (peer == null) {
                Map<String, String> attrs = new HashMap<String, String>();
                attrs.put(IPeer.ATTR_ID, "RSE:" + host + ":" + port_str); //$NON-NLS-1$ //$NON-NLS-2$
                attrs.put(IPeer.ATTR_NAME, getName());
                attrs.put(IPeer.ATTR_TRANSPORT_NAME, "TCP"); //$NON-NLS-1$
                attrs.put(IPeer.ATTR_IP_HOST, host);
                attrs.put(IPeer.ATTR_IP_PORT, port_str);
                peer = new AbstractPeer(attrs);
            }
            channel = peer.openChannel();
            channel.addChannelListener(new IChannel.IChannelListener() {

                public void onChannelOpened() {
                    assert channel != null;
                    run_wait_list();
                }

                public void congestionLevel(int level) {
                }

                public void onChannelClosed(Throwable error) {
                    assert channel != null;
                    channel.removeChannelListener(this);
                    channel_error = error;
                    if (wait_list.isEmpty()) {
                        fireCommunicationsEvent(CommunicationsEvent.CONNECTION_ERROR);
                    }
                    else {
                        run_wait_list();
                    }
                    channel = null;
                    channel_error = null;
                }

            });
            assert channel.getState() == IChannel.STATE_OPENNING;
        }
        return false;
    }

    private boolean disconnectTCFChannel(Exception[] res, IProgressMonitor monitor) {
        if (channel == null || channel.getState() == IChannel.STATE_CLOSED) {
            synchronized (res) {
                res[0] = null;
                res.notify();
                return true;
            }
        }
        if (monitor.isCanceled()) {
            synchronized (res) {
                res[0] = new Exception("Canceled"); //$NON-NLS-1$
                res.notify();
                return true;
            }
        }
        if (channel.getState() == IChannel.STATE_OPEN) channel.close();
        return false;
    }

    public <V extends IService> V getService(Class<V> service_interface) {
        if (channel == null || channel.getState() != IChannel.STATE_OPEN) throw new Error("Not connected"); //$NON-NLS-1$
        V m = channel.getRemoteService(service_interface);
        if (m == null) throw new Error("Remote peer does not support " + service_interface.getName() + " service"); //$NON-NLS-1$  //$NON-NLS-2$
        return m;
    }

    public ISysMonitor getSysMonitorService() {
        return getService(ISysMonitor.class);
    }

    public IFileSystem getFileSystemService() {
        return getService(IFileSystem.class);
    }
}
