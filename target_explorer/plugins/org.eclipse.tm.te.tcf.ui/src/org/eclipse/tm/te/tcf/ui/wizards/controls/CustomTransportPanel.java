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
import org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl;
import org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel;
import org.eclipse.tm.te.ui.controls.validator.RegexValidator;
import org.eclipse.tm.te.ui.controls.validator.Validator;
import org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage;
import org.eclipse.tm.te.ui.wizards.interfaces.IValidatableWizardPage;
import org.eclipse.ui.forms.widgets.FormToolkit;

/**
 * Custom transport type panel implementation.
 */
public class CustomTransportPanel extends AbstractWizardConfigurationPanel implements ISharedDataWizardPage {

	private CustomTransportNameControl customTransportNameControl;

	/**
	 * Local custom transport name control implementation.
	 */
	protected class CustomTransportNameControl extends BaseEditBrowseTextControl {

		/**
		 * Constructor.
		 *
		 * @param parentPage The parent dialog page this control is embedded in.
		 *                   Might be <code>null</code> if the control is not associated with a page.
		 */
        public CustomTransportNameControl(IDialogPage parentPage) {
	        super(parentPage);
	        setIsGroup(false);
	        setHasHistory(false);
	        setHideBrowseButton(true);
	        setEditFieldLabel(Messages.CustomTransportNameControl_label);
        }

        /* (non-Javadoc)
         * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#doCreateEditFieldValidator()
         */
        @Override
        protected Validator doCreateEditFieldValidator() {
            return new RegexValidator(Validator.ATTR_MANDATORY, ".*"); //$NON-NLS-1$
        }

        /* (non-Javadoc)
         * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#configureEditFieldValidator(org.eclipse.tm.te.ui.controls.validator.Validator)
         */
        @Override
        protected void configureEditFieldValidator(Validator validator) {
        	if (validator instanceof RegexValidator) {
        		validator.setMessageText(RegexValidator.INFO_MISSING_VALUE, Messages.CustomTransportNameControl_information_missingValue);
        		validator.setMessageText(RegexValidator.ERROR_INVALID_VALUE, Messages.CustomTransportNameControl_error_invalidValue);
        	}
        }

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.controls.BaseDialogPageControl#getValidatableWizardPage()
		 */
		@Override
		public IValidatableWizardPage getValidatableWizardPage() {
			return CustomTransportPanel.this.getParentControl().getValidatableWizardPage();
		}

		/* (non-Javadoc)
		 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#modifyText(org.eclipse.swt.events.ModifyEvent)
		 */
		@Override
		public void modifyText(ModifyEvent e) {
			super.modifyText(e);
			if (CustomTransportPanel.this.getParentControl() instanceof ModifyListener) {
				((ModifyListener)CustomTransportPanel.this.getParentControl()).modifyText(e);
			}
		}
	}

	/**
	 * Constructor.
	 *
	 * @param parentPageControl The parent control. Must not be <code>null</code>!
	 */
	public CustomTransportPanel(BaseDialogPageControl parentControl) {
		super(parentControl);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel#dispose()
	 */
	@Override
	public void dispose() {
		if (customTransportNameControl != null) { customTransportNameControl.dispose(); customTransportNameControl = null; }
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

		customTransportNameControl = doCreateCustomTransportNameControl(getParentControl().getParentPage());
		customTransportNameControl.setupPanel(panel);
	}

	/**
	 * Creates the pipe name control instance.
	 *
	 * @param parentPage The parent dialog page or <code>null</code>.
	 * @return The pipe name control instance.
	 */
	protected CustomTransportNameControl doCreateCustomTransportNameControl(IDialogPage parentPage) {
		return new CustomTransportNameControl(parentPage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel#isValid()
	 */
	@Override
	public boolean isValid() {
		boolean valid = super.isValid();
		if (!valid) return false;

		valid = customTransportNameControl.isValid();
		setMessage(customTransportNameControl.getMessage(), customTransportNameControl.getMessageType());

		return valid;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.interfaces.IWizardConfigurationPanel#dataChanged(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.swt.events.TypedEvent)
	 */
	@Override
	public boolean dataChanged(IPropertiesContainer data, TypedEvent e) {
		Assert.isNotNull(data);

		boolean isDirty = false;

		if (customTransportNameControl != null) {
			String CustomTransportName = customTransportNameControl.getEditFieldControlText();
			if (CustomTransportName != null) isDirty |= !CustomTransportName.equals(data.getStringProperty(IPeer.ATTR_TRANSPORT_NAME));
		}

		return isDirty;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#setupData(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
	public void setupData(IPropertiesContainer data) {
		if (data == null) return;

		if (customTransportNameControl != null) {
			customTransportNameControl.setEditFieldControlText(data.getStringProperty(IPeer.ATTR_TRANSPORT_NAME));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#extractData(org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer)
	 */
	@Override
	public void extractData(IPropertiesContainer data) {
		if (data == null) return;

		if (customTransportNameControl != null) {
			data.setProperty(IPeer.ATTR_TRANSPORT_NAME, customTransportNameControl.getEditFieldControlText());
		}
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
		if (data == null) return;
		data.setProperty(IPeer.ATTR_TRANSPORT_NAME, null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel#doSaveWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		super.doSaveWidgetValues(settings, idPrefix);
		if (customTransportNameControl != null) customTransportNameControl.doSaveWidgetValues(settings, idPrefix);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.panels.AbstractWizardConfigurationPanel#doRestoreWidgetValues(org.eclipse.jface.dialogs.IDialogSettings, java.lang.String)
	 */
	@Override
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		super.doRestoreWidgetValues(settings, idPrefix);
		if (customTransportNameControl != null) customTransportNameControl.doRestoreWidgetValues(settings, idPrefix);
	}
}
