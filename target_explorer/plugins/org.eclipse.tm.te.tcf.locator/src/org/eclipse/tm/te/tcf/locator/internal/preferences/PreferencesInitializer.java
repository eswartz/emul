/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.locator.internal.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.tm.te.tcf.locator.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.locator.interfaces.preferences.IPreferenceKeys;


/**
 * The locator model bundle preference initializer.
 */
public class PreferencesInitializer extends AbstractPreferenceInitializer {

	/**
	 * Constructor.
	 */
	public PreferencesInitializer() {
		super();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
	 */
	@Override
	public void initializeDefaultPreferences() {
		// Get the bundles preferences manager
		IEclipsePreferences prefs = new DefaultScope().getNode(CoreBundleActivator.getUniqueIdentifier());
		if (prefs != null) {
			// [Hidden] Filtered by agent id: default on
			prefs.putBoolean(IPreferenceKeys.PREF_FILTER_BY_AGENT_ID, true);
		}
	}
}
