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
package org.eclipse.tm.internal.tcf.debug.ui.launch.setup;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

class WizardFirstPage extends WizardPage implements Listener {

    private final SetupWizardDialog wizard;

    private Button button_props;
    private Button button_local;
    private Button button_login;

    WizardFirstPage(SetupWizardDialog wizard) {
        super("FirstPage");
        this.wizard = wizard;
        setTitle("Select a task");
        setDescription("Select an option that describes the task you want the wizard to perform");
    }

    public void createControl(Composite parent) {
        Composite composite =  new Composite(parent, SWT.NULL);
        GridLayout gl = new GridLayout();
        gl.numColumns = 1;
        composite.setLayout(gl);

        button_props = new Button(composite, SWT.RADIO | SWT.WRAP | SWT.MULTI);
        button_props.addListener(SWT.Selection, this);
        button_props.setText("Manual setup of TCF connection properties.");

        button_local = new Button(composite, SWT.RADIO | SWT.WRAP | SWT.MULTI);
        button_local.addListener(SWT.Selection, this);
        button_local.setText("Setup TCF agent on local host.");

        button_login = new Button(composite, SWT.RADIO | SWT.WRAP | SWT.MULTI);
        button_login.addListener(SWT.Selection, this);
        button_login.setText("Setup TCF agent on remote host over Telnet or SSH.");

        setControl(composite);
    }

    public void handleEvent(Event event) {
        getContainer().updateButtons();
    }

    @Override
    public IWizardPage getNextPage() {
        if (button_props.getSelection()) return wizard.getPage("PropsPage");
        if (button_local.getSelection()) return wizard.getPage("LocalPage");
        if (button_login.getSelection()) return wizard.getPage("LoginPage");
        return null;
    }
}