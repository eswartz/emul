/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.wizards.pages;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.properties.PropertiesContainer;
import org.eclipse.tm.te.tcf.core.interfaces.ITransportTypes;
import org.eclipse.tm.te.tcf.ui.internal.help.IContextHelpIds;
import org.eclipse.tm.te.tcf.ui.nls.Messages;
import org.eclipse.tm.te.tcf.ui.wizards.controls.CustomTransportPanel;
import org.eclipse.tm.te.tcf.ui.wizards.controls.PeerAttributesTablePart;
import org.eclipse.tm.te.tcf.ui.wizards.controls.PeerIdControl;
import org.eclipse.tm.te.tcf.ui.wizards.controls.PeerNameControl;
import org.eclipse.tm.te.tcf.ui.wizards.controls.PipeTransportPanel;
import org.eclipse.tm.te.tcf.ui.wizards.controls.TcpTransportPanel;
import org.eclipse.tm.te.tcf.ui.wizards.controls.TransportTypeControl;
import org.eclipse.tm.te.tcf.ui.wizards.controls.TransportTypePanelControl;
import org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel;
import org.eclipse.tm.te.ui.forms.FormLayoutFactory;
import org.eclipse.tm.te.ui.swt.SWTControlUtil;
import org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage;
import org.eclipse.tm.te.ui.wizards.pages.AbstractValidatableWizardPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;

/**
 * Wizard page implementation querying all information needed
 * to create the different TCF peer types.
 */
public class NewTargetWizardPage extends AbstractValidatableWizardPage {
	private PeerIdControl peerIdControl;
	private PeerNameControl peerNameControl;
	TransportTypeControl transportTypeControl;
	TransportTypePanelControl transportTypePanelControl;
	private PeerAttributesTablePart tablePart;

	private FormToolkit toolkit = null;

	/**
	 * Local transport type control implementation.
	 */
	private class MyTransportTypeControl extends TransportTypeControl {

		/**
		 * Constructor.
		 *
		 * @param parentPage The parent dialog page this control is embedded in.
		 *                   Might be <code>null</code> if the control is not associated with a page.
		 */
		public MyTransportTypeControl(IDialogPage parentPage) {
			super(parentPage);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#widgetSelected(org.eclipse.swt.events.SelectionEvent)
		 */
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (transportTypePanelControl != null) {
				transportTypePanelControl.showConfigurationPanel(getSelectedTransportType());
				validatePage();
			}
		}
	}

	/**
	 * Local transport type panel control implementation.
	 */
	private class MyTransportTypePanelControl extends TransportTypePanelControl {

		/**
		 * Constructor.
		 *
		 * @param parentPage The parent dialog page this control is embedded in.
		 *                   Might be <code>null</code> if the control is not associated with a page.
		 */
		public MyTransportTypePanelControl(IDialogPage parentPage) {
			super(parentPage);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.controls.BaseControl#isValid()
		 */
		@Override
		public boolean isValid() {
			boolean valid = super.isValid();
			if (!valid) return false;

			// Get the currently selected transport type
			if (transportTypeControl != null) {
				String transportType = transportTypeControl.getSelectedTransportType();
				if (transportType != null) {
					// get the panel for the transport type and validate the panel
					IWizardConfigurationPanel panel = getConfigurationPanel(transportType);

					if (panel != null) {
						valid = panel.isValid();
						setMessage(panel.getMessage(), panel.getMessageType());
					}
				}
			}

			return valid;
		}
	}

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
	 * @see org.eclipse.jface.dialogs.DialogPage#dispose()
	 */
	@Override
	public void dispose() {
		if (peerIdControl != null) { peerIdControl.dispose(); peerIdControl = null; }
		if (peerNameControl != null) { peerNameControl.dispose(); peerNameControl = null; }
		if (transportTypeControl != null) { transportTypeControl.dispose(); transportTypeControl = null; }
		if (transportTypePanelControl != null) { transportTypePanelControl.dispose(); transportTypePanelControl = null; }
		if (tablePart != null) { tablePart.dispose(); tablePart = null; }

	    super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IDialogPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		// Setup title and description
		setTitle(Messages.NewTargetWizardPage_title);
		setDescription(Messages.NewTargetWizardPage_description);

		// Create the forms toolkit
		toolkit = new FormToolkit(parent.getDisplay());

		// Create the main panel
		Composite mainPanel = toolkit.createComposite(parent);
		mainPanel.setLayout(FormLayoutFactory.createClearGridLayout(false, 1));
		mainPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		mainPanel.setBackground(parent.getBackground());

		setControl(mainPanel);

		// Setup the help
		PlatformUI.getWorkbench().getHelpSystem().setHelp(mainPanel, IContextHelpIds.NEW_TARGET_WIZARD_PAGE);

		// Do not validate the page while creating the controls
		boolean changed = setValidationInProgress(true);
		// Create the main panel sub controls
		createMainPanelControls(mainPanel, toolkit);
		// Reset the validation in progress state
		if (changed) setValidationInProgress(false);

		// Adjust the font
		Dialog.applyDialogFont(mainPanel);

		// Validate the page for the first time
		validatePage();
	}

