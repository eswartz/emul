/*********************************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River)		- initial API and implementation
 * William Chen (Wind River)	- [345384]Provide property pages for remote file system nodes
 *********************************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.model;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.te.tcf.filesystem.interfaces.IWindowsFileAttributes;
import org.eclipse.tm.te.tcf.filesystem.internal.url.TcfURLConnection;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;

/**
 * Target Explorer: Representation of a file system tree node.
 * <p>
 * <b>Note:</b> Node construction and child list access is limited to
 * the TCF event dispatch thread.
 */
public final class FSTreeNode extends PlatformObject {
	private final UUID uniqueId = UUID.randomUUID();

	/**
	 * The tree node name.
	 */
	public String name = null;

	/**
	 * The tree node type.
	 */
	public String type = null;

	/**
	 * The tree node file system attributes
	 */
	public IFileSystem.FileAttrs attr = null;

	/**
	 * The peer node the file system tree node is associated with.
	 */
	public IPeerModel peerNode = null;

	/**
	 * The tree node parent.
	 */
	public FSTreeNode parent = null;

	/**
	 * The tree node children.
	 */
	private List<FSTreeNode> children = new ArrayList<FSTreeNode>();

	/**
	 * Flag to mark once the children of the node got queried
	 */
	public boolean childrenQueried = false;

	/**
	 * Flag to mark once the children query is running
	 */
	public boolean childrenQueryRunning = false;

	/**
	 * Constructor.
	 */
	public FSTreeNode() {
		super();
		Assert.isTrue(Protocol.isDispatchThread());
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public final int hashCode() {
		return uniqueId.hashCode();
	}

	/**
	 * Returns the children list storage object.
	 * <p>
	 * <b>Note:</b> This method must be called from within the TCF event dispatch thread only!
	 *
	 * @return The children list storage object.
	 */
	public final List<FSTreeNode> getChildren() {
		Assert.isTrue(Protocol.isDispatchThread());
		return children;
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public final boolean equals(Object obj) {
		if (obj instanceof FSTreeNode) {
			return uniqueId.equals(((FSTreeNode)obj).uniqueId);
		}
		return super.equals(obj);
	}

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder buffer = new StringBuilder(getClass().getSimpleName());
		buffer.append(": name=" + (name != null ? name : super.toString())); //$NON-NLS-1$
		buffer.append(", UUID=" + uniqueId.toString()); //$NON-NLS-1$
		return buffer.toString();
	}

	/**
	 * Return if the node is a Windows file/folder node.
	 * @return true if it is a Windows node, or else false.
	 */
	public boolean isWindowsNode() {
		return attr != null && attr.attributes != null && attr.attributes.containsKey("Win32Attrs"); //$NON-NLS-1$
	}

	/**
	 * Return if the node is a file.
	 * @return true if it is a file, or else false.
	 */
	public boolean isFile() {
		return attr != null && attr.isFile();
	}

	/**
	 * Return if the node is a directory.
	 * @return true if it is a directory, or else false.
	 */
	public boolean isDirectory() {
		return attr != null && attr.isDirectory();
	}

	/**
	 * Return if the attribute specified by the mask bit is turned on.
	 * @param bit The attribute's mask bit.
	 * @return true if it is on, or else false.
	 */
	public boolean isWin32AttrOn(int bit) {
		if (attr != null && attr.attributes.get("Win32Attrs") instanceof Integer) { //$NON-NLS-1$
			Integer win32Attrs = (Integer) attr.attributes.get("Win32Attrs"); //$NON-NLS-1$
			return (win32Attrs.intValue() & bit) != 0;
		}
		return false;
	}

	/**
	 * Return if this file/folder is hidden.
	 * @return true if it is hidden, or else false.
	 */
	public boolean isHidden() {
		return isWin32AttrOn(IWindowsFileAttributes.FILE_ATTRIBUTE_HIDDEN);
	}

	/**
	 * Return if this file/folder is read-only.
	 * @return true if it is read-only, or else false.
	 */
	public boolean isReadOnly() {
		return isWin32AttrOn(IWindowsFileAttributes.FILE_ATTRIBUTE_READONLY);
	}

	/**
	 * Get the location of a file/folder node using the format of the file
	 * system's platform.
	 *
	 * @param node The file/folder node.
	 * @return The location of the file/folder.
	 */
	public String getLocation() {
		if (parent == null)
			return null;
		String location = parent.getLocation(false);
		if (parent.isRoot()) {
			return location + (isWindowsNode() ? "\\" : "/"); //$NON-NLS-1$ //$NON-NLS-2$
		}
		return location;
	}

	/**
	 * Get the location of a file/folder.
	 * 
	 * @param cross If the format is cross-platform.
	 * @return The path to the file/folder.
	 */
	private String getLocation(boolean cross) {
		if (parent == null)
			return null;
		String pLoc = parent.getLocation(cross);
		if (pLoc == null) {
			return name.substring(0, name.length() - 1);
		}
		String pathSep = (!cross && isWindowsNode()) ? "\\" : "/"; //$NON-NLS-1$ //$NON-NLS-2$
		return pLoc + pathSep + name;
	}

	/**
	 * Get the URL of the file or folder. The URL's format
	 * is created in the following way:
	 * tcf:///<TCF_AGENT_ID>/remote/path/to/the/resource...
	 * See {@link TcfURLConnection#TcfURLConnection(URL)}
	 *
	 * @return The URL of the file/folder.
	 * @throws MalformedURLException
	 */
	public URL getLocationURL() throws MalformedURLException {
		String id = peerNode.getPeer().getID();
		String path = getLocation(true);
		String url = TcfURLConnection.PROTOCOL_SCHEMA+":///" + id + (isWindowsNode() ? "/" + path : path); //$NON-NLS-1$ //$NON-NLS-2$
		if (url.endsWith("/")) { //$NON-NLS-1$
			url += name;
		} else {
			url += "/" + name; //$NON-NLS-1$
		}
		return new URL(url);
	}

	/**
	 * If this node is a root node.
	 * @return true if this node is a root node.
	 */
	public boolean isRoot() {
		return type.endsWith("FSRootDirNode"); //$NON-NLS-1$
	}
}
