/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.wizards.interfaces;

import org.eclipse.tm.te.core.interfaces.properties.IPropertiesContainer;

/**
 * Target Explorer: Public interface for wizard or dialog pages or wizard page
 *                  widgets sharing a common data object.
 */
public interface ISharedDataWizardPage {

	/**
	 * Initialize the page widgets based of the data from the given properties container.
	 * <p>
	 * This method may called multiple times during the lifetime of the page and
	 * the given properties container might be even <code>null</code>.
	 *
	 * @param data The properties container or <code>null</code>.
	 */
	public void setupData(IPropertiesContainer data);

	/**
	 * Extract the data from the page widgets and write it back to the given
	 * properties container.
	 * <p>
	 * This method may called multiple times during the lifetime of the page and
	 * the given properties container might be even <code>null</code>.
	 *
	 * @param data The properties container or <code>null</code>.
	 */
	public void extractData(IPropertiesContainer data);

	/**
	 * Initialize the given properties container with default values for the data
	 * this page is managing.
	 * <p>
	 * This method is called once for each wizard page and is typically called from a
	 * new target wizard. The page widgets are typically not yet created as this method
	 * can be called before the page is set visible.
	 *
	 * @param data The properties container or <code>null</code>.
	 */
	public void initializeData(IPropertiesContainer data);

	/**
	 * Remove the data of the page widgets from the given properties
	 * container.
	 * <p>
	 * This method may called multiple times during the lifetime of the page and the
	 * given properties container might be even <code>null</code>.
	 *
	 * @param data The properties container or <code>null</code>.
	 */
	public void removeData(IPropertiesContainer data);
}
