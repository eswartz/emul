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
package org.eclipse.tm.internal.tcf.debug.ui.launch.setup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.protocol.IPeer;

public class PeerPropsControl {

    private static final int
        SIZING_TABLE_WIDTH = 400,
        SIZING_TABLE_HEIGHT = 200;

    private static final String[] column_names = { "Name", "Value" };

    private final Map<String,String> attrs;
    private final ArrayList<Attribute> attr_table_data;
    private final boolean create_new;
    private final boolean enable_editing;
    private final Runnable listener;

    private Text id_text;
    private Text name_text;
    private Table attr_table;
    private TableViewer table_viewer;
    private Image attr_image;

    private class Attribute {
        String name;
        String value;
    }

    private class AttributeLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int column) {
            if (column == 0) return attr_image;
            return null;
        }

        public String getColumnText(Object element, int column) {
            Attribute a = (Attribute)element;
            return column == 0 ? a.name : a.value;
        }

        public String getText(Object element) {
            TableColumn column = attr_table.getSortColumn();
            if (column == null) return "";
            return getColumnText(element, attr_table.indexOf(column));
        }
    }

    public PeerPropsControl(Composite parent, Map<String,String> attrs, boolean enable_editing, Runnable listener) {
        this.attrs = attrs;
        this.enable_editing = enable_editing;
        this.listener = listener;
        create_new = attrs.isEmpty();
        attr_table_data = new ArrayList<Attribute>();

        createTextFields(parent);
        createAttrTable(parent);
    }

    private void createTextFields(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label id_label = new Label(composite, SWT.WRAP);
        id_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        id_label.setFont(font);
        id_label.setText("Peer &ID:");

        id_text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        id_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        id_text.setFont(font);
        id_text.setEditable(false);

        Label name_label = new Label(composite, SWT.WRAP);
        name_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        name_label.setFont(font);
        name_label.setText("Peer &name:");

        name_text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        name_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        name_text.setFont(font);
        name_text.setEditable(enable_editing);
        name_text.addListener(SWT.KeyUp, new Listener() {
            public void handleEvent(Event event) {
                listener.run();
            }
        });
    }

    private void createAttrTable(Composite parent) {
        Font font = parent.getFont();
        Label props_label = new Label(parent, SWT.WRAP);
        props_label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        props_label.setFont(font);
        props_label.setText("Peer &properties:");

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));

        attr_table = new Table(composite, SWT.SINGLE | SWT.BORDER |
                SWT.H_SCROLL | SWT.V_SCROLL |
                SWT.FULL_SELECTION | SWT.HIDE_SELECTION);
        attr_table.setFont(font);
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = SIZING_TABLE_WIDTH;
        data.heightHint = SIZING_TABLE_HEIGHT;
        attr_table.setLayoutData(data);

        for (int i = 0; i < column_names.length; i++) {
            final TableColumn column = new TableColumn(attr_table, SWT.LEAD, i);
            column.setMoveable(false);
            column.setText(column_names[i]);
            column.setWidth(SIZING_TABLE_WIDTH / column_names.length);
            column.addSelectionListener(new SelectionAdapter() {

                public void widgetSelected(SelectionEvent e) {
                    if (column == attr_table.getSortColumn()) {
                        switch (attr_table.getSortDirection()) {
                        case SWT.NONE:
                            attr_table.setSortDirection(SWT.DOWN);
                            break;
                        case SWT.DOWN:
                            attr_table.setSortDirection(SWT.UP);
                            break;
                        case SWT.UP:
                            attr_table.setSortDirection(SWT.NONE);
                            break;
                        }
                    }
                    else {
                        attr_table.setSortColumn(column);
                        attr_table.setSortDirection(SWT.DOWN);
                    }
                    table_viewer.refresh();
                }
            });
        }
        attr_table.setHeaderVisible(true);
        attr_table.setLinesVisible(true);

        attr_image = ImageCache.getImage(ImageCache.IMG_ATTRIBUTE);

        table_viewer = new TableViewer(attr_table);
        table_viewer.setUseHashlookup(true);
        table_viewer.setColumnProperties(column_names);

        CellEditor[] editors = new CellEditor[column_names.length];
        for (int i = 0; i < column_names.length; i++) {
            TextCellEditor editor = new TextCellEditor(attr_table);
            ((Text)editor.getControl()).setTextLimit(250);
            editors[i] = editor;
        }
        table_viewer.setCellEditors(editors);

        table_viewer.setCellModifier(new ICellModifier() {

            public boolean canModify(Object element, String property) {
                return enable_editing;
            }

            public Object getValue(Object element, String property) {
                if (element instanceof Item) element = ((Item)element).getData();
                Attribute a = (Attribute)element;
                return property.equals(column_names[0]) ? a.name : a.value;
            }

            public void modify(Object element, String property, Object value) {
                if (element instanceof Item) element = ((Item)element).getData();
                Attribute a = (Attribute)element;
                if (property.equals(column_names[0])) {
                    a.name = (String)value;
                }
                else {
                    a.value = (String)value;
                }
                table_viewer.update(element, new String[] { property });
            }
        });

        String[] keys = attrs.keySet().toArray(new String[attrs.size()]);
        Arrays.sort(keys);
        for (String key : keys) {
            if (key.equals(IPeer.ATTR_ID)) {
                id_text.setText(attrs.get(key));
            }
            else if (key.equals(IPeer.ATTR_NAME)) {
                name_text.setText(attrs.get(key));
            }
            else {
                Attribute a = new Attribute();
                a.name = key;
                a.value = attrs.get(key);
                attr_table_data.add(a);
            }
        }
        if (create_new) id_text.setText("USR:" + System.currentTimeMillis());

        table_viewer.setContentProvider(new IStructuredContentProvider() {

            public Object[] getElements(Object input) {
                assert input == attr_table_data;
                return attr_table_data.toArray();
            }

            public void dispose() {
            }

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
            }
        });

        table_viewer.setLabelProvider(new AttributeLabelProvider());
        table_viewer.setInput(attr_table_data);
        table_viewer.setComparator(new ViewerComparator() {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2) {
                switch (attr_table.getSortDirection()) {
                case SWT.UP  : return -super.compare(viewer, e1, e2);
                case SWT.DOWN: return +super.compare(viewer, e1, e2);
                }
                return 0;
            }
        });

        createTableButtons(composite);
    }

    private void createTableButtons(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

        final Button button_new = new Button(composite, SWT.PUSH);
        button_new.setText("&Add");
        button_new.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button_new.setEnabled(enable_editing);
        button_new.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Attribute a = new Attribute();
                a.name = "";
                a.value = "";
                attr_table_data.add(a);
                table_viewer.add(a);
                table_viewer.setSelection(new StructuredSelection(a), true);
                attr_table.setFocus();
            }
        });

        final Button button_remove = new Button(composite, SWT.PUSH);
        button_remove.setText("&Remove");
        button_remove.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button_remove.setEnabled(enable_editing);
        button_remove.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                Attribute a = (Attribute) ((IStructuredSelection)
                        table_viewer.getSelection()).getFirstElement();
                if (a == null) return;
                attr_table_data.remove(a);
                table_viewer.remove(a);
            }
        });
    }

    public boolean isComplete() {
        return name_text.getText().length() > 0;
    }

    public void okPressed() {
        if (enable_editing) {
            if (create_new) attrs.put(IPeer.ATTR_ID, id_text.getText());
            String id = attrs.get(IPeer.ATTR_ID);
            String nm = name_text.getText();
            attrs.clear();
            for (Attribute a : attr_table_data) attrs.put(a.name, a.value);
            attrs.put(IPeer.ATTR_ID, id);
            attrs.put(IPeer.ATTR_NAME, nm);
        }
    }
}
