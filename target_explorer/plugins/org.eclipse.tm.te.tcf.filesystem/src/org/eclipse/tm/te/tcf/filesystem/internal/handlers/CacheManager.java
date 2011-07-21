/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345387]Open the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.DecimalFormat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;

/**
 * The local file system cache used to manage the temporary files downloaded
 * from a remote file system.
 */
public class CacheManager {
	private static final String WS_AGENT_DIR_PREFIX = "agent_"; //$NON-NLS-1$

	// The singleton instance.
	private static CacheManager instance;
	// The formatter used to format the size displayed while downloading.
	private static DecimalFormat SIZE_FORMAT = new DecimalFormat("#,##0.##"); //$NON-NLS-1$
	// The default chunk size of the buffer used during downloading files.
	private static final int DEFAULT_CHUNK_SIZE = 5 * 1024;

	/**
	 * Get the singleton cache manager.
	 *
	 * @return The singleton cache manager.
	 */
	public static CacheManager getInstance() {
		if (instance == null) {
			instance = new CacheManager();
		}
		return instance;
	}

	/**
	 * Get the local path of a node's cached file.
	 * <p>
	 * The preferred location is within the plugin's state location, in
	 * example <code>&lt;state location&gt;agent_<hashcode_of_peerId>/remote/path/to/the/file...</code>.
	 * <p>
	 * If the plugin is loaded in a RCP workspace-less environment, the
	 * fallback strategy is to use the users home directory.
	 *
	 * @param node
	 *            The file/folder node.
	 * @return The local path of the node's cached file.
	 */
	public IPath getCachePath(FSTreeNode node) {
        File location;
        try {
        	location = UIPlugin.getDefault().getStateLocation().toFile();
        }
        catch (IllegalStateException e) {
            // An RCP workspace-less environment (-data @none)
        	location = new File(System.getProperty("user.home"), ".tcf"); //$NON-NLS-1$ //$NON-NLS-2$
        	location = new File(location, "fs"); //$NON-NLS-1$
        }

        // Create the location if it not exist
		if (!location.exists()) location.mkdir();

		String agentId = node.peerNode.getPeer().getID();
		// Use Math.abs to avoid negative hash value.
		String agent = WS_AGENT_DIR_PREFIX + Math.abs(agentId.hashCode());
		IPath agentDir = new Path(location.getAbsolutePath()).append(agent);
		File agentDirFile = agentDir.toFile();
		if (!agentDirFile.exists()) {
			agentDirFile.mkdir();
		}

		return appendNodePath(agentDir, node);
	}

	/**
	 * Append the path with the specified node's context path.
	 *
	 * @param path
	 *            The path to be appended.
	 * @param node
	 *            The file/folder node.
	 * @return The path to the node.
	 */
	private IPath appendNodePath(IPath path, FSTreeNode node) {
		if (!node.isRoot()) {
			path = appendNodePath(path, node.parent);
			return appendPathSegment(node, path, node.name);
		}
		if (node.isWindowsNode()) {
			String name = node.name.substring(0, 1);
			return appendPathSegment(node, path, name);
		}
		return path;
	}

	/**
	 * Append the path with the segment "name". Create a directory
	 * if the node is a directory which does not yet exist.
	 *
	 * @param node The file/folder node.
	 * @param path The path to appended.
	 * @param name The segment's name.
	 * @return
	 */
	private IPath appendPathSegment(FSTreeNode node, IPath path, String name) {
		IPath newPath = path.append(name);
		File newFile = newPath.toFile();
		if (node.isDirectory() && !newFile.exists()) {
			newFile.mkdir();
		}
		return newPath;
	}

	/**
	 * Get the cache file's staleness. If the cache file does not exist or the
	 * file is already out-dated, then return true.
	 * <p>
	 * When a local cache file is created, the modified time is set to that of
	 * its remote corresponding file. So if its remote file is changed, the
	 * modified time stamp should be different from the local cache file's
	 * modified time stamp.
	 * <p>
	 * See the method "download" for more details.
	 * <p>
	 *
	 * @param node
	 *            The file/folder node.
	 * @return true if the file doesn't exist or it is stale.
	 */
	public boolean isCacheStale(FSTreeNode node) {
		IPath path = getCachePath(node);
		File file = path.toFile();
		return !file.exists() || file.lastModified() != node.attr.mtime;
	}

