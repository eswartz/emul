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
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;
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
import org.eclipse.tm.tcf.protocol.IToken;
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

    private static final String PROP_ID = "ID";

    private final TCFModel model;
    private final IChannel channel;
    private final TCFNode selection;

    private Table map_table;
    private TableViewer table_viewer;
    private Runnable update_map_buttons;
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
            this.addr = addr == null || addr instanceof BigInteger ? (BigInteger)addr : new BigInteger(addr.toString());
            this.size = size == null || size instanceof BigInteger ? (BigInteger)size : new BigInteger(size.toString());
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
            if (addr == null && r.addr == null) return 0;
            if (addr == null) return -1;
            if (r.addr == null) return +1;
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

    private class MapLabelProvider extends LabelProvider implements ITableLabelProvider, ITableColorProvider {

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
                    if (x == null) return "";
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

        public Color getBackground(Object element, int columnIndex) {
            return map_table.getBackground();
        }

        public Color getForeground(Object element, int columnIndex) {
            Region r = (Region)element;
            if (r.getProperties().get(PROP_ID) != null) {
                return map_table.getDisplay().getSystemColor(SWT.COLOR_BLUE);
            }
            return map_table.getForeground();
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
        shell.setImage(ImageCache.getImage(ImageCache.IMG_MEMORY_MAP));
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
                SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION |
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
        map_table.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                IMemoryMap.MemoryRegion r = (IMemoryMap.MemoryRegion)((IStructuredSelection)
                        table_viewer.getSelection()).getFirstElement();
                if (r == null) return;
                Image image = ImageCache.getImage(ImageCache.IMG_MEMORY_MAP);
                new MemoryMapItemDialog(getShell(), image, r.getProperties(), false).open();
            }
            @Override
            public void widgetSelected(SelectionEvent e) {
                update_map_buttons.run();
            }
        });

        table_viewer = new TableViewer(map_table);
        table_viewer.setUseHashlookup(true);
        table_viewer.setColumnProperties(column_names);

        cur_map = new TCFTask<IMemoryMap.MemoryRegion[]>(channel) {
            @SuppressWarnings("unchecked")
            public void run() {
                TCFDataCache<TCFNodeExecContext> mem_cache = model.searchMemoryContext(selection);
                if (!mem_cache.validate(this)) return;
                if (mem_cache.getError() != null) {
                    error(mem_cache.getError());
                    return;
                }
                node = mem_cache.getData();
                mem_ctx = node.getMemoryContext().getData();
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
                            Map<String,Object> props = m.region.getProperties();
                            if (props.get(PROP_ID) != null) continue;
                            lst.add(new Region(props));
                        }
                    }
                }
                try {
                    TCFLaunch launch = model.getLaunch();
                    ILaunchConfiguration cfg = launch.getLaunchConfiguration();
                    String map = cfg.getAttribute(TCFLaunchDelegate.ATTR_MEMORY_MAP, "");
                    if (map.length() > 0) {
                        Object o = JSON.parseOne(map.getBytes("UTF-8"));
                        if (o != null) {
                            for (Object x : (Collection<Object>)o) {
                                Map<String,Object> props = (Map<String,Object>)x;
                                if (props.get(PROP_ID) == null) continue;
                                lst.add(new Region(props));
                            }
                        }
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

        createMapButtons(composite);
    }

    private void createMapButtons(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));
        Menu menu = new Menu(map_table);
        SelectionAdapter sel_adapter = null;

        final Button button_add = new Button(composite, SWT.PUSH);
        button_add.setText("&Add...");
        button_add.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button_add.addSelectionListener(sel_adapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Map<String,Object> attrs = new HashMap<String,Object>();
                Image image = ImageCache.getImage(ImageCache.IMG_MEMORY_MAP);
                if (new MemoryMapItemDialog(getShell(), image, attrs, true).open() == OK) {
                    attrs.put(PROP_ID, Long.toString(System.currentTimeMillis()));
                    IMemoryMap.MemoryRegion[] arr = new IMemoryMap.MemoryRegion[cur_map.length + 1];
                    System.arraycopy(cur_map, 0, arr, 0, cur_map.length);
                    Region r = new Region(attrs);
                    arr[cur_map.length] = r;
                    Arrays.sort(arr);
                    cur_map = arr;
                    table_viewer.refresh();
                }
            }
        });
        final MenuItem item_add = new MenuItem(menu, SWT.PUSH);
        item_add.setText("&Add...");
        item_add.addSelectionListener(sel_adapter);

        final Button button_edit = new Button(composite, SWT.PUSH);
        button_edit.setText("E&dit...");
        button_edit.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button_edit.addSelectionListener(sel_adapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IMemoryMap.MemoryRegion r = (IMemoryMap.MemoryRegion)((IStructuredSelection)
                        table_viewer.getSelection()).getFirstElement();
                if (r == null) return;
                Image image = ImageCache.getImage(ImageCache.IMG_MEMORY_MAP);
                new MemoryMapItemDialog(getShell(), image, r.getProperties(), false).open();
            }
        });
        final MenuItem item_edit = new MenuItem(menu, SWT.PUSH);
        item_edit.setText("E&dit...");
        item_edit.addSelectionListener(sel_adapter);

        final Button button_remove = new Button(composite, SWT.PUSH);
        button_remove.setText("&Remove");
        button_remove.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button_remove.addSelectionListener(sel_adapter = new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                IMemoryMap.MemoryRegion r = (IMemoryMap.MemoryRegion)((IStructuredSelection)
                        table_viewer.getSelection()).getFirstElement();
                if (r == null) return;
                for (int n = 0; n < cur_map.length; n++) {
                    if (cur_map[n] == r) {
                        IMemoryMap.MemoryRegion[] arr = new IMemoryMap.MemoryRegion[cur_map.length - 1];
                        System.arraycopy(cur_map, 0, arr, 0, n);
                        System.arraycopy(cur_map, n + 1, arr, n, arr.length - n);
                        cur_map = arr;
                        table_viewer.refresh();
                        return;
                    }
                }
            }
        });
        final MenuItem item_remove = new MenuItem(menu, SWT.PUSH);
        item_remove.setText("&Remove");
        item_remove.addSelectionListener(sel_adapter);

        map_table.setMenu(menu);

        update_map_buttons = new Runnable() {
            public void run() {
                IMemoryMap.MemoryRegion r = (IMemoryMap.MemoryRegion)((IStructuredSelection)
                        table_viewer.getSelection()).getFirstElement();
                boolean manual = r != null && r.getProperties().get(PROP_ID) != null;
                button_edit.setEnabled(r != null);
                button_remove.setEnabled(manual);
                item_edit.setEnabled(r != null);
                item_remove.setEnabled(manual);
            }
        };
        update_map_buttons.run();
    }

    @Override
    protected void okPressed() {
        if (!Arrays.equals(org_map, cur_map)) {
            try {
                final ArrayList<IMemoryMap.MemoryRegion> lst = new ArrayList<MemoryRegion>();
                for (IMemoryMap.MemoryRegion r : cur_map) {
                    if (r.getProperties().get(PROP_ID) != null) lst.add(r);
                }
                new TCFTask<Boolean>(channel) {
                    public void run() {
                        IMemoryMap.MemoryRegion[] map = lst.toArray(new IMemoryMap.MemoryRegion[lst.size()]);
                        try {
                            TCFLaunch launch = model.getLaunch();
                            ILaunchConfigurationWorkingCopy cfg = launch.getLaunchConfiguration().getWorkingCopy();
                            cfg.setAttribute(TCFLaunchDelegate.ATTR_MEMORY_MAP, JSON.toJSON(map));
                            cfg.doSave();
                        }
                        catch (Throwable x) {
                            error(x);
                            return;
                        }
                        IMemoryMap mm = channel.getRemoteService(IMemoryMap.class);
                        if (mm == null) {
                            error(new Exception("Target does not provide Memory Map service"));
                            return;
                        }
                        mm.set(mem_ctx.getID(), map, new IMemoryMap.DoneSet() {
                            public void doneSet(IToken token, Exception error) {
                                if (error != null) {
                                    error(error);
                                }
                                else {
                                    done(null);
                                }
                            }
                        });
                    }
                }.getE();
            }
            catch (Throwable x) {
                MessageBox mb = new MessageBox(getShell(), SWT.ICON_ERROR | SWT.OK);
                mb.setText("Cannot update target memory map");
                mb.setMessage(TCFModel.getErrorMessage(x, true));
                mb.open();
                return;
            }
        }
        super.okPressed();
    }
}
