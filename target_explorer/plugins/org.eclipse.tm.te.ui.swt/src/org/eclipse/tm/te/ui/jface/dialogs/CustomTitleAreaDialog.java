/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.jface.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Layout;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.te.ui.swt.activator.UIPlugin;
import org.eclipse.ui.PlatformUI;

/**
 * Target Explorer: Custom title area dialog implementation.
 */
public class CustomTitleAreaDialog extends TitleAreaDialog implements IMessageProvider {
	protected static final int comboHistoryLength = 10;
	private String contextHelpId = null;

	// The dialog settings storage
	private IDialogSettings dialogSettings;

	private String message;
	private int messageType;
	private String errorMessage;
	private String title;

	// The default message is shown to the user if no other message is set
	private String defaultMessage;
	private int defaultMessageType;

	/**
	 * Constructor.
	 *
	 * @param parent The parent shell used to view the dialog.
	 */
	public CustomTitleAreaDialog(Shell parent) {
		this(parent, null);
	}

	/**
	 * Constructor.
	 *
	 * @param parent The parent shell used to view the dialog.
	 * @param contextHelpId The dialog context help id or <code>null</code>.
	 */
	public CustomTitleAreaDialog(Shell parent, String contextHelpId) {
		super(parent);
		initializeDialogSettings();
		setContextHelpId(contextHelpId);
	}

	protected void setContextHelpId(String contextHelpId) {
		this.contextHelpId = contextHelpId;
		setHelpAvailable(contextHelpId != null);
	}

	/**
	 * Initialize the dialog settings storage.
	 */
	protected void initializeDialogSettings() {
		IDialogSettings settings = doGetDialogSettingsToInitialize();
		assert settings != null;
		IDialogSettings section = settings.getSection(getDialogSettingsSectionName());
		if (section == null) {
			section = settings.addNewSection(getDialogSettingsSectionName());
		}
		setDialogSettings(section);
	}

	/**
	 * Returns the dialog settings container to use and to initialize. This
	 * method is called from <code>initializeDialogSettings</code> and allows
	 * overriding the dialog settings container without changing the dialog
	 * settings structure.
	 *
	 * @return The dialog settings container to use. Must not be <code>null</code>.
	 */
	protected IDialogSettings doGetDialogSettingsToInitialize() {
		return UIPlugin.getDefault().getDialogSettings();
	}

