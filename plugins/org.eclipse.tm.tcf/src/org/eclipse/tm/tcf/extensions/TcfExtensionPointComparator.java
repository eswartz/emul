/*******************************************************************************
 * Copyright (c) 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.tcf.extensions;

import java.util.Comparator;

import org.eclipse.core.runtime.IExtension;

/**
 * TCF extension point comparator. Used to asure that extension are
 * always read in the same order.
 * <p>
 * The order of the extensions is defined as following:<br>
 * <ul><li>Extensions contributed by the TCF core plug-ins (<code>org.eclipse.tm.tcf.*</code>)
 *         in ascending alphabetic order and</li>
 *     <li>Extensions contributed by any other plug-in in ascending alphabetic order.</li>
 *     <li>Extensions contributed by the same plug-in in ascending alphabetic order by the
 *         extensions unique id</li>
 * </ul>
 */
public class TcfExtensionPointComparator implements Comparator<IExtension> {
    private final static String TCF_PLUGIN_PATTERN = "org.eclipse.tm.tcf.*"; //$NON-NLS-1$

    /* (non-Javadoc)
     * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
     */
    public int compare(IExtension o1, IExtension o2) {
        // We ignore any comparisation with null and
        if (o1 == null || o2 == null) return 0;
        // Check if it is the exact same element
        if (o1 == o2) return 0;

        // The extensions are compared by the unique id of the contributing plugin first
        String contributor1 = o1.getContributor().getName();
        String contributor2 = o2.getContributor().getName();

        // Contributions from TCF core plugins comes before 3rdParty Plugins
        if (contributor1.startsWith(TCF_PLUGIN_PATTERN) && !contributor2.startsWith(TCF_PLUGIN_PATTERN))
            return -1;
        if (!contributor1.startsWith(TCF_PLUGIN_PATTERN) && contributor2.startsWith(TCF_PLUGIN_PATTERN))
            return 1;
        if (contributor1.startsWith(TCF_PLUGIN_PATTERN) && contributor2.startsWith(TCF_PLUGIN_PATTERN)) {
            int value = contributor1.compareTo(contributor2);
            // Within the same plugins, the extension are sorted by thier unique id (if available)
            if (value == 0 && o1.getUniqueIdentifier() != null && o2.getUniqueIdentifier() != null)
                return o1.getUniqueIdentifier().compareTo(o2.getUniqueIdentifier());
            // Otherwise, just return the comparisation result from the contributors
            return value;
        }

        // Contributions from all other plugins are sorted alphabetical
        int value = contributor1.compareTo(contributor2);
        // Within the same plugins, the extension are sorted by thier unique id (if available)
        if (value == 0 && o1.getUniqueIdentifier() != null && o2.getUniqueIdentifier() != null)
            return o1.getUniqueIdentifier().compareTo(o2.getUniqueIdentifier());
        // Otherwise, just return the comparisation result from the contributors
        return value;
    }

}
