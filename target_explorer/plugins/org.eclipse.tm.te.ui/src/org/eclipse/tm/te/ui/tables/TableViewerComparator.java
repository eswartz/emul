/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.tables;

import java.util.Arrays;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Table;
import org.eclipse.tm.te.ui.AbstractViewerComparator;


/**
 * Target Explorer: Common table control viewer comparator implementation.
 */
public class TableViewerComparator extends AbstractViewerComparator {
	private final ITableLabelProvider labelProvider;

	/**
	 * Constructor.
	 *
	 * @param viewer The parent viewer. Must not be <code>null</code>.
	 * @param labelProvider The table label provider. Must not be <code>null</code>.
	 */
	public TableViewerComparator(Viewer viewer, ITableLabelProvider labelProvider) {
		super(viewer);
		Assert.isNotNull(labelProvider);
		this.labelProvider = labelProvider;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.AbstractViewerComparator#doDetermineInverter(org.eclipse.jface.viewers.Viewer)
	 */
	@Override
	protected int doDetermineInverter(Viewer viewer) {
		int inverter = 1;

		// Viewer must be of type TableViewer and the table must not be disposed yet
		if (viewer instanceof TableViewer && ((TableViewer)viewer).getTable() != null) {
			Table table = ((TableViewer)viewer).getTable();
			if (!table.isDisposed() && table.getSortDirection() == SWT.DOWN) inverter = -1;
		}

		return inverter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.AbstractViewerComparator#doGetText(java.lang.Object, int)
	 */
	@Override
	protected String doGetText(Object node, int index) {
		if (node != null && labelProvider != null) {
			return index != -1 ? labelProvider.getColumnText(node, index) : ((ILabelProvider)labelProvider).getText(node);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.AbstractViewerComparator#doGetSortColumnLabel(org.eclipse.jface.viewers.Viewer)
	 */
	@Override
	protected String doGetSortColumnLabel(Viewer viewer) {
		// Viewer must be of type TableViewer and the table must not be disposed yet
		if (viewer instanceof TableViewer && ((TableViewer)viewer).getTable() != null && !((TableViewer)viewer).getTable().isDisposed()) {
			Table table = ((TableViewer)viewer).getTable();
			return table.getSortColumn() != null ? table.getSortColumn().getText() : ""; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.AbstractViewerComparator#doGetSortColumnIndex(org.eclipse.jface.viewers.Viewer)
	 */
	@Override
	protected int doGetSortColumnIndex(Viewer viewer) {
		// Viewer must be of type TableViewer and the table must not be disposed yet
		if (viewer instanceof TableViewer && ((TableViewer)viewer).getTable() != null && !((TableViewer)viewer).getTable().isDisposed()) {
			Table table = ((TableViewer)viewer).getTable();
			return table.getSortColumn() != null ? Arrays.asList(table.getColumns()).indexOf(table.getSortColumn()) : -1;
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.AbstractViewerComparator#doCompare(java.lang.Object, java.lang.Object, java.lang.String, int, int)
	 */
	@Override
	protected int doCompare(Object node1, Object node2, String sortColumn, int index, int inverter) {
		if (node1 == null && node2 == null) return 0;
		if (node1 != null && node2 == null) return 1;
		if (node1 == null && node2 != null) return -1;

		// Get the labels
		String text1 = doGetText(node1, index);
		String text2 = doGetText(node2, index);

		// If the text is matching ".*[0-9]+$" -> compare numerical instead of alphabetical
		if (text1 != null && text1.matches(".*[0-9]+$") && text2 != null && text2.matches(".*[0-9]+$")) { //$NON-NLS-1$ //$NON-NLS-2$
			// Split numbers and text (note that this effectively removes the number ... splitted[1] == "").
			String[] splitted1 = text1.split("[0-9]+$", 2); //$NON-NLS-1$
			String[] splitted2 = text2.split("[0-9]+$", 2); //$NON-NLS-1$

			// Get the parts to match alphabetical
			String alpha1 = splitted1[0];
			String alpha2 = splitted2[0];

			// The numerical parts is what remains if we strip the alpha parts from the original text
			String num1 = text1.replace(alpha1, ""); //$NON-NLS-1$
			String num2 = text2.replace(alpha2, ""); //$NON-NLS-1$

			// Compare the alpha parts
			int result = getComparator().compare(alpha1, alpha2) * inverter;
			// Only if the alpha parts are equal, compare the numerical parts too
			if (result == 0) {
				result = Integer.decode(num1).compareTo(Integer.decode(num2)) * inverter;
			}

			return result;
		}

		// Compare the text alphabetical
		return getComparator().compare(text1, text2) * inverter;
	}
}
