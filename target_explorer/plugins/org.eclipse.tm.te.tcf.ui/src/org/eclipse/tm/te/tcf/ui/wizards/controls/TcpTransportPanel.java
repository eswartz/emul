/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.wizards.controls;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.tcf.ui.nls.Messages;
import org.eclipse.tm.te.ui.controls.BaseDialogPageControl;
import org.eclipse.tm.te.ui.controls.net.RemoteHostAddressControl;
import org.eclipse.tm.te.ui.controls.net.RemoteHostPortControl;
import org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel;
import org.eclipse.tm.te.ui.controls.validator.NameOrIPValidator;
import org.eclipse.tm.te.ui.controls.validator.Validator;
import org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage;
import org.eclipse.tm.te.ui.wizards.interfaces.IValidatableWizardPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * TCP transport type wizard configuration panel.
 */
public class TcpTransportPanel extends AbstractWizardConfigurationPanel implements ISharedDataWizardPage {

	public static final String TRANSPORT_TYPE_ID = "TCP"; //$NON-NLS-1$

	private RemoteHostAddressControl addressControl = null;
	private RemoteHostPortControl portControl = null;

	/**
	 * Local remote host address control implementation.
	 */
	protected class MyRemoteHostAddressControl extends RemoteHostAddressControl {

		/**
		 * Constructor.
		 *
		 * @param parentPage The parent dialog page this control is embedded in. Must not be <code>null</code>!
		 */
		public MyRemoteHostAddressControl(IDialogPage parentPage) {
			super(parentPage);
			setEditFieldLabel(Messages.MyRemoteHostAddressControl_label);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.controls.net.RemoteHostAddressControl#configureEditFieldValidator(org.eclipse.tm.te.ui.controls.validator.Validator)
		 */
		@Override
		protected void configureEditFieldValidator(Validator validator) {
			if (validator instanceof NameOrIPValidator) {
				validator.setMessageText(NameOrIPValidator.INFO_MISSING_NAME_OR_IP, Messages.MyRemoteHostAddressControl_information_missingTargetNameAddress);
				validator.setMessageText(NameOrIPValidator.ERROR_INVALID_NAME_OR_IP, Messages.MyRemoteHostAddressControl_error_invalidTargetNameAddress);
				validator.setMessageText(NameOrIPValidator.ERROR_INVALID_NAME, Messages.MyRemoteHostAddressControl_error_invalidTargetNameAddress);
				validator.setMessageText(NameOrIPValidator.ERROR_INVALID_IP, Messages.MyRemoteHostAddressControl_error_invalidTargetIpAddress);
				validator.setMessageText(NameOrIPValidator.INFO_CHECK_NAME, getUserInformationTextCheckNameAddress());
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.controls.net.RemoteHostAddressControl#getUserInformationTextCheckNameAddress()
		 */
		@Override
		protected String getUserInformationTextCheckNameAddress() {
			return Messages.MyRemoteHostAddressControl_information_checkNameAddressUserInformation;
		}


		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.controls.BaseDialogPageControl#getValidatableWizardPage()
		 */
		@Override
		public IValidatableWizardPage getValidatableWizardPage() {
			return TcpTransportPanel.this.getParentControl().getValidatableWizardPage();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		@Override
		public void modifyText(ModifyEvent e) {
			super.modifyText(e);
			if (TcpTransportPanel.this.getParentControl() instanceof ModifyListener) {
				((ModifyListener)TcpTransportPanel.this.getParentControl()).modifyText(e);
			}
		}
	}

	/**
	 * Local remote host port control implementation.
	 */
	protected class MyRemoteHostPortControl extends RemoteHostPortControl {

		/**
		 * Constructor.
		 *
		 * @param parentPage The parent dialog page this control is embedded in. Must not be <code>null</code>!
		 */
		public MyRemoteHostPortControl(IDialogPage parentPage) {
			super(parentPage);
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.controls.BaseDialogPageControl#getValidatableWizardPage()
		 */
		@Override
		public IValidatableWizardPage getValidatableWizardPage() {
			return TcpTransportPanel.this.getParentControl().getValidatableWizardPage();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		@Override
		public void modifyText(ModifyEvent e) {
			super.modifyText(e);
			if (TcpTransportPanel.this.getParentControl() instanceof ModifyListener) {
				((ModifyListener)TcpTransportPanel.this.getParentControl()).modifyText(e);
			}
		}
	}

	/**
	 * Constructor.
	 *
	 * @param parentPageControl The parent control. Must not be <code>null</code>!
	 */
	public TcpTransportPanel(BaseDialogPageControl parentPageControl) {
		super(parentPageControl);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel#dispose()
	 */
	@Override
	public void dispose() {
		if (addressControl != null) { addressControl.dispose(); addressControl = null; }
		if (portControl != null) { portControl.dispose(); portControl = null; }
		super.dispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel#setupPanel(org.eclipse.swt.widgets.Composite, org.eclipse.ui.forms.widgets.FormToolkit)
	 */
	@Override
    public void setupPanel(Composite parent, FormToolkit toolkit) {
		Assert.isNotNull(parent);
		Assert.isNotNull(toolkit);

		boolean adjustBackgroundColor = getParentControl().getParentPage() != null;

		Composite panel = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0; layout.marginWidth = 0;
		panel.setLayout(layout);
		panel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		if (adjustBackgroundColor) panel.setBackground(parent.getBackground());

		setControl(panel);

		addressControl = doCreateAddressControl(getParentControl().getParentPage());
		addressControl.setupPanel(panel);

		portControl = doCreatePortControl(getParentControl().getParentPage());
		portControl.setParentControlIsInnerPanel(true);
		portControl.setupPanel(addressControl.getInnerPanelComposite());
		portControl.setEditFieldControlText("1534"); //$NON-NLS-1$
	}

	/**
	 * Creates the address control instance.
	 *
	 * @param parentPage The parent dialog page or <code>null</code>.
	 * @return The address control instance.
	 */
	protected RemoteHostAddressControl doCreateAddressControl(IDialogPage parentPage) {
		return new MyRemoteHostAddressControl(parentPage);
	}

	/**
	 * Creates the port control instance.
	 *
	 * @param parentPage The parent dialog page or <code>null</code>.
	 * @return The port control instance.
	 */
	protected RemoteHostPortControl doCreatePortControl(IDialogPage parentPage) {
		return new MyRemoteHostPortControl(parentPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel#isValid()
	 */
	@Override
	public boolean isValid() {
		boolean valid = super.isValid();
		if (!valid) return false;

		valid = addressControl.isValid();
		setMessage(addressControl.getMessage(), addressControl.getMessageType());

		valid &= portControl.isValid();
		if (portControl.getMessageType() > getMessageType()) {
			setMessage(portControl.getMessage(), portControl.getMessageType());
		}

		return valid;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel#dataChanged(org.eclipse.tm.te.runtime.interfaces.nodes.IPropertiesContainer, org.eclipse.swt.events.TypedEvent)
	 */
	@Override
    public boolean dataChanged(IPropertiesContainer data, TypedEvent e) {
		Assert.isNotNull(data);

		boolean isDirty = false;

		if (addressControl != null) {
			String address = addressControl.getEditFieldControlText();
			if (address != null) isDirty |= !address.equals(data.getStringProperty(IPeer.ATTR_IP_HOST));
		}

		if (portControl != null) {
			String port = portControl.getEditFieldControlText();
			if (port != null) isDirty |= !port.equals(data.getStringProperty(IPeer.ATTR_IP_PORT));
		}

		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#setupData(org.eclipse.tm.te.runtime.interfaces.nodes.IPropertiesContainer)
	 */
	@Override
    public void setupData(IPropertiesContainer data) {
		if (data == null) return;

		if (addressControl != null) {
			addressControl.setEditFieldControlText(data.getStringProperty(IPeer.ATTR_IP_HOST));
		}

		if (portControl != null) {
			portControl.setEditFieldControlText(data.getStringProperty(IPeer.ATTR_IP_PORT));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#extractData(org.eclipse.tm.te.runtime.interfaces.nodes.IPropertiesContainer)
	 */
	@Override
    public void extractData(IPropertiesContainer data) {
		if (data == null) return;

		if (addressControl != null) {
			data.setProperty(IPeer.ATTR_IP_HOST, addressControl.getEditFieldControlText());
		}

		if (portControl != null) {
			data.setProperty(IPeer.ATTR_IP_PORT, portControl.getEditFieldControlText());
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#initializeData(org.eclipse.tm.te.runtime.interfaces.nodes.IPropertiesContainer)
	 */
	@Override
    public void initializeData(IPropertiesContainer data) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#removeData(org.eclipse.tm.te.runtime.interfaces.nodes.IPropertiesContainer)
	 */
	@Override
    public void removeData(IPropertiesContainer data) {
		if (data == null) return;
		data.setProperty(IPeer.ATTR_IP_HOST, null);
		data.setProperty(IPeer.ATTR_IP_PORT, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		super.doSaveWidgetValues(settings, idPrefix);
		if (addressControl != null) addressControl.doSaveWidgetValues(settings, idPrefix);
		if (portControl != null) portControl.doSaveWidgetValues(settings, idPrefix);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		super.doRestoreWidgetValues(settings, idPrefix);
		if (addressControl != null) addressControl.doRestoreWidgetValues(settings, idPrefix);
		if (portControl != null) portControl.doRestoreWidgetValues(settings, idPrefix);
	}
}
