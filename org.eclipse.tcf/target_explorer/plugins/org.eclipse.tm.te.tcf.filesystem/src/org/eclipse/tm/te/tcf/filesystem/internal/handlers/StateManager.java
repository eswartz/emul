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
package org.eclipse.tm.te.tcf.filesystem.internal.handlers;

import java.io.File;

import org.eclipse.core.runtime.Assert;
import org.eclipse.osgi.util.NLS;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.DoneSetStat;
import org.eclipse.tm.tcf.services.IFileSystem.DoneStat;
import org.eclipse.tm.tcf.services.IFileSystem.FileAttrs;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.te.tcf.core.Tcf;
import org.eclipse.tm.te.tcf.core.interfaces.IChannelManager.DoneOpenChannel;
import org.eclipse.tm.te.tcf.filesystem.internal.exceptions.TCFChannelException;
import org.eclipse.tm.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tm.te.tcf.filesystem.internal.exceptions.TCFFileSystemException;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tm.te.tcf.filesystem.internal.url.Rendezvous;
import org.eclipse.tm.te.tcf.filesystem.model.CacheState;
import org.eclipse.tm.te.tcf.filesystem.model.FSModel;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;

/**
 * This class provides several utility methods to get, update, commit
 * or refresh a file node's state.
 *
 */
public class StateManager {

	// The singleton instance.
	private static StateManager instance;

	/**
	 * Get the singleton user manager.
	 *
	 * @return The singleton cache manager.
	 */
	public static StateManager getInstance() {
		if (instance == null) {
			instance = new StateManager();
		}
		return instance;
	}

	/**
	 * Create a StateManager fInstance.
	 */
	private StateManager() {
	}

	/**
	 * Update the state of the specified node.
	 *
	 * @param node The tree node whose state is going to be updated.
	 * @throws TCFException
	 */
	public void updateState(FSTreeNode node) throws TCFException {
		updateFileStat(node, true);
	}

	/**
	 * Refresh the state of the specified node.
	 *
	 * @param node The tree node whose state is going to be refreshed.
	 * @throws TCFException
	 */
	public void refreshState(FSTreeNode node) throws TCFException {
		updateFileStat(node, false);
	}

	/**
	 * Update the file's state of the specified tree node. Synchronize the time stamp of
	 * the file with the base time stamp and that of the remote file if sync is true.
	 *
	 * @param node The tree node whose file state is going to be updated.
	 * @param sync If its base time stamp and its remote file's time stamp should be synchronized.
	 */
	private void updateFileStat(final FSTreeNode node, final boolean sync) throws TCFException {
		IChannel channel = null;
		try {
			channel = openChannel(node.peerNode.getPeer());
			if (channel != null) {
				IFileSystem service = channel.getRemoteService(IFileSystem.class);
				if (service != null) {
					final TCFFileSystemException[] errors = new TCFFileSystemException[1];
					final Rendezvous rendezvous = new Rendezvous();
					String path = node.getLocation(true);
					service.stat(path, new DoneStat() {
						@Override
						public void doneStat(IToken token, FileSystemException error, FileAttrs attrs) {
							if (error == null) {
								updateNodeAttr(node, attrs, sync);
							} else {
								String message = NLS.bind(Messages.StateManager_CannotGetFileStatMessage, new Object[]{node.name, error});
								errors[0] = new TCFFileSystemException(message, error);
							}
							rendezvous.arrive();
						}
					});
					try {
						rendezvous.waiting(5000L);
					} catch (InterruptedException e) {
						String message = NLS.bind(Messages.StateManager_CannotGetFileStateMessage2, new Object[]{node.name, e});
						errors[0] = new TCFFileSystemException(message, e);
					}
					if (errors[0] != null) {
						throw errors[0];
					}
				}else{
					String message = NLS.bind(Messages.StateManager_TCFNotProvideFSMessage, node.peerNode.getPeer().getID());
					throw new TCFFileSystemException(message);
				}
			}
		} finally {
			if (channel != null)
				channel.close();
		}
	}

	/**
	 * Update the file attribute of the specified tree node with the specified value
	 * and synchronize its base timestamp and its remote file's timestamp if "sync" is true.
	 *
	 * @param node The tree node whose file attribute is to updated.
	 * @param attr The new file attribute.
	 * @param sync If the timestamps should be synchronized.
	 */
	void updateNodeAttr(FSTreeNode node, FileAttrs attr, boolean sync){
		node.attr = attr;
		if (sync) {
			File file = CacheManager.getInstance().getCacheFile(node);
			Assert.isTrue(file.exists());
			file.setLastModified(attr.mtime);
			PersistenceManager.getInstance().setBaseTimestamp(node.getLocationURL(), attr.mtime);
		}
		FSModel.getInstance().fireNodeStateChanged(node);
	}

