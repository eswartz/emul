/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
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
	private Combo fTransportTypeControl;
	private Text fAddressControl;
	private Text fPortControl;
	private Text fPeerIdControl;
	private Text fPeerNameControl;

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
		setDescription(Messages.NewTargetWizardPage_description);

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

		fTransportTypeControl = new Combo(panel, SWT.READ_ONLY);
		fTransportTypeControl.setItems(new String[] { "TCP" }); //$NON-NLS-1$
		fTransportTypeControl.select(0);
		fTransportTypeControl.setEnabled(false);
		fTransportTypeControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(panel, SWT.NONE);
		label.setText(Messages.NewTargetWizardPage_AgentHostControl_label);

		fAddressControl = new Text(panel, SWT.SINGLE | SWT.BORDER);
		fAddressControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fAddressControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updatePeerId();
			}
		});

		label = new Label(panel, SWT.NONE);
		label.setText(Messages.NewTargetWizardPage_AgentPortControl_label);

		fPortControl = new Text(panel, SWT.SINGLE | SWT.BORDER);
		fPortControl.setText("1534"); //$NON-NLS-1$
		fPortControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		fPortControl.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updatePeerId();
			}
		});

		label = new Label(panel, SWT.NONE);
		label.setText(Messages.NewTargetWizardPage_PeerIdControl_label);

		fPeerIdControl = new Text(panel, SWT.SINGLE | SWT.BORDER);
		fPeerIdControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		label = new Label(panel, SWT.NONE);
		label.setText(Messages.NewTargetWizardPage_PeerNameControl_label);

		fPeerNameControl = new Text(panel, SWT.SINGLE | SWT.BORDER);
		fPeerNameControl.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

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
		String address = fAddressControl.getText();
		String port = fPortControl.getText();
		String type = fTransportTypeControl.getText();

		if (!"".equals(address) && !"".equals(port)) { //$NON-NLS-1$ //$NON-NLS-2$
			fPeerIdControl.setText(type + ":" //$NON-NLS-1$
			                        	+ address + ":" //$NON-NLS-1$
			                        	+ port);
		} else {
			fPeerIdControl.setText(""); //$NON-NLS-1$
		}

		validatePage();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.pages.AbstractWizardPage#validatePage()
	 */
	@Override
	public void validatePage() {
		boolean valid = true;

		if ("".equals(fAddressControl.getText()) || "".equals(fPortControl)) { //$NON-NLS-1$ //$NON-NLS-2$
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

		peerAttributes.put(IPeer.ATTR_IP_HOST, fAddressControl.getText());
		peerAttributes.put(IPeer.ATTR_IP_PORT, fPortControl.getText());
		peerAttributes.put(IPeer.ATTR_ID, fPeerIdControl.getText());
		peerAttributes.put(IPeer.ATTR_NAME, fPeerNameControl.getText());
		peerAttributes.put(IPeer.ATTR_TRANSPORT_NAME, fTransportTypeControl.getText());
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
			settings.put(IPeer.ATTR_IP_HOST, fAddressControl.getText());
			settings.put(IPeer.ATTR_IP_PORT, fPortControl.getText());
			settings.put(IPeer.ATTR_ID, fPeerIdControl.getText());
			settings.put(IPeer.ATTR_NAME, fPeerNameControl.getText());
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
			if (settings.get(IPeer.ATTR_IP_HOST) != null) fAddressControl.setText(settings.get(IPeer.ATTR_IP_HOST));
			if (settings.get(IPeer.ATTR_IP_PORT) != null) fPortControl.setText(settings.get(IPeer.ATTR_IP_PORT));
			if (settings.get(IPeer.ATTR_ID) != null) fPeerIdControl.setText(settings.get(IPeer.ATTR_ID));
			if (settings.get(IPeer.ATTR_NAME) != null) fPeerNameControl.setText(settings.get(IPeer.ATTR_NAME));
		}
	}
}
