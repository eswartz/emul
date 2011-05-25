/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;

/**
 * Target Explorer: Common viewer comparator implementation.
 */
public abstract class AbstractViewerComparator extends ViewerComparator {
	// Reference to the viewer
	private final Viewer viewer;

	/**
	 * Constructor.
	 *
	 * @param viewer The parent viewer. Must not be <code>null</code>.
	 */
	public AbstractViewerComparator(Viewer viewer) {
		Assert.isNotNull(viewer);
		this.viewer = viewer;
	}

	/**
	 * Returns the parent viewer instance.
	 *
	 * @return The parent viewer instance.
	 */
	protected final Viewer getParentViewer() {
		return viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (viewer != null && viewer.getControl() != null && !viewer.getControl().isDisposed()) {
			return doCompare(e1, e2, doGetSortColumnLabel(viewer), doGetSortColumnIndex(viewer) , doDetermineInverter(viewer));
		}
		return super.compare(viewer, e1, e2);
	}

	/* (non-Javadoc)
	 * @see java.util.Comparator#compare(java.lang.Object, java.lang.Object)
	 */
	public int compare(Object o1, Object o2) {
		return doCompare(o1, o2, null, -1, doDetermineInverter(getParentViewer()));
	}

	/**
	 * Compare the given nodes by the given sort column and inverter.
	 *
	 * @param node1 The first node or <code>null</code>.
	 * @param node2 The second node or <code>null</code>.
	 * @param sortColumn The sort column text or <code>null</code>.
	 * @param index The sort column index or <code>-1</code>.
	 * @param inverter The inverter.
	 *
	 * @return The compare result.
	 */
	protected abstract int doCompare(Object node1, Object node2, String sortColumn, int index, int inverter);

	/**
	 * Returns the text to compare for the given node and column index.
	 *
	 * @param node The node or <code>null</code>.
	 * @param index The column index or <code>-1</code>.
	 *
	 * @return The text for the given node and column index or <code>null</code>.
	 */
	protected abstract String doGetText(Object node, int index);

	/**
	 * Determine if or if not the sort direction needs to be inverted.
	 *
	 * @param viewer The viewer or <code>null</code>.
	 * @return <code>1</code> for original sort order, or <code>-1</code> for inverted sort order.
	 */
	protected abstract int doDetermineInverter(Viewer viewer);

	/**
	 * Return the label of the sort column of the given viewer.
	 *
	 * @param viewer The viewer or <code>null</code>.
	 * @return The label of the sort column or an empty string.
	 */
	protected abstract String doGetSortColumnLabel(Viewer viewer);

	/**
	 * Return the index of the sort column of the given viewer.
	 *
	 * @param viewer The viewer or <code>null</code>.
	 * @return The index of the sort column or <code>-1</code>.
	 */
	protected abstract int doGetSortColumnIndex(Viewer viewer);
}
