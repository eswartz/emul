/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.internal.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.properties.PropertiesContainer;
import org.eclipse.tm.te.ui.controls.BaseDialogPageControl;
import org.eclipse.tm.te.ui.controls.BaseWizardConfigurationPanelControl;
import org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel;
import org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel;
import org.eclipse.tm.te.ui.jface.dialogs.CustomTrayDialog;
import org.eclipse.tm.te.ui.swt.SWTControlUtil;
import org.eclipse.tm.te.ui.terminals.help.IContextHelpIds;
import org.eclipse.tm.te.ui.terminals.interfaces.ILauncherDelegate;
import org.eclipse.tm.te.ui.terminals.launcher.LauncherDelegateManager;
import org.eclipse.tm.te.ui.terminals.nls.Messages;
import org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Launch terminal settings dialog implementation.
 */
public class LaunchTerminalSettingsDialog extends CustomTrayDialog {
	// The parent selection
	private ISelection selection = null;

	// The subcontrols
	/* default */ Combo terminals;
	/* default */ SettingsPanelControl settings;

	// Map the label added to the combobox to the corresponding launcher delegate.
	private final Map<String, ILauncherDelegate> label2delegate = new HashMap<String, ILauncherDelegate>();

	// The data object containing the currently selected settings
	private IPropertiesContainer data = null;

	/**
	 * The control managing the terminal setting panels.
	 */
	protected class SettingsPanelControl extends BaseWizardConfigurationPanelControl {

		/**
		 * Constructor.
		 *
		 * @param parentPage The parent dialog page this control is embedded in.
		 *                   Might be <code>null</code> if the control is not associated with a page.
		 */
        public SettingsPanelControl(IDialogPage parentPage) {
	        super(parentPage);
	        setPanelIsGroup(true);
        }

        /* (non-Javadoc)
         * @see org.eclipse.tm.te.ui.controls.BaseWizardConfigurationPanelControl#getGroupLabel()
         */
        @Override
        public String getGroupLabel() {
            return Messages.LaunchTerminalSettingsDialog_group_label;
        }
	}

	/**
	 * An empty terminal settings panel.
	 */
	protected class EmptySettingsPanel extends AbstractWizardConfigurationPanel {

		/**
	     * Constructor.
	     *
		 * @param parentControl The parent control. Must not be <code>null</code>!
	     */
	    public EmptySettingsPanel(BaseDialogPageControl parentControl) {
		    super(parentControl);
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
	    }

	    /* (non-Javadoc)
	     * @see org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel#dataChanged(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.swt.events.TypedEvent)
	     */
	    @Override
	    public boolean dataChanged(IPropertiesContainer data, TypedEvent e) {
	        return false;
	    }
	}

	/**
     * Constructor.
     *
	 * @param shell The parent shell or <code>null</code>.
     */
    public LaunchTerminalSettingsDialog(Shell shell) {
	    super(shell, IContextHelpIds.LAUNCH_TERMINAL_SETTINGS_DIALOG);
    }

    /**
     * Sets the parent selection.
     *
     * @param selection The parent selection or <code>null</code>.
     */
    public void setSelection(ISelection selection) {
    	this.selection = selection;
    }

