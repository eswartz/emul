/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.trees;

import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.tm.te.ui.AbstractViewerComparator;


/**
 * Target Explorer: Common tree control viewer comparator implementation.
 */
public class TreeViewerComparator extends AbstractViewerComparator {
	private final ILabelProvider labelProvider;

	/**
	 * Constructor.
	 *
	 * @param viewer The parent viewer. Must not be <code>null</code>.
	 * @param labelProvider The label provider. Must not be <code>null</code>.
	 */
	public TreeViewerComparator(Viewer viewer, ILabelProvider labelProvider) {
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

		// Viewer must be of type TreeViewer and the tree must not be disposed yet
		if (viewer instanceof TreeViewer && ((TreeViewer)viewer).getTree() != null) {
			Tree tree = ((TreeViewer)viewer).getTree();
			if (!tree.isDisposed() && tree.getSortDirection() == SWT.DOWN) inverter = -1;
		}

		return inverter;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.AbstractViewerComparator#doGetText(java.lang.Object, int)
	 */
	@Override
	protected String doGetText(Object node, int index) {
		if (node != null && labelProvider != null) {
			return labelProvider.getText(node);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.AbstractViewerComparator#doGetSortColumnLabel(org.eclipse.jface.viewers.Viewer)
	 */
	@Override
	protected String doGetSortColumnLabel(Viewer viewer) {
		// Viewer must be of type TreeViewer and the tree must not be disposed yet
		if (viewer instanceof TreeViewer && ((TreeViewer)viewer).getTree() != null && !((TreeViewer)viewer).getTree().isDisposed()) {
			Tree tree = ((TreeViewer)viewer).getTree();
			return tree.getSortColumn() != null ? tree.getSortColumn().getText() : ""; //$NON-NLS-1$
		}
		return ""; //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.AbstractViewerComparator#doGetSortColumnIndex(org.eclipse.jface.viewers.Viewer)
	 */
	@Override
	protected int doGetSortColumnIndex(Viewer viewer) {
		if (viewer instanceof TreeViewer && ((TreeViewer)viewer).getTree() != null && !((TreeViewer)viewer).getTree().isDisposed()) {
			Tree tree = ((TreeViewer)viewer).getTree();
			return tree.getSortColumn() != null ? Arrays.asList(tree.getColumns()).indexOf(tree.getSortColumn()) : -1;
		}
		return -1;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.tm.te.ui.controls.AbstractViewerComparator#doCompare(java.lang.Object, java.lang.Object, java.lang.String, int, int)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected int doCompare(Object node1, Object node2, String sortColumn, int index, int inverter) {
		if (node1 == null && node2 == null) return 0;
		if (node1 != null && node2 == null) return 1;
		if (node1 == null && node2 != null) return -1;

		// Get the labels
		String text1 = doGetText(node1, index);
		String text2 = doGetText(node2, index);

		// Normalize labels
		if (text1 == null) text1 = ""; //$NON-NLS-1$
		if (text2 == null) text2 = ""; //$NON-NLS-1$

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
