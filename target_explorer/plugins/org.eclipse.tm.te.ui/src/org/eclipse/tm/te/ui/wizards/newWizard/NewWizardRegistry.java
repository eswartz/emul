/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.wizards.newWizard;


import org.eclipse.tm.te.ui.activator.UIPlugin;
import org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry;

/**
 * Target Explorer: New target wizard registry.
 *
 * @see org.eclipse.ui.internal.wizards.NewWizardRegistry
 */
@SuppressWarnings("restriction")
public final class NewWizardRegistry extends AbstractExtensionWizardRegistry {

	/*
	 * Thread save singleton instance creation.
	 */
	private static class LazyInstance {
		public static NewWizardRegistry instance = new NewWizardRegistry();
	}

	/**
	 * Constructor.
	 */
	/* default */ NewWizardRegistry() {
		super();
	}

	/**
	 * Returns the singleton instance of the wizard registry.
	 */
	public static NewWizardRegistry getInstance() {
		return LazyInstance.instance;
	}


	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry#getExtensionPoint()
	 */
	@Override
	protected String getExtensionPoint() {
		return "newWizards"; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.internal.wizards.AbstractExtensionWizardRegistry#getPlugin()
	 */
	@Override
	protected String getPlugin() {
		return UIPlugin.getUniqueIdentifier();
	}

}
