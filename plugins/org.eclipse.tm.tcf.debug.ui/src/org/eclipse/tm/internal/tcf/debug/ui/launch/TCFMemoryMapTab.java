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
package org.eclipse.tm.internal.tcf.debug.ui.launch;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.tm.internal.tcf.debug.ui.commands.MemoryMapWidget;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;

public class TCFMemoryMapTab extends AbstractLaunchConfigurationTab {

    private static final String TAB_ID = "org.eclipse.tm.tcf.launch.memoryMapTab";

    private MemoryMapWidget widget;

    public void createControl(Composite parent) {
        TCFNode node = null;
        IAdaptable adaptable = DebugUITools.getDebugContext();
        if (adaptable != null) node = (TCFNode)adaptable.getAdapter(TCFNode.class);
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, false);
        composite.setFont(parent.getFont());
        composite.setLayout(layout);
        composite.setLayoutData(new GridData(GridData.FILL, GridData.FILL, true, true, 1, 1));
        widget = new MemoryMapWidget(composite, node);
        setControl(composite);
    }

    public void setDefaults(ILaunchConfigurationWorkingCopy cfg) {
    }

    public void initializeFrom(ILaunchConfiguration cfg) {
        setErrorMessage(null);
        setMessage(null);
        widget.loadData(cfg);
    }

    public void performApply(ILaunchConfigurationWorkingCopy cfg) {
        try {
            widget.saveData(cfg);
        }
        catch (Throwable x) {
            setErrorMessage("Cannot update memory map: " + x);
        }
    }

    public String getName() {
        return "Symbol Files";
    }

    @Override
    public Image getImage() {
        return ImageCache.getImage(ImageCache.IMG_MEMORY_MAP);
    }

    @Override
    public String getId() {
        return TAB_ID;
    }
}
