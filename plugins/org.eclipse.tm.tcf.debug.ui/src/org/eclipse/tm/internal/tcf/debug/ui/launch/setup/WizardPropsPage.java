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

import java.util.Map;

import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

class WizardPropsPage extends WizardPage {

    private final Map<String,String> attrs;

    private PeerPropsControl props;

    protected WizardPropsPage(SetupWizardDialog wizard, Map<String,String> attrs) {
        super("PropsPage");
        this.attrs = attrs;
        setTitle("Manual client configuration");
    }

    public void createControl(Composite parent) {
        Composite composite =  new Composite(parent, SWT.NULL);
        GridLayout gl = new GridLayout();
        gl.numColumns = 1;
        composite.setLayout(gl);
        props = new PeerPropsControl(composite, attrs, true, new Runnable() {
            public void run() {
                getContainer().updateButtons();
            }
        });
        setControl(composite);
    }

    public IWizardPage getNextPage() {
        return null;
    }

    public boolean canFinish() {
        return props.isComplete();
    }

    public boolean performFinish() {
        props.okPressed();
        return true;
    }
}