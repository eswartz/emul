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
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.Path;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
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
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.tcf.services.IFileSystem.IFileHandle;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;

public class FileSystemBrowserControl {

    static class FileInfo {
        String name;
        String fullname;
        boolean isDir;
        FileInfo[] children;
        Throwable children_error;
        int index;
        boolean children_pending;
        FileInfo parent;
    }

    private Tree fileTree;
    private Display fDisplay;
    private IPeer fPeer;
    private final FileInfo fRootInfo = new FileInfo();
    private IChannel fChannel;
    private IFileSystem fFileSystem;
    private String fFileToSelect;
    private LinkedList<String> fPathToSelect;
    private FileInfo fLastSelectedFileInfo;
    private final boolean fDirectoriesOnly;

    public FileSystemBrowserControl(Composite parent, boolean directoriesOnly) {
        fDirectoriesOnly = directoriesOnly;
        fDisplay = parent.getDisplay();
        parent.addDisposeListener(new DisposeListener() {
            public void widgetDisposed(DisposeEvent e) {
                handleDispose();
            }
        });
        createFileListArea(parent);
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
        fileTree.setItemCount(0);
        fRootInfo.children = null;
        fPeer = peer;
        if (fPeer != null) {
            Protocol.invokeAndWait(new Runnable() {
                public void run() {
                    connectPeer();
                }
            });
        }
    }

    public Tree getTree() {
        return fileTree;
    }