	/**
	 * Commit the content of the local file to the target.
	 *
	 * @param node The tree node whose local file is going to committed.
	 * @throws TCFException
	 */
	public void commitState(final FSTreeNode node) throws TCFException {
		File file = CacheManager.getInstance().getCacheFile(node);
		Assert.isTrue(file.exists());
		long mtime = file.lastModified();
		// Create the new file attribute based on the file's last modified time.
		IFileSystem.FileAttrs attrs = new IFileSystem.FileAttrs(node.attr.flags, node.attr.size, node.attr.uid, node.attr.gid, node.attr.permissions, node.attr.atime, mtime,
				node.attr.attributes);
		setFileAttrs(node, attrs);
	}

	/**
	 * Set the file's attributes using the new attributes.
	 * 
	 * @param node The file's node.
	 * @param attrs The new file attributes.
	 * @throws TCFException
	 */
	public void setFileAttrs(final FSTreeNode node, final IFileSystem.FileAttrs attrs) throws TCFException {
	    IChannel channel = null;
		try {
			channel = openChannel(node.peerNode.getPeer());
			if (channel != null) {
				IFileSystem service = channel.getRemoteService(IFileSystem.class);
				if (service != null) {
					final TCFFileSystemException[] errors = new TCFFileSystemException[1];
					final Rendezvous rendezvous = new Rendezvous();
					String path = node.getLocation(true);
					service.setstat(path, attrs, new DoneSetStat() {
						@Override
						public void doneSetStat(IToken token, FileSystemException error) {
							if (error == null) {
								commitNodeAttr(node, attrs);
							} else {
								String message = NLS.bind(Messages.StateManager_CannotSetFileStateMessage, new Object[] { node.name, error });
								errors[0] = new TCFFileSystemException(message, error);
							}
							rendezvous.arrive();
						}
					});
					try {
						rendezvous.waiting(5000L);
					} catch (InterruptedException e) {
						String message = NLS.bind(Messages.StateManager_CannotSetFileStateMessage2, new Object[] { node.name, e });
						errors[0] = new TCFFileSystemException(message, e);
					}
					if (errors[0] != null) {
						throw errors[0];
					}
				} else {
					String message = NLS.bind(Messages.StateManager_TCFNotProvideFSMessage2, node.peerNode.getPeer().getID());
					throw new TCFFileSystemException(message);
				}
			}
		}  finally {
			if (channel != null)
				channel.close();
		}
	}

	/**
	 * Open a channel connected to the target represented by the peer.
	 *
	 * @return The channel or null if the operation fails.
	 */
	private IChannel openChannel(final IPeer peer) throws TCFChannelException {
		final Rendezvous rendezvous = new Rendezvous();
		final TCFChannelException[] errors = new TCFChannelException[1];
		final IChannel[] channels = new IChannel[1];
		Tcf.getChannelManager().openChannel(peer, new DoneOpenChannel(){
			@Override
            public void doneOpenChannel(Throwable error, IChannel channel) {
				if(error!=null){
					String message = NLS.bind(Messages.TCFUtilities_OpeningFailureMessage,
							new Object[]{peer.getID(), error.getLocalizedMessage()});
					errors[0] = new TCFChannelException(message, error);
				}else{
					channels[0] = channel;
				}
				rendezvous.arrive();
            }});
		try {
			rendezvous.waiting(5000L);
		} catch (InterruptedException e) {
			String message = NLS.bind(Messages.TCFUtilities_OpeningFailureMessage,
					new Object[]{peer.getID(), e.getLocalizedMessage()});
			errors[0] = new TCFChannelException(message, e);
		}
		if(errors[0] != null){
			throw errors[0];
		}
		return channels[0];
	}

	/**
	 * Commit the file attribute of the specified tree node with the specified value.
	 *
	 * @param node The tree node whose file attribute is to committed.
	 * @param attr The new file attribute.
	 */
	void commitNodeAttr(FSTreeNode node, FileAttrs attr){
		node.attr = attr;
		PersistenceManager.getInstance().setBaseTimestamp(node.getLocationURL(), attr.mtime);
		FSModel.getInstance().fireNodeStateChanged(node);
	}

	/**
	 * Get the local file's state of the specified tree node. The local file must exist
	 * before calling this method to get its state.
	 *
	 * @param node The tree node whose local file state is going to retrieved.
	 * @return The tree node's latest cache state.
	 */
	public CacheState getCacheState(FSTreeNode node) {
		File file = CacheManager.getInstance().getCacheFile(node);
		if(!file.exists())
			return CacheState.consistent;
		long ltime = file.lastModified();
		long btime = PersistenceManager.getInstance().getBaseTimestamp(node.getLocationURL());
		long mtime = 0;
		if(node.attr!=null)
			mtime = node.attr.mtime;
		if(btime == ltime){
			if(isUnchanged(mtime, btime))
				return CacheState.consistent;
			return CacheState.outdated;
		}
		if(isUnchanged(mtime, btime))
			return CacheState.modified;
		return CacheState.conflict;
	}

	/**
	 * Compare the modified time of the remote file and the base timestamp
	 * and see if they are equal to each other.
	 *
	 * @param mtime The modified time of the remote file.
	 * @param btime The base timestamp cached.
	 * @return true if they are equal in second precision.
	 */
	private boolean isUnchanged(long mtime, long btime){
		return Math.abs(mtime-btime)/1000 == 0;
	}
}
