/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.core.interfaces;

/**
 * Declaration of the TCF transport type constants.
 * <p>
 * The constants defined in this interface needs to be kept in sync with
 * the TCF framework transport manager implementation. Probably the interface
 * should be moved to the core TCF framework plug-in's some when.
 */
public interface ITransportTypes {

	/**
	 * Transport type "TCP".
	 */
	public static final String TRANSPORT_TYPE_TCP = "TCP"; //$NON-NLS-1$

	/**
	 * Transport type "SSL".
	 */
	public static final String TRANSPORT_TYPE_SSL = "SSL"; //$NON-NLS-1$

	/**
	 * Transport type "PIPE".
	 */
	public static final String TRANSPORT_TYPE_PIPE = "PIPE"; //$NON-NLS-1$

	/**
	 * Transport type "Loop".
	 */
	public static final String TRANSPORT_TYPE_LOOP = "Loop"; //$NON-NLS-1$

	/**
	 * Custom transport type.
	 */
	public static final String TRANSPORT_TYPE_CUSTOM = "Custom"; //$NON-NLS-1$
}
