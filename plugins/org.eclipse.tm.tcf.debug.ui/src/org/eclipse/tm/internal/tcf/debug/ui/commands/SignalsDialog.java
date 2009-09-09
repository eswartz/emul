/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
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
import java.util.LinkedList;
import java.util.Map;

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
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
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

    private Table signal_table;
    private TableViewer table_viewer;
    private Map<Number,Signal> org_signals;

    private final TCFModel model;
    private final IChannel channel;
    private final TCFDataCache<SignalList> signal_list;
    private final TCFDataCache<SignalState> signal_state;

    private static class SignalList {
        String context_id;
        Collection<Map<String,Object>> list;
    }

    private static class SignalState {
        String context_id;
        Signal[] list;
    }

    private class Signal implements Cloneable {
        Map<String,Object> attrs;
        boolean dont_stop;
        boolean dont_pass;
        boolean pending;

        public Signal copy() {
            try {
                return (Signal)clone();
            }
            catch (CloneNotSupportedException e) {
                throw new Error(e);
            }
        }

        @Override
        public String toString() {
            StringBuffer bf = new StringBuffer();
            bf.append("[attrs=");
            bf.append(attrs.toString());
            if (dont_stop) bf.append(",don't stop");
            if (dont_pass) bf.append(",don't pass");
            if (pending) bf.append(",pending");
            bf.append(']');
            return bf.toString();
        }
    }

    private final IStructuredContentProvider content_provider = new IStructuredContentProvider() {

        public Object[] getElements(Object input) {
            return new TCFTask<Signal[]>(channel) {

                public void run() {
                    if (!signal_state.validate(this)) return;
                    if (signal_state.getError() != null) error(signal_state.getError());
                    else done(signal_state.getData().list);
                }
            }.getE();
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
            Signal s = (Signal)element;
            switch (column) {
            case 3:
                return s.dont_stop ? img_rs : img_ds;
            case 4:
                return s.dont_pass ? img_dl : img_ds;
            case 5:
                return s.pending ? img_en : img_ds;
            }
            return null;
        }

        public String getColumnText(Object element, int column) {
            Signal s = (Signal)element;
            switch (column) {
            case 0:
                long n = ((Number)s.attrs.get(IProcesses.SIG_CODE)).longValue();
                if (n < 32) return Long.toString(n);
                String q = Long.toHexString(n);
                if (q.length() < 8) q = "00000000".substring(q.length()) + q;
                return "0x" + q;
            case 1:
                return (String)s.attrs.get(IProcesses.SIG_NAME);
            case 2:
                return (String)s.attrs.get(IProcesses.SIG_DESCRIPTION);
            }
            return "";
        }

        public String getText(Object element) {
            return element.toString();
        }
    }

    SignalsDialog(Shell parent, final TCFNode node) {
        super(parent);
        model = node.getModel();
        channel = new TCFTask<IChannel>() {

            public void run() {
                done(model.getLaunch().getChannel());
            }
        }.getE();
        signal_list = new TCFTask<TCFDataCache<SignalList>>(channel) {

            public void run() {
                done(new TCFDataCache<SignalList>(channel) {

                    @Override
                    protected boolean startDataRetrieval() {
                        IProcesses prs = channel.getRemoteService(IProcesses.class);
                        TCFNode n = node;
                        while (n != null) {
                            if (n instanceof TCFNodeExecContext) break;
                            n = n.getParent();
                        }
                        if (n == null || prs == null) {
                            set(null, null, null);
                            return true;
                        }
                        final SignalList l = new SignalList();
                        l.context_id = n.getID();
                        command = prs.getSignalList(l.context_id, new IProcesses.DoneGetSignalList() {
                            public void doneGetSignalList(IToken token, Exception error, Collection<Map<String, Object>> list) {
                                l.list = list;
                                set(token, error, l);
                            }
                        });
                        return false;
                    }
                });
            }
        }.getE();
        signal_state = new TCFTask<TCFDataCache<SignalState>>(channel) {

            public void run() {
                done(new TCFDataCache<SignalState>(channel) {

                    @Override
                    protected boolean startDataRetrieval() {
                        if (!signal_list.validate(this)) return false;
                        IProcesses prs = channel.getRemoteService(IProcesses.class);
                        final SignalList sigs = signal_list.getData();
                        if (prs == null || sigs == null) {
                            set(null, null, null);
                            return true;
                        }
                        command = prs.getSignalMask(sigs.context_id, new IProcesses.DoneGetSignalMask() {
                            public void doneGetSignalMask(IToken token, Exception error, int dont_stop, int dont_pass, int pending) {
                                int n = 0;
                                Signal[] list = new Signal[sigs.list.size()];
                                for (Map<String,Object> m : sigs.list) {
                                    Signal s = list[n++] = new Signal();
                                    int mask = 1 << ((Number)m.get(IProcesses.SIG_INDEX)).intValue();
                                    s.attrs = m;
                                    s.dont_stop = (dont_stop & mask) != 0;
                                    s.dont_pass = (dont_pass & mask) != 0;
                                    s.pending = (pending & mask) != 0;
                                }
                                SignalState state = new SignalState();
                                state.context_id = sigs.context_id;
                                state.list = list;
                                set(token, error, state);
                            }
                        });
                        return false;
                    }
                });
            }
        }.getE();
    }

    @Override
    public boolean close() {
        new TCFTask<Boolean>() {

            public void run() {
                signal_list.reset(null);
                done(Boolean.TRUE);
            }
        }.getE();
        return super.close();
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
                int count = signal_table.getColumnCount();
                for (int row = 0; row < signal_table.getItemCount(); row++) {
                    for (int col = 0; col < count; col++) {
                        TableItem item = signal_table.getItem(row);
                        if (item.getBounds(col).contains(e.x, e.y)) {
                            Object[] arr = content_provider.getElements(null);
                            if (arr == null || row < 0 || row >= arr.length) break;
                            Signal s = (Signal)arr[row];
                            switch (col) {
                            case 3:
                                s.dont_stop = !s.dont_stop;
                                break;
                            case 4:
                                s.dont_pass = !s.dont_pass;
                                break;
                            case 5:
                                if (s.pending) {
                                    // Cannot clear a signal that is already generated
                                    Number code = (Number)s.attrs.get(IProcesses.SIG_INDEX);
                                    Signal x = org_signals.get(code);
                                    if (x != null && x.pending) break;
                                }
                                s.pending = !s.pending;
                                break;
                            }
                            table_viewer.refresh(s);
                            break;
                        }
                    }
                }
            }

            public void mouseDown(MouseEvent e) {
            }

            public void mouseUp(MouseEvent e) {
            }
        });

        table_viewer = new TableViewer(signal_table);
        table_viewer.setUseHashlookup(true);
        table_viewer.setColumnProperties(column_names);

        table_viewer.setContentProvider(content_provider);

        table_viewer.setLabelProvider(new SignalLabelProvider());
        table_viewer.setInput(this);

        org_signals = new HashMap<Number,Signal>();
        for (Signal s : (Signal[])content_provider.getElements(null)) {
            org_signals.put((Number)s.attrs.get(IProcesses.SIG_INDEX), s.copy());
        }
    }

    @Override
    protected void okPressed() {
        try {
            final SignalState state = new TCFTask<SignalState>(channel) {
                public void run() {
                    if (!signal_state.validate(this)) return;
                    if (signal_state.getError() != null) error(signal_state.getError());
                    else done(signal_state.getData());
                }
            }.getE();

            if (state != null && state.list != null) {
                boolean set_mask = false;
                int dont_stop_set = 0;
                int dont_pass_set = 0;
                final LinkedList<Number> send_list = new LinkedList<Number>();
                for (Signal s : state.list) {
                    Number index = (Number)s.attrs.get(IProcesses.SIG_INDEX);
                    Signal x = org_signals.get(index);
                    if (!set_mask) set_mask = x == null || x.dont_stop != s.dont_stop || x.dont_pass != s.dont_pass;
                    if (s.dont_stop) dont_stop_set |= 1 << index.intValue();
                    if (s.dont_pass) dont_pass_set |= 1 << index.intValue();
                    if ((x == null || !x.pending) && s.pending) send_list.add((Number)s.attrs.get(IProcesses.SIG_CODE));
                }
                if (set_mask) {
                    final int dont_stop = dont_stop_set;
                    final int dont_pass = dont_pass_set;
                    new TCFTask<Boolean>(channel) {
                        public void run() {
                            IProcesses prs = channel.getRemoteService(IProcesses.class);
                            prs.setSignalMask(state.context_id, dont_stop, dont_pass, new IProcesses.DoneCommand() {
                                public void doneCommand(IToken token, Exception error) {
                                    if (error != null) error(error);
                                    else done(Boolean.TRUE);
                                }
                            });
                        }
                    }.getE();
                }
                if (send_list.size() > 0) {
                    new TCFTask<Boolean>(channel) {
                        public void run() {
                            final IProcesses prs = channel.getRemoteService(IProcesses.class);
                            prs.signal(state.context_id, send_list.removeFirst().longValue(), new IProcesses.DoneCommand() {
                                public void doneCommand(IToken token, Exception error) {
                                    if (error != null) {
                                        error(error);
                                    }
                                    else if (send_list.isEmpty()) {
                                        done(Boolean.TRUE);
                                    }
                                    else {
                                        prs.signal(state.context_id, send_list.removeFirst().longValue(), this);
                                    }
                                }
                            });
                        }
                    }.getE();
                }
            }
        }
        catch (Throwable x) {
            model.showMessageBox("Cannot update signals state", x);
            return;
        }
        super.okPressed();
    }
}
