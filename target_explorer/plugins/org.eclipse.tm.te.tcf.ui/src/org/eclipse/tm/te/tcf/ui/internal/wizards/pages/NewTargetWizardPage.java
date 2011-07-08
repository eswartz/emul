/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.wizards.pages;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.te.tcf.ui.internal.help.IContextHelpIds;
import org.eclipse.tm.te.tcf.ui.internal.nls.Messages;
import org.eclipse.tm.te.ui.wizards.pages.AbstractWizardPage;
import org.eclipse.ui.PlatformUI;

/**
 * Wizard page implementation querying all information needed
 * to create the different TCF peer types.
 */
public class NewTargetWizardPage extends AbstractWizardPage {
	private Combo transportTypeControl;
	private Text addressControl;
	private Text portControl;
	private Text peerIdControl;
	private Text peerNameControl;

	/**
	 * Constructor.
	 */
	public NewTargetWizardPage() {
		this(NewTargetWizardPage.class.getName());
	}

	/**
	 * Constructor.
	 *
	 * @param pageName The page name. Must not be <code>null</code>.
	 */
	public NewTargetWizardPage(String pageName) {
		super(pageName);
	}


	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		// Setup title and description
		setTitle(Messages.NewTargetWizardPage_title);

		// Create the main panel
		Composite mainPanel = new Composite(parent, SWT.NONE);
		mainPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainPanel.setLayout(new GridLayout());

		setControl(mainPanel);

		// Setup the help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(mainPanel, IContextHelpIds.NEW_TARGET_WIZARD_PAGE);

		// Create the main panel sub controls
		createMainPanelControls(mainPanel);

		// Adjust the font
		Dialog.applyDialogFont(mainPanel);
	}

	/**
	 * Creates the main panel sub controls.
	 *
	 * @param parent The parent main panel composite. Must not be <code>null</code>.
	 */
	protected void createMainPanelControls(Composite parent) {
		Assert.isNotNull(parent);

		Composite panel = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(2, false);
		layout.marginHeight = 0; layout.marginWidth = 0;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// Add the controls
		Label label = new Label(panel, SWT.NONE);
		label.setText(Messages.NewTargetWizardPage_TransportTypeControl_label);

		transportTypeControl = new Combo(panel, SWT.READ_ONLY);
		transportTypeControl.setItems(new String[] { "TCP" }); //$NON-NLS-1$
		transportTypeControl.select(0);
		transportTypeControl.setEnabled(false);
		transportTypeControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(panel, SWT.NONE);
		label.setText(Messages.NewTargetWizardPage_AgentHostControl_label);

		addressControl = new Text(panel, SWT.SINGLE | SWT.BORDER);
		addressControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		addressControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updatePeerId();
			}
		});

		label = new Label(panel, SWT.NONE);
		label.setText(Messages.NewTargetWizardPage_AgentPortControl_label);

		portControl = new Text(panel, SWT.SINGLE | SWT.BORDER);
		portControl.setText("1534"); //$NON-NLS-1$
		portControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		portControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updatePeerId();
			}
		});

		label = new Label(panel, SWT.NONE);
		label.setText(Messages.NewTargetWizardPage_PeerIdControl_label);

		peerIdControl = new Text(panel, SWT.SINGLE | SWT.BORDER);
		peerIdControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(panel, SWT.NONE);
		label.setText(Messages.NewTargetWizardPage_PeerNameControl_label);

		peerNameControl = new Text(panel, SWT.SINGLE | SWT.BORDER);
		peerNameControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		// Restore the page history and trigger
		// an update of the peer id
		setupContent();
	}

	/**
	 * Setup the control content.
	 */
	protected void setupContent() {
		restoreWidgetValues();
		updatePeerId();
	}

	/**
	 * Update peer id control.
	 */
	protected void updatePeerId() {
		String address = addressControl.getText();
		String port = portControl.getText();
		String type = transportTypeControl.getText();

		if (!"".equals(address) && !"".equals(port)) { //$NON-NLS-1$ //$NON-NLS-2$
			peerIdControl.setText(type + ":" //$NON-NLS-1$
			                        	+ address + ":" //$NON-NLS-1$
			                        	+ port);
		} else {
			peerIdControl.setText(""); //$NON-NLS-1$
		}

		validatePage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.pages.AbstractWizardPage#validatePage()
	 */
	@Override
	public void validatePage() {
		boolean valid = true;

		if ("".equals(addressControl.getText()) || "".equals(portControl)) { //$NON-NLS-1$ //$NON-NLS-2$
			valid = false;
		}

		setPageComplete(valid);
	}

	/**
	 * Updates the given attributes map with the current control content.
	 *
	 * @param peerAttributes The peer attributes map to update. Must not be <code>null</code>.
	 */
	protected void updatePeerAttributes(Map<String, String> peerAttributes) {
		Assert.isNotNull(peerAttributes);

		peerAttributes.put(IPeer.ATTR_IP_HOST, addressControl.getText());
		peerAttributes.put(IPeer.ATTR_IP_PORT, portControl.getText());
		peerAttributes.put(IPeer.ATTR_ID, peerIdControl.getText());
		peerAttributes.put(IPeer.ATTR_NAME, peerNameControl.getText());
		peerAttributes.put(IPeer.ATTR_TRANSPORT_NAME, transportTypeControl.getText());
	}

	/**
	 * Returns the peer attributes.
	 *
	 * @return The peer attributes or <code>null</code> if canceled.
	 */
	public final Map<String, String> getPeerAttributes() {
		// Create a new peer attributes map
		Map<String, String> peerAttributes = new HashMap<String, String>();
		// Update with the current control content
		updatePeerAttributes(peerAttributes);

		return peerAttributes;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.pages.AbstractWizardPage#saveWidgetValues()
	 */
	@Override
	public void saveWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			// The transport type control is not saved
			settings.put(IPeer.ATTR_IP_HOST, addressControl.getText());
			settings.put(IPeer.ATTR_IP_PORT, portControl.getText());
			settings.put(IPeer.ATTR_ID, peerIdControl.getText());
			settings.put(IPeer.ATTR_NAME, peerNameControl.getText());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.pages.AbstractWizardPage#restoreWidgetValues()
	 */
	@Override
	public void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			// The transport type control is not restored
			if (settings.get(IPeer.ATTR_IP_HOST) != null) addressControl.setText(settings.get(IPeer.ATTR_IP_HOST));
			if (settings.get(IPeer.ATTR_IP_PORT) != null) portControl.setText(settings.get(IPeer.ATTR_IP_PORT));
			if (settings.get(IPeer.ATTR_ID) != null) peerIdControl.setText(settings.get(IPeer.ATTR_ID));
			if (settings.get(IPeer.ATTR_NAME) != null) peerNameControl.setText(settings.get(IPeer.ATTR_NAME));
		}
	}
}
