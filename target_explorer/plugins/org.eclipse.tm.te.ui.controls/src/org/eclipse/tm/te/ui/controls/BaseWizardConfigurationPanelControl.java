/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Target Explorer: Base control to deal with wizard or property page controls
 * which should share the same UI space.
 */
public class BaseWizardConfigurationPanelControl extends BaseDialogPageControl {
	private final Map<String, IWizardConfigurationPanel> configurationPanels = new Hashtable<String, IWizardConfigurationPanel>();

	private boolean isGroup;

	private Composite panel;
	private StackLayout panelLayout;

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent dialog page this control is embedded in.
	 *                   Might be <code>null</code> if the control is not associated with a page.
	 */
	public BaseWizardConfigurationPanelControl(IDialogPage parentPage) {
		super(parentPage);
		clear();
		setPanelIsGroup(false);
	}

	/**
	 * Sets if or if not the controls panel is a <code>Group</code>.
	 *
	 * @param isGroup <code>True</code> if the controls panel is a group, <code>false</code> otherwise.
	 */
	public void setPanelIsGroup(boolean isGroup) {
		this.isGroup = isGroup;
	}

	/**
	 * Returns if or if not the controls panel is a <code>Group</code>.
	 *
	 * @return <code>True</code> if the controls panel is a group, <code>false</code> otherwise.
	 */
	public boolean isPanelIsGroup() {
		return isGroup;
	}

	/**
	 * Returns the controls panel.
	 *
	 * @return The controls panel or <code>null</code>.
	 */
	public Composite getPanel() {
		return panel;
	}

	/**
	 * Returns the label text to set for the group (if the panel is a group).
	 *
	 * @return The label text to apply or <code>null</code>.
	 */
	public String getGroupLabel() {
		return null;
	}

	/**
	 * To be called from the embedding control to setup the controls UI elements.
	 *
	 * @param parent The parent control. Must not be <code>null</code>!
	 * @param toolkit The form toolkit. Must not be <code>null</code>.
	 */
	public void setupPanel(Composite parent, String[] configurationPanelKeys, FormToolkit toolkit) {
		Assert.isNotNull(parent);
		Assert.isNotNull(toolkit);

		if (isPanelIsGroup()) {
			panel = new Group(parent, SWT.NONE);
			if (getGroupLabel() != null) ((Group)panel).setText(getGroupLabel());
		} else {
			panel = new Composite(parent, SWT.NONE);
		}
		Assert.isNotNull(panel);
		panel.setFont(parent.getFont());
		panel.setBackground(parent.getBackground());

		panelLayout = new StackLayout();
		panel.setLayout(panelLayout);

		setupConfigurationPanels(panel, configurationPanelKeys, toolkit);
	}

	/**
	 * Removes all configuration panels.
	 */
	public void clear() {
		configurationPanels.clear();
	}

	/**
	 * Returns a unsorted list of all registered wizard configuration
	 * panel id's.
	 *
	 * @return A list of registered wizard configuration panel id's.
	 */
	public String[] getConfigurationPanelIds() {
		return configurationPanels.keySet().toArray(new String[configurationPanels.keySet().size()]);
	}

	/**
	 * Returns the wizard configuration panel instance registered for the given configuration panel key.
	 *
	 * @param key The key to get the wizard configuration panel for. Must not be <code>null</code>!
	 * @return The wizard configuration panel instance or <code>null</code> if the key is unknown.
	 */
	public IWizardConfigurationPanel getConfigurationPanel(String key) {
		if (key == null) return null;
		return configurationPanels.get(key);
	}

	/**
	 * Adds the given wizard configuration panel under the given configuration panel key to the
	 * list of known panels. If the given configuration panel is <code>null</code>, any configuration
	 * panel stored under the given key is removed from the list of known panels.
	 *
	 * @param key The key to get the wizard configuration panel for. Must not be <code>null</code>!
	 * @param panel The wizard configuration panel instance or <code>null</code>.
	 */
	public void addConfigurationPanel(String key, IWizardConfigurationPanel panel) {
		if (key == null) return;
		if (panel != null) {
			configurationPanels.put(key, panel);
		} else {
			configurationPanels.remove(key);
		}
	}

	/**
	 * Setup the wizard configuration panels for being presented to the user. This method is called by the
	 * controls <code>doSetupPanel(...)</code> and initialize all possible wizard configuration panels to show.
	 * The default implementation iterates over the given list of configuration panel keys and calls
	 * <code>setupPanel(...)</code> for each of them.
	 *
	 * @param parent The parent composite to use for the wizard configuration panels. Must not be <code>null</code>!
	 * @param configurationPanelKeys The list of configuration panels to initialize. Might be <code>null</code> or empty!
	 * @param toolkit The form toolkit. Must not be <code>null</code>.
	 */
	public void setupConfigurationPanels(Composite parent, String[] configurationPanelKeys, FormToolkit toolkit) {
		Assert.isNotNull(parent);
		Assert.isNotNull(toolkit);

		if (configurationPanelKeys != null) {
			for (int i = 0; i < configurationPanelKeys.length; i++) {
				IWizardConfigurationPanel configPanel = getConfigurationPanel(configurationPanelKeys[i]);
				if (configPanel != null) configPanel.setupPanel(parent, toolkit);
			}
		}
	}

	/**
	 * Make the wizard configuration panel registered under the given configuration panel key the
	 * most top configuration panel. If no configuration panel is registered under the given key,
	 * nothing will happen.
	 *
	 * @param key The key to get the wizard configuration panel for. Must not be <code>null</code>!
	 */
	public void showConfigurationPanel(String key) {
		if (key == null) return;

		IWizardConfigurationPanel configPanel = getConfigurationPanel(key);
		if (configPanel != null && configPanel.getControl() != null) {
			panelLayout.topControl = configPanel.getControl();
			panel.layout();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseControl#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		super.doSaveWidgetValues(settings, idPrefix);
		if (settings != null) {
			for (String key : configurationPanels.keySet()) {
				IWizardConfigurationPanel configPanel = getConfigurationPanel(key);
				if (configPanel != null) configPanel.doSaveWidgetValues(settings, idPrefix);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseControl#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		super.doRestoreWidgetValues(settings, idPrefix);
		if (settings != null) {
			for (String key : configurationPanels.keySet()) {
				IWizardConfigurationPanel configPanel = getConfigurationPanel(key);
				if (configPanel != null) configPanel.doRestoreWidgetValues(settings, idPrefix);
			}
		}
	}
}
