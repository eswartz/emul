/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.controls;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tm.te.ui.trees.TreeViewerComparator;


/**
 * Target Explorer: File system tree control viewer comparator implementation.
 */
public class FSTreeViewerComparator extends TreeViewerComparator {

	/**
	 * Constructor.
	 *
	 * @param viewer The parent viewer. Must not be <code>null</code>.
	 * @param labelProvider The label provider. Must not be <code>null</code>.
	 */
	public FSTreeViewerComparator(Viewer viewer, ILabelProvider labelProvider) {
		super(viewer, labelProvider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.tcf.vtl.ui.trees.TreeViewerComparator#doCompare(java.lang.Object, java.lang.Object, java.lang.String, int, int)
	 */
	@Override
	protected int doCompare(Object node1, Object node2, String sortColumn, int index, int inverter) {
		if (node1 instanceof FSTreeNode && node2 instanceof FSTreeNode) {
			// Get the type labels
			String t1 = ((FSTreeNode)node1).type;
			String t2 = ((FSTreeNode)node2).type;

			// Group directories and files always together before sorting by name
			if (("FSRootDirNode".equals(t1) || "FSDirNode".equals(t1)) //$NON-NLS-1$ //$NON-NLS-2$
					&& !("FSRootDirNode".equals(t2) || "FSDirNode".equals(t2))) { //$NON-NLS-1$ //$NON-NLS-2$
				return -1 * inverter;
			}

			if (("FSRootDirNode".equals(t2) || "FSDirNode".equals(t2)) //$NON-NLS-1$ //$NON-NLS-2$
					&& !("FSRootDirNode".equals(t1) || "FSDirNode".equals(t1))) { //$NON-NLS-1$ //$NON-NLS-2$
				return 1 * inverter;
			}

			// If the nodes are of the same type and one entry starts
			// with a '.', it comes before the one without a '.'
			if (t1 != null && t2 != null && t1.equals(t2)) {
				String n1 = doGetText(node1, index);
				String n2 = doGetText(node2, index);
				if (n1 != null && n2 != null) {
					if (n1.startsWith(".") && !n2.startsWith(".")) return -1 * inverter; //$NON-NLS-1$ //$NON-NLS-2$
					if (!n1.startsWith(".") && n2.startsWith(".")) return 1 * inverter; //$NON-NLS-1$ //$NON-NLS-2$
				}
			}

		}

		return super.doCompare(node1, node2, sortColumn, index, inverter);
	}
}
