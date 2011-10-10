/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.statushandler;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.properties.PropertiesContainer;
import org.eclipse.tm.te.runtime.statushandler.AbstractStatusHandler;
import org.eclipse.tm.te.runtime.statushandler.interfaces.IStatusHandlerConstants;
import org.eclipse.tm.te.runtime.utils.Host;
import org.eclipse.tm.te.ui.activator.UIPlugin;
import org.eclipse.tm.te.ui.jface.dialogs.OptionalMessageDialog;
import org.eclipse.tm.te.ui.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * The default status handler implementation.
 * <p>
 * This status handler is returned by the status handler manager if no other
 * status handler can be found for a given context object.
 */
public class DefaultStatusHandler extends AbstractStatusHandler {
	// Declare some default title messages
	protected final static String QUESTION_TITLE = Messages.DefaultStatusHandler_question_title;
	protected final static String WARNING_TITLE = Messages.DefaultStatusHandler_warning_title;
	protected final static String ERROR_TITLE = Messages.DefaultStatusHandler_error_title;
	protected final static String INFORMATION_TITLE = Messages.DefaultStatusHandler_information_title;

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.runtime.statushandler.interfaces.IStatusHandler#handleStatus(org.eclipse.core.runtime.IStatus, org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer, org.eclipse.tm.te.runtime.statushandler.interfaces.IStatusHandler.DoneHandleStatus)
	 */
	@Override
	public void handleStatus(final IStatus status, final IPropertiesContainer data, final DoneHandleStatus done) {
		Assert.isNotNull(status);
		Assert.isNotNull(done);

		// If the platform UI is not longer running or the display does not
		// exist or is disposed already, don't do anything.
		Display display = PlatformUI.getWorkbench().getDisplay();
		if (!PlatformUI.isWorkbenchRunning() || display == null || display.isDisposed()) {
			return;
		}

		// The message dialog has to open within the display thread. Check if we are in
		// the correct display thread and spawn to it if not.
		if (Thread.currentThread().equals(display.getThread())) {
			// The current thread is the display thread, execute synchronously
			doHandleStatus(status, data, done);
		} else {
			// The current thread is not the display, execute asynchronously
			display.asyncExec(new Runnable() {
				@Override
				public void run() {
					doHandleStatus(status, data, done);
				}
			});
		}
	}

	/**
	 * Execute the status handling.
	 * <p>
	 * <b>Note:</b> This method must be called within the platforms display thread.
	 *
	 * @param status The status. Must not be <code>null</code>.
	 * @param data The custom status data object, or <code>null</code> if none.
	 * @param done The callback. Must not be <code>null</code>.
	 */
	protected void doHandleStatus(IStatus status, IPropertiesContainer data, DoneHandleStatus done) {
		Assert.isNotNull(status);
		Assert.isNotNull(done);
		Assert.isTrue(Thread.currentThread().equals(PlatformUI.getWorkbench().getDisplay().getThread()));

		Object result = null;
		Throwable error = null;

		try {
			// Unpack the status object
			String message = status.getMessage();
			String pluginId = status.getPlugin();
			int code = status.getCode();
			int severity = status.getSeverity();
			Throwable exception = status.getException();

			String title = null;
			String[] buttonLabel = null;
			String contextHelpId = null;
			String dontAskAgainId = null;
			Object caller = null;

			// Unpack the custom data
			if (data != null) {
				title = data.getStringProperty(IStatusHandlerConstants.PROPERTY_TITLE);
				buttonLabel = (String[])data.getProperty(IStatusHandlerConstants.PROPERTY_BUTTON_LABEL);
				contextHelpId = data.getStringProperty(IStatusHandlerConstants.PROPERTY_CONTEXT_HELP_ID);
				dontAskAgainId = data.getStringProperty(IStatusHandlerConstants.PROPERTY_DONT_ASK_AGAIN_ID);
				caller = data.getProperty(IStatusHandlerConstants.PROPERTY_CALLER);
			}

			if (message != null && pluginId != null) {
				// Determine the shell (null if workbench is not running, typically for headless mode).
				Shell shell = null;

				if (PlatformUI.isWorkbenchRunning()
								&&	PlatformUI.getWorkbench() != null
								&& PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null
								&& PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell() != null) {
					shell = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				}

				// Invoke subclass hook to overwrite the severity
				severity = adjustSeverity(severity, exception);
				// Invoke subclass hook to overwrite the title
				title = adjustTitle(title, severity, exception);
				// Invoke subclass hook to overwrite the message
				message = adjustMessage(message, severity, exception, caller);

				if (Host.isInteractive() && shell != null) {
					// we can show a real dialogs to the user. However, warnings and
					// errors will go to the error log too. It will give us a clue later
					// if we have to analyze the log files for possible failure scenarios.

					// Use a default dialog box title in case no specific title is given.
					if (title == null) {
						switch (severity) {
						case IStatusHandlerConstants.QUESTION:
						case IStatusHandlerConstants.YES_NO_CANCEL:
							title = QUESTION_TITLE;
							break;
						case IStatus.WARNING:
							title = WARNING_TITLE;
							break;
						case IStatus.ERROR:
							title = ERROR_TITLE;
							break;
						default:
							title = INFORMATION_TITLE;
						}
					}

					// In case the status represents a warning or an error,
					// log the status to the error log.
					if (severity == IStatus.WARNING || severity == IStatus.ERROR) {
						UIPlugin.getDefault().getLog().log(status);
					}

					// Invoke subclass hook to overwrite the context help id.
					String[] contextHelpIds = adjustContextHelpIds(contextHelpId, code, caller);
					String helpContextId = (contextHelpIds.length > 0) ? contextHelpIds[0] : null;

					// Show the message dialog finally
					result = doOpenMessageDialog(shell, title, message, buttonLabel, severity, dontAskAgainId, helpContextId);
				} else {
					// Not interactive -> Re-pack the status and log it to the error log
					status = new Status(severity == IStatusHandlerConstants.QUESTION ? IStatus.WARNING : severity,
										pluginId, code, message, exception);
					UIPlugin.getDefault().getLog().log(status);
				}
			}

			// Fill in the result object to the custom data object
			if (data == null && result != null) data = new PropertiesContainer();
			if (data != null) data.setProperty(IStatusHandlerConstants.PROPERTY_RESULT, result);
		} catch (Throwable e) {
			error = e;
		} finally {
			// Invoke the callback
			done.doneHandleStatus(error, data);
		}

		return;
	}

