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
package org.eclipse.tm.internal.tcf.debug.ui.properties;

import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.eclipse.ui.views.properties.PropertySheetPage;

/**
 * Generic property page based on PropertySheetPage.
 */
public class TCFPropertyPage extends PropertyPage {

    private PropertySheetPage fPage;

    public TCFPropertyPage() {
    }

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        fPage = new PropertySheetPage();
        fPage.createControl(composite);
        fPage.getControl().setLayoutData(new GridData(GridData.FILL_BOTH));
        fPage.selectionChanged(null, new StructuredSelection(getElement()));
        return composite;
    }

}
