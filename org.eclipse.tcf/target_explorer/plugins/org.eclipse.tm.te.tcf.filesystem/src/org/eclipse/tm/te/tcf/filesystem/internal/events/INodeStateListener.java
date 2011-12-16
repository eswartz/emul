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
package org.eclipse.tm.te.tcf.filesystem.internal.events;

import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;
/**
 * An INodeStateListener is a listener interface. Classes that implement this 
 * interface serve as a listener processing the event that a node state has changed.
 *
 */
public interface INodeStateListener {
	/**
	 * Fired when the state of the specified FSTreeNode has changed.
	 *  
	 * @param node The FSTreeNode whose state has changed.
	 */
	void stateChanged(FSTreeNode node);
}