	/**
	 * Download the data of the file from the remote file system.
	 *
	 * @param node
	 *            The file node.
	 * @param parent
	 *            The shell parent used to display messages.
	 *
	 * @return true if it is successful, false there're errors or it is
	 *         canceled.
	 */
	public boolean download(final FSTreeNode node, Shell parent) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor)
					throws InvocationTargetException, InterruptedException {
				monitor.beginTask(NLS.bind(Messages.CacheManager_DowloadingFile, node.name), 100);
				try {
					boolean canceled = downloadFile(node, monitor);
					if (canceled)
						throw new InterruptedException();
				} catch (IOException e) {
					throw new InvocationTargetException(e);
				} finally {
					monitor.done();
				}
			}
		};
		TimeTriggeredProgressMonitorDialog dialog = new TimeTriggeredProgressMonitorDialog(
				parent, 250);
		dialog.setCancelable(true);
		File file = getCachePath(node).toFile();
		try {
			dialog.run(true, true, runnable);
			// If downloading is successful, set the last modified time to
			// that of its corresponding file.
			file.setLastModified(node.attr.mtime);
			// If the node is read-only, make the cache file read-only.
			if(!node.isWritable())
				file.setReadOnly();
			return true;
		} catch (InvocationTargetException e) {
			// Something's gone wrong. Roll back the downloading and display the
			// error.
			file.delete();
			displayError(node, parent, e);
		} catch (InterruptedException e) {
			// It is canceled. Just roll back the downloading result.
			file.delete();
		}
		return false;
	}

	/**
	 * Display the error in an error dialog.
	 *
	 * @param node
	 *            the file node.
	 * @param parent
	 *            the parent shell.
	 * @param e
	 *            The error exception.
	 */
	private void displayError(FSTreeNode node, Shell parent, InvocationTargetException e) {
		Throwable target = e.getTargetException();
		Throwable cause = target.getCause() != null ? target.getCause() : target;
		MessageDialog.openError(parent, Messages.CacheManager_DownloadingError, cause.getLocalizedMessage());
	}

	/**
	 * Download the specified file using the monitor to report the progress.
	 *
	 * @param node
	 *            The file to be downloaded.
	 * @param monitor
	 *            The monitor used to report the progress.
	 * @return true if it is canceled or else false.
	 * @throws IOException
	 *             an IOException thrown during downloading and storing data.
	 */
	protected boolean downloadFile(FSTreeNode node, IProgressMonitor monitor)
			throws IOException {
		BufferedInputStream input = null;
		BufferedOutputStream output = null;
		try {
			// Open the input stream of the node using the tcf stream protocol.
			URL url = node.getLocationURL();
			InputStream in = url.openStream();
			input = new BufferedInputStream(in);
			// Write the data to its local cache file.
			File file = getCachePath(node).toFile();
			if(file.exists() && !file.canWrite()){
				// If the file exists and is read-only, delete it.
				file.delete();
			}
			output = new BufferedOutputStream(new FileOutputStream(file));

			// The buffer used to download the file.
			byte[] data = new byte[DEFAULT_CHUNK_SIZE];
			// Calculate the chunk size of one percent.
			int chunk_size = (int) node.attr.size / 100;
			// Total size displayed on the progress dialog.
			String total_size = formatSize(node.attr.size);

			int percentRead = 0;
			long bytesRead = 0;
			int length;
			while ((length = input.read(data)) >= 0 && !monitor.isCanceled()) {
				output.write(data, 0, length);
				output.flush();
				bytesRead += length;
				if (chunk_size != 0) {
					int percent = (int) bytesRead / chunk_size;
					if (percent != percentRead) { // Update the progress.
						monitor.worked(percent - percentRead);
						percentRead = percent; // Remember the percentage.
						// Report the progress.
						monitor.subTask(NLS.bind(Messages.CacheManager_DownloadingProgress, formatSize(bytesRead), total_size));
					}
				}
			}
			return monitor.isCanceled();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (Exception e) {
				}
			}
			if (input != null) {
				try {
					input.close();
				} catch (Exception e) {
				}
			}
		}
	}

	/**
	 * Use the SIZE_FORMAT to format the file's size. The rule is: 1. If the
	 * size is less than 1024 bytes, then show it as "####" bytes. 2. If the
	 * size is less than 1024 KBs, while more than 1 KB, then show it as
	 * "####.##" KBs. 3. If the size is more than 1 MB, then show it as
	 * "####.##" MBs.
	 *
	 * @param size
	 *            The file size to be displayed.
	 * @return The string representation of the size.
	 */
	private String formatSize(long size) {
		double kbSize = size / 1024.0;
		if (kbSize < 1.0) {
			return SIZE_FORMAT.format(size) + Messages.CacheManager_Bytes;
		}
		double mbSize = kbSize / 1024.0;
		if (mbSize < 1.0)
			return SIZE_FORMAT.format(kbSize) + Messages.CacheManager_KBs;
		return SIZE_FORMAT.format(mbSize) + Messages.CacheManager_MBs;
	}
}
