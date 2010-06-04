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
package org.eclipse.tm.internal.tcf.debug.ui.launch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.viewers.CellEditor;
import org.eclipse.jface.viewers.ICellModifier;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TextCellEditor;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Item;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate.PathMapRule;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.tcf.services.IPathMap;

// TODO: add source lookup container that represents ATTR_PATH_MAP
public class TCFPathMapTab extends AbstractLaunchConfigurationTab {

    private TableViewer viewer;
    private Button button_remove;
    private Button button_new;

    private static final String[] column_ids = {
        IPathMap.PROP_SOURCE,
        IPathMap.PROP_HOST,
        IPathMap.PROP_PROTOCOL,
        IPathMap.PROP_DESTINATION,
    };

    private ArrayList<PathMapRule> map;

    private class FileMapContentProvider implements IStructuredContentProvider  {

        public Object[] getElements(Object input) {
            return map.toArray(new PathMapRule[map.size()]);
        }

        public void inputChanged(Viewer viewer, Object old_input, Object new_input) {
        }

        public void dispose() {
        }
    }

    private class FileMapLabelProvider extends LabelProvider implements ITableLabelProvider {

        public Image getColumnImage(Object element, int column) {
            if (column == 0) return ImageCache.getImage(ImageCache.IMG_ATTRIBUTE);
            return null;
        }

        public String getColumnText(Object element, int column) {
            PathMapRule e = (PathMapRule)element;
            Object o = e.getProperties().get(column_ids[column]);
            if (o == null) return "";
            return o.toString();
        }
    }

    private class FileMapCellModifier implements ICellModifier {

        public boolean canModify(Object element, String property) {
            return true;
        }

        public Object getValue(Object element, String property) {
            if (element instanceof Item) element = ((Item)element).getData();
            PathMapRule a = (PathMapRule)element;
            Object o = a.getProperties().get(property);
            if (o == null) return "";
            return o.toString();
        }

        public void modify(Object element, String property, Object value) {
            if (element instanceof Item) element = ((Item)element).getData();
            PathMapRule a = (PathMapRule)element;
            if ("".equals(value)) a.getProperties().remove(property);
            else a.getProperties().put(property, value);
            viewer.update(element, new String[] { property });
            updateLaunchConfigurationDialog();
        }
    }

    private Exception init_error;

    public String getName() {
        return "Path Map";
    }

    @Override
    public Image getImage() {
        return ImageCache.getImage(ImageCache.IMG_PATH);
    }

    public void createControl(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
        createTable(composite);
        setControl(composite);
    }

    private void createTable(Composite parent) {
        Font font = parent.getFont();
        Label map_label = new Label(parent, SWT.WRAP);
        map_label.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        map_label.setFont(font);
        map_label.setText("File path &map rules:");

        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 2, 1));

        viewer = new TableViewer(composite, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.MULTI | SWT.FULL_SELECTION);
        Table table = viewer.getTable();
        table.setLayoutData(new GridData(GridData.FILL_BOTH));
        table.setHeaderVisible(true);
        table.setLinesVisible(true);
        table.setFont(font);
        viewer.setContentProvider(new FileMapContentProvider());
        viewer.setLabelProvider(new FileMapLabelProvider());
        viewer.setColumnProperties(column_ids);

        CellEditor[] editors = new CellEditor[column_ids.length];
        for (int i = 0; i < column_ids.length; i++) {
            TableColumn c = new TableColumn(table, SWT.NONE, i);
            c.setText(column_ids[i]);
            c.setWidth(600 / column_ids.length);
            editors[i] = new TextCellEditor(table);
        }
        viewer.setCellEditors(editors);
        viewer.setCellModifier(new FileMapCellModifier());
        createTableButtons(composite);
        viewer.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                updateLaunchConfigurationDialog();
            }
        });
    }

    private void createTableButtons(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL | GridData.VERTICAL_ALIGN_FILL));

        button_new = new Button(composite, SWT.PUSH);
        button_new.setText("&Add");
        button_new.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button_new.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                PathMapRule a = new PathMapRule(new HashMap<String,Object>());
                a.getProperties().put(IPathMap.PROP_ID, "PR" + System.currentTimeMillis());
                map.add(a);
                viewer.add(a);
                viewer.setSelection(new StructuredSelection(a), true);
                viewer.getTable().setFocus();
                updateLaunchConfigurationDialog();
            }
        });

        button_remove = new Button(composite, SWT.PUSH);
        button_remove.setText("&Remove");
        button_remove.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_FILL));
        button_remove.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                for (Iterator<?> i = ((IStructuredSelection)viewer.getSelection()).iterator(); i.hasNext();) {
                    PathMapRule a = (PathMapRule)i.next();
                    map.remove(a);
                    viewer.remove(a);
                }
                updateLaunchConfigurationDialog();
            }
        });
    }

    List<IPathMap.PathMapRule> getPathMap() {
        List<IPathMap.PathMapRule> l = new ArrayList<IPathMap.PathMapRule>();
        for (PathMapRule r : map) l.add(r);
        return Collections.unmodifiableList(l);
    }

    public void initializeFrom(ILaunchConfiguration config) {
        setErrorMessage(null);
        setMessage(null);
        try {
            String s = config.getAttribute(TCFLaunchDelegate.ATTR_PATH_MAP, "");
            map = TCFLaunchDelegate.parsePathMapAttribute(s);
            viewer.setInput(config);
            button_remove.setEnabled(!viewer.getSelection().isEmpty());
        }
        catch (Exception e) {
            init_error = e;
            setErrorMessage("Cannot read launch configuration: " + e);
            Activator.log(e);
        }
    }

    public void performApply(ILaunchConfigurationWorkingCopy config) {
        StringBuffer bf = new StringBuffer();
        for (PathMapRule m : map) bf.append(m.toString());
        if (bf.length() == 0) config.removeAttribute(TCFLaunchDelegate.ATTR_PATH_MAP);
        else config.setAttribute(TCFLaunchDelegate.ATTR_PATH_MAP, bf.toString());
    }

    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
        config.removeAttribute(TCFLaunchDelegate.ATTR_PATH_MAP);
    }

    @Override
    protected void updateLaunchConfigurationDialog() {
        super.updateLaunchConfigurationDialog();
        button_remove.setEnabled(!viewer.getSelection().isEmpty());
    }

    @Override
    public boolean isValid(ILaunchConfiguration config) {
        setMessage(null);

        if (init_error != null) {
            setErrorMessage("Cannot read launch configuration: " + init_error);
            return false;
        }

        setErrorMessage(null);
        return true;
    }
}
