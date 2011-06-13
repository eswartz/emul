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
package org.eclipse.tm.internal.tcf.debug.ui.commands;

import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableColorProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate;
import org.eclipse.tm.internal.tcf.debug.model.TCFMemoryRegion;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFChildren;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeLaunch;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.JSON;
import org.eclipse.tm.tcf.services.IMemory;
import org.eclipse.tm.tcf.services.IMemoryMap;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.tm.tcf.util.TCFTask;

public class MemoryMapWidget {


    private static final int
        SIZING_TABLE_WIDTH = 700,
        SIZING_TABLE_HEIGHT = 300;

    private static final String[] column_names = {
        "File",
        "Address",
        "Size",
        "Flags",
        "File offset/section",
    };

    private final TCFModel model;
    private final IChannel channel;
    private final TCFNode selection;

    private Combo ctx_text;
    private Table map_table;
    private TableViewer table_viewer;
    private Runnable update_map_buttons;
    private final Map<String,ArrayList<IMemoryMap.MemoryRegion>> org_maps =
        new HashMap<String,ArrayList<IMemoryMap.MemoryRegion>>();
    private final Map<String,ArrayList<IMemoryMap.MemoryRegion>> cur_maps =
        new HashMap<String,ArrayList<IMemoryMap.MemoryRegion>>();
    private final ArrayList<IMemoryMap.MemoryRegion> target_map =
        new ArrayList<IMemoryMap.MemoryRegion>();
    private final HashMap<String,TCFNodeExecContext> target_map_nodes =
        new HashMap<String,TCFNodeExecContext>();
    private TCFNodeExecContext selected_mem_map_node;
    private IMemory.MemoryContext mem_ctx;
    private ILaunchConfiguration cfg;
    private final HashSet<String> loaded_files = new HashSet<String>();
    private String selected_mem_map_id;

    private final IStructuredContentProvider content_provider = new IStructuredContentProvider() {

        public Object[] getElements(Object input) {
            ArrayList<IMemoryMap.MemoryRegion> res = new ArrayList<IMemoryMap.MemoryRegion>();
            ArrayList<IMemoryMap.MemoryRegion> lst = cur_maps.get((String)input);
            if (lst != null) res.addAll(lst);
            res.addAll(target_map);
            return res.toArray();
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
            TCFMemoryRegion r = (TCFMemoryRegion)element;
            switch (column) {
            case 0:
                return r.getFileName();
            case 1:
            case 2:
                {
                    BigInteger x = column == 1 ? r.addr : r.size;
                    if (x == null) return "";
                    String s = x.toString(16);
                    int sz = mem_ctx.getAddressSize() * 2;
                    int l = sz - s.length();
                    if (l < 0) l = 0;
                    if (l > 16) l = 16;
                    return "0x0000000000000000".substring(0, 2 + l) + s;
                }
            case 3:
                {
                    int n = r.getFlags();
                    StringBuffer bf = new StringBuffer();
                    if ((n & IMemoryMap.FLAG_READ) != 0) bf.append('r');
                    if ((n & IMemoryMap.FLAG_WRITE) != 0) bf.append('w');
                    if ((n & IMemoryMap.FLAG_EXECUTE) != 0) bf.append('x');
                    return bf.toString();
                }
            case 4:
                {
                    Number n = r.getOffset();
                    if (n != null) {
                        BigInteger x = n instanceof BigInteger ? (BigInteger)n : new BigInteger(n.toString());
                        String s = x.toString(16);
                        int l = 16 - s.length();
                        if (l < 0) l = 0;
                        if (l > 16) l = 16;
                        return "0x0000000000000000".substring(0, 2 + l) + s;
                    }
                    String s = r.getSectionName();
                    if (s != null) return s;
                    return "";
                }
            }
            return "";
        }

        public Color getBackground(Object element, int columnIndex) {
            return map_table.getBackground();
        }

        public Color getForeground(Object element, int columnIndex) {
            TCFMemoryRegion r = (TCFMemoryRegion)element;
            if (r.getProperties().get(IMemoryMap.PROP_ID) != null) {
                String fnm = r.getFileName();
                if (fnm != null && loaded_files.contains(fnm)) {
                    return map_table.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN);
                }
                return map_table.getDisplay().getSystemColor(SWT.COLOR_DARK_BLUE);
            }
            return map_table.getForeground();
        }

