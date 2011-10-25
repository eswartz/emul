/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.core.connection.interfaces;

import org.osgi.framework.Bundle;

/**
 * Connection type property constant definitions.
 */
public interface IConnectionTypeConstants {

	/**
	 * The bundle which is contributing the connection type definition.
	 * <p>
	 * Type: {@link Bundle}
	 */
	public static final String PROPERTY_DEFINING_BUNDLE = "definingBundle"; //$NON-NLS-1$

	/**
	 * The connection type unique id.
	 * <p>
	 * Type: {@link String}
	 */
	public static final String PROPERTY_ID = "id"; //$NON-NLS-1$

	/**
	 * The connection type label for representing the connection type within the UI.
	 * <p>
	 * Type: {@link String}
	 */
	public static final String PROPERTY_LABEL = "label"; //$NON-NLS-1$

	/**
	 * The connection type description.
	 * <p>
	 * The description is presented to the user within the new connection wizard to give the user a
	 * hint what the connection type can be used for.
	 * <p>
	 * Type: {@link String}
	 */
	public static final String PROPERTY_DESCRIPTION = "description"; //$NON-NLS-1$

	/**
	 * The connection type short name.
	 * <p>
	 * The short name is used to construct a default connection name for new connections of this
	 * type.
	 * <p>
	 * Type: {@link String}
	 */
	public static final String PROPERTY_SHORTNAME = "shortname"; //$NON-NLS-1$

	/**
	 * The connection type category id.
	 * <p>
	 * The id of the category the connection type is associated with. The categories are visualized
	 * to the user by grouping connection types of the same category within the new connection
	 * wizard.
	 * <p>
	 * Type: {@link String}
	 */
	public static final String PROPERTY_CATEGORY_ID = "categoryId"; //$NON-NLS-1$

	/**
	 * Supports early finish.
	 * <p>
	 * The user can finish the new connection wizard early. The user is not required to walk through
	 * all available new connection wizard pages, associated with this connection type.
	 * <p>
	 * Type: {@link Boolean}
	 */
	public static final String PROPERTY_SUPPORTS_EARLY_FINISH = "supportsEarlyFinish"; //$NON-NLS-1$

	/**
	 * The connection types explicit enabled state.
	 * <p>
	 * Type: {@link Boolean}
	 */
	public static final String PROPERTY_ENABLED = "enabled"; //$NON-NLS-1$

	/**
	 * Instances of this target connection type can be created by the user via the
	 * "New Connection" wizard UI. The default value is <code>true</code>.
	 * <p>
	 * Type: {@link Boolean}
	 */
	public static final String PROPERTY_IS_USER_CREATABLE = "isUserCreatable"; //$NON-NLS-1$

	/**
	 * The connection type last invalidation cause.
	 * <p>
	 * User readable string presented within the UI (error log) to inform the user why this
	 * connection type had been set invalid.
	 * <p>
	 * Type: {@link String}
	 */
	public static final String PROPERTY_LAST_INVALID_CAUSE = "lastInvalidCause"; //$NON-NLS-1$

	/**
	 * Property to control if newly created connections of this connection type will be connected
	 * immediately. This property is effective as long the user did not modified the setting via the
	 * new connection wizard. The users last choice for this connection type is remembered by the
	 * new connection wizard via the wizards dialog settings history (per workspace).
	 * <p>
	 * Type: {@link Boolean}
	 */
	public static final String PROPERTY_CONNECT_IMMEDIATELY = "connectImmediately"; //$NON-NLS-1$
}
