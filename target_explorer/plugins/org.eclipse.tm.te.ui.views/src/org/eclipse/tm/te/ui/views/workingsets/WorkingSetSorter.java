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

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/**
 * Working set sorter
 * <p>
 * Copied and adapted from <code>org.eclipse.ui.internal.navigator.workingsets.WorkingSetSorter</code>.
 */
public class WorkingSetSorter extends ViewerSorter {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerComparator#compare(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public int compare(Viewer viewer, Object e1, Object e2) {
		if (viewer instanceof StructuredViewer) {
			ILabelProvider labelProvider = (ILabelProvider) ((StructuredViewer) viewer).getLabelProvider();
			String text1 = labelProvider.getText(e1);
			String text2 = labelProvider.getText(e2);
			if (text1 != null) {
				return text1.compareTo(text2);
			}
		}
		return -1;
	}

}
