/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.ui.views.internal;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.TreePathViewerSorter;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm.te.ui.trees.TreeViewerSorter;
import org.eclipse.ui.navigator.CommonViewerSorter;
import org.eclipse.ui.navigator.INavigatorContentService;

/**
 * Wrapper for the common navigator sorter
 */
public final class ViewViewerSorter extends TreePathViewerSorter {
	// Reference to the wrapped common navigator viewer sorter
	private final CommonViewerSorter sorter;
	// Reference to the default viewer sorter
	private final TreeViewerSorter defaultSorter = new TreeViewerSorter();

	/**
     * Constructor.
     *
     * @param sorter the common navigator viewer sorter to wrap. Must not be <code>null</code>.
     */
    public ViewViewerSorter(CommonViewerSorter sorter) {
    	super();
    	Assert.isNotNull(sorter);
    	this.sorter = sorter;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerComparator#category(java.lang.Object)
     */
    @Override
    public int category(Object element) {
        return sorter.category(element);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.TreePathViewerSorter#compare(org.eclipse.jface.viewers.Viewer, org.eclipse.jface.viewers.TreePath, java.lang.Object, java.lang.Object)
     */
    @Override
    public int compare(Viewer viewer, TreePath parentPath, Object e1, Object e2) {
        int result = sorter.compare(viewer, parentPath, e1, e2);

        if (result == category(e1) - category(e2)) {
        	int defaultSorterResult = defaultSorter.compare(viewer, parentPath, e1, e2);
        	if (defaultSorterResult != 0) result = defaultSorterResult;
        }

        return result;
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.ViewerComparator#isSorterProperty(java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isSorterProperty(Object element, String property) {
        return sorter.isSorterProperty(element, property);
    }

    /* (non-Javadoc)
     * @see org.eclipse.jface.viewers.TreePathViewerSorter#isSorterProperty(org.eclipse.jface.viewers.TreePath, java.lang.Object, java.lang.String)
     */
    @Override
    public boolean isSorterProperty(TreePath parentPath, Object element, String property) {
        return sorter.isSorterProperty(parentPath, element, property);
    }

    /**
     * Sets the content service instance to the common navigator viewer sorter.
     *
     * @param contentService The content service instance. Must not be <code>null</code>:
     */
	public void setContentService(INavigatorContentService contentService) {
		Assert.isNotNull(contentService);
		sorter.setContentService(contentService);
	}

}
