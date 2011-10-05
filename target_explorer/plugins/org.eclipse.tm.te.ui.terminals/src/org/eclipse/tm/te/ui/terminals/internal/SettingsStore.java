/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.terminals.internal;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;
import org.eclipse.tm.te.runtime.properties.PropertiesContainer;

/**
 * Simple default Terminal settings store implementation keeping the settings
 * within memory.
 */
@SuppressWarnings("restriction")
public class SettingsStore extends PropertiesContainer implements ISettingsStore {

	/**
	 * Constructor.
	 */
	public SettingsStore() {
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#get(java.lang.String, java.lang.String)
	 */
	@Override
	public String get(String key, String defaultValue) {
		String value = getStringProperty(key);
		return value != null ? value : defaultValue;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#get(java.lang.String)
	 */
	@Override
	public String get(String key) {
		return getStringProperty(key);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore#put(java.lang.String, java.lang.String)
	 */
	@Override
	public void put(String key, String value) {
		setProperty(key, value);
	}
}
