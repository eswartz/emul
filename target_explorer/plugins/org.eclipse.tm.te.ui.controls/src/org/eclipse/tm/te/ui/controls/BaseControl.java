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

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.swt.widgets.Composite;

/**
 * Target Explorer: Base implementation of a common UI control.
 * <p>
 * The control can be embedded into any UI container like dialogs,
 * wizard pages or preference pages.
 */
public class BaseControl extends PlatformObject implements IMessageProvider {

	/**
	 * Reference to the parent control.
	 */
	private Composite parentControl;

	/**
	 * A message associated with the control.
	 */
	private String message = null;

	/**
	 * The message type of the associated message.
	 * @see IMessageProvider
	 */
	private int messageType = IMessageProvider.NONE;

	/**
	 * Flag to remember the controls enabled state
	 */
	private boolean enabled = true;

	/**
	 * Constructor.
	 */
	public BaseControl() {
		super();
	}

	/**
	 * Returns if the <code>setupPanel(...)</code> method has been called at least once with
	 * a non-null parent control.
	 *
	 * @return <code>true</code> if the associated parent control is not <code>null</code>, <code>false</code> otherwise.
	 */
	public final boolean isControlCreated() {
		return (parentControl != null);
	}

	/**
	 * Returns the parent control of the control.
	 *
	 * @return The parent control or <code>null</code>.
	 */
	public final Composite getParentControl() {
		return parentControl;
	}

	/**
	 * Cleanup all resources the control might have been created.
	 */
	public void dispose() {
		parentControl = null;
	}

	/**
	 * Creates the controls UI elements.
	 *
	 * @param parent The parent control. Must not be <code>null</code>!
	 */
	public void setupPanel(Composite parent) {
		Assert.isNotNull(parent);
		parentControl = parent;
	}

	/**
	 * Enables or disables all UI elements belonging to this control. The
	 * control remembers the last set enabled state to allow easy enabled checks.
	 *
	 * @param enabled <code>True</code> to enable the UI elements, <code>false</code> otherwise.
	 */
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	/**
	 * Returns the control is enabled or not.
	 */
	public final boolean isEnabled() {
		return enabled;
	}

	/**
	 * Validates the control and sets the message text and type so the parent
	 * page or control is able to display validation result informations.
	 * The validation should be done by implementations of WRValidator!
	 * The default implementation of this method does nothing.
	 * Use the isValid(WRBaseControl, boolean) method to validate child-controls.
	 *
	 * @return Result of validation.
	 */
	public boolean isValid() {
		setMessage(null, IMessageProvider.NONE);
		return true;
	}

