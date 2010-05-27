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
package org.eclipse.tm.internal.tcf.debug.ui.commands;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.MouseListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFChildrenExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext.SignalMask;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;

class SignalsDialog extends Dialog {

    private static final int
        SIZING_TABLE_WIDTH = 800,
        SIZING_TABLE_HEIGHT = 300;

    private static final String[] column_names = { "Code", "Name", "Description", "Don't stop", "Don't pass", "Pending" };

    private static class Signal extends SignalMask {

        Signal(Map<String,Object> m) {
            props = m;
        }

        Signal(SignalMask m) {
            props = m.getProperties();
            dont_stop = m.isDontStop();
            dont_pass = m.isDontPass();
            pending = m.isPending();
        }

        void setDontStop(boolean b) {
            dont_stop = b;
        }

        void setDontPass(boolean b) {
            dont_pass = b;
        }

        void setPending(boolean b) {
            pending = b;
        }
    }

    private Table signal_table;
    private TableViewer table_viewer;
    private Map<Number,Signal> org_signals;
    private Signal[] cur_signals;

    private final TCFModel model;
    private final IChannel channel;
    private final TCFNode selection;

    private TCFNodeExecContext node;

    private final IStructuredContentProvider content_provider = new IStructuredContentProvider() {

        public Object[] getElements(Object input) {
            return cur_signals;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    };

    private static class SignalLabelProvider extends LabelProvider implements ITableLabelProvider {

        final Image img_rs = ImageCache.getImage("icons/full/elcl16/resume_co.gif");
        final Image img_dl = ImageCache.getImage("icons/full/elcl16/rem_co.gif");
        final Image img_en = ImageCache.getImage("icons/full/elcl16/enabled_co.gif");
        final Image img_ds = ImageCache.getImage("icons/full/elcl16/disabled_co.gif");

        public Image getColumnImage(Object element, int column) {
            SignalMask s = (SignalMask)element;
            switch (column) {
            case 3:
                return s.isDontStop() ? img_rs : img_ds;
            case 4:
                return s.isDontPass() ? img_dl : img_ds;
            case 5:
                return s.isPending() ? img_en : img_ds;
            }
            return null;
        }

        public String getColumnText(Object element, int column) {
            SignalMask s = (SignalMask)element;
            switch (column) {
            case 0:
                long n = s.getCode().longValue();
                if (n < 32) return Long.toString(n);
                String q = Long.toHexString(n);
                if (q.length() < 8) q = "00000000".substring(q.length()) + q;
                return "0x" + q;
            case 1:
                return (String)s.getProperties().get(IProcesses.SIG_NAME);
            case 2:
                return (String)s.getProperties().get(IProcesses.SIG_DESCRIPTION);
            }
            return "";
        }

        public String getText(Object element) {
            return element.toString();
        }
    }

    SignalsDialog(Shell parent, TCFNode node) {
        super(parent);
        model = node.getModel();
        channel = node.getChannel();
        selection = node;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Signals");
        shell.setImage(ImageCache.getImage(ImageCache.IMG_SIGNALS));
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "&OK", true);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite)super.createDialogArea(parent);

        createSignalTable(composite);

        composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return composite;
    }

    private void createSignalTable(Composite parent) {
        Font font = parent.getFont();
        Label props_label = new Label(parent, SWT.WRAP);
        props_label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        props_label.setFont(font);
        props_label.setText("&Signals:");

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));

        signal_table = new Table(composite, SWT.SINGLE | SWT.BORDER |
                SWT.H_SCROLL | SWT.V_SCROLL);
        signal_table.setFont(font);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = SIZING_TABLE_WIDTH;
        data.heightHint = SIZING_TABLE_HEIGHT;
        signal_table.setLayoutData(data);

        int w = SIZING_TABLE_WIDTH / (column_names.length + 5);
        for (int i = 0; i < column_names.length; i++) {
            final TableColumn column = new TableColumn(signal_table, SWT.LEAD, i);
            column.setMoveable(false);
            column.setText(column_names[i]);
            switch (i) {
            case 0:
                column.setWidth(w * 2);
                break;
            case 1:
            case 2:
                column.setWidth(w * 3);
                break;
            default:
                column.setWidth(w);
                break;
            }
        }
        signal_table.setHeaderVisible(true);
        signal_table.setLinesVisible(true);
        signal_table.addMouseListener(new MouseListener() {

            public void mouseDoubleClick(MouseEvent e) {
            }

            public void mouseDown(MouseEvent e) {
                int count = signal_table.getColumnCount();
                for (int row = 0; row < signal_table.getItemCount(); row++) {
                    for (int col = 0; col < count; col++) {
                        TableItem item = signal_table.getItem(row);
                        if (item.getBounds(col).contains(e.x, e.y)) {
                            if (row < 0 || row >= cur_signals.length) break;
                            Signal s = cur_signals[row];
                            switch (col) {
                            case 3:
                                s.setDontStop(!s.isDontStop());
                                break;
                            case 4:
                                s.setDontPass(!s.isDontPass());
                                break;
                            case 5:
                                if (node == null) break;
                                if (s.isPending()) {
                                    // Cannot clear a signal that is already generated
                                    Signal x = org_signals.get(s.getIndex());
                                    if (x != null && x.isPending()) break;
                                }
                                s.setPending(!s.isPending());
                                break;
                            }
                            table_viewer.refresh(s);
                            break;
                        }
                    }
                }
            }

            public void mouseUp(MouseEvent e) {
            }
        });

        table_viewer = new TableViewer(signal_table);
        table_viewer.setUseHashlookup(true);
        table_viewer.setColumnProperties(column_names);

        cur_signals = new TCFTask<Signal[]>(channel) {
            public void run() {
                TCFNode n = selection;
                while (n != null && !(n instanceof TCFNodeExecContext)) n = n.getParent();
                node = (TCFNodeExecContext)n;
                if (node == null) {
                    TCFLaunch launch = selection.getModel().getLaunch();
                    Collection<Map<String,Object>> sigs = launch.getSignalList();
                    if (sigs == null) {
                        done(new Signal[0]);
                    }
                    else {
                        int i = 0;
                        int no_stop = 0;
                        int no_pass = 0;
                        Signal[] arr = new Signal[sigs.size()];
                        try {
                            ILaunchConfiguration cfg = launch.getLaunchConfiguration();
                            String dont_stop = cfg.getAttribute(TCFLaunchDelegate.ATTR_SIGNALS_DONT_STOP, "");
                            String dont_pass = cfg.getAttribute(TCFLaunchDelegate.ATTR_SIGNALS_DONT_PASS, "");
                            if (dont_stop.length() > 0) no_stop = Integer.parseInt(dont_stop, 16);
                            if (dont_pass.length() > 0) no_pass = Integer.parseInt(dont_pass, 16);
                        }
                        catch (Exception x) {
                            Activator.log("Invalid launch cofiguration attribute", x);
                        }
                        for (Map<String,Object> m : sigs) {
                            Signal s = arr[i++] = new Signal(m);
                            Number num = s.getIndex();
                            int j = num == null ? 0 : 1 << num.intValue();
                            s.setDontStop((no_stop & j) != 0);
                            s.setDontPass((no_pass & j) != 0);
                        }
                        done(arr);
                    }
                }
                else {
                    TCFDataCache<SignalMask[]> dc = node.getSignalMask();
                    if (!dc.validate(this)) return;
                    if (dc.getError() != null) {
                        error(dc.getError());
                    }
                    else if (dc.getData() == null) {
                        done(new Signal[0]);
                    }
                    else {
                        int i = 0;
                        Signal[] arr = new Signal[dc.getData().length];
                        for (SignalMask m : dc.getData()) arr[i++] = new Signal(m);
                        done(arr);
                    }
                }
            }
        }.getE();
        org_signals = new HashMap<Number,Signal>();
        for (Signal m : cur_signals) org_signals.put(m.getIndex(), new Signal(m));
        table_viewer.setContentProvider(content_provider);

        table_viewer.setLabelProvider(new SignalLabelProvider());
        table_viewer.setInput(this);
    }

    @Override
    protected void okPressed() {
        try {
            boolean set_mask = false;
            int dont_stop_set = 0;
            int dont_pass_set = 0;
            final LinkedList<Number> send_list = new LinkedList<Number>();
            for (Signal s : cur_signals) {
                Number index = s.getIndex();
                Signal x = org_signals.get(index);
                if (!set_mask) set_mask = x == null || x.isDontStop() != s.isDontStop() || x.isDontPass() != s.isDontPass();
                if (s.isDontStop()) dont_stop_set |= 1 << index.intValue();
                if (s.isDontPass()) dont_pass_set |= 1 << index.intValue();
                if ((x == null || !x.isPending()) && s.isPending()) send_list.add(s.getCode());
            }
            if (set_mask) {
                TCFLaunch launch = selection.getModel().getLaunch();
                ILaunchConfigurationWorkingCopy cfg = launch.getLaunchConfiguration().getWorkingCopy();
                cfg.setAttribute(TCFLaunchDelegate.ATTR_SIGNALS_DONT_STOP, Integer.toHexString(dont_stop_set));
                cfg.setAttribute(TCFLaunchDelegate.ATTR_SIGNALS_DONT_PASS, Integer.toHexString(dont_pass_set));
                cfg.doSave();
                final int dont_stop = dont_stop_set;
                final int dont_pass = dont_pass_set;
                new TCFTask<Boolean>(channel) {
                    final HashSet<IToken> cmds = new HashSet<IToken>();
                    final LinkedList<TCFNodeExecContext> nodes = new LinkedList<TCFNodeExecContext>();
                    TCFDataCache<?> pending;
                    public void run() {
                        nodes.clear();
                        pending = null;
                        addNodes(model.getRootNode().getChildren());
                        if (pending != null) {
                            pending.wait(this);
                        }
                        else {
                            while (nodes.size() > 0) {
                                TCFNodeExecContext exe = nodes.removeFirst();
                                exe.getSignalMask().reset();
                                IProcesses prs = channel.getRemoteService(IProcesses.class);
                                cmds.add(prs.setSignalMask(exe.getID(), dont_stop, dont_pass, new IProcesses.DoneCommand() {
                                    public void doneCommand(IToken token, Exception error) {
                                        cmds.remove(token);
                                        if (isDone()) return;
                                        if (error != null) error(error);
                                        if (cmds.size() == 0) done(Boolean.TRUE);
                                    }
                                }));
                            }
                        }
                    }
                    private void addNodes(TCFChildrenExecContext children) {
                        if (!children.validate()) {
                            pending = children;
                        }
                        else {
                            Map<String,TCFNode> map = children.getData();
                            if (map != null) {
                                for (TCFNode n : map.values()) {
                                    TCFNodeExecContext exe = (TCFNodeExecContext)n;
                                    addNodes(exe.getChildren());
                                    nodes.add(exe);
                                }
                            }
                        }
                    }
                }.getE();
            }
            if (send_list.size() > 0 && node != null) {
                new TCFTask<Boolean>(channel) {
                    public void run() {
                        node.getSignalMask().reset();
                        final IProcesses prs = channel.getRemoteService(IProcesses.class);
                        prs.signal(node.getID(), send_list.removeFirst().longValue(), new IProcesses.DoneCommand() {
                            public void doneCommand(IToken token, Exception error) {
                                if (error != null) {
                                    error(error);
                                }
                                else if (send_list.isEmpty()) {
                                    done(Boolean.TRUE);
                                }
                                else {
                                    node.getSignalMask().reset();
                                    prs.signal(node.getID(), send_list.removeFirst().longValue(), this);
                                }
                            }
                        });
                    }
                }.getE();
            }
        }
        catch (Throwable x) {
            model.showMessageBox("Cannot update signals state", x);
            return;
        }
        super.okPressed();
    }
}
