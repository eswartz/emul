/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.ui.controls;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.tcf.services.ISysMonitor;

/**
 * Representation of a file system tree node.
 */
public final class ProcessesTreeNode extends PlatformObject {
	private final UUID uuid = UUID.randomUUID();

	/**
	 * The tree node name.
	 */
	public String name = null;

	/**
	 * The tree node type.
	 */
	public String type = null;

	/**
	 * The process context object
	 */
	public ISysMonitor.SysMonitorContext context;

	/**
	 * The internal process id
	 */
	public String id = null;

	/**
	 * The internal parent process id.
	 */
	public String parentId = null;

	/**
	 * The native process id.
	 */
	public long pid = 0L;

	/**
	 * The native parent process id.
	 */
	public long ppid = 0L;

	/**
	 * The process state
	 */
	public String state = null;

	/**
	 * The process owner/creator
	 */
	public String username = null;

	/**
	 * The tree node parent.
	 */
	public ProcessesTreeNode parent = null;

	/**
	 * The tree node children.
	 */
	public List<ProcessesTreeNode> children = new ArrayList<ProcessesTreeNode>();

	/**
	 * Flag to mark once the children of the node got queried
	 */
	public boolean childrenQueried = false;

	/**
	 * Flag to mark once the children query is running
	 */
	public boolean childrenQueryRunning = false;

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		return uuid.hashCode();
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof ProcessesTreeNode) {
			return uuid.equals(((ProcessesTreeNode) obj).uuid);
		}
		return super.equals(obj);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return name != null ? name : super.toString();
	}
}
