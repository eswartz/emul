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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;

class WizardLocalPage extends WizardPage implements Listener {

    WizardLocalPage(SetupWizardDialog wizard) {
        super("LocalPage");
        setTitle("Local TCF agent configuration");
    }

    public void handleEvent(Event event) {
        getContainer().updateButtons();
    }

    public void createControl(Composite parent) {
        Composite composite =  new Composite(parent, SWT.NULL);
        GridLayout gl = new GridLayout();
        gl.numColumns = 1;
        composite.setLayout(gl);

        new Label(composite, SWT.WRAP).setText("Under construction...");

        setControl(composite);
    }

    @Override
    public IWizardPage getNextPage() {
        return null;
    }
}