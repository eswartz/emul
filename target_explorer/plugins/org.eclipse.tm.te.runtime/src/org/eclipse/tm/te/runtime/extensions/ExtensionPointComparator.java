/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.runtime.extensions;

import java.util.Comparator;

import org.eclipse.core.runtime.IExtension;

/**
 * Extension point comparator implementation.
 * <p>
 * The comparator assure that extension are read in a predictable order.
 * <p>
 * The order of the extensions is defined as following:<br>
 * <ul><li>Extensions contributed by Target Explorer plug-ins (<code>org.eclipse.tm.te.*</code>)
 *         in ascending alphabetic order and</li>
 *     <li>Extensions contributed by any other plug-in in ascending alphabetic order.</li>
 *     <li>Extensions contributed by the same plug-in in ascending alphabetic order by the
 *         extensions unique id</li>
 */
public class ExtensionPointComparator implements Comparator<IExtension> {
	private final static String TARGET_EXPLORER_PLUGINS_PATTERN = "org.eclipse.tm.te."; //$NON-NLS-1$

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	@Override
    public int compare(IExtension o1, IExtension o2) {
		// We ignore any comparisation with null and
		if (o1 == null || o2 == null) return 0;
		// Check if it is the exact same element
		if (o1 == o2) return 0;

		// The extensions are compared by the unique id of the contributing plugin first
		String contributor1 = o1.getContributor().getName();
		String contributor2 = o2.getContributor().getName();

		// Contributions from Target Explorer plug-ins comes before 3rdParty plug-ins
		if (contributor1.startsWith(TARGET_EXPLORER_PLUGINS_PATTERN) && !contributor2.startsWith(TARGET_EXPLORER_PLUGINS_PATTERN))
			return -1;
		if (!contributor1.startsWith(TARGET_EXPLORER_PLUGINS_PATTERN) && contributor2.startsWith(TARGET_EXPLORER_PLUGINS_PATTERN))
			return 1;
		if (contributor1.startsWith(TARGET_EXPLORER_PLUGINS_PATTERN) && contributor2.startsWith(TARGET_EXPLORER_PLUGINS_PATTERN)) {
			int value = contributor1.compareTo(contributor2);
			// Within the same plug-in, the extension are sorted by their unique id (if available)
			if (value == 0 && o1.getUniqueIdentifier() != null && o2.getUniqueIdentifier() != null)
				return o1.getUniqueIdentifier().compareTo(o2.getUniqueIdentifier());
			// Otherwise, just return the comparisation result from the contributors
			return value;
		}

		// Contributions from all other plug-ins are sorted alphabetical
		int value = contributor1.compareTo(contributor2);
		// Within the same plug-in, the extension are sorted by their unique id (if available)
		if (value == 0 && o1.getUniqueIdentifier() != null && o2.getUniqueIdentifier() != null)
			return o1.getUniqueIdentifier().compareTo(o2.getUniqueIdentifier());
		// Otherwise, just return the comparisation result from the contributors
		return value;
	}

}
