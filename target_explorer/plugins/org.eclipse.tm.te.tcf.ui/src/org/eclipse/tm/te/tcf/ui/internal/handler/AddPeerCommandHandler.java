/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.ui.internal.handler;

import java.util.Map;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.tcf.core.Tcf;
import org.eclipse.tm.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.ILocatorModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.services.ILocatorModelLookupService;
import org.eclipse.tm.te.tcf.ui.internal.dialogs.AddPeerDialog;
import org.eclipse.tm.te.tcf.ui.internal.model.Model;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;


/**
 * Target Explorer: Adds a new peer to the TCF locator model.
 */
public class AddPeerCommandHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// Get the shell
		Shell shell = HandlerUtil.getActiveShell(event);
		// Open the dialog
		AddPeerDialog dialog = new AddPeerDialog(shell);
		if (dialog.open() == Window.OK) {
			// Get the new peer attributes
			final Map<String, String> peerAttributes = dialog.getPeerAttributes();
			if (peerAttributes != null) {
				// Try to connect to the peer
				IChannel channel = null;
				try {
					IWorkbenchPart activePart = HandlerUtil.getActivePart(event);
					IEditorPart editorPart = activePart != null ? (IEditorPart)activePart.getAdapter(IEditorPart.class) : null;
					final ISelectionProvider selectionProvider = editorPart != null ? editorPart.getEditorSite().getSelectionProvider() : null;

					Tcf.getChannelManager().openChannel(peerAttributes, new IChannelManager.DoneOpenChannel() {
						public void doneOpenChannel(Throwable error, IChannel channel) {
							if (error == null) {
								IPeer peer = channel.getRemotePeer();
								if (selectionProvider != null && peer != null) {
									// OK, we have the peer instance now, for the rest,
									// we need the locator model
									ILocatorModel model = Model.getModel();
									if (model != null) {
										final IPeerModel peerNode = model.getService(ILocatorModelLookupService.class).lkupPeerModelById(peer.getID());
										if (peerNode != null) {
											PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
												public void run() {
													selectionProvider.setSelection(new StructuredSelection(peerNode));
												}
											});
										}
									}
								}
							}

							channel.close();
						}
					});
				} catch (Exception e) {
				} finally {
					// Close the channel again
					if (channel != null) {
						final IChannel finChannel = channel;
						if (Protocol.isDispatchThread()) {
							finChannel.close();
						} else {
							Protocol.invokeAndWait(new Runnable() {
								public void run() {
									finChannel.close();
								}
							});
						}
					}
				}
			}
		}

		return null;
	}

}
