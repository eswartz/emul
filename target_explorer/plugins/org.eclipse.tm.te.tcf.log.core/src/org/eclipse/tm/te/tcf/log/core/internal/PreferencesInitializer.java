/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.log.core.internal;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.core.runtime.preferences.DefaultScope;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.tm.te.tcf.log.core.activator.CoreBundleActivator;
import org.eclipse.tm.te.tcf.log.core.interfaces.IPreferenceKeys;


/**
 * TCF logging bundle preference initializer.
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
		IEclipsePreferences prefs = DefaultScope.INSTANCE.getNode(CoreBundleActivator.getUniqueIdentifier());
		if (prefs != null) {
			// [Hidden] Enable back-end communication logging: default on
			prefs.putBoolean(IPreferenceKeys.PREF_LOGGING_ENABLED, true);
			// [Hidden] Heat beat events: default off
			prefs.putBoolean(IPreferenceKeys.PREF_SHOW_HEARTBEATS, false);
			// [Hidden] Maximum log file size in bytes: default 5M
			prefs.put(IPreferenceKeys.PREF_MAX_FILE_SIZE, "5M"); //$NON-NLS-1$
			// [Hidden] Maximum number of log files in cycle: default 5
			prefs.putInt(IPreferenceKeys.PREF_MAX_FILES_IN_CYCLE, 5);
		}
	}
}
