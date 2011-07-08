/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.filters;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;

/**
 * Target Explorer: A filter implementation filtering hidden files or directories.
 */
public class HiddenFilesViewerFilter extends ViewerFilter {

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ViewerFilter#select(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		// The element needs to be a tree node, but not a root node
		if (element instanceof FSTreeNode && !"FSRootDirNode".equals(((FSTreeNode)element).type)) { //$NON-NLS-1$
			return !((FSTreeNode)element).isHidden();
		}
		// Let pass all other elements unharmed
		return true;
	}

}
