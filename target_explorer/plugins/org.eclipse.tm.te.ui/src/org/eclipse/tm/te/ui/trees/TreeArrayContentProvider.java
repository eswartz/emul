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

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;

/**
 * This implementation of <code>ITreeContentProvider</code> handles
 * the case where the viewer input is an unchanging array or collection of elements.
 */
public class TreeArrayContentProvider extends ArrayContentProvider implements ITreeContentProvider {

	private static TreeArrayContentProvider instance;

	/**
	 * Returns an instance of TreeArrayContentProvider. Since instances of this
	 * class do not maintain any state, they can be shared between multiple
	 * clients.
	 *
	 * @return an instance of TreeArrayContentProvider
	 */
	public static TreeArrayContentProvider getInstance() {
		synchronized(TreeArrayContentProvider.class) {
			if (instance == null) {
				instance = new TreeArrayContentProvider();
			}
			return instance;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
    public Object[] getChildren(Object parentElement) {
		return getElements(parentElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
    public Object getParent(Object element) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
    public boolean hasChildren(Object element) {
		return false;
	}
}