	/**
	 * Validates the given subcontrol and bitwise "AND" the subcontrols valid
	 * state to the passed in current valid state. If the subcontrol validation
	 * results into a message which has a higher message type than the currently
	 * set message, the message from the subcontrol is applied to the control itself.
	 * <p>
	 * <b>Note:</b> If the given subcontrol is <code>null</code>, the current validation
	 * state is returned unchanged.
	 *
	 * @param subControl The subcontrol instance or <code>null</code>.
	 * @param currentValidationState The current control validation state before the subcontrol is validated.
	 *
	 * @return The new controls validation state after the subcontrol has been validated.
	 */
	protected final boolean isSubControlValid(BaseControl subControl, boolean currentValidationState) {
		if (subControl == null) return currentValidationState;

		// Validate the subcontrol and bitwise "AND" the result to the current validation state
		currentValidationState &= subControl.isValid();
		// Check if the subcontrol has set a message which has a higher message
		// type than the currently set message.
		if (subControl.getMessageType() > getMessageType()) {
			// Apply the message from the subcontrol to the control
			setMessage(subControl.getMessage(), subControl.getMessageType());
		}

		// Returns the resulting validation state.
		return currentValidationState;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessage()
	 */
	@Override
	public final String getMessage() {
		return message;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.dialogs.IMessageProvider#getMessageType()
	 */
	@Override
	public final int getMessageType() {
		return messageType;
	}

	/**
	 * Set the message and the message type this control wants to display in
	 * the outer control or panel.
	 *
	 * @param message The message from this control.
	 * @param messageType The type o the message (NONE, INFORMATION, WARNING, ERROR).
	 */
	protected final void setMessage(String message, int messageType) {
		// Check if we should apply the default message instead.
		if (message == null && getDefaultMessage() != null) {
			message = getDefaultMessage();
			messageType = getDefaultMessageType();
		}
		// Set the message and message type
		this.message = message;
		this.messageType = messageType;
	}

	/**
	 * Returns the controls default message or <code>null</code> if none.
	 *
	 * @return The controls default message or <code>null</code>.
	 */
	public String getDefaultMessage() {
		return null;
	}

	/**
	 * Returns the controls default message type or {@link IMessageProvider#NONE} if none.
	 *
	 * @return The controls default message type.
	 */
	public int getDefaultMessageType() {
		return IMessageProvider.INFORMATION;
	}

	/**
	 * Returns the correctly prefixed dialog settings slot id. In case the given id
	 * suffix is <code>null</code> or empty, <code>id</code> is returned as is.
	 *
	 * @param settingsSlotId The dialog settings slot id to prefix.
	 * @param prefix The prefix.
	 * @return The correctly prefixed dialog settings slot id.
	 */
	public final String prefixDialogSettingsSlotId(String settingsSlotId, String prefix) {
		if (settingsSlotId != null && prefix != null && prefix.trim().length() > 0) {
			settingsSlotId = prefix + "." + settingsSlotId; //$NON-NLS-1$
		}
		return settingsSlotId;
	}

	/**
	 * Returns the parent section for the control dialog settings. The default implementation
	 * returns the passed in dialog settings instance unmodified. Overwrite to create additional
	 * subsections within the given dialog settings instance.
	 *
	 * @param settings The dialog settings instance. Must not be <code>null</code>.
	 *
	 * @return The parent section for the control dialog settings. Must never be <code>null</code>.
	 */
	protected IDialogSettings doGetParentSection(IDialogSettings settings) {
		Assert.isNotNull(settings);
		return settings;
	}

	/**
	 * Restore the widget values from the dialog settings store to recreate the control history.
	 * <p>
	 * <b>Note:</b>
	 * The control is saving the widget values into a section equal to the class name {@link Class#getName()}.
	 * After the sections has been created, the method calls <code>doRestoreWidgetValues</code> for restoring
	 * the single properties from the dialog settings. Subclasses may override <code>doRestoreWidgetValues</code>
	 * only to deal with the single properties only or <code>restoreWidgetValues</code> when to override the
	 * creation of the subsections.
	 *
	 * @param settings The dialog settings object instance to restore the widget values from. Must not be <code>null</code>!
	 * @param idPrefix The prefix to use for every dialog settings slot keys. If <code>null</code>, the dialog settings slot keys are not to prefix.
	 */
	public final void restoreWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);

		// Get the parent section for the control dialog settings.
		IDialogSettings parentSection = doGetParentSection(settings);
		Assert.isNotNull(parentSection);

		// Store the settings of the control within it's own section.
		IDialogSettings section = parentSection.getSection(this.getClass().getName());
		if (section == null) {
			section = parentSection.addNewSection(this.getClass().getName());
		}

		// now, call the hook for actually reading the single properties from the dialog settings.
		doRestoreWidgetValues(section, idPrefix);
	}

	/**
	 * Hook to restore the widget values finally plain from the given dialog settings. This method should
	 * not fragment the given dialog settings any further.
	 *
	 * @param settings The dialog settings to restore the widget values from. Must not be <code>null</code>!
	 * @param idPrefix The prefix to use for every dialog settings slot keys. If <code>null</code>, the dialog settings slot keys are not to prefix.
	 */
	public void doRestoreWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);
	}

	/**
	 * Saves the widget values to the dialog settings store for remembering the history. The control might
	 * be embedded within multiple pages multiple times handling different properties. Because the single
	 * controls should not mix up the history, we create subsections within the given dialog settings if
	 * they do not already exist. After the sections has been created, the method calls <code>doSaveWidgetValues</code>
	 * for saving the single properties to the dialog settings. Subclasses may override <code>doSaveWidgetValues</code>
	 * only to deal with the single properties only or <code>saveWidgetValues</code> when to override the
	 * creation of the subsections.
	 *
	 * @param settings The dialog settings object instance to save the widget values to. Must not be <code>null</code>!
	 * @param idPrefix The prefix to use for every dialog settings slot keys. If <code>null</code>, the dialog settings slot keys are not to prefix.
	 */
	public final void saveWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);

		// Get the parent section for the control dialog settings.
		IDialogSettings parentSection = doGetParentSection(settings);
		Assert.isNotNull(parentSection);

		// Store the settings of the control within it's own section.
		IDialogSettings section = parentSection.getSection(this.getClass().getName());
		if (section == null) {
			section = parentSection.addNewSection(this.getClass().getName());
		}

		// now, call the hook for actually writing the single properties to the dialog settings.
		doSaveWidgetValues(section, idPrefix);
	}

	/**
	 * Hook to save the widget values finally plain to the given dialog settings. This method should
	 * not fragment the given dialog settings any further.
	 *
	 * @param settings The dialog settings to save the widget values to. Must not be <code>null</code>!
	 * @param idPrefix The prefix to use for every dialog settings slot keys. If <code>null</code>, the dialog settings slot keys are not to prefix.
	 */
	public void doSaveWidgetValues(IDialogSettings settings, String idPrefix) {
		Assert.isNotNull(settings);
	}
}
