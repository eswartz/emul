/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.workingsets;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm.te.ui.trees.TreeViewerSorter;
import org.eclipse.tm.te.ui.views.interfaces.workingsets.IWorkingSetIDs;
import org.eclipse.ui.IWorkingSet;

/**
 * Working set viewer sorter implementation.
 */
public class WorkingSetViewerSorter extends TreeViewerSorter {

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.trees.TreeViewerSorter#doCompare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object, java.lang.String, int, int)
	 */
	@Override
	protected int doCompare(Viewer viewer, Object node1, Object node2, String sortColumn, int index, int inverter) {
		if (node1 instanceof IWorkingSet && node2 instanceof IWorkingSet) {
			// The "Others" working set will appear always at the bottom of the tree
			if (IWorkingSetIDs.ID_WS_OTHERS.equals(((IWorkingSet)node1).getId())) {
				return 1;
			}
			if (IWorkingSetIDs.ID_WS_OTHERS.equals(((IWorkingSet)node2).getId())) {
				return -1;
			}
		}
	    return super.doCompare(viewer, node1, node2, sortColumn, index, inverter);
	}

}
