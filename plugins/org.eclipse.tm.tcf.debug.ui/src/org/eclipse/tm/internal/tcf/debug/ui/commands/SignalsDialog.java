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
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IProcesses;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;

class SignalsDialog extends Dialog {

    private static final int 
        SIZING_TABLE_WIDTH = 400,
        SIZING_TABLE_HEIGHT = 300;

    private static final String[] column_names = { "Code", "Name", "Description", "Intercept", "Ignore", "Sent" };
    
    private Table signal_table;
    private TableViewer table_viewer;
    
    private final IChannel channel;
    private final TCFDataCache<Collection<Map<String,Object>>> signal_list;
    private final TCFDataCache<Signal[]> signal_state;
    
    private class Signal {
        Map<String,Object> attrs;
        boolean intercept;
        boolean ignore;
        boolean sent;
    }
    
    private class SignalLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int column) {
            return null;
        }

        public String getColumnText(Object element, int column) {
            Signal s = (Signal)element;
            switch (column) {
            case 0:
                return s.attrs.get(IProcesses.SIG_CODE).toString(); 
            case 1:
                return (String)s.attrs.get(IProcesses.SIG_NAME); 
            case 2:
                return (String)s.attrs.get(IProcesses.SIG_DESCRIPTION);
            case 3:
                return s.intercept ? "yes" : "";
            case 4:
                return s.ignore ? "yes" : "";
            case 5:
                return s.sent ? "yes" : "";
            }
            return "";
        }
        
        public String getText(Object element) {
            TableColumn column = signal_table.getSortColumn();
            if (column == null) return "";
            return getColumnText(element, signal_table.indexOf(column));
        }
    }

    SignalsDialog(Shell parent, final TCFNode node) {
        super(parent);
        channel = new TCFTask<IChannel>() {

            public void run() {
                done(node.getModel().getLaunch().getChannel());
            }
        }.getE();
        signal_list = new TCFTask<TCFDataCache<Collection<Map<String,Object>>>>(channel) {

            public void run() {
                done(new TCFDataCache<Collection<Map<String,Object>>>(channel) {

                    @Override
                    protected boolean startDataRetrieval() {
                        TCFNode n = node;
                        IProcesses.ProcessContext prs = null;
                        while (n != null) {
                            if (n instanceof TCFNodeExecContext) {
                                TCFDataCache<IProcesses.ProcessContext> cache = ((TCFNodeExecContext)n).getProcessContext();
                                if (!cache.validate()) {
                                    cache.wait(this);
                                    return false;
                                }
                                prs = cache.getData();
                                if (prs != null) break;
                            }
                            n = n.getParent();
                        }
                        if (prs == null) {
                            set(null, null, null);
                            return true;
                        }
                        command = prs.getSignalList(new IProcesses.DoneGetSignalList() {
                            public void doneGetSignalList(IToken token, Exception error, Collection<Map<String, Object>> list) {
                                set(token, error, list);
                            }
                        });
                        return false;
                    }
                });
            }
        }.getE();
        signal_state = new TCFTask<TCFDataCache<Signal[]>>(channel) {

            public void run() {
                done(new TCFDataCache<Signal[]>(channel) {

                    @Override
                    protected boolean startDataRetrieval() {
                        TCFNode n = node;
                        IProcesses.ProcessContext prs = null;
                        while (n != null) {
                            if (n instanceof TCFNodeExecContext) {
                                TCFDataCache<IProcesses.ProcessContext> cache = ((TCFNodeExecContext)n).getProcessContext();
                                if (!cache.validate()) {
                                    cache.wait(this);
                                    return false;
                                }
                                prs = cache.getData();
                                if (prs != null) break;
                            }
                            n = n.getParent();
                        }
                        if (!signal_list.validate()) {
                            signal_list.wait(this);
                            return false;
                        }
                        final Collection<Map<String,Object>> sigs = signal_list.getData();
                        if (sigs == null || prs == null) {
                            set(null, null, null);
                            return true;
                        }
                        command = prs.getSignalMask(new IProcesses.DoneGetSignalMask() {
                            public void doneGetSignalMask(IToken token, Exception error, int intercept, int ignore) {
                                int n = 0;
                                Signal[] res = new Signal[sigs.size()];
                                for (Map<String,Object> m : sigs) {
                                    Signal s = res[n++] = new Signal();
                                    int code = 1 << ((Number)m.get(IProcesses.SIG_CODE)).intValue();
                                    s.attrs = m;
                                    s.intercept = (intercept & code) != 0;
                                    s.ignore = (ignore & code) != 0;
                                }
                                set(token, error, res);
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
        new TCFTask<Boolean>(channel) {

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
                SWT.H_SCROLL | SWT.V_SCROLL | 
                SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
        signal_table.setFont(font);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = SIZING_TABLE_WIDTH;
        data.heightHint = SIZING_TABLE_HEIGHT;
        signal_table.setLayoutData(data);
        
        for (int i = 0; i < column_names.length; i++) {
            final TableColumn column = new TableColumn(signal_table, SWT.LEAD, i);
            column.setMoveable(false);
            column.setText(column_names[i]);
            column.setWidth(SIZING_TABLE_WIDTH / column_names.length);
        }
        signal_table.setHeaderVisible(true);
        signal_table.setLinesVisible(true);
        
        table_viewer = new TableViewer(signal_table);    
        table_viewer.setUseHashlookup(true);
        table_viewer.setColumnProperties(column_names);
   
        table_viewer.setContentProvider(new IStructuredContentProvider() {

            public Object[] getElements(Object input) {
                return new TCFTask<Signal[]>(channel) {

                    public void run() {
                        if (!signal_state.validate()) {
                            signal_state.wait(this);
                            return;
                        }
                        if (signal_state.getError() != null) error(signal_state.getError());
                        else done(signal_state.getData());
                    }
                }.getE();
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        });
        
        table_viewer.setLabelProvider(new SignalLabelProvider());
        table_viewer.setInput(this);
    }
}
