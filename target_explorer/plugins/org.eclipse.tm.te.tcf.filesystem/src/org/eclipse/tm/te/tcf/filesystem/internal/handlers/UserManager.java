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

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.DoneUser;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.te.tcf.filesystem.internal.UserAccount;
import org.eclipse.tm.te.tcf.filesystem.internal.exceptions.TCFException;
import org.eclipse.tm.te.tcf.filesystem.internal.exceptions.TCFFileSystemException;
import org.eclipse.tm.te.tcf.filesystem.internal.nls.Messages;
import org.eclipse.tm.te.tcf.filesystem.internal.url.Rendezvous;
import org.eclipse.tm.te.tcf.filesystem.internal.url.TCFUtilities;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.ui.PlatformUI;

/**
 * A facility class to retrieve the user's information for a target file system.
 */
public class UserManager {
	// The key to save and retrieve the user account in a peer model.
	private static final String USER_ACCOUNT_KEY = "user.account"; //$NON-NLS-1$

	// The singleton fInstance.
	private static UserManager instance;

	/**
	 * Get the singleton user manager.
	 *
	 * @return The singleton cache manager.
	 */
	public static UserManager getInstance() {
		if (instance == null) {
			instance = new UserManager();
		}
		return instance;
	}

	private UserManager() {
	}

	/**
	 * Get the user account from the peer using the channel connected to the
	 * remote target.
	 *
	 * @param channel
	 *            The channel connected to the remote target.
	 * @return The user account information or null if it fails.
	 */
	private UserAccount getUserByChannel(final IChannel channel) throws TCFFileSystemException {
		IFileSystem service = channel.getRemoteService(IFileSystem.class);
		if (service != null) {
			final TCFFileSystemException[] errors = new TCFFileSystemException[1];
			final Rendezvous rendezvous = new Rendezvous();
			final UserAccount[] accounts = new UserAccount[1];
			service.user(new DoneUser() {
				@Override
				public void doneUser(IToken token, FileSystemException error, int real_uid, int effective_uid, int real_gid, int effective_gid, String home) {
					if (error == null) {
						accounts[0] = new UserAccount(real_uid, real_gid, effective_uid, effective_gid, home);
					}else {
						String message = NLS.bind(Messages.UserManager_CannotGetUserAccountMessage, channel.getRemotePeer().getID());
						errors[0] = new TCFFileSystemException(message, error);
					}
					rendezvous.arrive();
				}
			});
			try {
				rendezvous.waiting(5000L);
			} catch (InterruptedException e) {
				String message = NLS.bind(Messages.UserManager_CannotGetUserAccountMessage2, channel.getRemotePeer().getID());
				errors[0] = new TCFFileSystemException(message, e);
			}
			if (errors[0] != null) {
				throw errors[0];
			}
			return accounts[0];
		}
		String message = NLS.bind(Messages.UserManager_TCFNotProvideFSMessage, channel.getRemotePeer().getID());
		throw new TCFFileSystemException(message);
	}

	/**
	 * Get the information of the client user account.
	 *
	 * @return The client user account's information.
	 */
	public UserAccount getUserAccount(IPeerModel peerNode) {
		UserAccount account = getUserFromPeer(peerNode);
		if (account == null) {
			IChannel channel = null;
			try{
			channel = TCFUtilities.openChannel(peerNode.getPeer());
			if (channel != null) {
				account = getUserByChannel(channel);
				if (account != null)
					setUserToPeer(peerNode, account);
			}
			}catch(TCFException e){
				Shell parent = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell();
				MessageDialog.openError(parent, Messages.UserManager_UserAccountTitle, e.getLocalizedMessage());
			}finally{
				if(channel!=null){
					channel.close();
				}
			}
		}
		return account;
	}

	/**
	 * Get the user account stored in the specified peer model using a key named
	 * "user.account" defined by the constant USER_ACCOUNT_KEY.
	 *
	 * @param peer
	 *            The peer model from which the user account is retrieved.
	 * @return The user account if it exists or null if not.
	 */
	private UserAccount getUserFromPeer(final IPeerModel peer) {
		if (Protocol.isDispatchThread()) {
			return (UserAccount) peer.getProperty(USER_ACCOUNT_KEY);
		}
		final UserAccount[] accounts = new UserAccount[1];
		Protocol.invokeAndWait(new Runnable() {
			@Override
			public void run() {
				accounts[0] = (UserAccount) peer.getProperty(USER_ACCOUNT_KEY);
			}
		});
		return accounts[0];
	}

	/**
	 * Save the user account to the specified peer model using a key named
	 * "user.account" defined by the constant USER_ACCOUNT_KEY.
	 *
	 * @param peer
	 *            The peer model to which the user account is saved.
	 */
	private void setUserToPeer(final IPeerModel peer, final UserAccount account) {
		assert peer != null && account != null;
		if (Protocol.isDispatchThread()) {
			peer.setProperty(USER_ACCOUNT_KEY, account);
		} else {
			Protocol.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					peer.setProperty(USER_ACCOUNT_KEY, account);
				}
			});
		}
	}
}
