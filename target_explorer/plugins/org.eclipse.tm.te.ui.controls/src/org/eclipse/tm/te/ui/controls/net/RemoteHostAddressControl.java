/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls.net;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl;
import org.eclipse.tm.te.ui.controls.activator.UIPlugin;
import org.eclipse.tm.te.ui.controls.nls.Messages;
import org.eclipse.tm.te.ui.controls.validator.NameOrIPValidator;
import org.eclipse.tm.te.ui.controls.validator.NameOrIPVerifyListener;
import org.eclipse.tm.te.ui.controls.validator.Validator;


/**
 * Target Explorer: Basic remote host name or IP-address control.
 */
public class RemoteHostAddressControl extends BaseEditBrowseTextControl {

	/**
	 * Constructor.
	 *
	 * @param parentPage The parent dialog page this control is embedded in. Must not be <code>null</code>!
	 */
	public RemoteHostAddressControl(IDialogPage parentPage) {
		super(parentPage);
		setIsGroup(false);
		setHasHistory(false);
		setEditFieldLabel(Messages.RemoteHostAddressControl_label);
		setButtonLabel(Messages.RemoteHostAddressControl_button_label);
		setAdjustBackgroundColor(parentPage != null);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#onButtonControlSelected()
	 */
	@Override
	protected void onButtonControlSelected() {
		onCheckAddress();
		getButtonControl().setEnabled(false);
		// Reset the validation message.
		if (getMessage() != null && getMessage().equals(getUserInformationTextCheckNameAddress())) {
			setMessage(null, IMessageProvider.NONE);
			if (getEditFieldControlDecoration() != null) {
				getEditFieldControlDecoration().hide();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#doCreateEditFieldValidator()
	 */
	@Override
	protected Validator doCreateEditFieldValidator() {
		return new NameOrIPValidator(
			Validator.ATTR_MANDATORY |
			NameOrIPValidator.ATTR_IP |
			NameOrIPValidator.ATTR_NAME |
			NameOrIPValidator.ATTR_CHECK_AVAILABLE);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#configureEditFieldValidator(org.eclipse.tm.te.ui.controls.validator.Validator)
	 */
	@Override
	protected void configureEditFieldValidator(Validator validator) {
		if (validator != null && validator instanceof NameOrIPValidator) {
			validator.setMessageText(NameOrIPValidator.INFO_MISSING_NAME_OR_IP, Messages.RemoteHostAddressControl_information_missingTargetNameAddress);
			validator.setMessageText(NameOrIPValidator.ERROR_INVALID_NAME_OR_IP, Messages.RemoteHostAddressControl_error_invalidTargetNameAddress);
			validator.setMessageText(NameOrIPValidator.ERROR_INVALID_NAME, Messages.RemoteHostAddressControl_error_invalidTargetNameAddress);
			validator.setMessageText(NameOrIPValidator.ERROR_INVALID_IP, Messages.RemoteHostAddressControl_error_invalidTargetIpAddress);
			validator.setMessageText(NameOrIPValidator.INFO_CHECK_NAME, getUserInformationTextCheckNameAddress());
		}
	}

	private VerifyListener verifyListener;

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.BaseEditBrowseTextControl#doGetEditFieldControlVerifyListener()
	 */
	@Override
	protected VerifyListener doGetEditFieldControlVerifyListener() {
		if (verifyListener == null) {
			verifyListener =
				new NameOrIPVerifyListener(
					NameOrIPVerifyListener.ATTR_IP |
					NameOrIPVerifyListener.ATTR_NAME);
		}
		return verifyListener;
	}

	/**
	 * Returns the human readable text to present to the user in case of the edit field control
	 * content should be checked by user action (pressing the check button).
	 *
	 * @return The user information text or an empty string. Must be never <code>null</code>.
	 */
	protected String getUserInformationTextCheckNameAddress() {
		return Messages.RemoteHostAddressControl_information_checkNameAddressUserInformation;
	}

	/**
	 * Returns the human readable text to present to the user as task name if checking if or if not
	 * the edit field content can be resolved to an IP-address.
	 *
	 * @return The task name for checking the host name. Must be never <code>null</code>.
	 */
	protected String getTaskNameCheckNameAddress() {
		return Messages.RemoteHostAddressControl_information_checkNameAddressField;
	}

	/**
	 * Returns the human readable text to present to the user if the edit field content resolving to
	 * an IP-address succeeded.
	 *
	 * @return The information text. Must be never <code>null</code>.
	 */
	protected String getInformationTextCheckNameAddressSuccess() {
		return Messages.RemoteHostAddressControl_information_checkNameAddressFieldOk;
	}

	/**
	 * Returns the human readable text to present to the user if the edit field content resolving to
	 * an IP-address failed.
	 *
	 * @return The error text. Must be never <code>null</code>.
	 */
	protected String getErrorTextCheckNameAddressFailed() {
		return Messages.RemoteHostAddressControl_error_targetNameNotResolveable;
	}

	/**
	 * If the user entered a host name, we have to validate that we can really resolve the name
	 * to an IP address. Because this may really take a while, give the user the feedback what
	 * we are actually doing.
	 */
	private void onCheckAddress() {
		ProgressMonitorDialog dialog = new ProgressMonitorDialog(getParentControl().getShell());
		try {
			dialog.run(false, false, new IRunnableWithProgress() {
				private final String address = getEditFieldControlText();
				private final Control control = getEditFieldControl();
				private final IDialogPage parentPage = getParentPage();

				/* (non-Javadoc)
				 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						monitor.setTaskName(getTaskNameCheckNameAddress());
						InetAddress[] addresses = InetAddress.getAllByName(address);
						if (Platform.inDebugMode() && addresses != null) {
							String message = "RemoteHostAddressControl: Name '" + address + "' resolves to: "; //$NON-NLS-1$ //$NON-NLS-2$
							for (InetAddress address : addresses) {
								message += address.getHostAddress() + ", "; //$NON-NLS-1$
							}

							IStatus status = new Status(IStatus.WARNING, UIPlugin.getUniqueIdentifier(), message);
							UIPlugin.getDefault().getLog().log(status);
						}

						setCheckResultMessage(IMessageProvider.INFORMATION, getInformationTextCheckNameAddressSuccess());
					}	catch (Exception e) {
						setCheckResultMessage(IMessageProvider.WARNING, getErrorTextCheckNameAddressFailed());
						control.setFocus();
					} finally {
						// Trigger the wizard container update
						IWizardContainer container = null;

						try {
							// Try to get the wizard container from the parent page
							Class<?>[] paramTypes = new Class[0];
							Object[] args = new Object[0];
							Method method = parentPage.getClass().getMethod("getContainer", paramTypes); //$NON-NLS-1$
							if (!method.isAccessible()) {
								method.setAccessible(true);
							}
							Object result = method.invoke(parentPage, args);
							if (result instanceof IWizardContainer) {
								container = (IWizardContainer)result;
							}
						} catch (Exception e) {
							// If the object does not have a "getContainer()" method,
							// or the invocation fails or the access to the method
							// is denied, we are done here and break the loop
							container = null;
						}

						if (container != null) {
							container.updateButtons();
							container.updateMessage();
						}
					}
				}
			});
		}	catch (Exception e) {}
	}

	protected void setCheckResultMessage(int severity, String message) {
		setMessage(message, severity);
		if (getParentPage() instanceof DialogPage) {
			((DialogPage)getParentPage()).setMessage(message, severity);
		}
	}
}
