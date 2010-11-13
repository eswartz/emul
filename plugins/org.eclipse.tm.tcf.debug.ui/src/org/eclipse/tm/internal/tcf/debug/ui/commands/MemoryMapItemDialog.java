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
import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.tcf.services.IMemoryMap;

class MemoryMapItemDialog extends Dialog {

    private final Map<String,Object> attrs;
    private final boolean enable_editing;
    private final Image image;

    private Text addr_text;
    private Text size_text;
    private Text offset_text;
    private Text file_text;
    private Button rd_button;
    private Button wr_button;
    private Button ex_button;

    MemoryMapItemDialog(Shell parent, Image image, Map<String,Object> attrs, boolean enable_editing) {
        super(parent);
        this.image = image;
        this.attrs = attrs;
        this.enable_editing = enable_editing;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Memory Map Item");
        shell.setImage(image);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "&OK", true);
        updateButtons();
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite)super.createDialogArea(parent);
        createFileNameFields(composite);
        createPropsFields(composite);
        setData();
        composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return composite;
    }

    private void createFileNameFields(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(3, false);
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Label file_label = new Label(composite, SWT.WRAP);
        file_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        file_label.setFont(font);
        file_label.setText("File name:");

        file_text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        file_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        file_text.setFont(font);
        file_text.setEditable(enable_editing);

        file_text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                updateButtons();
            }
        });

        Button button = new Button(composite, SWT.PUSH);
        button.setFont(font);
        button.setText("...");
        button.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        button.setEnabled(enable_editing);
        button.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                FileDialog file_dialog = new FileDialog(getShell(), SWT.NONE);
                file_dialog.setFileName(file_text.getText());
                String path = file_dialog.open();
                if (path != null) file_text.setText(path);
            }
        });
    }

    private void createPropsFields(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setFont(font);
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        createTextFields(composite);
        createFlagsGroup(composite);
    }

    private void createTextFields(Composite parent) {
        Font font = parent.getFont();
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        composite.setFont(font);
        composite.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.widthHint = 250;
        composite.setLayoutData(gd);

        Label addr_label = new Label(composite, SWT.WRAP);
        addr_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        addr_label.setFont(font);
        addr_label.setText("Address:");

        addr_text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        addr_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        addr_text.setFont(font);
        addr_text.setEditable(enable_editing);

        Label size_label = new Label(composite, SWT.WRAP);
        size_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        size_label.setFont(font);
        size_label.setText("Size:");

        size_text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        size_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        size_text.setFont(font);
        size_text.setEditable(enable_editing);

        Label offset_label = new Label(composite, SWT.WRAP);
        offset_label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING));
        offset_label.setFont(font);
        offset_label.setText("File offset:");

        offset_text = new Text(composite, SWT.SINGLE | SWT.BORDER);
        offset_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        offset_text.setFont(font);
        offset_text.setEditable(enable_editing);
    }

    private void createFlagsGroup(Composite parent) {
        Font font = parent.getFont();

        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.verticalSpacing = 0;
        layout.numColumns = 1;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setFont(font);
        group.setText("Flags");

        rd_button = new Button(group, SWT.CHECK);
        rd_button.setFont(font);
        rd_button.setText("Data read");
        rd_button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        rd_button.setEnabled(enable_editing);

        wr_button = new Button(group, SWT.CHECK);
        wr_button.setFont(font);
        wr_button.setText("Data write");
        wr_button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        wr_button.setEnabled(enable_editing);

        ex_button = new Button(group, SWT.CHECK);
        ex_button.setFont(font);
        ex_button.setText("Instructions read");
        ex_button.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        ex_button.setEnabled(enable_editing);
    }

    private String toHex(Number n) {
        if (n == null) return null;
        BigInteger x = n instanceof BigInteger ? (BigInteger)n : new BigInteger(n.toString());
        String s = x.toString(16);
        int l = 16 - s.length();
        if (l < 0) l = 0;
        if (l > 16) l = 16;
        return "0x0000000000000000".substring(0, 2 + l) + s;
    }

    private void setText(Text text, String str) {
        if (str == null) str = "";
        text.setText(str);
    }

    private void setData() {
        setText(addr_text, toHex((Number)attrs.get(IMemoryMap.PROP_ADDRESS)));
        setText(size_text, toHex((Number)attrs.get(IMemoryMap.PROP_SIZE)));
        setText(offset_text, toHex((Number)attrs.get(IMemoryMap.PROP_OFFSET)));
        setText(file_text, (String)attrs.get(IMemoryMap.PROP_FILE_NAME));
        int flags = 0;
        Number n = (Number)attrs.get(IMemoryMap.PROP_FLAGS);
        if (n != null) flags = n.intValue();
        rd_button.setSelection((flags & IMemoryMap.FLAG_READ) != 0);
        wr_button.setSelection((flags & IMemoryMap.FLAG_WRITE) != 0);
        ex_button.setSelection((flags & IMemoryMap.FLAG_EXECUTE) != 0);
        updateButtons();
    }

    private void getNumber(Text text, String key) {
        String s = text.getText().trim();
        if (s == null || s.length() == 0) {
            attrs.remove(key);
        }
        else if (s.startsWith("0x")) {
            attrs.put(key, new BigInteger(s.substring(2), 16));
        }
        else {
            attrs.put(key, new BigInteger(s));
        }
    }

    private void getText(Text text, String key) {
        String s = text.getText().trim();
        if (s == null || s.length() == 0) {
            attrs.remove(key);
        }
        else {
            attrs.put(key, s);
        }
    }

    private void getData() {
        getNumber(addr_text, IMemoryMap.PROP_ADDRESS);
        getNumber(size_text, IMemoryMap.PROP_SIZE);
        getNumber(offset_text, IMemoryMap.PROP_OFFSET);
        getText(file_text, IMemoryMap.PROP_FILE_NAME);
        int flags = 0;
        if (rd_button.getSelection()) flags |= IMemoryMap.FLAG_READ;
        if (wr_button.getSelection()) flags |= IMemoryMap.FLAG_WRITE;
        if (ex_button.getSelection()) flags |= IMemoryMap.FLAG_EXECUTE;
        attrs.put(IMemoryMap.PROP_FLAGS, flags);
    }

    private void updateButtons() {
        Button btn = getButton(IDialogConstants.OK_ID);
        if (btn != null && file_text != null) btn.setEnabled(!enable_editing || file_text.getText().trim().length() > 0);
    }

    @Override
    protected void okPressed() {
        getData();
        super.okPressed();
    }
}
