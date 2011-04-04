/*******************************************************************************
 * Copyright (c) 2004, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Wind River Systems - Adapted to TCF
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui.breakpoints; 

import org.eclipse.cdt.debug.core.model.ICBreakpoint;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm.internal.tcf.debug.model.ITCFConstants;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Property page to define the scope of a breakpoint.
 */
public class TCFBreakpointThreadFilterPage extends PropertyPage {

    private TCFThreadFilterEditor fThreadFilterEditor;

    @Override
    protected Control createContents(Composite parent) {
        noDefaultAndApplyButton();
        Composite mainComposite = new Composite(parent, SWT.NONE);
        mainComposite.setFont(parent.getFont());
        mainComposite.setLayout(new GridLayout());
        mainComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
        createThreadFilterEditor(mainComposite);
        setValid(true);
        return mainComposite;
    }

    protected ICBreakpoint getBreakpoint() {
        return (ICBreakpoint) getElement().getAdapter(ICBreakpoint.class);
    }

    protected TCFBreakpointScopeExtension getFilterExtension() {
        ICBreakpoint bp = getBreakpoint();
        if (bp != null) {
            try {
                TCFBreakpointScopeExtension filter =
                    (TCFBreakpointScopeExtension) bp.getExtension(
                            ITCFConstants.ID_TCF_DEBUG_MODEL, TCFBreakpointScopeExtension.class);
                filter.initialize(bp);
                return filter;
            } catch (CoreException e) {
                // potential race condition: ignore
            }
        }
        return null;
    }

    protected void createThreadFilterEditor(Composite parent) {
        fThreadFilterEditor = new TCFThreadFilterEditor(parent, this);
    }

    protected TCFThreadFilterEditor getThreadFilterEditor() {
        return fThreadFilterEditor;
    }

    @Override
    public boolean performOk() {
        doStore();
        return super.performOk();
    }

    /**
     * Stores the values configured in this page.
     */
    protected void doStore() {
        fThreadFilterEditor.doStore();
    }
}
