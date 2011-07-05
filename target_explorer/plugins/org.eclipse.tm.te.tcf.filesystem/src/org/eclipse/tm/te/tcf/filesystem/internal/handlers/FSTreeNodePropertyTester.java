/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * William Chen (Wind River)- [345387]Open the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.handlers;

import org.eclipse.core.expressions.PropertyTester;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;

/**
 * The property tester of an FSTreeNode. The properties include "isFile" telling
 * if it is a file node, "isDirectory" telling if it is a directory, or
 * "isBinaryFile" telling if the file is a binary file.
 */
public class FSTreeNodePropertyTester extends PropertyTester {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.expressions.IPropertyTester#test(java.lang.Object,
	 * java.lang.String, java.lang.Object[], java.lang.Object)
	 */
	public boolean test(Object receiver, String property, Object[] args,
			Object expectedValue) {
		assert receiver != null && receiver instanceof FSTreeNode;
		FSTreeNode node = (FSTreeNode) receiver;
		if (property.equals("isFile")) { //$NON-NLS-1$
			return node.isFile();
		} else if (property.equals("isDirectory")) { //$NON-NLS-1$
			return node.isDirectory();
		} else if (property.equals("isBinaryFile")) { //$NON-NLS-1$
			return ContentTypeHelper.getInstance().isBinaryFile(node);
		}
		return false;
	}
}