    /**
     * Returns the parent selection.
     *
     * @return The parent selection or <code>null</code>.
     */
    public ISelection getSelection() {
    	return selection;
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.jface.dialogs.CustomTrayDialog#dispose()
     */
    @Override
    protected void dispose() {
    	if (settings != null) { settings.dispose(); settings = null; }
        super.dispose();
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.jface.dialogs.CustomTrayDialog#createDialogArea(org.eclipse.swt.widgets.Composite)
     */
    @Override
    protected Control createDialogArea(Composite parent) {
    	setDialogTitle(Messages.LaunchTerminalSettingsDialog_title);

        Composite composite = (Composite)super.createDialogArea(parent);

        Composite panel = new Composite(composite, SWT.NONE);
        GridLayout layout = new GridLayout(2, false);
        layout.marginHeight = 0; layout.marginWidth = 0;
        panel.setLayout(layout);
        panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

        Label label = new Label(panel, SWT.HORIZONTAL);
        label.setText(Messages.LaunchTerminalSettingsDialog_combo_label);

        terminals = new Combo(panel, SWT.READ_ONLY);
        terminals.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        terminals.addSelectionListener(new SelectionAdapter() {
        	@Override
        	public void widgetSelected(SelectionEvent e) {
        		settings.showConfigurationPanel(terminals.getText());
        	}
		});

        // fill the combo with content
        fillCombo(terminals);

        // Create the settings panel control
        settings = new SettingsPanelControl(null);

		// Create and add the panels
        for (String terminalLabel : terminals.getItems()) {
        	// Get the corresponding delegate
        	ILauncherDelegate delegate = label2delegate.get(terminalLabel);
        	Assert.isNotNull(delegate);
        	// Get the wizard configuration panel instance
        	IWizardConfigurationPanel configPanel = delegate.getPanel(settings);
        	if (configPanel == null) configPanel = new EmptySettingsPanel(settings);
        	// Add it
        	settings.addConfigurationPanel(terminalLabel, configPanel);
        }

		// Setup the panel control
		settings.setupPanel(panel, terminals.getItems(), new FormToolkit(panel.getDisplay()));
		GridData layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalSpan = 2;
		layoutData.widthHint = convertWidthInCharsToPixels(30);
		layoutData.heightHint = convertHeightInCharsToPixels(5);
		settings.getPanel().setLayoutData(layoutData);

		// Preselect the first terminal launcher
		terminals.select(0);
		settings.showConfigurationPanel(terminals.getText());

		SWTControlUtil.setEnabled(terminals, terminals.getItemCount() > 1);

		restoreWidgetValues();

        applyDialogFont(composite);
        return composite;
    }

    /**
     * Fill the given combo with content. The content are the terminal
     * launcher delegate labels.
     *
     * @param combo The combo. Must not be <code>null</code>.
     */
    protected void fillCombo(Combo combo) {
    	Assert.isNotNull(combo);

    	List<String> items = new ArrayList<String>();

    	ILauncherDelegate[] delegates = LauncherDelegateManager.getInstance().getApplicableLauncherDelegates(selection);
    	for (ILauncherDelegate delegate : delegates) {
    		String label = delegate.getLabel();
    		if (label == null || "".equals(label.trim())) label = delegate.getId(); //$NON-NLS-1$
    		label2delegate.put(label, delegate);
    		items.add(label);
    	}

    	Collections.sort(items);
    	combo.setItems(items.toArray(new String[items.size()]));
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.jface.dialogs.CustomTrayDialog#saveWidgetValues()
     */
    @Override
    protected void saveWidgetValues() {
    	IDialogSettings settings = getDialogSettings();
    	if (settings != null) {
    		settings.put("terminalLabel", SWTControlUtil.getText(terminals)); //$NON-NLS-1$
    		this.settings.saveWidgetValues(settings, null);
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.jface.dialogs.CustomTrayDialog#restoreWidgetValues()
     */
    @Override
    protected void restoreWidgetValues() {
    	IDialogSettings settings = getDialogSettings();
    	if (settings != null) {
    		String terminalLabel = settings.get("terminalLabel"); //$NON-NLS-1$
    		int index = terminalLabel != null ? Arrays.asList(terminals.getItems()).indexOf(terminalLabel) : -1;
    		if (index != -1) {
    			terminals.select(index);
    			this.settings.showConfigurationPanel(terminals.getText());
    		}

    		this.settings.restoreWidgetValues(settings, null);
    	}
    }

    /* (non-Javadoc)
     * @see org.eclipse.tm.te.ui.jface.dialogs.CustomTrayDialog#okPressed()
     */
    @Override
    protected void okPressed() {
    	data = new PropertiesContainer();

    	// Store the id of the selected delegate
    	data.setProperty("delegateId", label2delegate.get(terminals.getText()).getId()); //$NON-NLS-1$
    	// Store the selection
    	data.setProperty("selection", selection); //$NON-NLS-1$

    	// Store the delegate specific settings
    	IWizardConfigurationPanel panel = this.settings.getConfigurationPanel(terminals.getText());
    	if (panel instanceof ISharedDataWizardPage) {
    		((ISharedDataWizardPage)panel).extractData(data);
    	}

        super.okPressed();
    }

    /**
     * Returns the configured terminal launcher settings.
     * <p>
     * The settings are extracted from the UI widgets once
     * OK got pressed.
     *
     * @return The configured terminal launcher settings or <code>null</code>.
     */
    public IPropertiesContainer getSettings() {
    	return data;
    }
}
