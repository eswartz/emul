/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.commands;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;
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
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IMemoryMap;
import org.eclipse.tm.tcf.services.IMemoryMap.MemoryRegion;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;

class MemoryMapDialog extends Dialog {

    private static final int
        SIZING_TABLE_WIDTH = 800,
        SIZING_TABLE_HEIGHT = 300;

    private static final String[] column_names = {
        "Address",
        "Size",
        "Flags",
        "File Offset",
        "File Name"
    };

    private final TCFModel model;
    private final IChannel channel;
    private final TCFNode selection;

    private Table map_table;
    private TableViewer table_viewer;
    private IMemoryMap.MemoryRegion[] org_map;
    private IMemoryMap.MemoryRegion[] cur_map;

    private TCFNodeExecContext node;
    private IMemory.MemoryContext mem_ctx;

    private static class Region implements MemoryRegion, Comparable<Region> {

        final Map<String,Object> props;
        final BigInteger addr;
        final BigInteger size;

        Region(Map<String,Object> props) {
            this.props = props;
            Number addr = (Number)props.get(IMemoryMap.PROP_ADDRESS);
            Number size = (Number)props.get(IMemoryMap.PROP_SIZE);
            this.addr = addr instanceof BigInteger ? (BigInteger)addr : new BigInteger(addr.toString());
            this.size = size instanceof BigInteger ? (BigInteger)size : new BigInteger(size.toString());
        }

        public Number getAddress() {
            return addr;
        }

        public Number getSize() {
            return size;
        }

        public Number getOffset() {
            return (Number)props.get(IMemoryMap.PROP_OFFSET);
        }

        public String getFileName() {
            return (String)props.get(IMemoryMap.PROP_FILE_NAME);
        }

        public int getFlags() {
            Number n = (Number)props.get(IMemoryMap.PROP_FLAGS);
            if (n != null) return n.intValue();
            return 0;
        }

        public Map<String,Object> getProperties() {
            return props;
        }

        public int compareTo(Region r) {
            return addr.compareTo(r.addr);
        }
    }

    private final IStructuredContentProvider content_provider = new IStructuredContentProvider() {

        public Object[] getElements(Object input) {
            return cur_map;
        }

        public void dispose() {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
        }
    };

