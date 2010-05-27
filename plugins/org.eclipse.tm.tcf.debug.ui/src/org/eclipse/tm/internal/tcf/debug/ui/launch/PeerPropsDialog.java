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

import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.tcf.debug.ui.launch.setup.PeerPropsControl;

class PeerPropsDialog extends Dialog {

    private final Map<String,String> attrs;
    private final boolean enable_editing;
    private final Image image;

    private PeerPropsControl props;

    PeerPropsDialog(Shell parent, Image image, Map<String,String> attrs, boolean enable_editing) {
        super(parent);
        this.image = image;
        this.attrs = attrs;
        this.enable_editing = enable_editing;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("TCF Peer Properties");
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
        props = new PeerPropsControl(composite, attrs, enable_editing, new Runnable() {
            public void run() {
                updateButtons();
            }
        });
        composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
        return composite;
    }

    private void updateButtons() {
        getButton(IDialogConstants.OK_ID).setEnabled(props.isComplete());
    }

    @Override
    protected void okPressed() {
        props.okPressed();
        super.okPressed();
    }
}
