/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * Max Weninger (Wind River) - [361352] [TERMINALS][SSH] Add SSH terminal support
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.ssh.controls;

import java.util.concurrent.atomic.AtomicReference;

import org.eclipse.core.runtime.Assert;
import org.eclipse.equinox.security.storage.ISecurePreferences;
import org.eclipse.equinox.security.storage.SecurePreferencesFactory;
import org.eclipse.equinox.security.storage.StorageException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.internal.terminal.provisional.api.ISettingsPage;
import org.eclipse.tm.internal.terminal.ssh.SshConnector;
import org.eclipse.tm.internal.terminal.ssh.SshSettings;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.services.interfaces.constants.ITerminalsConnectorConstants;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.ui.controls.BaseDialogPageControl;
import org.eclipse.tm.te.ui.terminals.panels.AbstractConfigurationPanel;
import org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * SSH wizard configuration panel implementation.
 */
@SuppressWarnings("restriction")
public class SshWizardConfigurationPanel extends AbstractConfigurationPanel implements ISharedDataWizardPage {

    private SshSettings sshSettings;
	private ISettingsPage sshSettingsPage;

	/**
	 * Constructor.
	 *
	 * @param parentControl The parent control. Must not be <code>null</code>!
	 */
	public SshWizardConfigurationPanel(BaseDialogPageControl parentControl) {
	    super(parentControl);
    }

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel#setupPanel(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
	public void setupPanel(Composite parent, FormToolkit toolkit) {
		Composite panel = new Composite(parent, SWT.NONE);
		panel.setLayout(new GridLayout());
		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		panel.setLayoutData(data);

		SshConnector conn = new SshConnector();
		sshSettings = (SshSettings) conn.getSshSettings();
		sshSettings.setHost(getHost());
		sshSettings.setUser(getDefaultUser());
		sshSettingsPage = conn.makeSettingsPage();
		sshSettingsPage.createControl(panel);

		setControl(panel);
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

	/**
	 * Returns the default user name.
	 *
	 * @return The default user name.
	 */
	private String getDefaultUser(){
		return System.getProperty("user.name");
	}

	/**
	 * Returns the host name or IP from the current selection.
	 *
	 * @return The host name or IP.
	 */
	private String getHost() {
		ISelection selection = getSelection();
		final AtomicReference<String> result = new AtomicReference<String>();
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			Object element = ((IStructuredSelection) selection).getFirstElement();
			if (element instanceof IPeerModel) {
				final IPeerModel peerModel = (IPeerModel) element;
				if (Protocol.isDispatchThread()) {
					result.set(peerModel.getPeer().getAttributes().get(IPeer.ATTR_IP_HOST));
				}
				else {
					Protocol.invokeAndWait(new Runnable() {
						@Override
						public void run() {
							result.set(peerModel.getPeer().getAttributes().get(IPeer.ATTR_IP_HOST));
						}
					});
				}
			}
		}

		return result.get();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#extractData(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
    public void extractData(IPropertiesContainer data) {
    	// set the terminal connector id for ssh
    	data.setProperty(ITerminalsConnectorConstants.PROP_TERMINAL_CONNECTOR_ID, "org.eclipse.tm.internal.terminal.ssh.SshConnector");

    	// set the connector type for ssh
    	data.setProperty(ITerminalsConnectorConstants.PROP_CONNECTOR_TYPE_ID, "org.eclipse.tm.te.ui.terminals.type.ssh");

    	sshSettingsPage.saveSettings();
		data.setProperty(ITerminalsConnectorConstants.PROP_IP_HOST,sshSettings.getHost());
		data.setProperty(ITerminalsConnectorConstants.PROP_IP_PORT, sshSettings.getPort());
		data.setProperty(ITerminalsConnectorConstants.PROP_TIMEOUT, sshSettings.getTimeout());
		data.setProperty(ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE, sshSettings.getKeepalive());
		data.setProperty(ITerminalsConnectorConstants.PROP_SSH_PASSWORD, sshSettings.getPassword());
		data.setProperty(ITerminalsConnectorConstants.PROP_SSH_USER, sshSettings.getUser());
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

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		String host = getHost();
		if (settings.get(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_IP_HOST)) != null) {
			sshSettings.setHost(settings.get(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_IP_HOST)));
		}
		if (settings.get(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_IP_PORT)) != null) {
			sshSettings.setPort(settings.get(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_IP_PORT)));
		}
		if (settings.get(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_TIMEOUT)) != null) {
			sshSettings.setTimeout(settings.get(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_TIMEOUT)));
		}
		if (settings.get(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE)) != null) {
			sshSettings.setKeepalive(settings.get(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE)));
		}
		if (settings.get(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_SSH_USER)) != null) {
			sshSettings.setUser(settings.get(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_SSH_USER)));
		}
		String password = accessSecurePassword(sshSettings.getHost());
		if (password != null) {
			sshSettings.setPassword(password);
		}
		// set settings in page
		sshSettingsPage.loadSettings();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		// make sure the values are saved
		// actually not needed since this is done before in extractData
		sshSettingsPage.saveSettings();
		String host = getHost();
		settings.put(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_IP_HOST), sshSettings.getHost());
		settings.put(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_IP_PORT), sshSettings.getPort());
		settings.put(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_TIMEOUT), sshSettings.getTimeout());
		settings.put(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_SSH_KEEP_ALIVE), sshSettings.getKeepalive());
		settings.put(getSettingsKeyWithPrefix(host, ITerminalsConnectorConstants.PROP_SSH_USER), sshSettings.getUser());

		saveSecurePassword(sshSettings.getHost(), sshSettings.getPassword());
	}

	/**
	 * Constructs the full settings key.
	 */
	private String getSettingsKeyWithPrefix(String host, String value) {
		return host + "." + value;
	}

	/**
	 * Save the password to the secure storage.
	 *
	 * @param host The host. Must not be <code>null</code>.
	 * @param password The password. Must not be <code>null</code>.
	 */
	private void saveSecurePassword(String host, String password) {
		Assert.isNotNull(host);
		Assert.isNotNull(password);

		// To access the secure storage, we need the preference instance
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		if (preferences != null) {
			// Construct the secure preferences node key
			String nodeKey = "/Target Explorer SSH Password/" + host;
			ISecurePreferences node = preferences.node(nodeKey);
			if (node != null) {
				try {
					node.put("password", password, true);
				}
				catch (StorageException ex) { /* ignored on purpose */ }
			}
		}
	}

	/**
	 * Reads the password from the secure storage.
	 *
	 * @param host The host. Must not be <code>null</code>.
	 * @return The password or <code>null</code>.
	 */
	private String accessSecurePassword(String host) {
		Assert.isNotNull(host);

		// To access the secure storage, we need the preference instance
		ISecurePreferences preferences = SecurePreferencesFactory.getDefault();
		if (preferences != null) {
			// Construct the secure preferences node key
			String nodeKey = "/Target Explorer SSH Password/" + host;
			ISecurePreferences node = preferences.node(nodeKey);
			if (node != null) {
				String password = null;
				try {
					password = node.get("password", null);
				}
				catch (StorageException ex) { /* ignored on purpose */ }

				return password;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel#isValid()
	 */
	@Override
    public boolean isValid(){
		return sshSettingsPage.validateSettings();
	}
}
