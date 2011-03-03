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
package org.eclipse.tm.internal.tcf.cdt.ui.launch;

import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tm.internal.tcf.cdt.ui.launch.FileSystemBrowserControl.FileInfo;
import org.eclipse.tm.tcf.protocol.IPeer;

/**
 * Dialog to select a remote file.
 */
public class RemoteFileSelectionDialog extends Dialog {

    private String fSelection;
    private FileSystemBrowserControl fFileList;
    private IPeer fPeer;
    private boolean fForSave;
    private Text fFileNameText;

    protected RemoteFileSelectionDialog(IShellProvider parentShell, int style) {
        super(parentShell);
        setShellStyle(getShellStyle() | SWT.RESIZE);
        fForSave = (style & SWT.SAVE) != 0;
    }

    public void setPeer(IPeer peer) {
        fPeer = peer;
        if (fFileList != null) {
            fFileList.setInput(peer);
        }
    }
    
    public void setSelection(String fileSelection) {
        fSelection = fileSelection;
    }

    public String getSelection() {
        return fSelection;
    }

    @Override
    protected void configureShell(Shell newShell) {
        newShell.setText("Select File");
        super.configureShell(newShell);
    }

    @Override
    protected Control createContents(Composite parent) {
        Control control = super.createContents(parent);
        updateButtonState();
        return control;
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite) super.createDialogArea(parent);
        fFileList = new FileSystemBrowserControl(composite, false);
        fFileList.getTree().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                FileInfo contextInfo = fFileList.findFileInfo((TreeItem) e.item);
                if (contextInfo != null) {
                    handleFileSelected(contextInfo);
                }
            }
            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                widgetSelected(e);
                if (getButton(IDialogConstants.OK_ID).isEnabled()) {
                    buttonPressed(IDialogConstants.OK_ID);
                }
            }
        });
        
        if (fForSave) {
            Composite fileNameComp = new Composite(composite, SWT.NONE);
            fileNameComp.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
            GridLayout layout = new GridLayout(2, false);
            layout.marginWidth = 0;
            fileNameComp.setLayout(layout);
            new Label(fileNameComp, SWT.NONE).setText("File Name:");
            fFileNameText = new Text(fileNameComp, SWT.BORDER);
            fFileNameText.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
            fFileNameText.addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    handleFileNameChanged();
                }
            });
        }
        
        if (fSelection != null) {
            fFileList.setInitialSelection(fSelection);
            if (fFileNameText != null) {
                String basename = new Path(fSelection).lastSegment();
                if (basename != null) {
                    fFileNameText.setText(basename);
                }
            }
        }
        if (fPeer != null) {
            fFileList.setInput(fPeer);
        }
        return composite;
    }

    private void updateButtonState() {
        boolean enabled = fSelection != null;
        if (enabled && fForSave) {
            enabled = fFileNameText.getText().trim().length() > 0;
        }
        getButton(IDialogConstants.OK_ID).setEnabled(enabled);
    }

    protected void handleFileNameChanged() {
        if (fSelection != null) {
            fSelection = new Path(fSelection).removeLastSegments(1).append(fFileNameText.getText().trim()).toString();
            updateButtonState();
        }
    }
    
    protected void handleFileSelected(FileInfo fileInfo) {
        if (fileInfo.isDir) {
            if (fForSave) {
                String basename = fFileNameText.getText().trim();
                if (basename.length() > 0) {
                    fSelection = new Path(fileInfo.fullname).append(basename).toString();
                } else {
                    fSelection = fileInfo.fullname;
                }
            } else {
                fSelection = null;
            }
        } else {
            fSelection = fileInfo.fullname;
            if (fFileNameText != null) {
                String basename = new Path(fSelection).lastSegment();
                if (basename != null) {
                    fFileNameText.setText(basename);
                }
            }
        }
        updateButtonState();
    }

}
