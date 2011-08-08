/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.wizards.pages;

import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.tm.te.core.interfaces.nodes.IPropertiesContainer;
import org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage;

/**
 * An abstract shared data wizard page implementation.
 */
public abstract class AbstractSharedDataWizardPage extends AbstractValidatableWizardPage implements ISharedDataWizardPage {

	/**
	 * Constructor.
	 *
	 * @param pageName The page name. Must not be <code>null</code>.
	 */
	public AbstractSharedDataWizardPage(String pageName) {
		super(pageName);
	}

	/**
	 * Constructor.
	 *
	 * @param pageName The page name. Must not be <code>null</code>.
	 * @param title The wizard page title or <code>null</code>.
	 * @param titleImage The wizard page title image or <code>null</code>.
	 */
	public AbstractSharedDataWizardPage(String pageName, String title, ImageDescriptor titleImage) {
		super(pageName, title, titleImage);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#setupData(org.eclipse.tm.te.core.interfaces.nodes.IPropertiesContainer)
	 */
	public void setupData(IPropertiesContainer data) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#extractData(org.eclipse.tm.te.core.interfaces.nodes.IPropertiesContainer)
	 */
	public void extractData(IPropertiesContainer data) {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.wizards.interfaces.ISharedDataWizardPage#initializeData(org.eclipse.tm.te.core.interfaces.nodes.IPropertiesContainer)
	 */
	public void initializeData(IPropertiesContainer data) {
	}
}
