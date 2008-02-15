/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.debug.tcf.ui.launch;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CLabel;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;

import com.windriver.debug.tcf.core.launch.TCFLaunchDelegate;
import com.windriver.debug.tcf.ui.TCFUI;
import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.api.protocol.IPeer;
import com.windriver.tcf.api.protocol.Protocol;
import com.windriver.tcf.api.services.ILocator;

/**
 * Launch configuration dialog tab to specify the Target Communication Framework
 * configuration.
 */
public class TCFMainTab extends AbstractLaunchConfigurationTab {
    
    private Text peer_id_text;
    private Text program_text;
    private Tree peer_tree;
    private PeerInfo[] peer_info;
    private Display display;

    private final Map<LocatorListener,ILocator> listeners = new HashMap<LocatorListener,ILocator>();
    private final Map<String,Image> image_cache = new HashMap<String,Image>();
    
    private static class PeerInfo {
        PeerInfo parent;
        int index;
        PeerInfo[] children;
        Map<String,String> attrs;
    }
    
    private class LocatorListener implements ILocator.LocatorListener {
        
        private final PeerInfo parent;
        
        LocatorListener(PeerInfo parent) {
            this.parent = parent;
        }

        public void peerAdded(IPeer peer) {
            if (display == null) return;
            final Map<String,String> attrs = new HashMap<String,String>(peer.getAttributes());
            display.asyncExec(new Runnable() {
                public void run() {
                    PeerInfo[] arr = parent == null ? peer_info : parent.children;
                    PeerInfo[] buf = new PeerInfo[arr.length + 1];
                    System.arraycopy(arr, 0, buf, 0, arr.length);
                    PeerInfo info = new PeerInfo();
                    info.parent = parent;
                    info.index = arr.length;
                    info.attrs = attrs;
                    buf[arr.length] = info;
                    if (parent == null) {
                        peer_info = buf;
                    }
                    else {
                        parent.children = buf;
                    }
                    updateItems();
                }
            });
        }

        public void peerChanged(IPeer peer) {
            if (display == null) return;
            final Map<String,String> attrs = new HashMap<String,String>(peer.getAttributes());
            display.asyncExec(new Runnable() {
                public void run() {
                    String id = attrs.get(IPeer.ATTR_ID);
                    PeerInfo[] arr = parent == null ? peer_info : parent.children;
                    for (int i = 0; i < arr.length; i++) {
                        if (arr[i].attrs.get(IPeer.ATTR_ID).equals(id)) {
                            arr[i].attrs = attrs;
                            updateItems();
                        }
                    }
                    assert false;
                }
            });
        }

        public void peerRemoved(final String id) {
            if (display == null) return;
            display.asyncExec(new Runnable() {
                public void run() {
                    PeerInfo[] arr = parent == null ? peer_info : parent.children;
                    PeerInfo[] buf = new PeerInfo[arr.length - 1];
                    int j = 0;
                    for (int i = 0; i < arr.length; i++) {
                        if (!arr[i].attrs.get(IPeer.ATTR_ID).equals(id)) {
                            buf[j++] = arr[i];
                        }
                    }
                    if (parent == null) {
                        peer_info = buf;
                    }
                    else {
                        parent.children = buf;
                    }
                    updateItems();
                }
            });
        }
        
        private void updateItems() {
            PeerInfo[] arr = null;
            TreeItem[] items = null;
            if (parent == null) {
                arr = peer_info;
                peer_tree.setItemCount(arr.length);
                items = peer_tree.getItems();
            }
            else {
                TreeItem item = findItem(parent);
                if (item == null) return;
                arr = parent.children;
                item.setItemCount(arr.length);
                items = item.getItems();
            }
            assert items.length == arr.length;
            for (int i = 0; i < items.length; i++) {
                fillItem(items[i], arr[i]);
            }
            String id = peer_id_text.getText();
            TreeItem item = findItem(findPeerInfo(id));
            if (item != null) peer_tree.setSelection(item);
        }
    }
    
