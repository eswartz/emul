/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.navigator;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.viewers.ContentViewer;
import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;

/**
 * Common sorter implementation.
 */
public class Sorter extends TreePathViewerSorter {
	private final ILabelProvider labelProvider = new LabelProviderDelegate();

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

	/**
	 * Returns the text to compare for the given node and column index.
	 *
	 * @param node The node or <code>null</code>.
	 * @param index The column index or <code>-1</code>.
	 *
	 * @return The text for the given node and column index or <code>null</code>.
	 */
	protected String doGetText(Object node, int index) {
		if (node != null) {
			return labelProvider.getText(node);
		}
		return null;
	}

	/**
	 * Return the label provider associated with the specified viewer.
	 *
	 * @param viewer The viewer or <code>null</code>.
	 * @return The label provider or <code>null</code>.
	 */
	protected ILabelProvider doGetLabelProvider(Viewer viewer) {
		if (viewer instanceof ContentViewer) {
			IBaseLabelProvider candidate = ((ContentViewer)viewer).getLabelProvider();
			if (candidate instanceof ILabelProvider) return (ILabelProvider)candidate;
		}
		return null;
	}

	/**
	 * Determine if or if not the sort direction needs to be inverted.
	 *
	 * @param viewer The viewer or <code>null</code>.
	 * @return <code>1</code> for original sort order, or <code>-1</code> for inverted sort order.
	 */
	protected int doDetermineInverter(Viewer viewer) {
		int inverter = 1;

		// Viewer must be of type TreeViewer and the tree must not be disposed yet
		if (viewer instanceof TreeViewer && ((TreeViewer)viewer).getTree() != null) {
			Tree tree = ((TreeViewer)viewer).getTree();
			if (!tree.isDisposed() && tree.getSortDirection() == SWT.DOWN) inverter = -1;
		}

		return inverter;
	}

	/**
	 * Return the label of the sort column of the given viewer.
	 *
	 * @param viewer The viewer or <code>null</code>.
	 * @return The label of the sort column or an empty string.
	 */
	protected String doGetSortColumnLabel(Viewer viewer) {
		// Viewer must be of type TreeViewer and the tree must not be disposed yet
		if (viewer instanceof TreeViewer && ((TreeViewer)viewer).getTree() != null && !((TreeViewer)viewer).getTree().isDisposed()) {
			Tree tree = ((TreeViewer)viewer).getTree();
			return tree.getSortColumn() != null ? tree.getSortColumn().getText() : ""; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	/**
	 * Return the index of the sort column of the given viewer.
	 *
	 * @param viewer The viewer or <code>null</code>.
	 * @return The index of the sort column or <code>-1</code>.
	 */
	protected int doGetSortColumnIndex(Viewer viewer) {
		if (viewer instanceof TreeViewer && ((TreeViewer)viewer).getTree() != null && !((TreeViewer)viewer).getTree().isDisposed()) {
			Tree tree = ((TreeViewer)viewer).getTree();
			return tree.getSortColumn() != null ? Arrays.asList(tree.getColumns()).indexOf(tree.getSortColumn()) : -1;
		}
		return -1;
	}

	/**
	 * Compare the given model nodes by the given sort column and inverter.
	 *
	 * @param node1 The first node or <code>null</code>.
	 * @param node2 The second node or <code>null</code>.
	 * @param sortColumn The sort column text or <code>null</code>.
	 * @param index The sort column index or <code>-1</code>.
	 * @param inverter The inverter.
	 *
	 * @return The compare result.
	 */
	protected int doCompare(Object node1, Object node2, String sortColumn, int index, int inverter) {
		if (node1 == null && node2 == null) return 0;
		if (node1 != null && node2 == null) return 1;
		if (node1 == null && node2 != null) return -1;

		// Get the labels
		String text1 = doGetText(node1, index);
		String text2 = doGetText(node2, index);

		// The tree sorts not strictly alphabetical. First comes entries starting with numbers,
		// second entries starting with uppercase and than all the rest. Additional, if a label contains
		// uppercase characters, it is sorted in before any labels being lowercase only.
		if (text1.length() > 0 && text2.length() > 0) {
			// Get the first characters of both
			char c1 = text1.charAt(0);
			char c2 = text2.charAt(0);

			if (Character.isDigit(c1) || Character.isDigit(c2)) {
				// Check on the differences. If both are digits, the standard compare will do it
				if (Character.isDigit(c1) && !Character.isDigit(c2)) return -1 * inverter;
				if (!Character.isDigit(c1) && Character.isDigit(c2)) return 1 * inverter;
			}

			if (Character.isUpperCase(c1) || Character.isUpperCase(c2)) {
				// Check on the differences. If both are uppercase characters, the standard compare will do it
				if (Character.isUpperCase(c1) && !Character.isUpperCase(c2)) return -1 * inverter;
				if (!Character.isUpperCase(c1) && Character.isUpperCase(c2)) return 1 * inverter;
			}

			Matcher m1 = Pattern.compile("(\\D+)(\\d+)").matcher(text1); //$NON-NLS-1$
			Matcher m2 = Pattern.compile("(\\D+)(\\d+)").matcher(text2); //$NON-NLS-1$
			if (m1.matches() && m2.matches()) {
				String p11 = m1.group(1);
				String p12 = m1.group(2);

				String p21 = m2.group(1);
				String p22 = m2.group(2);

				if (p11 != null && p11.equals(p21)) {
					// Compare the second parts as number
					try {
						int result = 0;
						long l1 = Long.parseLong(p12);
						long l2 = Long.parseLong(p22);

						if (l1 > l2) result = 1;
						if (l1 < l2) result = -1;

						return result;
					} catch (NumberFormatException e) { /* ignored on purpose */ }
				}
			}

			if (text1.matches(".*[A-Z]+.*") || text2.matches(".*[A-Z]+.*")) { //$NON-NLS-1$ //$NON-NLS-2$
				if (text1.matches(".*[A-Z]+.*") && !text2.matches(".*[A-Z]+.*")) return -1 * inverter; //$NON-NLS-1$ //$NON-NLS-2$
				if (!text1.matches(".*[A-Z]+.*") && text2.matches(".*[A-Z]+.*")) return 1 * inverter; //$NON-NLS-1$ //$NON-NLS-2$

				// Additionally, it even depends on the position of the first uppercase
				// character if both strings contains them :-(
				int minLength = Math.min(text1.length(), text2.length());
				for (int i = 0; i < minLength; i++) {
					char ch1 = text1.charAt(i);
					char ch2 = text2.charAt(i);

					if (Character.isUpperCase(ch1) && !Character.isUpperCase(ch2)) return -1 * inverter;
					if (!Character.isUpperCase(ch1) && Character.isUpperCase(ch2)) return 1 * inverter;
					// If both are uppercase, we break the loop and compare as usual
					if (Character.isUpperCase(ch1) && Character.isUpperCase(ch2)) break;
				}
			}
		}

		// Compare the text alphabetical
		return getComparator().compare(text1, text2) * inverter;
	}
}
