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

import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.Viewer;

/**
 * Target Explorer: File system tree control viewer sorter implementation.
 */
public class FSTreeViewerSorter extends TreePathViewerSorter {
	private final FSTreeLabelProvider labelProvider = new FSTreeLabelProvider();
	private final FSTreeViewerComparator comparator;

	/**
	 * Constructor.
	 */
	public FSTreeViewerSorter() {
		comparator = new FSTreeViewerComparator(labelProvider.getParentViewer(), labelProvider);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		return comparator.compare(viewer, e1, e2);
	}
}