    private void createFileListArea(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(font);
        GridLayout layout = new GridLayout(1, false);
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true));

        fileTree = new Tree(composite, SWT.VIRTUAL | SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.minimumHeight = 300;
        gd.minimumWidth = 350;
        fileTree.setLayoutData(gd);
        fileTree.setFont(font);
        fileTree.addListener(SWT.SetData, new Listener() {
            public void handleEvent(Event event) {
                TreeItem item = (TreeItem)event.item;
                FileInfo info = findFileInfo(item);
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
                fFileSystem = null;
                fDisplay = null;
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
        fFileSystem = null;
        channel.addChannelListener(new IChannelListener() {
            public void congestionLevel(int level) {
            }
            public void onChannelClosed(final Throwable error) {
                if (fChannel != channel) return;
                fChannel = null;
                if (fDisplay != null) {
                    fDisplay.asyncExec(new Runnable() {
                        public void run() {
                            if (fRootInfo.children_pending) return;
                            fRootInfo.children = null;
                            fRootInfo.children_error = error;
                            updateItems(fRootInfo);
                        }
                    });
                }
            }
            public void onChannelOpened() {
                if (fChannel != channel) return;
                fFileSystem = fChannel.getRemoteService(IFileSystem.class);
                if (fFileSystem != null) {
                    if (fFileToSelect != null && fFileToSelect.length() > 0) {
                        final LinkedList<String> filePath = new LinkedList<String>();
                        filePath.addAll(Arrays.asList(fFileToSelect.split("[/\\\\]", -1)));
                        if (fFileToSelect.charAt(0) == '/') {
                            filePath.set(0, "/");
                        }
                        fPathToSelect = filePath;
                        fLastSelectedFileInfo = fRootInfo;
                    }
                }
                if (fDisplay != null) {
                    fDisplay.asyncExec(new Runnable() {
                        public void run() {
                            if (fRootInfo.children_pending) return;
                            fRootInfo.children = null;
                            fRootInfo.children_error = null;
                            updateItems(fRootInfo);
                        }
                    });
                }
            }
        });
    }

    private void updateItems(TreeItem parent_item, boolean reload) {
        final FileInfo parent_info = findFileInfo(parent_item);
        if (parent_info == null) {
            parent_item.setText("Invalid");
        }
        else {
            if (reload && parent_info.children_error != null) {
                loadChildren(parent_info);
            }
            fDisplay.asyncExec(new Runnable() {
                public void run() {
                    updateItems(parent_info);
                }
            });
        }
    }

    private void updateItems(final FileInfo parent) {
        if (fDisplay == null) return;
        assert Thread.currentThread() == fDisplay.getThread();
        TreeItem[] items = null;
        boolean expanded = true;
        if (parent.children == null || parent.children_error != null) {
            if (parent == fRootInfo) {
                fileTree.setItemCount(1);
                items = fileTree.getItems();
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
                items[0].setForeground(fDisplay.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                items[0].setText("Pending...");
            }
            else if (parent.children_error != null) {
                String msg = parent.children_error.getMessage();
                if (msg == null) msg = parent.children_error.getClass().getName();
                else msg = msg.replace('\n', ' ');
                items[0].setForeground(fDisplay.getSystemColor(SWT.COLOR_RED));
                items[0].setText(msg);
                items[0].setImage((Image) null);
            }
            else if (expanded) {
                loadChildren(parent);
                items[0].setForeground(fDisplay.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
                items[0].setText("Pending...");
            }
            else {
                items[0].setText("");
            }
        }
        else {
            FileInfo[] arr = parent.children;
            if (parent == fRootInfo) {
                fileTree.setItemCount(arr.length);
                items = fileTree.getItems();
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
            fFileToSelect = null;
            return;
        }
        do {
            String name = fPathToSelect.getFirst();
            if (name.length() == 0) {
                fPathToSelect.removeFirst();
                continue;
            }
            FileInfo info = findFileInfo(fLastSelectedFileInfo, name);
            if (info == null) break;
            TreeItem item = findItem(info);
            if (item == null) break;
            fPathToSelect.removeFirst();
            if (fPathToSelect.isEmpty()) {
                fileTree.setSelection(item);
                fileTree.showItem(item);
            } else {
                item.setExpanded(true);
                fileTree.showItem(item);
            }
            fLastSelectedFileInfo = info;
        } while (!fPathToSelect.isEmpty());
    }

    private void loadChildren(final FileInfo parent) {
        assert Thread.currentThread() == fDisplay.getThread();
        if (parent.children_pending) return;
        assert parent.children == null;
        parent.children_pending = true;
        parent.children_error = null;
        Protocol.invokeLater(new Runnable() {
            public void run() {
                final IFileSystem fs = fFileSystem;
                if (fs == null || !canHaveChildren(parent)) {
                    doneLoadChildren(parent, null, new FileInfo[0]);
                    return;
                }
                if (parent.fullname == null) {
                    fs.roots(new IFileSystem.DoneRoots() {
                        public void doneRoots(IToken token, FileSystemException error, DirEntry[] entries) {
                            if (error != null) {
                                doneLoadChildren(parent, error, null);
                            } else {
                                final List<FileInfo> fileInfos = new ArrayList<FileInfo>(entries.length);
                                for (DirEntry entry : entries) {
                                    FileInfo info = new FileInfo();
                                    info.parent = parent;
                                    String name = entry.filename;
                                    int length = name.length();
                                    if (length > 1 && (name.endsWith("\\") || name.endsWith("/"))) {
                                        name = name.substring(0, length - 1);
                                    }
                                    info.name = name;
                                    info.fullname = entry.longname != null ? entry.longname : entry.filename;
                                    info.isDir = entry.attrs != null ? entry.attrs.isDirectory() : false;
                                    if (!fDirectoriesOnly || info.isDir) {
                                        fileInfos.add(info);
                                    }
                                }
                                doneLoadChildren(parent, null, fileInfos.toArray(new FileInfo[fileInfos.size()]));
                            }
                        }
                    });
                    return;
                }
                fs.opendir(parent.fullname, new IFileSystem.DoneOpen() {
                    final List<FileInfo> fileInfos = new ArrayList<FileInfo>();
                    public void doneOpen(IToken token, FileSystemException error, final IFileHandle handle) {
                        if (error != null) {
                            doneLoadChildren(parent, error, null);
                            return;
                        }
                        fs.readdir(handle, new IFileSystem.DoneReadDir() {
                            public void doneReadDir(IToken token, FileSystemException error, DirEntry[] entries, boolean eof) {
                                if (entries != null) {
                                    for (DirEntry entry : entries) {
                                        FileInfo info = new FileInfo();
                                        info.parent = parent;
                                        info.name = entry.filename;
                                        info.fullname = entry.longname != null ? entry.longname : (new Path(parent.fullname).append(info.name).toString());
                                        info.isDir = entry.attrs != null ? entry.attrs.isDirectory() : false;
                                        if (!fDirectoriesOnly || info.isDir) {
                                            fileInfos.add(info);
                                        }
                                    }
                                }
                                if (error != null || eof) {
                                    fs.close(handle, new IFileSystem.DoneClose() {
                                        public void doneClose(IToken token, FileSystemException error) {
                                            // ignore error
                                        }
                                    });
                                    int size = fileInfos.size();
                                    if (size == 0 && error != null) {
                                        doneLoadChildren(parent, error, null);
                                    } else {
                                        doneLoadChildren(parent, null, fileInfos.toArray(new FileInfo[size]));
                                    }
                                } else {
                                    fs.readdir(handle, this);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void doneLoadChildren(final FileInfo parent, final Throwable error, final FileInfo[] children) {
        assert Protocol.isDispatchThread();
        assert error == null || children == null;
        if (fDisplay == null) return;
        Arrays.sort(children, new Comparator<FileInfo>() {
            public int compare(FileInfo o1, FileInfo o2) {
                if (o1.isDir == o2.isDir)
                    return o1.name.compareTo(o2.name);
                if (o1.isDir) return 1;
                return -1;
            }});
        int i = 0;
        for (FileInfo fileInfo : children) {
            fileInfo.index = i++;
        }
        fDisplay.asyncExec(new Runnable() {
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

    public FileInfo findFileInfo(TreeItem item) {
        assert Thread.currentThread() == fDisplay.getThread();
        if (item == null) return fRootInfo ;
        TreeItem parent = item.getParentItem();
        FileInfo info = findFileInfo(parent);
        if (info == null) return null;
        if (info.children == null) return null;
        if (info.children_error != null) return null;
        int i = parent == null ? fileTree.indexOf(item) : parent.indexOf(item);
        if (i < 0 || i >= info.children.length) return null;
        assert info.children[i].index == i;
        return info.children[i];
    }

    private FileInfo findFileInfo(FileInfo parent, String name) {
        assert Thread.currentThread() == fDisplay.getThread();
        if (name == null) return fRootInfo;
        if (name.equals(parent.name)) return parent;
        FileInfo[] childInfos = parent.children;
        if (childInfos != null) {
            for (FileInfo fileInfo : childInfos) {
                FileInfo found = findFileInfo(fileInfo, name);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    private TreeItem findItem(FileInfo info) {
        if (info == null) return null;
        assert info.parent != null;
        if (info.parent == fRootInfo) {
            int n = fileTree.getItemCount();
            if (info.index >= n) return null;
            return fileTree.getItem(info.index);
        }
        TreeItem i = findItem(info.parent);
        if (i == null) return null;
        int n = i.getItemCount();
        if (info.index >= n) return null;
        return i.getItem(info.index);
    }

    private void fillItem(TreeItem item, FileInfo info) {
        assert Thread.currentThread() == fDisplay.getThread();
        Object data = item.getData("TCFContextInfo");
        if (data != null && data != info) item.removeAll();
        item.setData("TCFContextInfo", info);
        String text = info.name != null ? info.name : info.fullname;
        item.setText(text);
        item.setForeground(fDisplay.getSystemColor(SWT.COLOR_LIST_FOREGROUND));
        item.setImage(getImage(info));
        if (!canHaveChildren(info)) item.setItemCount(0);
        else if (info.children == null || info.children_error != null) item.setItemCount(1);
        else item.setItemCount(info.children.length);
    }

    private boolean canHaveChildren(FileInfo info) {
        return info.isDir || info == fRootInfo;
    }

    private Image getImage(FileInfo info) {
        if (info.isDir) {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
        } else {
            return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
        }
    }

    public void setInitialSelection(final String filename) {
        fPathToSelect = null;
        Protocol.invokeLater(new Runnable() {
            public void run() {
                fFileToSelect = filename;
            }
        });
    }

    public FileInfo getSelection() {
        if (fileTree != null) {
            TreeItem[] items = fileTree.getSelection();
            if (items.length > 0) {
                FileInfo info = findFileInfo(items[0]);
                return info;
            }
        }
        return null;
    }

}
