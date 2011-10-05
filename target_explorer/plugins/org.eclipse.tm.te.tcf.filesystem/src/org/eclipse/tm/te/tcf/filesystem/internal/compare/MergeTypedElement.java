/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.compare;

import org.eclipse.compare.BufferedContent;
import org.eclipse.compare.CompareUI;
import org.eclipse.compare.ITypedElement;
import org.eclipse.swt.graphics.Image;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;

/**
 * A <code>MergeTypedElement</code> wraps an <code>FSTreeNode</code> so that it
 * can be used as input for the differencing engine (<code>ITypedElement</code>).
 */
public abstract class MergeTypedElement extends BufferedContent implements ITypedElement {
	// The File System tree node to be wrapped.
	protected FSTreeNode node;

	/**
	 * Create a MergeTypedElement for the given node.
	 *
	 * @param node
	 *            The node.
	 */
	public MergeTypedElement(FSTreeNode node) {
		this.node = node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ITypedElement#getImage()
	 */
	@Override
	public Image getImage() {
		return CompareUI.getImage(getType());
	}

	/**
	 * Return the tree node wrapped.
	 *
	 * @return The tree node of the file
	 */
	public FSTreeNode getFSTreeNode() {
		return node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ITypedElement#getType()
	 */
	@Override
	public String getType() {
		if (node.isDirectory())
			return ITypedElement.FOLDER_TYPE;
		if (node != null) {
			String s = node.name;
			int dot = s.lastIndexOf('.');
			if (dot != -1)
				s = s.substring(dot + 1);
			if (s != null)
				return s;
		}
		return ITypedElement.UNKNOWN_TYPE;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object other) {
		if (other instanceof ITypedElement) {
			return toString().equals(other.toString());
		}
		return super.equals(other);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.compare.ITypedElement#getName()
	 */
	@Override
	public String getName() {
		return node.name;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 *
	 * Returns the hash code of the name.
	 */
	@Override
	public int hashCode() {
		return toString().hashCode();
	}
}
