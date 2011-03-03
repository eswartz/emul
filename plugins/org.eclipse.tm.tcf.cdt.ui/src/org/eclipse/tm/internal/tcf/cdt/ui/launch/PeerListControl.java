/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui.launch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TreeEvent;
import org.eclipse.swt.events.TreeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tm.internal.tcf.cdt.ui.Activator;
import org.eclipse.tm.internal.tcf.cdt.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ILocator;

// Cloned from TCFTargetTab
public class PeerListControl implements ISelectionProvider {

    private Tree peer_tree;
    private final PeerInfo peer_info = new PeerInfo();
    private Display display;
    private final ListenerList fSelectionListeners = new ListenerList(ListenerList.IDENTITY);
    private String fInitialPeerId = "*";

    static class PeerInfo {
        PeerInfo parent;
        int index;
        String id;
        Map<String,String> attrs;
        PeerInfo[] children;
        boolean children_pending;
        Throwable children_error;
        IPeer peer;
        IChannel channel;
        ILocator locator;
        LocatorListener listener;
    }

    private class LocatorListener implements ILocator.LocatorListener {

        private final PeerInfo parent;

        LocatorListener(PeerInfo parent) {
            this.parent = parent;
        }

        public void peerAdded(final IPeer peer) {
            if (display == null) return;
            final String id = peer.getID();
            final HashMap<String,String> attrs = new HashMap<String,String>(peer.getAttributes());
            display.asyncExec(new Runnable() {
                public void run() {
                    if (parent.children_error != null) return;
                    PeerInfo[] arr = parent.children;
                    String agentId = attrs.get(IPeer.ATTR_AGENT_ID);
                    for (PeerInfo p : arr) {
                        assert !p.id.equals(id);
                        if (agentId != null && agentId.equals(p.attrs.get(IPeer.ATTR_AGENT_ID)))
                            return;
                    }
                    PeerInfo[] buf = new PeerInfo[arr.length + 1];
                    System.arraycopy(arr, 0, buf, 0, arr.length);
                    PeerInfo info = new PeerInfo();
                    info.parent = parent;
                    info.index = arr.length;
                    info.id = id;
                    info.attrs = attrs;
                    info.peer = peer;
                    buf[arr.length] = info;
                    parent.children = buf;
                    updateItems(parent);
                }
            });
        }

