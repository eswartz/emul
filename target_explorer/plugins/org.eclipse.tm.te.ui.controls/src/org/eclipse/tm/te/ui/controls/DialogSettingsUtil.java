/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.controls;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.dialogs.IDialogSettings;

/**
 * Target Explorer: Utility class providing static methods to centralize
 * common operations to perform on JFace dialog settings.
 */
public class DialogSettingsUtil {

	/**
	 * Default number of entries within the combo box history.
	 */
	public static final int COMBO_HISTORY_LENGTH = 5;

	/**
	 * Empty array to be returned by {@link #getSettingsArraySafe(IDialogSettings, String)}.
	 */
	public static final String[] NO_ELEMENTS = new String[0];

	/**
	 * Add the given new history entry to the given array of existing history entries.
	 * Empty history entries or <code>null</code> values will be ignored and not added.
	 *
	 * @param history The array containing the existing history entries. Must not be <code>null</code>!
	 * @param newEntry The new entry to add to the history list.
	 *
	 * @return The new list of history entries.
	 */
	public static String[] addToHistory(String[] history, String newEntry) {
		Assert.isNotNull(history);

		// We have to create a new ArrayList from the transformed array. Otherwise
		// modification of the list would not be supported (java.lang.UnsupportedOperationException).
		List<String> historyList = new ArrayList<String>(Arrays.asList(history));
		addToHistory(historyList, newEntry);
		history = historyList.toArray(new String[historyList.size()]);

		return history;
	}

	/**
	 * Adds the given history entry to the given list of history entries at index 0.
	 * Consistency checks are performed for the resulting list.
	 */
	private static void addToHistory(List<String> history, String newEntry) {
		// just ignore values and do not add them to the history. Null values cannot
		// be handle by SWT controls (like Combobox). However, do not assert null values!
		if (newEntry != null && newEntry.trim().length() > 0) {
			// remove all entries which just are prefixes of the newEntry
			// Avoid to flood the history with partial typed entries for early finish supporting connections!
			StringBuffer entry = new StringBuffer(newEntry);
			while (entry.length() > 0) {
				history.remove(entry.toString());
				entry.deleteCharAt(entry.length() - 1);
			}
			history.add(0, newEntry);

			// since only one new item was added, we can be over the limit
			// by at most one item
			if (history.size() > COMBO_HISTORY_LENGTH) history.remove(COMBO_HISTORY_LENGTH);
		}
	}

	/**
	 * Return a string array from given dialog settings. <code>Null</code> values
	 * will be filtered out.
	 *
	 * @param settings The dialog settings. Must not be <code>null</code>.
	 * @param key The dialog settings attribute key. Must not be <code>null</code>.
	 *
	 * @return The string array or an empty array.
	 */
	public static String[] getSettingsArraySafe(IDialogSettings settings, String key) {
		Assert.isNotNull(settings);
		Assert.isNotNull(key);

		String[] result = null;

		String[] values = settings.getArray(key);
		if (values != null && values.length > 0) {
			boolean filtered = false;
			List<String> filteredValue = new ArrayList<String>(values.length);
			for (String value : values) {
				if (value != null && value.trim().length() > 0) {
					filteredValue.add(value);
				} else {
					filtered = true;
				}
			}
			if (filtered) {
				if (!filteredValue.isEmpty()) {
					result = filteredValue.toArray(new String[filteredValue.size()]);
				}
			} else {
				result = values;
			}
		}
		return result != null ? result : NO_ELEMENTS;
	}
}
