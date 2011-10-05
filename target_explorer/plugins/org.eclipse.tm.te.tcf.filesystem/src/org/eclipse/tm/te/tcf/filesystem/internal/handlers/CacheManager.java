/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 * William Chen (Wind River)- [345387] Open the remote files with a proper editor
 * William Chen (Wind River)- [345552] Edit the remote files with a proper editor
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.internal.handlers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.te.tcf.filesystem.activator.UIPlugin;
import org.eclipse.tm.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tm.te.tcf.filesystem.internal.preferences.TargetExplorerPreferencePage;
import org.eclipse.tm.te.tcf.filesystem.internal.url.TcfURLConnection;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.ui.PlatformUI;

/**
 * The local file system cache used to manage the temporary files downloaded
 * from a remote file system.
 */
public class CacheManager {

	// Time stamp file used to persist the time stamps of each file.
	private static final String TIMESTAMP_FILE = "timestamps.xml"; //$NON-NLS-1$

	// The agent directory's prefixed name.
	private static final String WS_AGENT_DIR_PREFIX = "agent_"; //$NON-NLS-1$

	// The default chunk size of the buffer used during downloading files.
	private static final int DEFAULT_CHUNK_SIZE = 5 * 1024;

	// The formatter used to format the size displayed while downloading.
	private static final DecimalFormat SIZE_FORMAT = new DecimalFormat("#,##0.##"); //$NON-NLS-1$

	// The singleton instance.
	private static CacheManager instance;

	// The time stamp for each file.
	private Map<URL, Long> timestamps;

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
	 * Create a cache manager.
	 */
	private CacheManager() {
		loadTimestamps();
	}

	/**
	 * If the option of "autosaving" is set to on.
	 *
	 * @return true if it is auto saving or else false.
	 */
	public boolean isAutoSaving(){
		IPreferenceStore preferenceStore = UIPlugin.getDefault().getPreferenceStore();
		boolean autoSaving = preferenceStore.getBoolean(TargetExplorerPreferencePage.PREF_AUTOSAVING);
		return autoSaving;
	}