	/**
	 * Allows overrides to adjust the message severity based on the passed information finally
	 * before the message box will show up. The default implementation will return whatever has
	 * been passed in as proposed severity.
	 *
	 * @param proposedSeverity The proposed message severity.
	 * @param exception The associated message exception.
	 * @return The final message box title. Must not be <code>null</code>!
	 */
	protected int adjustSeverity(int proposedSeverity, Throwable exception) {
		return proposedSeverity;
	}

	/**
	 * Allows subclasses to adjust the message box title based on the passed information finally
	 * before the message box will show up. The default implementation will return whatever has
	 * been passed in as proposed title.
	 *
	 * @param proposedTitle The proposed message box title.
	 * @param severity The message severity.
	 * @param exception The associated message exception.
	 *
	 * @return The final message box title. Must not be <code>null</code>!
	 */
	protected String adjustTitle(String proposedTitle, int severity, Throwable exception) {
		return proposedTitle;
	}

	/**
	 * Allows subclasses to adjust the message box message based on the passed information finally
	 * before the message box will show up. The default implementation will return whatever has
	 * been passed in as proposed message.
	 *
	 * @param proposedMessage The proposed message box message.
	 * @param severity The message severity.
	 * @param exception The associated message exception.
	 * @param caller The caller of the status handler or <code>null</code>.
	 *
	 * @return The final message box message. Must not be <code>null</code>!
	 */
	protected String adjustMessage(String proposedMessage, int severity, Throwable exception, Object caller) {
		return proposedMessage;
	}

	protected final static String[] EMPTY = new String[0];

	/**
	 * Allows subclasses to finally adjust the set of context help id's associated with message box. The
	 * method must allows return a non <code>null</code> value! The default implementation only transforms
	 * the proposed contextHelpId within an array.
	 * <p>
	 * Note: Only message with severity <code>ERROR</code> can have multiple context help id's associated!
	 * <p>
	 * @param proposedContextHelpId The proposed context help id.
	 * @param errorCode The associated error code.
	 * @param caller The caller of the status handler or <code>null</code>.
	 *
	 * @return An array of context help id's. Must not be <code>null</code>!
	 */
	protected String[] adjustContextHelpIds(String proposedContextHelpId, int errorCode, Object caller) {
		if (proposedContextHelpId == null) {
			return EMPTY;
		}
		return new String[] { proposedContextHelpId };
	}

	/**
	 * Open the message dialog.
	 *
	 * @param shell The shell. Must not be <code>null</code>.
	 * @param title The title. Must not be <code>null</code>.
	 * @param message The message. Must not be <code>null</code>.
	 * @param buttonLabel An string array listing the labels of the message dialog buttons. If <code>null</code>, the default
	 *                    labeling, typically &quot;OK&quot; for a single button message dialog, will be applied.
	 * @param severity The severity. Must be one of the {@link IStatus} constants.
	 * @param keyDontAskAgain The unique key for the stored result value or <code>null</code>.
	 * @param helpContextId The help context id or <code>null</code>.
	 *
	 * @return {@link Boolean} if the severity is {@link IWRMessageStatusHandler#QUESTION}, <code>null</code> otherwise.
	 */
	protected Object doOpenMessageDialog(Shell shell, String title, String message, String[] buttonLabel, int severity, String keyDontAskAgain, String helpContextId) {
		Assert.isNotNull(shell);
		Assert.isNotNull(title);
		Assert.isNotNull(message);

		Object result = null;

		switch (severity) {
			case IStatusHandlerConstants.QUESTION:
				result = new Boolean(OptionalMessageDialog.openYesNoDialog(shell, title, message, buttonLabel, keyDontAskAgain, helpContextId) == IDialogConstants.YES_ID);
				break;
			case IStatusHandlerConstants.YES_NO_CANCEL:
				result = new Integer(OptionalMessageDialog.openYesNoCancelDialog(shell, title, message, buttonLabel, keyDontAskAgain, helpContextId));
				break;
			case IStatus.WARNING:
				OptionalMessageDialog.openWarningDialog(shell, title, message, buttonLabel, keyDontAskAgain, helpContextId);
				break;
			case IStatus.ERROR:
				OptionalMessageDialog.openErrorDialog(shell, title, message, buttonLabel, keyDontAskAgain, helpContextId);
				break;
			default:
				OptionalMessageDialog.openInformationDialog(shell, title, message, buttonLabel, keyDontAskAgain, helpContextId);
		}

		return result;
	}
}