        public void peerChanged(final IPeer peer) {
            if (display == null) return;
            final String id = peer.getID();
            final HashMap<String,String> attrs = new HashMap<String,String>(peer.getAttributes());
            display.asyncExec(new Runnable() {
                public void run() {
                    if (parent.children_error != null) return;
                    PeerInfo[] arr = parent.children;
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].id.equals(id)) {
                            arr[i].attrs = attrs;
                            arr[i].peer = peer;
                            updateItems(parent);
                        }
                    }
                }
            });
        }

        public void peerRemoved(final String id) {
            if (display == null) return;
            display.asyncExec(new Runnable() {
                public void run() {
                    if (parent.children_error != null) return;
                    PeerInfo[] arr = parent.children;
                    PeerInfo[] buf = new PeerInfo[arr.length - 1];
                    int j = 0;
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].id.equals(id)) {
                            final PeerInfo info = arr[i];
                            Protocol.invokeLater(new Runnable() {
                                public void run() {
                                    disconnectPeer(info);
                                }
                            });
                        }
                        else {
                            arr[i].index = j;
                            buf[j++] = arr[i];
                        }
                    }
                    parent.children = buf;
                    updateItems(parent);
                }
            });
        }

        public void peerHeartBeat(final String id) {
            if (display == null) return;
            display.asyncExec(new Runnable() {
                public void run() {
                    if (parent.children_error != null) return;
                    PeerInfo[] arr = parent.children;
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].id.equals(id)) {
                            if (arr[i].children_error != null) {
                                TreeItem item = findItem(arr[i]);
                                boolean visible = item != null;
                                while (visible && item != null) {
                                    if (!item.getExpanded()) visible = false;
                                    item = item.getParentItem();
                                }
                                if (visible) loadChildren(arr[i]);
                            }
                            break;
                        }
                    }
                }
            });
        }
    }

    public PeerListControl(Composite parent) {
        display = parent.getDisplay();
        parent.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleDispose();
            }
        });
        loadChildren(peer_info);
        createPeerListArea(parent);
    }

    public void setInitialSelectedPeerId(String peerId) {
        fInitialPeerId = peerId;
    }

    public Tree getTree() {
        return peer_tree;
    }

    private void createPeerListArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(font);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        peer_tree = new Tree(composite, SWT.VIRTUAL | SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 80;
        gd.minimumWidth = 400;
        peer_tree.setLayoutData(gd);

        for (int i = 0; i < 5; i++) {
            TreeColumn column = new TreeColumn(peer_tree, SWT.LEAD, i);
            column.setMoveable(true);
            switch (i) {
            case 0:
                column.setText("Name");
                column.setWidth(160);
                break;
            case 1:
                column.setText("OS");
                column.setWidth(100);
                break;
            case 2:
                column.setText("Transport");
                column.setWidth(60);
                break;
            case 3:
                column.setText("Host");
                column.setWidth(100);
                break;
            case 4:
                column.setText("Port");
                column.setWidth(40);
                break;
            }
        }

        peer_tree.setHeaderVisible(true);
        peer_tree.setFont(font);
        peer_tree.addListener(SWT.SetData, new Listener() {
            public void handleEvent(Event event) {
                TreeItem item = (TreeItem)event.item;
                PeerInfo info = findPeerInfo(item);
                if (info == null) {
                    updateItems(item.getParentItem(), false);
                }
                else {
                    fillItem(item, info);
                }
            }
        });
        peer_tree.addTreeListener(new TreeListener() {
            public void treeCollapsed(TreeEvent e) {
                updateItems((TreeItem)e.item, false);
            }
            public void treeExpanded(TreeEvent e) {
                updateItems((TreeItem)e.item, true);
            }
        });
        peer_tree.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                fireSelectionChangedEvent();
            }
        });
    }

    private void handleDispose() {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                disconnectPeer(peer_info);
                display = null;
            }
        });
    }

    private void disconnectPeer(final PeerInfo info) {
        assert Protocol.isDispatchThread();
        if (info.children != null) {
            for (PeerInfo p : info.children) disconnectPeer(p);
        }
        if (info.listener != null) {
            info.locator.removeListener(info.listener);
            info.listener = null;
            info.locator = null;
        }
        if (info.channel != null) {
            info.channel.close();
        }
    }

    private boolean canHaveChildren(PeerInfo parent) {
        return parent == peer_info || parent.attrs.get(IPeer.ATTR_PROXY) != null;
    }

    private void loadChildren(final PeerInfo parent) {
        assert Thread.currentThread() == display.getThread();
        if (parent.children_pending) return;
        assert parent.children == null;
        parent.children_pending = true;
        parent.children_error = null;
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                assert parent.listener == null;
                assert parent.channel == null;
                if (!canHaveChildren(parent)) {
                    doneLoadChildren(parent, null, new PeerInfo[0]);
                }
                else if (parent == peer_info) {
                    peer_info.locator = Protocol.getLocator();
                    doneLoadChildren(parent, null, createLocatorListener(peer_info));
                }
                else {
                    final IChannel channel = parent.peer.openChannel();
                    parent.channel = channel;
                    parent.channel.addChannelListener(new IChannelListener() {
                        boolean opened = false;
                        boolean closed = false;
                        public void congestionLevel(int level) {
                        }
                        public void onChannelClosed(final Throwable error) {
                            assert !closed;
                            if (parent.channel != channel) return;
                            if (!opened) {
                                doneLoadChildren(parent, error, null);
                            }
                            else {
                                if (display != null) {
                                    display.asyncExec(new Runnable() {
                                        public void run() {
                                            if (parent.children_pending) return;
                                            parent.children = null;
                                            parent.children_error = error;
                                            updateItems(parent);
                                        }
                                    });
                                }
                            }
                            closed = true;
                            parent.channel = null;
                            parent.locator = null;
                            parent.listener = null;
                        }
                        public void onChannelOpened() {
                            assert !opened;
                            assert !closed;
                            if (parent.channel != channel) return;
                            opened = true;
                            parent.locator = parent.channel.getRemoteService(ILocator.class);
                            if (parent.locator == null) {
                                doneLoadChildren(parent, new Exception("Service not supported: " + ILocator.NAME), null);
                                parent.channel.close();
                            }
                            else {
                                doneLoadChildren(parent, null, createLocatorListener(parent));
                            }
                        }
                    });
                }
            }
        });
    }

    private PeerInfo[] createLocatorListener(PeerInfo peer) {
        assert Protocol.isDispatchThread();
        Map<String,IPeer> map = peer.locator.getPeers();
        List<PeerInfo> filteredPeers = new ArrayList<PeerInfo>();
        Set<String> agentIds = new HashSet<String>();
        for (IPeer p : map.values()) {
            String agentID = p.getAgentID();
            if (agentID != null && agentIds.add(agentID)) {
                PeerInfo info = new PeerInfo();
                info.parent = peer;
                info.index = filteredPeers.size();
                info.id = p.getID();
                info.attrs = new HashMap<String,String>(p.getAttributes());
                info.peer = p;
                filteredPeers.add(info);
            }
        }
        PeerInfo[] buf = (PeerInfo[]) filteredPeers.toArray(new PeerInfo[filteredPeers.size()]);
        peer.listener = new LocatorListener(peer);
        peer.locator.addListener(peer.listener);
        return buf;
    }

    private void doneLoadChildren(final PeerInfo parent, final Throwable error, final PeerInfo[] children) {
        assert Protocol.isDispatchThread();
        assert error == null || children == null;
        if (display == null) return;
        display.asyncExec(new Runnable() {
            public void run() {
                assert parent.children_pending;
                assert parent.children == null;
                parent.children_pending = false;
                parent.children = children;
                parent.children_error = error;
                updateItems(parent);
            }
        });
    }

    private void updateItems(TreeItem parent_item, boolean reload) {
        final PeerInfo parent_info = findPeerInfo(parent_item);
        if (parent_info == null) {
            parent_item.setText("Invalid");
        }
        else {
            if (reload && parent_info.children_error != null) {
                loadChildren(parent_info);
            }
            display.asyncExec(new Runnable() {
                public void run() {
                    updateItems(parent_info);
                }
            });
        }
    }

    private void updateItems(final PeerInfo parent) {
        if (display == null) return;
        assert Thread.currentThread() == display.getThread();
        TreeItem[] items = null;
        boolean expanded = true;
        if (parent.children == null || parent.children_error != null) {
            if (parent == peer_info) {
                peer_tree.setItemCount(1);
                items = peer_tree.getItems();
            }
            else {
                TreeItem item = findItem(parent);
                if (item == null) return;
                expanded = item.getExpanded();
                item.setItemCount(1);
                items = item.getItems();
            }
            assert items.length == 1;
            items[0].removeAll();
            if (parent.children_pending) {
                items[0].setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                items[0].setText("Connecting...");
            }
            else if (parent.children_error != null) {
                String msg = parent.children_error.getMessage();
                if (msg == null) msg = parent.children_error.getClass().getName();
                else msg = msg.replace('\n', ' ');
                items[0].setForeground(display.getSystemColor(SWT.COLOR_RED));
                items[0].setText(msg);
            }
            else if (expanded) {
                loadChildren(parent);
                items[0].setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                items[0].setText("Connecting...");
            }
            else {
                Protocol.invokeAndWait(new Runnable() {
                    public void run() {
                        disconnectPeer(parent);
                    }
                });
                items[0].setText("");
            }
            int n = peer_tree.getColumnCount();
            for (int i = 1; i < n; i++) items[0].setText(i, "");
            items[0].setImage((Image)null);
        }
        else {
            PeerInfo[] arr = parent.children;
            if (parent == peer_info) {
                peer_tree.setItemCount(arr.length);
                items = peer_tree.getItems();
            }
            else {
                TreeItem item = findItem(parent);
                if (item == null) return;
                expanded = item.getExpanded();
                item.setItemCount(expanded ? arr.length : 1);
                items = item.getItems();
            }
            if (expanded) {
                assert items.length == arr.length;
                for (int i = 0; i < items.length; i++) fillItem(items[i], arr[i]);
                if (fInitialPeerId != null) {
                    if ("*".equals(fInitialPeerId)) {
                        fInitialPeerId = null;
                        peer_tree.setSelection(items[0]);
                        fireSelectionChangedEvent();
                    } else {
                        int i = 0;
                        for (PeerInfo peerInfo : arr) {
                            if (fInitialPeerId.equals(peerInfo.id)) {
                                fInitialPeerId = null;
                                peer_tree.setSelection(items[i]);
                                fireSelectionChangedEvent();
                                break;
                            }
                            i++;
                        }
                    }
                }
            }
            else {
                Protocol.invokeAndWait(new Runnable() {
                    public void run() {
                        disconnectPeer(parent);
                    }
                });
                items[0].setText("");
                int n = peer_tree.getColumnCount();
                for (int i = 1; i < n; i++) items[0].setText(i, "");
            }
        }
    }

    public PeerInfo findPeerInfo(String peerId) {
        return findPeerInfo(peer_info, peerId);
    }

    private PeerInfo findPeerInfo(PeerInfo parent, String peerId) {
        if (peerId.equals(parent.id)) return parent;
        PeerInfo[] children = parent.children;
        if (children == null) return null;
        for (PeerInfo child : children) {
            PeerInfo info = findPeerInfo(child, peerId);
            if (info != null) return info;
        }
        return null;
    }

    public PeerInfo findPeerInfo(TreeItem item) {
        assert Thread.currentThread() == display.getThread();
        if (item == null) return peer_info;
        TreeItem parent = item.getParentItem();
        PeerInfo info = findPeerInfo(parent);
        if (info == null) return null;
        if (info.children == null) return null;
        if (info.children_error != null) return null;
        int i = parent == null ? peer_tree.indexOf(item) : parent.indexOf(item);
        if (i < 0 || i >= info.children.length) return null;
        assert info.children[i].index == i;
        return info.children[i];
    }

    private TreeItem findItem(PeerInfo info) {
        if (info == null) return null;
        assert info.parent != null;
        if (info.parent == peer_info) {
            int n = peer_tree.getItemCount();
            if (info.index >= n) return null;
            return peer_tree.getItem(info.index);
        }
        TreeItem i = findItem(info.parent);
        if (i == null) return null;
        int n = i.getItemCount();
        if (info.index >= n) return null;
        return i.getItem(info.index);
    }

    private void fillItem(TreeItem item, PeerInfo info) {
        assert Thread.currentThread() == display.getThread();
        Object data = item.getData("TCFPeerInfo");
        if (data != null && data != info) item.removeAll();
        item.setData("TCFPeerInfo", info);
        String text[] = new String[5];
        text[0] = info.attrs.get(IPeer.ATTR_NAME);
        text[1] = info.attrs.get(IPeer.ATTR_OS_NAME);
        text[2] = info.attrs.get(IPeer.ATTR_TRANSPORT_NAME);
        text[3] = info.attrs.get(IPeer.ATTR_IP_HOST);
        text[4] = info.attrs.get(IPeer.ATTR_IP_PORT);
        for (int i = 0; i < text.length; i++) {
            if (text[i] == null) text[i] = "";
        }
        item.setText(text);
        item.setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
        item.setImage(ImageCache.getImage(getImageName(info)));
        if (!canHaveChildren(info)) item.setItemCount(0);
        else if (info.children == null || info.children_error != null) item.setItemCount(1);
        else item.setItemCount(info.children.length);
    }

    private String getImageName(PeerInfo info) {
        return ImageCache.IMG_TARGET_TAB;
    }

    public void addSelectionChangedListener(ISelectionChangedListener listener) {
        fSelectionListeners .add(listener);
    }

    public ISelection getSelection() {
        TreeItem[] items = peer_tree.getSelection();
        PeerInfo[] peers = new PeerInfo[items.length];
        int i = 0;
        for (TreeItem item : items) {
            peers[i++] = findPeerInfo(item);
        }
        return new StructuredSelection(peers);
    }

    public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        fSelectionListeners.remove(listener);
    }

    public void setSelection(ISelection selection) {
        peer_tree.deselectAll();
        if (selection instanceof IStructuredSelection) {
            Object[] elements = ((IStructuredSelection) selection).toArray();
            for (Object object : elements) {
                if (object instanceof PeerInfo) {
                    TreeItem item = findItem((PeerInfo) object);
                    if (item != null) {
                        peer_tree.select(item);
                    }
                }
            }
        }
    }

    private void fireSelectionChangedEvent() {
        SelectionChangedEvent event = new SelectionChangedEvent(this, getSelection());
        Object[] listeners = fSelectionListeners.getListeners();
        for (Object listener : listeners) {
            try {
                ((ISelectionChangedListener) listener).selectionChanged(event);
            } catch (Exception e) {
                Activator.log(e);
            }
        }
    }

}
