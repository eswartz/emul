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
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;

public class SetupWizardDialog extends Wizard {

    private final Map<String,String> peer_attrs;

    public SetupWizardDialog(Map<String,String> peer_attrs) {
        this.peer_attrs = peer_attrs;
        setWindowTitle("TCF Debug Target Setup");
    }

    @Override
    public void addPages() {
        addPage(new WizardFirstPage(this));
        addPage(new WizardLoginPage(this));
        addPage(new WizardLogPage(this));
        addPage(new WizardLocalPage(this));
        addPage(new WizardPropsPage(this, peer_attrs));
    }

    @Override
    public Image getDefaultPageImage() {
        return ImageCache.getImage(ImageCache.IMG_TARGET_WIZARD);
    }

    @Override
    public boolean canFinish() {
        IWizardPage page = getContainer().getCurrentPage();
        if (page instanceof WizardLogPage) return ((WizardLogPage)page).canFinish();
        if (page instanceof WizardPropsPage) return ((WizardPropsPage)page).canFinish();
        return false;
    }

    @Override
    public boolean performFinish() {
        if (!canFinish()) return false;
        IWizardPage page = getContainer().getCurrentPage();
        if (page instanceof WizardPropsPage) {
            if (!((WizardPropsPage)page).performFinish()) return false;;
        }
        return true;
    }
}