    public void createControl(Composite parent) {
        display = parent.getDisplay();

        Font font = parent.getFont();
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        comp.setLayout(layout);
        comp.setFont(font);

        GridData gd = new GridData(GridData.FILL_BOTH);
        comp.setLayoutData(gd);
        setControl(comp);

        createTargetGroup(comp);
        createProgramGroup(comp);
    }

    private void createTargetGroup(Composite parent) {
        Font font = parent.getFont();
        
        Group group = new Group(parent, SWT.NONE);
        GridLayout top_layout = new GridLayout();
        top_layout.verticalSpacing = 0;
        top_layout.numColumns = 2;
        group.setLayout(top_layout);
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setFont(font);
        group.setText("Target");
        
        createVerticalSpacer(group, top_layout.numColumns);
        
        Label host_label = new Label(group, SWT.NONE);
        host_label.setText("Target ID:");
        host_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        host_label.setFont(font);
        
        peer_id_text = new Text(group, SWT.SINGLE | SWT.BORDER);
        peer_id_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        peer_id_text.setFont(font);
        peer_id_text.setEditable(false);
        peer_id_text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateLaunchConfigurationDialog();
            }
        });

        createVerticalSpacer(group, top_layout.numColumns);

        Label peer_label = new Label(group, SWT.NONE);
        peer_label.setText("Available targets:");
        peer_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        peer_label.setFont(font);
                
        if (peer_info == null) loadPeerInfo(null);
        
        peer_tree = new Tree(group, SWT.VIRTUAL | SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1);
        gd.minimumHeight = 150;
        peer_tree.setLayoutData(gd);
        
        for (int i = 0; i < 5; i++) {
            TreeColumn column = new TreeColumn(peer_tree, SWT.LEAD, i);
            column.setMoveable(true);
            switch (i) {
            case 0:
                column.setText("Name");
                column.setWidth(120);
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
        peer_tree.setItemCount(peer_info.length);
        peer_tree.addListener(SWT.SetData, new Listener() {
            public void handleEvent(Event event) {
                TreeItem item = (TreeItem)event.item;
                PeerInfo info = findPeerInfo(item);
                fillItem(item, info);
            }
        });
        peer_tree.addSelectionListener(new SelectionListener() {
            public void widgetDefaultSelected(SelectionEvent e) {
            }
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] selections = peer_tree.getSelection();
                if (selections.length > 0) {
                    assert selections.length == 1;
                    PeerInfo info = findPeerInfo(selections[0]);
                    peer_id_text.setText(getPath(info));
                }
            }
        });

        createVerticalSpacer(group, top_layout.numColumns);
        
        Button button_test = new Button(group, SWT.PUSH);
        button_test.setText("Run &Diagnostics");
        button_test.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        button_test.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] selection = peer_tree.getSelection();
                if (selection.length > 0) {
                    assert selection.length == 1;
                    runDiagnostics(selection[0], false);
                }
            }
        });

        Button button_loop = new Button(group, SWT.PUSH);
        button_loop.setText("Diagnostics &Loop");
        button_loop.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        button_loop.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                TreeItem[] selection = peer_tree.getSelection();
                if (selection.length > 0) {
                    assert selection.length == 1;
                    runDiagnostics(selection[0], true);
                }
            }
        });
    }

    private void createProgramGroup(Composite parent) {
        display = parent.getDisplay();

        Font font = parent.getFont();
        
        Group group = new Group(parent, SWT.NONE);
        GridLayout top_layout = new GridLayout();
        group.setLayout(top_layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setFont(font);
        group.setText("Program");
        
        program_text = new Text(group, SWT.SINGLE | SWT.BORDER);
        program_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        program_text.setFont(font);
        program_text.addModifyListener(new ModifyListener() {
                        public void modifyText(ModifyEvent e) {
                updateLaunchConfigurationDialog();
                        }
        });
    }
    
    @Override
    public void dispose() {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                for (Iterator<LocatorListener> i = listeners.keySet().iterator(); i.hasNext();) {
                    LocatorListener listener = i.next();
                    listeners.get(listener).removeListener(listener);
                }
                listeners.clear();
                peer_info = null;
                display = null;
            }
        });
        for (Image i : image_cache.values()) i.dispose();
        image_cache.clear();
        super.dispose();
    }

    public String getName() {
        return "Main";
    }
    
    @Override
    public Image getImage() {
        return getImage("icons/tcf.gif");
    }

    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            String id = configuration.getAttribute(
                    TCFLaunchDelegate.ATTR_PEER_ID, (String)null);
            if (id != null) {
                peer_id_text.setText(id);
                TreeItem item = findItem(findPeerInfo(id));
                if (item != null) peer_tree.setSelection(item);
            }
            program_text.setText(configuration.getAttribute(
                    TCFLaunchDelegate.ATTR_PROGRAM_FILE, "")); //$NON-NLS-1$
        }
        catch (CoreException e) {
            setErrorMessage(e.getMessage());
        }
    }

    public boolean isValid(ILaunchConfiguration launchConfig) {
        String id = peer_id_text.getText().trim();
        if (id.length() == 0) {
            setErrorMessage("Specify a target ID");
            return false;
        }
        setErrorMessage(null);
        return super.isValid(launchConfig);
    }

    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        String id = peer_id_text.getText().trim();
        if (id.length() == 0) id = null;
        configuration.setAttribute(TCFLaunchDelegate.ATTR_PEER_ID, id);
        String nm = program_text.getText().trim();
        if (nm.length() == 0) nm = null;
        configuration.setAttribute(TCFLaunchDelegate.ATTR_PROGRAM_FILE, nm);
    }

    public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(TCFLaunchDelegate.ATTR_PEER_ID, "TCFLocal");
        configuration.setAttribute(TCFLaunchDelegate.ATTR_PROGRAM_FILE, (String)null);
    }
    
    private void loadPeerInfo(final PeerInfo parent) {
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                if (parent == null) {
                    ILocator locator = Protocol.getLocator();
                    Map<String,IPeer> map = locator.getPeers();
                    PeerInfo[] buf = new PeerInfo[map.size()];
                    int n = 0;
                    for (Iterator<IPeer> i = map.values().iterator(); i.hasNext();) {
                        IPeer p = i.next();
                        PeerInfo info = new PeerInfo();
                        info.parent = parent;
                        info.index = n;
                        info.attrs = new HashMap<String,String>(p.getAttributes());
                        buf[n++] = info;
                    }
                    LocatorListener listener = new LocatorListener(parent);
                    listeners.put(listener, locator);
                    locator.addListener(listener);
                    assert peer_info == null;
                    peer_info = buf;
                }
                else {
                    PeerInfo[] buf = new PeerInfo[0];
                    assert parent.children == null;
                    parent.children = buf;
                }
            }
        });
    }
    
    private PeerInfo findPeerInfo(TreeItem item) {
        TreeItem parent = item.getParentItem();
        if (parent == null) return peer_info[peer_tree.indexOf(item)];
        PeerInfo info = findPeerInfo(parent);
        if (info.children == null) loadPeerInfo(info);
        return info.children[parent.indexOf(item)];
    }
    
    private PeerInfo findPeerInfo(String path) {
        int i = path.lastIndexOf('/');
        String id = null;
        PeerInfo[] arr = null;
        if (i < 0) {
            if (peer_info == null) loadPeerInfo(null);
            arr = peer_info;
            id = path;
        }
        else {
            PeerInfo p = findPeerInfo(path.substring(0, i));
            if (p == null) return null;
            if (p.children == null) loadPeerInfo(p);
            arr = p.children;
            id = path.substring(i + 1);
        }
        for (int n = 0; n < arr.length; n++) {
            if (arr[n].attrs.get(IPeer.ATTR_ID).equals(id)) return arr[n];
        }
        return null;
    }
    
    private TreeItem findItem(PeerInfo info) {
        if (info == null) return null;
        if (info.parent == null) {
            return peer_tree.getItem(info.index);
        }
        TreeItem i = findItem(info.parent);
        if (i == null) return null;
        peer_tree.showItem(i);
        return i.getItem(info.index);
    }
    
    private interface DoneFindPeer {
        void doneFindPeer(Collection<Throwable> errors, IPeer peer);
    }
    
    private void findPeer(TreeItem item, final DoneFindPeer done) {
        assert display != null;
        assert Thread.currentThread() == display.getThread();
        final String path = getPath(findPeerInfo(item));
        Protocol.invokeLater(new Runnable() {
            public void run() {
                final Collection<Throwable> errors = new ArrayList<Throwable>();
                try {
                    final int i = path.lastIndexOf('/');
                    if (i < 0) {
                        done.doneFindPeer(errors, Protocol.getLocator().getPeers().get(path));
                    }
                    else {
                        openChannel(path.substring(0, i), errors, new DoneOpenChannel() {
                            public void doneOpenChannel(IChannel channel) {
                                IPeer peer = null;
                                if (channel != null) {
                                    ILocator locator = channel.getRemoteService(ILocator.class);
                                    peer = locator.getPeers().get(path.substring(i + 1));
                                    channel.close();
                                }
                                done.doneFindPeer(errors, peer);
                            }
                        });
                    }
                }
                catch (Throwable x) {
                    errors.add(x);
                    done.doneFindPeer(errors, null);
                }
            }
        });
    }
    
    private interface DoneOpenChannel {
        void doneOpenChannel(IChannel channel);
    }
    
    private static class OpenChannelListener implements IChannel.IChannelListener {
        
        private final Collection<Throwable> errors;
        private final IChannel channel;
        private final DoneOpenChannel done;
        
        OpenChannelListener(Collection<Throwable> errors, IChannel channel, DoneOpenChannel done) {
            this.errors = errors;
            this.channel = channel;
            this.done = done;
            channel.addChannelListener(this);
        }
        
        public void onChannelOpened() {
            channel.removeChannelListener(this);
            done.doneOpenChannel(channel);
        }

        public void congestionLevel(int level) {
        }

        public void onChannelClosed(Throwable e) {
            errors.add(e);
            channel.removeChannelListener(this);
            done.doneOpenChannel(null);
        }
    }
    
    private void openChannel(String path, final Collection<Throwable> errors, final DoneOpenChannel done) {
        assert Protocol.isDispatchThread();
        try {
            int i = path.lastIndexOf('/');
            if (i < 0) {
                IPeer peer = Protocol.getLocator().getPeers().get(path);
                if (peer == null) {
                    errors.add(new Exception("Peer not found: " + path));
                    done.doneOpenChannel(null);
                    return;
                }
                new OpenChannelListener(errors, peer.openChannel(), done);
            }
            else {
                final String id = path.substring(i + 1);
                openChannel(path.substring(0, i), errors, new DoneOpenChannel() {
                    public void doneOpenChannel(IChannel channel) {
                        if (errors.size() > 0) {
                            if (channel != null) channel.close();
                            done.doneOpenChannel(null);
                        }
                        else {
                            channel.redirect(id);
                            new OpenChannelListener(errors, channel, done);
                        }
                    }
                });
            }
        }
        catch (Throwable x) {
            errors.add(x);
            done.doneOpenChannel(null);
        }
    }
    
    private void runDiagnostics(TreeItem item, boolean loop) {
        final Shell shell = new Shell(getShell(), SWT.TITLE | SWT.PRIMARY_MODAL);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 0;
        layout.numColumns = 2;
        shell.setLayout(layout);
        shell.setText("Running Diagnostics...");
        CLabel label = new CLabel(shell, SWT.NONE);
        label.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
        label.setText("Running Diagnostics...");
        final TCFSelfTest[] test = new TCFSelfTest[1];
        Button button_cancel = new Button(shell, SWT.PUSH);
        button_cancel.setText("&Cancel");
        button_cancel.setLayoutData(new GridData(GridData.END, GridData.CENTER, false, false));
        button_cancel.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                Protocol.invokeLater(new Runnable() {
                    public void run() {
                        if (test[0] != null) test[0].cancel();
                    }
                });
            }
        });
        createVerticalSpacer(shell, 0);
        ProgressBar bar = new ProgressBar(shell, SWT.HORIZONTAL);
        bar.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false, 2, 1));
        shell.setDefaultButton(button_cancel);
        shell.pack();
        shell.setSize(483, shell.getSize().y);
        Rectangle rc0 = getShell().getBounds();
        Rectangle rc1 = shell.getBounds();
        shell.setLocation(rc0.x + (rc0.width - rc1.width) / 2, rc0.y + (rc0.height - rc1.height) / 2);
        shell.setVisible(true);
        runDiagnostics(item, loop, test, shell, label, bar);
    }
    
    private void runDiagnostics(final TreeItem item, final boolean loop, final TCFSelfTest[] test,
            final Shell shell, final CLabel label, final ProgressBar bar) {
        final TCFSelfTest.TestListener done = new TCFSelfTest.TestListener() {
            private String last_text = "";
            private int last_count = 0;
            private int last_total = 0;
            public void progress(final String label_text, final int count_done, final int count_total) {
                if ((label_text == null || last_text.equals(label_text)) &&
                        last_total == count_total &&
                        (count_done - last_count) / (float)count_total < 0.02f) return;
                if (label_text != null) last_text = label_text;
                last_total = count_total;
                last_count = count_done;
                display.asyncExec(new Runnable() {
                    public void run() {
                        label.setText(last_text);
                        bar.setMinimum(0);
                        bar.setMaximum(last_total);
                        bar.setSelection(last_count);
                    }
                });
            }
            public void done(final Collection<Throwable> errors) {
                final boolean b = test[0] == null ? false : test[0].isCanceled();
                test[0] = null;
                display.asyncExec(new Runnable() {
                    public void run() {
                        if (errors.size() > 0) {
                            shell.dispose();
                            new TestErrorsDialog(getControl().getShell(),
                                    getImage("icons/tcf.gif"), errors).open();
                        }
                        else if (loop && !b) {
                            runDiagnostics(item, true, test, shell, label, bar);
                        }
                        else {
                            shell.dispose();
                        }
                    }
                });
            }
        };
        findPeer(item, new DoneFindPeer() {
            public void doneFindPeer(Collection<Throwable> errors, IPeer peer) {
                if (errors.size() > 0) {
                    done.done(errors);
                }
                else {
                    try {
                        test[0] = new TCFSelfTest(peer, done);
                    }
                    catch (Throwable x) {
                        errors.add(x);
                        done.done(errors);
                    }
                }
            }
        });
    }
    
    private void fillItem(TreeItem item, PeerInfo info) {
        String text[] = new String[5];
        text[0] = info.attrs.get(IPeer.ATTR_NAME);
        text[1] = info.attrs.get(IPeer.ATTR_OS_NAME);
        text[2] = info.attrs.get(IPeer.ATTR_TRANSPORT_NAME);
        text[3] = info.attrs.get(IPeer.ATTR_IP_HOST);
        text[4] = info.attrs.get(IPeer.ATTR_IP_PORT);
        item.setText(text);
        item.setImage(getImage(getImageName(info)));
        if (info.children == null) loadPeerInfo(info);
        item.setItemCount(info.children.length);
    }
    
    private String getPath(PeerInfo info) {
        String id = info.attrs.get(IPeer.ATTR_ID);
        if (info.parent == null) return id;
        return getPath(info.parent) + "/" + id;
    }
    
    private Image getImage(String name) {
        if (name == null) return null;
        if (display == null) return null;
        Image image = image_cache.get(name);
        if (image == null) {
            URL url = FileLocator.find(TCFUI.getDefault().getBundle(), new Path(name), null);
            ImageDescriptor descriptor = null;
            if (url == null) {
                descriptor = ImageDescriptor.getMissingImageDescriptor();
            }
            else {
                descriptor = ImageDescriptor.createFromURL(url);
            }
            image = descriptor.createImage(display);
            image_cache.put(name, image);
        }
        return image;
    }

    private String getImageName(PeerInfo info) {
        return "icons/tcf.gif";
    }
}
