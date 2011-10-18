/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.terminals.ui.launcher;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.tcf.terminals.core.interfaces.launcher.ITerminalsLauncher;
import org.eclipse.tm.te.tcf.terminals.ui.nls.Messages;
import org.eclipse.tm.te.ui.controls.BaseDialogPageControl;
import org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl;
import org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel;
import org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Terminals launcher settings panel implementation.
 */
public class SettingsPanel extends AbstractWizardConfigurationPanel implements ISharedDataWizardPage {
	private TerminalTypeControl terminalType;

	private final static String[] DEFAULT_TYPES = new String[] { Messages.TerminalTypeControl_default_ansi, Messages.TerminalTypeControl_default_vt100 };

	/**
	 * Terminal type control implementation.
	 */
	protected class TerminalTypeControl extends BaseEditBrowseTextControl {

		/**
		 * Constructor.
		 *
		 * @param parentPage The parent dialog page this control is embedded in.
		 *                   Might be <code>null</code> if the control is not associated with a page.
		 */
        public TerminalTypeControl(IDialogPage parentPage) {
	        super(parentPage);
	        setIsGroup(false);
	        setHideBrowseButton(true);
	        setHasHistory(true);
	        setEditFieldLabel(Messages.TerminalTypeControl_label);
        }

        /* (non-Javadoc)
         * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
         */
        @Override
        public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
        	if (settings != null && getDialogSettingsSlotId(idPrefix) != null) {
        		settings.put(getDialogSettingsSlotId(idPrefix), getEditFieldControlText());
        	}
        }

        /* (non-Javadoc)
         * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
         */
        @Override
        public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
        	if (settings != null && getDialogSettingsSlotId(idPrefix) != null) {
        		String type = settings.get(getDialogSettingsSlotId(idPrefix));
        		if (type != null && !"".equals(type.trim())) setEditFieldControlText(type.trim());
        	}
        }
	}

	/**
     * Constructor.
     *
	 * @param parentControl The parent control. Must not be <code>null</code>!
     */
    public SettingsPanel(BaseDialogPageControl parentControl) {
	    super(parentControl);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel#dispose()
     */
    @Override
    public void dispose() {
    	if (terminalType != null) { terminalType.dispose(); terminalType = null; }
        super.dispose();
    }

	/* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel#setupPanel(org.eclipse.swt.widgets.Composite, org.eclipse.tm.te.ui.controls.interfaces.FormToolkit)
     */
    @Override
    public void setupPanel(Composite parent, FormToolkit toolkit) {
    	Composite panel = new Composite(parent, SWT.NONE);
    	panel.setLayout(new GridLayout());
    	panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

    	setControl(panel);

    	terminalType = new TerminalTypeControl(null);
    	terminalType.setupPanel(panel);
    	terminalType.setEditFieldControlHistory(DEFAULT_TYPES);
    	terminalType.setEditFieldControlText(Messages.TerminalTypeControl_default_vt100);
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
     */
    @Override
    public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
    	super.doSaveWidgetValues(settings, idPrefix);
    	if (settings != null) {
    		terminalType.saveWidgetValues(settings, idPrefix);
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
     */
    @Override
    public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
    	super.doRestoreWidgetValues(settings, idPrefix);
    	if (settings != null) {
    		terminalType.restoreWidgetValues(settings, idPrefix);
    	}
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel#dataChanged(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.swt.events.TypedEvent)
	 */
	@Override
	public boolean dataChanged(IPropertiesContainer data, TypedEvent e) {
		return false;
	}

	/* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#setupData(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer)
     */
    @Override
    public void setupData(IPropertiesContainer data) {
    }

	/* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#extractData(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer)
     */
    @Override
    public void extractData(IPropertiesContainer data) {
    	if (data == null) return;
    	data.setProperty(ITerminalsLauncher.PROP_TERMINAL_TYPE, terminalType.getEditFieldControlText());
    }

	/* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#initializeData(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer)
     */
    @Override
    public void initializeData(IPropertiesContainer data) {
    }

	/* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#removeData(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer)
     */
    @Override
    public void removeData(IPropertiesContainer data) {
    }
}
