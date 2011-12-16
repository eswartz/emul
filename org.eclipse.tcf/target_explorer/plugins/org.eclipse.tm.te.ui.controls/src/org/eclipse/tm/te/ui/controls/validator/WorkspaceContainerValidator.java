/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls.validator;

import java.text.MessageFormat;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * Validates a given path to be a workspace path.
 * <p>
 * <b>Note:</b> This validator is useful only if the Eclipse resources plugin is installed.
 */
public class WorkspaceContainerValidator extends Validator {

	// keys for info messages
	public static final String INFO_MISSING_VALUE = "WorkspaceContainerValidator_Information_MissingValue"; //$NON-NLS-1$

	// keys for error messages
	public static final String ERROR_INVALID_VALUE = "WorkspaceContainerValidator_Error_InvalidValue"; //$NON-NLS-1$

	/**
	 * Constructor
	 *
	 * @param attributes The validator attributes.
	 */
	public WorkspaceContainerValidator(int attributes) {
		super(attributes);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.validator.Validator#isValid(java.lang.String)
	 */
	@Override
	public boolean isValid(String newText) {
		init();

		// info message when value is empty and mandatory
		if (newText == null || newText.trim().length() == 0) {
			if (isAttribute(ATTR_MANDATORY)) {
				setMessage(getMessageText(INFO_MISSING_VALUE), getMessageTextType(INFO_MISSING_VALUE, INFORMATION));
				return false;
			}
			return true;
		}

		if (Platform.getBundle("org.eclipse.core.resources") != null //$NON-NLS-1$
			&& Platform.getBundle("org.eclipse.core.resources").getState() == Bundle.ACTIVE) { //$NON-NLS-1$
			IStatus status = org.eclipse.core.resources.ResourcesPlugin.getWorkspace().validatePath(newText.trim(), org.eclipse.core.resources.IResource.FOLDER | org.eclipse.core.resources.IResource.PROJECT);
			if (status.getSeverity() != IStatus.OK) {
				// Try to format the returned message with the information returned
				// to use by the status object.
				setMessage(MessageFormat.format(getMessageText(ERROR_INVALID_VALUE), new Object[] { status.getMessage() }), getMessageTextType(ERROR_INVALID_VALUE, ERROR));
				return getMessageType() != ERROR;
			}
		}

		return true;
	}
}
