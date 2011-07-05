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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.content.IContentType;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;

/**
 * The content type helper used to provide helping methods about the content
 * types of the files in the remote file system.
 */
public class ContentTypeHelper {
	// The binary content type's id.
	private static final String CONTENT_TYPE_BINARY_ID = "org.eclipse.cdt.core.binaryFile"; //$NON-NLS-1$
	// The singleton of the content type helper.
	private static ContentTypeHelper instance;
	// Already known resolved content type of file nodes specified by their
	// URLs.
	private Map<URL, IContentType> resolvables = new HashMap<URL, IContentType>();
	// Already known unresolvable file nodes specified by their URLs.
	private Map<URL, FSTreeNode> unresolvables = new HashMap<URL, FSTreeNode>();
	
	/**
	 * Get the singleton instance of the content type helper.
	 * 
	 * @return The singleton instance of the content type helper.
	 */
	public static ContentTypeHelper getInstance() {
		if (instance == null) {
			instance = new ContentTypeHelper();
		}
		return instance;
	}

	/**
	 * Judges if the node is a binary file.
	 * 
	 * @param node
	 *            The file node.
	 * @return true if the node is a binary file or else false.
	 */
	public boolean isBinaryFile(FSTreeNode node) {
		IContentType contentType = getContentType(node);
		if (contentType != null) {
			IContentType binaryFile = Platform.getContentTypeManager()
					.getContentType(CONTENT_TYPE_BINARY_ID);
			if (binaryFile != null && contentType.isKindOf(binaryFile))
				return true;
		}
		return false;
	}

	/**
	 * Get the content type of the specified file node.
	 * 
	 * @param node
	 *            The file node.
	 * @return The content type of the file node.
	 */
	public IContentType getContentType(FSTreeNode node) {
		URL location = null;
		try {
			location = node.getLocationURL();
		} catch (MalformedURLException e1) {
			return null;
		}
		if (unresolvables.get(location) != null)
			// If it is already known unresolvable.
			return null;
		IContentType contentType = null;
		contentType = resolvables.get(location);
		if (contentType != null) 
			// If it is already known to have a certain content type.
			return contentType;
		// First check the content type by its name.
		contentType = Platform.getContentTypeManager().findContentTypeFor(
				node.name);
		if (contentType == null) { // Then find the content type by its stream.
			try {
				contentType = findContentTypeByStream(node);
			} catch (Exception e) {
			}
		}
		if (contentType != null) { // If it is resolved, cache it.
			resolvables.put(location, contentType);
		} else { // Or else, remember it as an unresolvable.
			unresolvables.put(location, node);
		}
		return contentType;
	}

	/**
	 * Find the content type of the file using its content stream.
	 * 
	 * @param node
	 *            The file node.
	 * @return The content type of the file.
	 * @throws CoreException
	 *             If the path of its local cache file couldn't be found.
	 * @throws IOException
	 *             If something goes wrong during the content type parsing.
	 */
	private IContentType findContentTypeByStream(FSTreeNode node)
			throws CoreException, IOException {
		InputStream is = null;
		try {
			if (!CacheManager.getInstance().isCacheStale(node)) {
				// If the local cache file is update to date, then use the local
				// cache file.
				IPath path = CacheManager.getInstance().getCachePath(node);
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(path);
				is = fileStore.openInputStream(EFS.NONE, null);
			} else {
				// Use its URL stream.
				URL url = node.getLocationURL();
				is = url.openStream();
			}
			return Platform.getContentTypeManager().findContentTypeFor(is,
					node.name);
		} finally {
			if (is != null) {
				try {
					is.close();
				} catch (IOException e) {
				}
			}
		}
	}
}