	/**
	 * Returns the section name to use for separating different persistent
	 * dialog settings from different dialogs.
	 *
	 * @return The section name used to store the persistent dialog settings within the plugins persistent
	 *         dialog settings store.
	 */
	public String getDialogSettingsSectionName() {
		return "CustomTitleAreaDialog"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#create()
	 */
	@Override
	public void create() {
		super.create();

		// If the dialog got set a message, make sure the message is really shown
		// to the user from the beginning.
		if (isMessageSet()) {
			if (errorMessage != null) {
				super.setErrorMessage(errorMessage);
			}
			else {
				super.setMessage(message, messageType);
			}
		} else if (defaultMessage != null) {
			// Default message set
			super.setMessage(defaultMessage, defaultMessageType);
		}

		// If the dialog got set a title, make sure the title is shown
		if (title != null) {
			super.setTitle(title);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#createDialogArea(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createDialogArea(Composite parent) {
		if (contextHelpId != null) {
			PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, contextHelpId);
		}

		// Let the super implementation create the dialog area control
		Control control = super.createDialogArea(parent);
		// But fix the layout data for the top control
		if (control instanceof Composite) {
			configureDialogAreaControl((Composite)control);
		}

		return control;
	}

	/**
	 * Configure the dialog top control.
	 *
	 * @param composite The dialog top control. Must not be <code>null</code>.
	 */
	protected void configureDialogAreaControl(Composite composite) {
		assert composite != null;
		Layout layout = composite.getLayout();
		if (layout == null || layout instanceof GridLayout) {
			composite.setLayout(new GridLayout());
		}
	}

	/**
	 * Returns the associated dialog settings storage.
	 *
	 * @return The dialog settings storage.
	 */
	public IDialogSettings getDialogSettings() {
		// The dialog settings may not been initialized here. Initialize first in this case
		// to be sure that we do have always the correct dialog settings.
		if (dialogSettings == null) {
			initializeDialogSettings();
		}
		return dialogSettings;
	}

	/**
	 * Sets the associated dialog settings storage.
	 *
	 * @return The dialog settings storage.
	 */
	public void setDialogSettings(IDialogSettings dialogSettings) {
		this.dialogSettings = dialogSettings;
	}

	/**
	 * Adds the given string to the given string array.
	 *
	 * @param history String array to add the given entry to it.
	 * @param newEntry The new entry to add.
	 * @return The updated string array containing the old array content plus the new entry.
	 */
	protected String[] addToHistory(String[] history, String newEntry) {
		List<String> l = new ArrayList<String>(Arrays.asList(history));
		addToHistory(l, newEntry);
		String[] r = new String[l.size()];
		l.toArray(r);
		return r;
	}

	/**
	 * Adds the given string to the given list.
	 *
	 * @param history List to add the given entry to it.
	 * @param newEntry The new entry to add. Must not be <code>null</code>
	 *
	 * @return The updated list containing the old list content plus the new entry.
	 */
	protected void addToHistory(List<String> history, String newEntry) {
		Assert.isNotNull(newEntry);

		history.remove(newEntry);
		history.add(0, newEntry);
		// since only one new item was added, we can be over the limit
		// by at most one item
		if (history.size() > comboHistoryLength) {
			history.remove(comboHistoryLength);
		}
	}

	/**
	 * Save current dialog widgets values.
	 * Called by <code>okPressed</code>.
	 */
	protected void saveWidgetValues() {
		return;
	}

	/**
	 * Restore previous dialog widgets values.
	 * Note: This method is not called automatically! You have
	 *       to call this method at the appropriate time and place.
	 */
	protected void restoreWidgetValues() {
		return;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.Dialog#okPressed()
	 */
	@Override
	protected void okPressed() {
		saveWidgetValues();
		super.okPressed();
	}

	/**
	 * Cleanup when dialog is closed.
	 */
	protected void dispose() {
		dialogSettings = null;
		message = null;
		messageType = IMessageProvider.NONE;
		errorMessage = null;
		title = null;
		defaultMessage = null;
		defaultMessageType = IMessageProvider.NONE;
	}

	/**
	 * Cleanup the Dialog and close it.
	 */
	@Override
	public boolean close() {
		dispose();
		return super.close();
	}

	/**
	 * Set the enabled state of the dialog button specified by the given id (@see <code>IDialogConstants</code>)
	 * to the given state.
	 *
	 * @param buttonId The button id for the button to change the enabled state for.
	 * @param enabled The new enabled state to set for the button.
	 */
	public void setButtonEnabled(int buttonId, boolean enabled) {
		Button button = getButton(buttonId);
		if (button != null) {
			button.setEnabled(enabled);
		}
	}

	/**
	 * Sets the title for this dialog.
	 *
	 * @param title The title.
	 */
	public void setDialogTitle(String title) {
		if (getShell() != null && !getShell().isDisposed()) {
			getShell().setText(title);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#setTitle(java.lang.String)
	 */
	@Override
	public void setTitle(String newTitle) {
		title = newTitle;
		super.setTitle(newTitle);
	}

	/**
	 * Set the default message. The default message is shown within the
	 * dialogs message area if no other message is set.
	 *
	 * @param message The default message or <code>null</code>.
	 * @param type The default message type. See {@link IMessageProvider}.
	 */
	public void setDefaultMessage(String message, int type) {
		defaultMessage = message;
		defaultMessageType = type;
		// Push the default message to the dialog if no other message is set
		if (!isMessageSet() && getContents() != null) {
			super.setMessage(defaultMessage, defaultMessageType);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#setMessage(java.lang.String, int)
	 */
	@Override
	public void setMessage(String newMessage, int newType) {
		// To be able to implement IMessageProvider, we have to remember the
		// set message ourselfs. There is no access to these information by the
		// base class.
		message = newMessage; messageType = newType;
		// Only pass on to super implementation if the control has been created yet
		if (getContents() != null) {
			super.setMessage(message != null ? message : defaultMessage, message != null ? messageType : defaultMessageType);
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.TitleAreaDialog#setErrorMessage(java.lang.String)
	 */
	@Override
	public void setErrorMessage(String newErrorMessage) {
		// See setMessage(...)
		errorMessage = newErrorMessage;
		super.setErrorMessage(newErrorMessage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
	 */
	@Override
	public String getMessage() {
		return errorMessage != null ? errorMessage : message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
	 */
	public int getMessageType() {
		return errorMessage != null ? IMessageProvider.ERROR : messageType;
	}

	/**
	 * Returns if or if not an message is set to the dialog.
	 *
	 * @return <code>True</code> if a message has been set, <code>false</code> otherwise.
	 */
	public boolean isMessageSet() {
		return errorMessage != null || message != null;
	}
}