	/**
	 * Load the time stamps from the time stamps file in the cache's root directory.
	 */
	private void loadTimestamps() {
		timestamps = Collections.synchronizedMap(new HashMap<URL, Long>());
		File location = getCacheRoot();
		File tsFile = new File(location, TIMESTAMP_FILE);
		if (tsFile.exists()) {
			Properties properties = new Properties();
			InputStream input = null;
			try {
				input = new BufferedInputStream(new FileInputStream(tsFile));
				properties.loadFromXML(input);
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
					}
				}
			}
			Enumeration<String> keys = (Enumeration<String>) properties.propertyNames();
			while (keys.hasMoreElements()) {
				String key = keys.nextElement();
				String value = properties.getProperty(key);
				long timestamp = 0L;
				try {
					timestamp = Long.parseLong(value);
					timestamps.put(new URL(key), Long.valueOf(timestamp));
				} catch (Exception nfe) {
				}
			}
		}
	}

	/**
	 * Save the time stamps to the time stamps file.
	 */
	private void saveTimestamps(){
		Properties properties = new Properties();
		for(URL key:timestamps.keySet()){
			Long timestamp = timestamps.get(key);
			properties.setProperty(key.toString(), timestamp.toString());
		}
        File location = getCacheRoot();
        File fTimestamp = new File(location, TIMESTAMP_FILE);
        OutputStream output = null;
        try{
        	output = new BufferedOutputStream(new FileOutputStream(fTimestamp));
        	properties.storeToXML(output, null);
        }catch(IOException e){
        	e.printStackTrace();
        }finally{
        	if(output!=null){
        		try{
        			output.close();
        		}catch(Exception e){}
        	}
        }
	}

	/**
	 * Set the time stamp of the FSTreeNode with the specified location.
	 * @param url The FSTreeNode's location URL.
	 * @param timestamp The new base time stamp to be set.
	 */
	public void setBaseTimestamp(URL url, long timestamp){
		timestamps.put(url, Long.valueOf(timestamp));
		// Persist as well.
		saveTimestamps();
	}

	/**
	 * Remove the time stamp entry with the specified URL.
	 * @param url The URL key.
	 */
	public void removeBaseTimestamp(URL url){
		timestamps.remove(url);
		// Persist
		saveTimestamps();
	}

	/**
	 * Get the time stamp of the FSTreeNode with the specified location.
	 *
	 * @param url The FSTreeNode's location URL.
	 * @return The FSTreeNode's base time stamp.
	 */
	public long getBaseTimestamp(URL url){
		Long timestamp = timestamps.get(url);
		return timestamp == null ? 0L : timestamp.longValue();
	}

	/**
	 * Get the local path of a node's cached file.
	 * <p>
	 * The preferred location is within the plugin's state location, in
	 * example <code>&lt;state location&gt;agent_<hashcode_of_peerId>/remote/path/to/the/file...</code>.
	 * <p>
	 * If the plug-in is loaded in a RCP workspace-less environment, the
	 * fall back strategy is to use the users home directory.
	 *
	 * @param node
	 *            The file/folder node.
	 * @return The local path of the node's cached file.
	 */
	public IPath getCachePath(FSTreeNode node) {
        File location = getCacheRoot();
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
	 * Get the local file of the specified node.
	 *
	 * <p>
	 * The preferred location is within the plugin's state location, in
	 * example <code>&lt;state location&gt;agent_<hashcode_of_peerId>/remote/path/to/the/file...</code>.
	 * <p>
	 * If the plug-in is loaded in a RCP workspace-less environment, the
	 * fall back strategy is to use the users home directory.
	 *
	 * @param node
	 *            The file/folder node.
	 * @return The file object of the node's local cache.
	 */
	public File getCacheFile(FSTreeNode node){
		return getCachePath(node).toFile();
	}

	/**
	 * Get the cache file system's root directory on the local host's
	 * file system.
	 *
	 * @return The root folder's location of the cache file system.
	 */
	private File getCacheRoot() {
		File location;
        try {
        	location = UIPlugin.getDefault().getStateLocation().toFile();
        }catch (IllegalStateException e) {
            // An RCP workspace-less environment (-data @none)
        	location = new File(System.getProperty("user.home"), ".tcf"); //$NON-NLS-1$ //$NON-NLS-2$
        	location = new File(location, "fs"); //$NON-NLS-1$
        }

        // Create the location if it not exist
		if (!location.exists()) location.mkdir();
		return location;
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
		if (!node.isRoot() && node.parent!=null) {
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
	 * @return The path with the segment "name" appended.
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
	 * Download the data of the file from the remote file system.
	 *	Must be called within a UI thread.
	 * @param node
	 *            The file node.
	 *
	 * @return true if it is successful, false there're errors or it is
	 *         canceled.
	 */
	public boolean download(final FSTreeNode node) {
		IRunnableWithProgress runnable = new IRunnableWithProgress() {
			@Override
			public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
				monitor.beginTask(NLS.bind(Messages.CacheManager_DowloadingFile, node.name), 100);
				OutputStream output = null;
				try {
					// Write the data to its local cache file.
					File file = getCachePath(node).toFile();
					if(file.exists() && !file.canWrite()){
						// If the file exists and is read-only, delete it.
						file.delete();
					}
					output = new BufferedOutputStream(new FileOutputStream(file));
					download2OutputStream(node, output, monitor);
					if (monitor.isCanceled())
						throw new InterruptedException();
				} catch (IOException e) {
					throw new InvocationTargetException(e);
				} finally {
					if (output != null) {
						try {
							output.close();
						} catch (Exception e) {
						}
					}
					monitor.done();
				}
			}
		};
		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		TimeTriggeredProgressMonitorDialog dialog = new TimeTriggeredProgressMonitorDialog(
				parent, 250);
		dialog.setCancelable(true);
		File file = getCachePath(node).toFile();
		try {
			dialog.run(true, true, runnable);
			// If downloading is successful, update the attributes of the file and
			// set the last modified time to that of its corresponding file.
			StateManager.getInstance().updateState(node);
			// If the node is read-only, make the cache file read-only.
			if(!node.isWritable())
				file.setReadOnly();
			return true;
		} catch(TCFException e) {
			MessageDialog.openError(parent, Messages.StateManager_UpdateFailureTitle, e.getLocalizedMessage());
		} catch (InvocationTargetException e) {
			// Something's gone wrong. Roll back the downloading and display the
			// error.
			file.delete();
			removeBaseTimestamp(node.getLocationURL());
			displayError(parent, e);
		} catch (InterruptedException e) {
			// It is canceled. Just roll back the downloading result.
			file.delete();
			removeBaseTimestamp(node.getLocationURL());
		}
		return false;
	}

	/**
	 * Upload the local files to the remote file system.
	 * Must be called within UI thread.
	 * @param nodes
	 *            The files' location. Not null.
	 *
	 * @return true if it is successful, false there're errors or it is
	 *         canceled.
	 */
	public boolean upload(final FSTreeNode... nodes) {
		Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
		try {
			IRunnableWithProgress runnable = new IRunnableWithProgress() {
				@Override
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						String message;
						if(nodes.length==1)
							message = NLS.bind(Messages.CacheManager_UploadSingleFile, nodes[0].name);
						else
							message = NLS.bind(Messages.CacheManager_UploadNFiles, Long.valueOf(nodes.length));
						monitor.beginTask(message, 100);
						boolean canceled = uploadFiles(monitor, nodes);
						if (canceled)
							throw new InterruptedException();
					} catch (Exception e) {
						throw new InvocationTargetException(e);
					} finally {
						monitor.done();
					}
				}
			};
			TimeTriggeredProgressMonitorDialog dialog = new TimeTriggeredProgressMonitorDialog(parent, 250);
			dialog.setCancelable(true);
			dialog.run(true, true, runnable);
			return true;
		} catch (InvocationTargetException e) {
			// Something's gone wrong. Roll back the downloading and display the
			// error.
			displayError(parent, e);
		} catch (InterruptedException e) {
			// It is canceled. Just roll back the downloading result.
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
	private void displayError(Shell parent, InvocationTargetException e) {
		Throwable target = e.getTargetException();
		Throwable cause = target.getCause() != null ? target.getCause() : target;
		MessageDialog.openError(parent, Messages.CacheManager_DownloadingError, cause.getLocalizedMessage());
	}

	/**
	 * Upload the specified files using the monitor to report the progress.
	 *
	 * @param peers
	 *            The local files' peer files.
	 * @param locals
	 *            The local files to be uploaded.
	 * @param monitor
	 *            The monitor used to report the progress.
	 * @return true if it is canceled or else false.
	 * @throws Exception
	 *             an Exception thrown during downloading and storing data.
	 */
	public boolean uploadFiles(IProgressMonitor monitor, FSTreeNode... nodes) throws IOException {
		BufferedInputStream input = null;
		BufferedOutputStream output = null;
		// The buffer used to download the file.
		byte[] data = new byte[DEFAULT_CHUNK_SIZE];
		// Calculate the total size.
		long totalSize = 0;
		for (FSTreeNode node : nodes) {
			File file = getCachePath(node).toFile();
			totalSize += file.length();
		}
		// Calculate the chunk size of one percent.
		int chunk_size = (int) totalSize / 100;
		// The current reading percentage.
		int percentRead = 0;
		// The current length of read bytes.
		long bytesRead = 0;
		for (int i = 0; i < nodes.length && !monitor.isCanceled(); i++) {
			File file = getCachePath(nodes[i]).toFile();
			try {
				URL url = nodes[i].getLocationURL();
				TcfURLConnection connection = (TcfURLConnection) url.openConnection();
				connection.setDoInput(false);
				connection.setDoOutput(true);
				input = new BufferedInputStream(new FileInputStream(file));
				output = new BufferedOutputStream(connection.getOutputStream());

				// Total size displayed on the progress dialog.
				String fileLength = formatSize(file.length());
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
							monitor.subTask(NLS.bind(Messages.CacheManager_UploadingProgress, new Object[]{file.getName(), formatSize(bytesRead), fileLength}));
						}
					}
				}
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
				if(!monitor.isCanceled()){
					// Once upload is successful, synchronize the modified time.
					try {
						StateManager.getInstance().commitState(nodes[i]);
					} catch (TCFException tcfe) {
						throw new IOException(tcfe.getLocalizedMessage());
					}
				}
			}
		}
		return monitor.isCanceled();
	}

	/**
	 * Download the specified file into an output stream using the monitor to report the progress.
	 *
	 * @param node
	 *            The file to be downloaded.
	 * @param output
	 * 				The output stream.
	 * @param monitor
	 *            The monitor used to report the progress.
	 * @throws IOException
	 *             an IOException thrown during downloading and storing data.
	 */
	public void download2OutputStream(FSTreeNode node, OutputStream output, IProgressMonitor monitor) throws IOException {
		InputStream input = null;
		// Open the input stream of the node using the tcf stream protocol.
		try{
			URL url = node.getLocationURL();
			InputStream in = url.openStream();
			input = new BufferedInputStream(in);
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
		}finally{
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
