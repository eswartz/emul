/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.launch;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

class TestErrorsDialog extends Dialog {

    private static final int
        SIZING_TEXT_WIDTH = 600,
        SIZING_TEXT_HEIGHT = 400;

    private Collection<Throwable> errors;
    private Image image;
    private Text text;

    TestErrorsDialog(Shell parent, Image image, Collection<Throwable> errors) {
        super(parent);
        this.image = image;
        this.errors = errors;
    }

    @Override
    protected void configureShell(Shell shell) {
        super.configureShell(shell);
        shell.setText("Connection Diagnostic errors");
        shell.setImage(image);
    }

    @Override
    protected void createButtonsForButtonBar(Composite parent) {
        createButton(parent, IDialogConstants.OK_ID, "&OK", true);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        Composite composite = (Composite)super.createDialogArea(parent);
        composite.setSize(composite.computeSize(SWT.DEFAULT, SWT.DEFAULT));

        Label label = new Label(composite, SWT.WRAP);
        label.setFont(JFaceResources.getFontRegistry().get(JFaceResources.BANNER_FONT));
        label.setText("Connection diagnostics ended with errors:");

        text = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        text.setFont(JFaceResources.getFontRegistry().get(JFaceResources.TEXT_FONT));
        text.setEditable(false);
        text.setText(createText());
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = SIZING_TEXT_WIDTH;
        data.heightHint = SIZING_TEXT_HEIGHT;
        text.setLayoutData(data);

        return composite;
    }

    private String createText() {
        StringWriter buf = new StringWriter();
        PrintWriter pwr = new PrintWriter(buf);
        for (Iterator<Throwable> i = errors.iterator(); i.hasNext();) {
            i.next().printStackTrace(pwr);
            pwr.println();
        }
        pwr.flush();
        return buf.toString();
    }
}