	/**
	 * Creates the main panel sub controls.
	 *
	 * @param parent The parent main panel composite. Must not be <code>null</code>.
	 * @param toolkit The form toolkit. Must not be <code>null</code>.
	 */
	protected void createMainPanelControls(Composite parent, FormToolkit toolkit) {
		Assert.isNotNull(parent);

		// Create the client composite
		Composite client = toolkit.createComposite(parent);
		client.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, 2));
		client.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		client.setBackground(parent.getBackground());

		// Add the controls
		peerIdControl = new PeerIdControl(this);
		peerIdControl.setFormToolkit(toolkit);
		peerIdControl.setParentControlIsInnerPanel(true);
		peerIdControl.setupPanel(client);
		peerIdControl.getEditFieldControl().setEnabled(false);
		peerIdControl.setEditFieldControlText(UUID.randomUUID().toString());

		peerNameControl = new PeerNameControl(this);
		peerNameControl.setFormToolkit(toolkit);
		peerNameControl.setParentControlIsInnerPanel(true);
		peerNameControl.setupPanel(client);
		peerNameControl.getEditFieldControl().setFocus();

		createEmptySpace(client, 5, 2, toolkit);

		// Create and configure the transport type section
		Section transportTypeSection = toolkit.createSection(client, ExpandableComposite.TITLE_BAR);
		transportTypeSection.setText(Messages.NewTargetWizardPage_section_transportType);
		transportTypeSection.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, 2));
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.horizontalSpan = 2;
		transportTypeSection.setLayoutData(layoutData);
		transportTypeSection.setBackground(client.getBackground());

		Composite transportTypeClient = toolkit.createComposite(transportTypeSection);
		transportTypeClient.setLayout(new GridLayout());
		transportTypeClient.setBackground(transportTypeSection.getBackground());
		transportTypeSection.setClient(transportTypeClient);

		// Create the transport type control
		transportTypeControl = new MyTransportTypeControl(this);
		transportTypeControl.setFormToolkit(toolkit);
		transportTypeControl.setupPanel(transportTypeClient);

		// The transport type specific controls are placed into a stack
		transportTypePanelControl = new MyTransportTypePanelControl(this);

		// Create and add the panels
		TcpTransportPanel tcpTransportPanel = new TcpTransportPanel(transportTypePanelControl);
		transportTypePanelControl.addConfigurationPanel(ITransportTypes.TRANSPORT_TYPE_TCP, tcpTransportPanel);
		transportTypePanelControl.addConfigurationPanel(ITransportTypes.TRANSPORT_TYPE_SSL, tcpTransportPanel);
		transportTypePanelControl.addConfigurationPanel(ITransportTypes.TRANSPORT_TYPE_PIPE, new PipeTransportPanel(transportTypePanelControl));
		transportTypePanelControl.addConfigurationPanel(ITransportTypes.TRANSPORT_TYPE_CUSTOM, new CustomTransportPanel(transportTypePanelControl));

		// Setup the panel control
		transportTypePanelControl.setupPanel(transportTypeClient, TransportTypeControl.TRANSPORT_TYPES, toolkit);
		layoutData = new GridData(SWT.FILL, SWT.FILL, true, true);
		layoutData.horizontalSpan = 2;
		transportTypePanelControl.getPanel().setLayoutData(layoutData);
		toolkit.adapt(transportTypePanelControl.getPanel());

		transportTypePanelControl.showConfigurationPanel(transportTypeControl.getSelectedTransportType());

		// Create the advanced peer properties table
		createPeerAttributesTableControl(client, toolkit);

		// restore the widget values from the history
		restoreWidgetValues();
	}

	/**
	 * Creates the peer attributes table controls.
	 *
	 * @param parent The parent composite. Must not be <code>null</code>.
	 * @param toolkit The form toolkit. Must not be <code>null</code>.
	 */
	protected void createPeerAttributesTableControl(Composite parent, FormToolkit toolkit) {
		Assert.isNotNull(parent);

		createEmptySpace(parent, 5, 2, toolkit);

		// Create and configure the advanced attributes section
		Section attributesSection = toolkit.createSection(parent, ExpandableComposite.TITLE_BAR);
		attributesSection.setText(Messages.NewTargetWizardPage_section_attributes);
		attributesSection.setLayout(FormLayoutFactory.createSectionClientGridLayout(false, 2));
		GridData layoutData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		layoutData.horizontalSpan = 2;
		attributesSection.setLayoutData(layoutData);
		attributesSection.setBackground(parent.getBackground());

		Composite client = toolkit.createComposite(attributesSection);
		client.setLayout(new GridLayout(2, false));
		client.setBackground(attributesSection.getBackground());
		attributesSection.setClient(client);

		tablePart = new PeerAttributesTablePart();
		tablePart.setMinSize(SWTControlUtil.convertWidthInCharsToPixels(client, 20), SWTControlUtil.convertHeightInCharsToPixels(client, 6));
		tablePart.setBannedNames(new String[] { IPeer.ATTR_ID, IPeer.ATTR_AGENT_ID, IPeer.ATTR_SERVICE_MANGER_ID, IPeer.ATTR_NAME, IPeer.ATTR_TRANSPORT_NAME, IPeer.ATTR_IP_HOST, IPeer.ATTR_IP_PORT, "PipeName" }); //$NON-NLS-1$
		tablePart.createControl(client, SWT.SINGLE | SWT.FULL_SELECTION, 2, toolkit);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.wizards.pages.AbstractValidatableWizardPage#validatePage()
	 */
	@Override
	public void validatePage() {
		super.validatePage();
		if (!isPageComplete()) return;

		if (isValidationInProgress()) return;
		setValidationInProgress(true);

		boolean valid = true;

		if (peerIdControl != null) {
			valid &= peerIdControl.isValid();
			setMessage(peerIdControl.getMessage(), peerIdControl.getMessageType());
		}

		if (peerNameControl != null) {
			valid &= peerNameControl.isValid();
			if (peerNameControl.getMessageType() > getMessageType()) {
				setMessage(peerNameControl.getMessage(), peerNameControl.getMessageType());
			}
		}

		if (transportTypeControl != null) {
			valid &= transportTypeControl.isValid();
			if (transportTypeControl.getMessageType() > getMessageType()) {
				setMessage(transportTypeControl.getMessage(), transportTypeControl.getMessageType());
			}
		}

		if (transportTypePanelControl != null) {
			valid &= transportTypePanelControl.isValid();
			if (transportTypePanelControl.getMessageType() > getMessageType()) {
				setMessage(transportTypePanelControl.getMessage(), transportTypePanelControl.getMessageType());
			}
		}

		setPageComplete(valid);
		setValidationInProgress(false);
	}

	/**
	 * Updates the given attributes map with the current control content.
	 *
	 * @param peerAttributes The peer attributes map to update. Must not be <code>null</code>.
	 */
	protected void updatePeerAttributes(Map<String, String> peerAttributes) {
		Assert.isNotNull(peerAttributes);

		peerAttributes.put(IPeer.ATTR_ID, peerIdControl.getEditFieldControlText());

		String value = peerNameControl.getEditFieldControlText();
		if (value != null && !"".equals(value)) peerAttributes.put(IPeer.ATTR_NAME, value); //$NON-NLS-1$

		value = transportTypeControl.getSelectedTransportType();
		if (value != null && !"".equals(value) && !ITransportTypes.TRANSPORT_TYPE_CUSTOM.equals(value)) { //$NON-NLS-1$
			peerAttributes.put(IPeer.ATTR_TRANSPORT_NAME, value);
		}

		IWizardConfigurationPanel panel = transportTypePanelControl.getConfigurationPanel(value);
		if (panel instanceof ISharedDataWizardPage) {
			IPropertiesContainer data = new PropertiesContainer();
			((ISharedDataWizardPage)panel).extractData(data);

			// Copy all string properties to the peer attributes map
			for (String key : data.getProperties().keySet()) {
				value = data.getStringProperty(key);
				if (value != null && !"".equals(value)) peerAttributes.put(key, value); //$NON-NLS-1$
			}
		}

		Map<String, String> additionalAttributes = tablePart.getAttributes();
		if (additionalAttributes != null && !additionalAttributes.isEmpty()) {
			peerAttributes.putAll(additionalAttributes);
		}
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
			if (peerIdControl != null) peerIdControl.saveWidgetValues(settings, null);
			if (peerNameControl != null) peerNameControl.saveWidgetValues(settings, null);
			if (transportTypeControl != null) transportTypeControl.saveWidgetValues(settings, null);
			if (transportTypePanelControl != null) transportTypePanelControl.saveWidgetValues(settings, null);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.pages.AbstractWizardPage#restoreWidgetValues()
	 */
	@Override
	public void restoreWidgetValues() {
		IDialogSettings settings = getDialogSettings();
		if (settings != null) {
			if (peerIdControl != null) peerIdControl.restoreWidgetValues(settings, null);
			if (peerNameControl != null) peerNameControl.restoreWidgetValues(settings, null);
			if (transportTypeControl != null) transportTypeControl.restoreWidgetValues(settings, null);
			if (transportTypePanelControl != null) transportTypePanelControl.restoreWidgetValues(settings, null);
		}
	}
}
