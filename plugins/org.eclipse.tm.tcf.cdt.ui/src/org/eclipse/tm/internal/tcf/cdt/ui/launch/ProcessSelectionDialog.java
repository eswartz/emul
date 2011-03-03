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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.window.IShellProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.tm.internal.tcf.cdt.launch.ContextSelection;
import org.eclipse.tm.internal.tcf.cdt.ui.launch.PeerListControl.PeerInfo;
import org.eclipse.tm.internal.tcf.cdt.ui.launch.ProcessListControl.ProcessInfo;

/**
 * Dialog to select a peer and context.
 */
public class ProcessSelectionDialog extends Dialog {

    private ContextSelection fSelection;
    private ProcessListControl fContextList;

    protected ProcessSelectionDialog(IShellProvider parentShell) {
        super(parentShell);
    }

    public void setSelection(ContextSelection selection) {
        fSelection = selection;
    }

    public ContextSelection getSelection() {
        return fSelection;
    }

    @Override
    protected void configureShell(Shell newShell) {
        newShell.setText("Select Peer and Context");
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
        new Label(composite, SWT.NONE).setText("TCF Peers:");
        final PeerListControl peerList = new PeerListControl(composite);
        new Label(composite, SWT.NONE).setText("Contexts:");
        fContextList = new ProcessListControl(composite);
        peerList.addSelectionChangedListener(new ISelectionChangedListener() {
            public void selectionChanged(SelectionChangedEvent event) {
                ISelection selection = event.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection ss = (IStructuredSelection) selection;
                    Object element = ss.getFirstElement();
                    if (element instanceof PeerInfo) {
                        handlePeerSelected((PeerInfo) element);
                    }
                }
            }
        });
        fContextList.getTree().addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                ProcessInfo contextInfo = fContextList.findProcessInfo((TreeItem) e.item);
                if (contextInfo != null) {
                    handleContextSelected(contextInfo);
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
        if (fSelection.fContextId != null) {
            fContextList.selectContext(fSelection.fContextId);
        }
        return composite;
    }

    private void updateButtonState() {
        getButton(IDialogConstants.OK_ID).setEnabled(fSelection.fContextId != null);
    }

    protected void handleContextSelected(ProcessInfo contextInfo) {
        fSelection.fContextId = contextInfo.id;
        updateButtonState();
    }

    protected void handlePeerSelected(PeerInfo peerInfo) {
        fSelection.fPeerId = peerInfo.id;
        fContextList.setInput(peerInfo.peer);
    }

}
