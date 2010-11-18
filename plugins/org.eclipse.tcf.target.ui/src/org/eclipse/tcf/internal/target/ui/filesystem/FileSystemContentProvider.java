/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.target.ui.filesystem;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tcf.target.core.ITarget;

public class FileSystemContentProvider implements ITreeContentProvider {

	private TreeViewer viewer;
	
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Doesn't make sense if this isn't a tree viewer.
		if (viewer instanceof TreeViewer)
			this.viewer = (TreeViewer)viewer;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		FileSystemNode fileSystemNode = null;
		if (parentElement instanceof ITarget) {
			ITarget target = (ITarget)parentElement;
			fileSystemNode = FileSystemNode.createFor(target);
			return new Object[] { fileSystemNode };
		}

		if (parentElement instanceof FileSystemNode)
			fileSystemNode = (FileSystemNode)parentElement;
		
		if (fileSystemNode != null) {
			return fileSystemNode.getRoots(viewer, parentElement);
		}

		return new Object[0];
	}
		
	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ITarget)
			return true;
		if (element instanceof FileSystemNode)
			return true;
		return false;
	}

}