        public String getText(Object element) {
            return element.toString();
        }
    }

    public MemoryMapWidget(Composite composite, TCFNode node) {
        if (node != null) {
            model = node.getModel();
            channel = node.getChannel();
            selection = node;
        }
        else {
            model = null;
            channel = null;
            selection = null;
        }
        createContextText(composite);
        createMemoryMapTable(composite);
    }

    public String getMemoryMapID() {
        return selected_mem_map_id;
    }

    private void createContextText(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label props_label = new Label(composite, SWT.WRAP);
        props_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        props_label.setFont(font);
        props_label.setText("&Debug context:");

        ctx_text = new Combo(composite, SWT.SINGLE | SWT.BORDER | SWT.READ_ONLY);
        ctx_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ctx_text.setFont(font);
        ctx_text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                selected_mem_map_id = ctx_text.getText();
                selected_mem_map_node = target_map_nodes.get(selected_mem_map_id);
                loadTargetMemoryMap();
                table_viewer.setInput(selected_mem_map_id);
                if (selected_mem_map_id.length() == 0) selected_mem_map_id = null;
                update_map_buttons.run();
            }
        });
    }

    private void createMemoryMapTable(Composite parent) {
        Font font = parent.getFont();

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        map_table = new Table(composite,
                SWT.SINGLE | SWT.BORDER | SWT.FULL_SELECTION |
                SWT.H_SCROLL | SWT.V_SCROLL);
        map_table.setFont(font);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = SIZING_TABLE_WIDTH;
        data.heightHint = SIZING_TABLE_HEIGHT;
        map_table.setLayoutData(data);

        int w = SIZING_TABLE_WIDTH / (column_names.length + 12);
        for (int i = 0; i < column_names.length; i++) {
            final TableColumn column = new TableColumn(map_table, SWT.LEAD, i);
            column.setMoveable(false);
            column.setText(column_names[i]);
            switch (i) {
            case 0:
                column.setWidth(w * 10);
                break;
            case 1:
            case 2:
            case 4:
                column.setWidth(w * 2);
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
                editRegion(r);
            }
            @Override
            public void widgetSelected(SelectionEvent e) {
                update_map_buttons.run();
            }
        });

        table_viewer = new TableViewer(map_table);
        table_viewer.setUseHashlookup(true);
        table_viewer.setColumnProperties(column_names);

        table_viewer.setContentProvider(content_provider);

        table_viewer.setLabelProvider(new MapLabelProvider());

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
                String id = ctx_text.getText();
                if (id == null || id.length() == 0) return;
                Map<String,Object> props = new HashMap<String,Object>();
                Image image = ImageCache.getImage(ImageCache.IMG_MEMORY_MAP);
                if (new MemoryMapItemDialog(map_table.getShell(), image, props, true).open() == Window.OK) {
                    props.put(IMemoryMap.PROP_ID, id);
                    ArrayList<IMemoryMap.MemoryRegion> lst = cur_maps.get(id);
                    if (lst == null) cur_maps.put(id, lst = new ArrayList<IMemoryMap.MemoryRegion>());
                    lst.add(new TCFMemoryRegion(props));
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
                editRegion(r);
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
                String id = ctx_text.getText();
                if (id == null || id.length() == 0) return;
                IMemoryMap.MemoryRegion r = (IMemoryMap.MemoryRegion)((IStructuredSelection)
                        table_viewer.getSelection()).getFirstElement();
                if (r == null) return;
                ArrayList<IMemoryMap.MemoryRegion> lst = cur_maps.get(id);
                if (lst != null && lst.remove(r)) table_viewer.refresh();
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
                boolean manual = r != null && r.getProperties().get(IMemoryMap.PROP_ID) != null;
                button_add.setEnabled(selected_mem_map_id != null);
                button_edit.setEnabled(r != null);
                button_remove.setEnabled(manual);
                item_add.setEnabled(selected_mem_map_id != null);
                item_edit.setEnabled(r != null);
                item_remove.setEnabled(manual);
            }
        };
        update_map_buttons.run();
    }

    private void editRegion(IMemoryMap.MemoryRegion r) {
        String id = ctx_text.getText();
        if (id == null || id.length() == 0) return;
        Map<String,Object> props = r.getProperties();
        boolean enable_editing = props.get(IMemoryMap.PROP_ID) != null;
        if (enable_editing) props = new HashMap<String,Object>(props);
        Image image = ImageCache.getImage(ImageCache.IMG_MEMORY_MAP);
        if (new MemoryMapItemDialog(map_table.getShell(), image, props, enable_editing).open() == Window.OK && enable_editing) {
            ArrayList<IMemoryMap.MemoryRegion> lst = cur_maps.get(id);
            if (lst != null) {
                int n = lst.indexOf(r);
                if (n >= 0) {
                    lst.set(n, new TCFMemoryRegion(props));
                    table_viewer.refresh();
                }
            }
        }
    }

    private void readMemoryMapAttribute() {
        cur_maps.clear();
        try {
            new TCFTask<Boolean>() {
                public void run() {
                    try {
                        TCFLaunchDelegate.getMemMapsAttribute(cur_maps, cfg);
                        done(true);
                    }
                    catch (Exception e) {
                        error(e);
                    }
                }
            }.get();
        }
        catch (Exception x) {
            Activator.log("Invalid launch cofiguration attribute", x);
        }
    }

    private void writeMemoryMapAttribute(ILaunchConfigurationWorkingCopy copy) throws Exception {
        String s = null;
        final ArrayList<IMemoryMap.MemoryRegion> lst = new ArrayList<IMemoryMap.MemoryRegion>();
        for (ArrayList<IMemoryMap.MemoryRegion> x : cur_maps.values()) lst.addAll(x);
        if (lst.size() > 0) {
            s = new TCFTask<String>() {
                public void run() {
                    try {
                        done(JSON.toJSON(lst));
                    }
                    catch (IOException e) {
                        error(e);
                    }
                }
            }.getIO();
        }
        copy.setAttribute(TCFLaunchDelegate.ATTR_MEMORY_MAP, s);
    }

    public void loadData(ILaunchConfiguration cfg) {
        this.cfg = cfg;
        cur_maps.clear();
        org_maps.clear();
        loadTargetMemoryNodes();
        readMemoryMapAttribute();
        for (String id : cur_maps.keySet()) {
            org_maps.put(id, new ArrayList<IMemoryMap.MemoryRegion>(cur_maps.get(id)));
        }
        // Update controls
        String map_id = getSelectedMemoryNode();
        HashSet<String> ids = new HashSet<String>(target_map_nodes.keySet());
        if (map_id != null) ids.add(map_id);
        ids.addAll(cur_maps.keySet());
        String[] arr = ids.toArray(new String[ids.size()]);
        Arrays.sort(arr);
        ctx_text.removeAll();
        for (String id : arr) ctx_text.add(id);
        if (map_id == null && arr.length > 0) map_id = arr[0];
        if (map_id == null) map_id = "";
        ctx_text.setText(map_id);
    }

    private String getSelectedMemoryNode() {
        if (channel == null) return null;
        try {
            return new TCFTask<String>(channel) {
                public void run() {
                    TCFDataCache<TCFNodeExecContext> mem_cache = model.searchMemoryContext(selection);
                    if (mem_cache == null) {
                        error(new Exception("Context does not provide memory access"));
                        return;
                    }
                    if (!mem_cache.validate(this)) return;
                    if (mem_cache.getError() != null) {
                        error(mem_cache.getError());
                        return;
                    }
                    String id = null;
                    TCFNodeExecContext node = mem_cache.getData();
                    if (node != null) {
                        TCFDataCache<String> name_cache = node.getFullName();
                        if (!name_cache.validate(this)) return;
                        id = name_cache.getData();
                    }
                    done(id);
                }
            }.get();
        }
        catch (Exception x) {
            if (channel.getState() != IChannel.STATE_OPEN) return null;
            Activator.log("Cannot get selected memory node", x);
            return null;
        }
    }

    private void loadTargetMemoryNodes() {
        target_map_nodes.clear();
        if (channel == null) return;
        try {
            new TCFTask<Boolean>(channel) {
                public void run() {
                    TCFNodeLaunch n = model.getRootNode();
                    if (!collectMemoryNodes(n.getFilteredChildren())) return;
                    done(true);
                }
                private boolean collectMemoryNodes(TCFChildren children) {
                    if (!children.validate(this)) return false;
                    Map<String,TCFNode> m = children.getData();
                    if (m != null) {
                        for (TCFNode n : m.values()) {
                            if (n instanceof TCFNodeExecContext) {
                                TCFNodeExecContext exe = (TCFNodeExecContext)n;
                                if (!collectMemoryNodes(exe.getChildren())) return false;
                                TCFDataCache<IMemory.MemoryContext> mem = exe.getMemoryContext();
                                if (!mem.validate(this)) return false;
                                if (mem.getData() != null) {
                                    TCFDataCache<String> nm = exe.getFullName();
                                    if (!nm.validate(this)) return false;
                                    String s = nm.getData();
                                    if (s != null) target_map_nodes.put(s, exe);
                                }
                            }
                        }
                    }
                    return true;
                }
            }.get();
        }
        catch (Exception x) {
            if (channel.getState() != IChannel.STATE_OPEN) return;
            Activator.log("Cannot load target memory context info", x);
        }
    }

    private void loadTargetMemoryMap() {
        loaded_files.clear();
        target_map.clear();
        mem_ctx = null;
        if (channel == null) return;
        try {
            new TCFTask<Boolean>(channel) {
                public void run() {
                    if (selected_mem_map_node != null && !selected_mem_map_node.isDisposed()) {
                        TCFDataCache<IMemory.MemoryContext> mem_cache = selected_mem_map_node.getMemoryContext();
                        if (!mem_cache.validate(this)) return;
                        if (mem_cache.getError() != null) {
                            error(mem_cache.getError());
                            return;
                        }
                        mem_ctx = mem_cache.getData();
                        TCFDataCache<TCFNodeExecContext.MemoryRegion[]> map_cache = selected_mem_map_node.getMemoryMap();
                        if (!map_cache.validate(this)) return;
                        if (map_cache.getError() != null) {
                            error(map_cache.getError());
                            return;
                        }
                        if (map_cache.getData() != null) {
                            for (TCFNodeExecContext.MemoryRegion m : map_cache.getData()) {
                                Map<String,Object> props = m.region.getProperties();
                                if (props.get(IMemoryMap.PROP_ID) != null) {
                                    String fnm = m.region.getFileName();
                                    if (fnm != null) loaded_files.add(fnm);
                                }
                                else {
                                    target_map.add(new TCFMemoryRegion(props));
                                }
                            }
                        }
                    }
                    done(true);
                }
            }.get();
        }
        catch (Exception x) {
            if (channel.getState() != IChannel.STATE_OPEN) return;
            Activator.log("Cannot load target memory map", x);
        }
    }

    public boolean saveData(ILaunchConfigurationWorkingCopy copy) throws Exception {
        boolean loaded_files_ok = true;
        if (selected_mem_map_id != null) {
            ArrayList<IMemoryMap.MemoryRegion> lst = cur_maps.get(selected_mem_map_id);
            if (lst != null) {
                for (IMemoryMap.MemoryRegion r : lst) {
                    String fnm = r.getFileName();
                    if (fnm != null && !loaded_files.contains(fnm)) loaded_files_ok = false;
                }
                if (lst.size() == 0) cur_maps.remove(selected_mem_map_id);
            }
        }
        if (!loaded_files_ok || !org_maps.equals(cur_maps)) {
            writeMemoryMapAttribute(copy);
            return true;
        }
        return false;
    }
}
