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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IChannel.IChannelListener;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.services.IProcesses.ProcessContext;

public class ProcessListControl {

    private final class ProcessListener implements IProcesses.ProcessesListener {
        public void exited(final String process_id, int exit_code) {
            if (display != null) {
                display.asyncExec(new Runnable() {
                    public void run() {
                        ProcessInfo info = findProcessInfo(root_info, process_id);
                        if (info != null && info.parent != null && info.parent.children != null) {
                            info.parent.children = null;
                            loadChildren(info.parent);
                        }
                    }
                });
            }
        }
    }

    static class ProcessInfo {
        String name;
        String id;
        boolean isContainer;
        ProcessInfo[] children;
        Throwable children_error;
        int index;
        boolean children_pending;
        ProcessInfo parent;
        protected boolean isAttached;
    }

    private Tree fProcessTree;
    private Display display;
    private IPeer fPeer;
    private final ProcessInfo root_info = new ProcessInfo();
    private IChannel fChannel;
    private IProcesses fProcesses;
    protected final ProcessListener fProcessListener = new ProcessListener();
    private String fContextToSelect;
    private LinkedList<String> fPathToSelect;
    private Composite fComposite;

    public ProcessListControl(Composite parent) {
        display = parent.getDisplay();
        parent.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleDispose();
            }
        });
        createProcessListArea(parent);
    }

    public void setInput(IPeer peer) {
        if (peer == fPeer) {
            return;
        }
        if (fPeer != null) {
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    disconnectPeer();
                }
            });
        }
        fProcessTree.setItemCount(0);
        root_info.children = null;
        fPeer = peer;
        if (fPeer != null) {
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    connectPeer();
                }
            });
        }
    }

    public Control getControl() {
        return fComposite;
    }

    public Tree getTree() {
        return fProcessTree;
    }

    public ProcessInfo getSelection() {
        if (fProcessTree != null) {
            TreeItem[] items = fProcessTree.getSelection();
            if (items.length > 0) {
                ProcessInfo info = findProcessInfo(items[0]);
                return info;
            }
        }
        return null;
    }

    private void createProcessListArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(font);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));
        fComposite = composite;
        
        fProcessTree = new Tree(composite, SWT.VIRTUAL | SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.minimumHeight = 150;
        gd.minimumWidth = 470;
        fProcessTree.setLayoutData(gd);
        fProcessTree.setFont(font);
        fProcessTree.addListener(SWT.SetData, new Listener() {
            public void handleEvent(Event event) {
                TreeItem item = (TreeItem)event.item;
                ProcessInfo info = findProcessInfo(item);
                if (info == null) {
                    updateItems(item.getParentItem(), false);
                }
                else {
                    fillItem(item, info);
                }
            }
        });
    }

    private void handleDispose() {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                disconnectPeer();
                if (fProcesses != null) {
                    fProcesses.removeListener(fProcessListener);
                    fProcesses = null;
                }
                display = null;
            }
        });
    }

    protected void disconnectPeer() {
        if (fChannel != null && fChannel.getState() != IChannel.STATE_CLOSED) {
            fChannel.close();
        }
    }

    protected void connectPeer() {
        final IChannel channel = fPeer.openChannel();
        fChannel = channel;
        fProcesses = null;
        channel.addChannelListener(new IChannelListener() {
            public void congestionLevel(int level) {
            }
            public void onChannelClosed(final Throwable error) {
                if (fChannel != channel) return;
                fChannel = null;
                if (display != null) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            if (root_info.children_pending) return;
                            root_info.children = null;
                            root_info.children_error = error;
                            updateItems(root_info);
                        }
                    });
                }
            }
            public void onChannelOpened() {
                if (fChannel != channel) return;
                fProcesses = fChannel.getRemoteService(IProcesses.class);
                if (fProcesses != null) {
                    fProcesses.addListener(fProcessListener);
                    if (fContextToSelect != null) {
                        final LinkedList<String> contextPath = new LinkedList<String>();
                        contextPath.addFirst(fContextToSelect);
                        fProcesses.getContext(fContextToSelect, new IProcesses.DoneGetContext() {
                            public void doneGetContext(IToken token, Exception error, ProcessContext context) {
                                if (error == null) {
                                    String parentId = context.getParentID();
                                    if (parentId != null) {
                                        contextPath.addFirst(parentId);
                                        fProcesses.getContext(parentId, this);
                                        return;
                                    }
                                    if (display != null) {
                                        display.asyncExec(new Runnable() {
                                            public void run() {
                                                fPathToSelect = contextPath;
                                                expandSelect();
                                            }
                                        });
                                    }
                                }
                            }
                        });
                    }
                }
                if (display != null) {
                    display.asyncExec(new Runnable() {
                        public void run() {
                            if (root_info.children_pending) return;
                            root_info.children = null;
                            root_info.children_error = null;
                            updateItems(root_info);
                        }
                    });
                }
            }
        });
    }

    private void updateItems(TreeItem parent_item, boolean reload) {
        final ProcessInfo parent_info = findProcessInfo(parent_item);
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

    private void updateItems(final ProcessInfo parent) {
        if (display == null) return;
        assert Thread.currentThread() == display.getThread();
        TreeItem[] items = null;
        boolean expanded = true;
        if (parent.children == null || parent.children_error != null) {
            if (parent == root_info) {
                fProcessTree.setItemCount(1);
                items = fProcessTree.getItems();
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
                items[0].setText("Pending...");
            }
            else if (parent.children_error != null) {
                String msg = parent.children_error.getMessage();
                if (msg == null) msg = parent.children_error.getClass().getName();
                else msg = msg.replace('\n', ' ');
                items[0].setForeground(display.getSystemColor(SWT.COLOR_RED));
                items[0].setText(msg);
                items[0].setImage((Image) null);
            }
            else if (expanded) {
                loadChildren(parent);
                items[0].setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                items[0].setText("Pending...");
            }
            else {
                items[0].setText("");
            }
        }
        else {
            ProcessInfo[] arr = parent.children;
            if (parent == root_info) {
                fProcessTree.setItemCount(arr.length);
                items = fProcessTree.getItems();
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
                // auto-expand single children
                if (items.length == 1 && !items[0].getExpanded()) {
                    items[0].setExpanded(true);
                }
                expandSelect();
            }
            else {
                items[0].setText("");
            }
        }
    }

    private void expandSelect() {
        if (fPathToSelect == null) return;
        if (fPathToSelect.isEmpty()) {
            fPathToSelect = null;
            fContextToSelect = null;
            return;
        }
        do {
            String id = fPathToSelect.get(0);
            ProcessInfo info = findProcessInfo(root_info, id);
            if (info == null) break;
            TreeItem item = findItem(info);
            if (item == null) break;
            fPathToSelect.removeFirst();
            if (fPathToSelect.isEmpty()) {
                fProcessTree.setSelection(item);
            } else {
                item.setExpanded(true);
            }
        } while (!fPathToSelect.isEmpty());
    }

    private void loadChildren(final ProcessInfo parent) {
        assert Thread.currentThread() == display.getThread();
        if (parent.children_pending) return;
        assert parent.children == null;
        parent.children_pending = true;
        parent.children_error = null;
        Protocol.invokeLater(new Runnable() {
            public void run() {
                final IProcesses proc = fProcesses;
                if (proc == null || !canHaveChildren(parent)) {
                    doneLoadChildren(parent, null, new ProcessInfo[0]);
                }
                else {
                    proc.getChildren(parent.id, false, new IProcesses.DoneGetChildren() {
                        public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
                            if (error != null) {
                                doneLoadChildren(parent, error, null);
                            } else if (context_ids.length > 0){
                                final List<ProcessInfo> contextInfos = new ArrayList<ProcessInfo>(context_ids.length);
                                final Set<IToken> pending = new HashSet<IToken>();
                                for (String id : context_ids) {
                                    pending.add(proc.getContext(id, new IProcesses.DoneGetContext() {
                                        public void doneGetContext(IToken token, Exception error, ProcessContext context) {
                                            if (context != null) {
                                                ProcessInfo info = new ProcessInfo();
                                                info.parent = parent;
                                                info.id = context.getID();
                                                info.name = context.getName();
                                                if (info.name == null || info.name.length() == 0) {
                                                    info.name = info.id;
                                                } else {
                                                    info.name += " [" + info.id + ']';
                                                }
                                                info.isContainer = false;
                                                info.isAttached = context.isAttached();
                                                info.index = contextInfos.size();
                                                contextInfos.add(info);
                                            }
                                            pending.remove(token);
                                            if (pending.isEmpty()) {
                                                doneLoadChildren(parent, null, contextInfos.toArray(new ProcessInfo[contextInfos.size()]));
                                            }
                                        }
                                    }));
                                }
                            } else {
                                doneLoadChildren(parent, null, new ProcessInfo[0]);
                            }
                        }
                    });
                }
            }
        });
    }

    private void doneLoadChildren(final ProcessInfo parent, final Throwable error, final ProcessInfo[] children) {
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

    public ProcessInfo findProcessInfo(TreeItem item) {
        assert Thread.currentThread() == display.getThread();
        if (item == null) return root_info ;
        TreeItem parent = item.getParentItem();
        ProcessInfo info = findProcessInfo(parent);
        if (info == null) return null;
        if (info.children == null) return null;
        if (info.children_error != null) return null;
        int i = parent == null ? fProcessTree.indexOf(item) : parent.indexOf(item);
        if (i < 0 || i >= info.children.length) return null;
        assert info.children[i].index == i;
        return info.children[i];
    }

    public ProcessInfo findProcessInfo(ProcessInfo parent, String id) {
        assert Thread.currentThread() == display.getThread();
        if (id == null) return root_info;
        if (id.equals(parent.id)) return parent;
        ProcessInfo[] childInfos = parent.children;
        if (childInfos != null) {
            for (ProcessInfo contextInfo : childInfos) {
                ProcessInfo found = findProcessInfo(contextInfo, id);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private TreeItem findItem(ProcessInfo info) {
        if (info == null) return null;
        assert info.parent != null;
        if (info.parent == root_info) {
            int n = fProcessTree.getItemCount();
            if (info.index >= n) return null;
            return fProcessTree.getItem(info.index);
        }
        TreeItem i = findItem(info.parent);
        if (i == null) return null;
        int n = i.getItemCount();
        if (info.index >= n) return null;
        return i.getItem(info.index);
    }

    private void fillItem(TreeItem item, ProcessInfo info) {
        assert Thread.currentThread() == display.getThread();
        Object data = item.getData("TCFContextInfo");
        if (data != null && data != info) item.removeAll();
        item.setData("TCFContextInfo", info);
        String text = info.name != null ? info.name : info.id;
        item.setText(text);
        item.setForeground(display.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
        item.setImage(getImage(info));
        if (!canHaveChildren(info)) item.setItemCount(0);
        else if (info.children == null || info.children_error != null) item.setItemCount(1);
        else item.setItemCount(info.children.length);
    }

    private boolean canHaveChildren(ProcessInfo info) {
        return info.isContainer || info == root_info;
    }

    private Image getImage(ProcessInfo info) {
        return DebugUITools.getImage(IDebugUIConstants.IMG_OBJS_OS_PROCESS);
    }

    public void selectContext(final String contextId) {
        fPathToSelect = null;
        Protocol.invokeLater(new Runnable() {
            public void run() {
                fContextToSelect = contextId;
            }
        });
    }

}
