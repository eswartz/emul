/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.model;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.tm.te.core.model.activator.CoreBundleActivator;

/**
 * A common (data) model node representing a message.
 * <p>
 * <b>Note:</b> The (data) model node implementation is not thread-safe. Clients requiring
 *              a thread-safe implementation should subclass the model node.
 */
public class MessageModelNode extends ModelNode {

	/**
	 * Property: Message severity.
	 */
	public static final String PROPERTY_SEVERITY = "severity";  //$NON-NLS-1$

	/**
	 * Message severity: Pending.
	 */
	public static final int PENDING = 0xFF;

	/**
	 * Message model node image id: Severity Pending.
	 */
	public final static String OBJECT_MESSAGE_PENDING_ID = CoreBundleActivator.getUniqueIdentifier() + ".message.pending"; //$NON-NLS-1$

	/**
	 * Message model node image id: Severity Info.
	 */
	public final static String OBJECT_MESSAGE_INFO_ID = CoreBundleActivator.getUniqueIdentifier() + ".message.info"; //$NON-NLS-1$

	/**
	 * Message model node image id: Severity Warning.
	 */
	public final static String OBJECT_MESSAGE_WARNING_ID = CoreBundleActivator.getUniqueIdentifier() + ".message.warning"; //$NON-NLS-1$

	/**
	 * Message model node image id: Severity Error.
	 */
	public final static String OBJECT_MESSAGE_ERROR_ID = CoreBundleActivator.getUniqueIdentifier() + ".message.error"; //$NON-NLS-1$

	// Flag to mark the message node locked (immutable).
	private boolean locked = false;

	/**
	 * Constructor.
	 *
	 * @param message The message to show in the tree.
	 * @param severity The severity for the message to show as icon.
	 *
	 * @see IStatus
	 */
	public MessageModelNode(String message, int severity, boolean locked) {
		super();
		setProperty(PROPERTY_NAME, message);
		setProperty(PROPERTY_SEVERITY, severity);
		this.locked = locked;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.nodes.PropertiesContainer#setProperty(java.lang.String, java.lang.Object)
	 */
	@Override
	public boolean setProperty(String key, Object value) {
		if (locked) return false;
		return super.setProperty(key, value);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.core.nodes.ModelNode#getImageId()
	 */
	@Override
	public String getImageId() {
		switch (getIntProperty(PROPERTY_SEVERITY)) {
			case PENDING:
				return OBJECT_MESSAGE_PENDING_ID;
			case IStatus.INFO:
				return OBJECT_MESSAGE_INFO_ID;
			case IStatus.WARNING:
				return OBJECT_MESSAGE_WARNING_ID;
			case IStatus.ERROR:
				return OBJECT_MESSAGE_ERROR_ID;
		}
		return null;
	}
}