    private class MapLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int column) {
            return null;
        }

        public String getColumnText(Object element, int column) {
            Region r = (Region)element;
            switch (column) {
            case 0:
            case 1:
                {
                    BigInteger x = column == 0 ? r.addr : r.size;
                    String s = x.toString(16);
                    int sz = mem_ctx.getAddressSize() * 2;
                    int l = sz - s.length();
                    if (l < 0) l = 0;
                    if (l > 16) l = 16;
                    return "0x0000000000000000".substring(0, 2 + l) + s;
                }
            case 2:
                {
                    int n = r.getFlags();
                    StringBuffer bf = new StringBuffer();
                    if ((n & IMemoryMap.FLAG_READ) != 0) bf.append('r');
                    if ((n & IMemoryMap.FLAG_WRITE) != 0) bf.append('w');
                    if ((n & IMemoryMap.FLAG_EXECUTE) != 0) bf.append('x');
                    return bf.toString();
                }
            case 3:
                {
                    Number n = r.getOffset();
                    if (n == null) return "";
                    BigInteger x = n instanceof BigInteger ? (BigInteger)n : new BigInteger(n.toString());
                    String s = x.toString(16);
                    int l = 16 - s.length();
                    if (l < 0) l = 0;
                    if (l > 16) l = 16;
                    return "0x0000000000000000".substring(0, 2 + l) + s;
                }
            case 4:
                return r.getFileName();
            }
            return "";
        }

        public String getText(Object element) {
            return element.toString();
        }
    }

    MemoryMapDialog(Shell parent, TCFNode node) {
        super(parent);
        model = node.getModel();
        channel = node.getChannel();
        selection = node;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Memory Map");
        shell.setImage(ImageCache.getImage(ImageCache.IMG_SIGNALS));
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "&OK", true);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite)super.createDialogArea(parent);

        createMemoryMapTable(composite);

        composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return composite;
    }

    private void createMemoryMapTable(Composite parent) {
        Font font = parent.getFont();
        Label props_label = new Label(parent, SWT.WRAP);
        props_label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        props_label.setFont(font);
        props_label.setText("&Memory Map:");

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));

        map_table = new Table(composite,
                SWT.SINGLE | SWT.BORDER |
                SWT.H_SCROLL | SWT.V_SCROLL);
        map_table.setFont(font);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = SIZING_TABLE_WIDTH;
        data.heightHint = SIZING_TABLE_HEIGHT;
        map_table.setLayoutData(data);

        int w = SIZING_TABLE_WIDTH / (column_names.length + 5);
        for (int i = 0; i < column_names.length; i++) {
            final TableColumn column = new TableColumn(map_table, SWT.LEAD, i);
            column.setMoveable(false);
            column.setText(column_names[i]);
            switch (i) {
            case 0:
            case 1:
            case 3:
                column.setWidth(w * 2);
                break;
            case 4:
                column.setWidth(w * 4);
                break;
            default:
                column.setWidth(w);
                break;
            }
        }
        map_table.setHeaderVisible(true);
        map_table.setLinesVisible(true);
        map_table.addMouseListener(new MouseListener() {

            public void mouseDoubleClick(MouseEvent e) {
            }

            public void mouseDown(MouseEvent e) {
            }

            public void mouseUp(MouseEvent e) {
            }
        });

        table_viewer = new TableViewer(map_table);
        table_viewer.setUseHashlookup(true);
        table_viewer.setColumnProperties(column_names);

        cur_map = new TCFTask<IMemoryMap.MemoryRegion[]>(channel) {
            public void run() {
                TCFNode n = selection;
                while (n != null) {
                    if (n instanceof TCFNodeExecContext) {
                        TCFDataCache<IMemory.MemoryContext> dc = ((TCFNodeExecContext)n).getMemoryContext();
                        if (!dc.validate(this)) return;
                        if (dc.getError() != null) {
                            error(dc.getError());
                            return;
                        }
                        if (dc.getData() != null) {
                            mem_ctx = dc.getData();
                            break;
                        }
                    }
                    n = n.getParent();
                }
                node = (TCFNodeExecContext)n;
                ArrayList<IMemoryMap.MemoryRegion> lst = new ArrayList<IMemoryMap.MemoryRegion>();
                if (node != null) {
                    TCFDataCache<TCFNodeExecContext.MemoryRegion[]> dc = node.getMemoryMap();
                    if (!dc.validate(this)) return;
                    if (dc.getError() != null) {
                        error(dc.getError());
                        return;
                    }
                    if (dc.getData() != null) {
                        for (TCFNodeExecContext.MemoryRegion m : dc.getData()) {
                            lst.add(new Region(m.region.getProperties()));
                        }
                    }
                }
                try {
                    TCFLaunch launch = model.getLaunch();
                    ILaunchConfiguration cfg = launch.getLaunchConfiguration();
                    String map = cfg.getAttribute(TCFLaunchDelegate.ATTR_MEMORY_MAP, "");
                    if (map.length() > 0) {
                        JSON.parseOne(map.getBytes("UTF-8"));
                    }
                }
                catch (Throwable x) {
                    Activator.log("Invalid launch cofiguration attribute", x);
                }
                done(lst.toArray(new IMemoryMap.MemoryRegion[lst.size()]));
            }
        }.getE();
        Arrays.sort(cur_map);
        org_map = new IMemoryMap.MemoryRegion[cur_map.length];
        System.arraycopy(cur_map, 0, org_map, 0, cur_map.length);
        table_viewer.setContentProvider(content_provider);

        table_viewer.setLabelProvider(new MapLabelProvider());
        table_viewer.setInput(this);
    }
}
