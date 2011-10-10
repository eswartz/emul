/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.processes.ui.internal.handler;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.runtime.Assert;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.te.runtime.interfaces.properties.IPropertiesContainer;
import org.eclipse.tm.te.runtime.properties.PropertiesContainer;
import org.eclipse.tm.te.runtime.statushandler.StatusHandlerManager;
import org.eclipse.tm.te.runtime.statushandler.interfaces.IStatusHandler;
import org.eclipse.tm.te.runtime.statushandler.interfaces.IStatusHandlerConstants;
import org.eclipse.tm.te.tcf.core.Tcf;
import org.eclipse.tm.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.processes.ui.activator.UIPlugin;
import org.eclipse.tm.te.tcf.processes.ui.internal.help.IContextHelpIds;
import org.eclipse.tm.te.tcf.processes.ui.nls.Messages;
import org.eclipse.ui.handlers.HandlerUtil;

/**
 * Abstract backend channel command handler.
 */
public abstract class AbstractChannelCommandHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
    public Object execute(final ExecutionEvent event) throws ExecutionException {
		// Get the active menu selection
		ISelection activeSelection = HandlerUtil.getActiveMenuSelection(event);
		// The selection is expected to be a structured exception
		if (activeSelection instanceof IStructuredSelection && !activeSelection.isEmpty()) {
			IStructuredSelection selection = (IStructuredSelection)activeSelection;
			// Loop the selection
			Iterator<?> iterator = selection.iterator();
			while (iterator.hasNext()) {
				Object element = iterator.next();
				// The selected element is expected to be a peer node
				if (element instanceof IPeerModel) {
					final IPeerModel node = (IPeerModel)element;
					IPeer peer = node.getPeer();
					// If the peer is available, we can open a channel to the remote peer
					if (peer != null) {
						// Get the channel
						Tcf.getChannelManager().openChannel(peer, new IChannelManager.DoneOpenChannel() {
							@Override
                            public void doneOpenChannel(final Throwable error, final IChannel channel) {
								if (error == null) {
									execute(event, channel, node, new DoneExecute() {
										@Override
                                        public void doneExecute(IStatus status, Object result) {
											if (status.getSeverity() != IStatus.OK && status.getSeverity() != IStatus.CANCEL) {
												handleException(channel, new CoreException(status));
											} else {
												// Close the channel
												if (channel != null) {
													final IChannel finChannel = channel;
													if (Protocol.isDispatchThread()) {
														finChannel.close();
													} else {
														Protocol.invokeAndWait(new Runnable() {
															@Override
                                                            public void run() {
																finChannel.close();
															}
														});
													}
												}
											}
										}
									});

								} else {
									handleException(channel, error);
								}
							}
						});
					}
				}
			}
		}

		return null;
	}

	/**
	 * Executes the command handler logic.
	 *
	 * @param event The execution event. Must be not <code>null</code>.
	 * @param channel The channel. Must be not <code>null</code>.
	 * @param node The selected node. Must be not <code>null</code>.
	 * @param callback The callback to invoke if the execution finished. Must be not <code>null</code>.
	 */
	protected abstract void execute(ExecutionEvent event, IChannel channel, IPeerModel node, DoneExecute callback);

    /**
     * Client call back interface for execute(...).
     */
	public static interface DoneExecute {
        /**
         * Called when execute(...) got finished.
         *
         * @param status The execution status. Must not be <code>null</code>.
         * @param result The execution result or <code>null</code>.
         */
		public void doneExecute(IStatus status, Object result);
	}

	/**
	 * Closes the given channel and handle the given exception.
	 *
	 * @param channel The channel instance or <code>null</code>.
	 * @param exception The exception to handle. Must be not <code>null</code>.
	 */
	protected void handleException(IChannel channel, Throwable exception) {
		Assert.isNotNull(exception);

		// Close the backend channel
		if (channel != null) {
			final IChannel finChannel = channel;
			if (Protocol.isDispatchThread()) {
				finChannel.close();
			} else {
				Protocol.invokeAndWait(new Runnable() {
					@Override
                    public void run() {
						finChannel.close();
					}
				});
			}
		}

		// Get the status handler
		IStatusHandler[] handler = StatusHandlerManager.getInstance().getHandler(getClass());
		if (handler != null && handler.length > 0) {
			// If the exception is a core exception, we can pass on the status object to the handler
			IStatus status = null;
			if (exception instanceof CoreException) ((CoreException)exception).getStatus();
			else {
				// Construct the status from the exception
				status = new Status(IStatus.ERROR, UIPlugin.getUniqueIdentifier(), 0, exception.getLocalizedMessage(), exception);
			}

			// Handle the status (Take the first status handler in the list)
			if (status != null) {
				IPropertiesContainer data = new PropertiesContainer();
				data.setProperty(IStatusHandlerConstants.PROPERTY_TITLE, getStatusDialogTitle());
				data.setProperty(IStatusHandlerConstants.PROPERTY_CONTEXT_HELP_ID, getStatusDialogContextHelpId());
				data.setProperty(IStatusHandlerConstants.PROPERTY_DONT_ASK_AGAIN_ID, getStatusDialogDontAskAgainKey());
				data.setProperty(IStatusHandlerConstants.PROPERTY_CALLER, this);

				handler[0].handleStatus(status, data, null);
			}
		}
	}

	/**
	 * Returns the title to be used for the status dialog in case of
	 * an execution failure.
	 *
	 * @return The status dialog title.
	 */
	protected String getStatusDialogTitle() {
		return Messages.AbstractChannelCommandHandler_statusDialog_title;
	}

	/**
	 * Returns the context help id to be used for the status dialog in case
	 * of an execution failure.
	 *
	 * @return The context help id or <code>null</code>.
	 */
	protected String getStatusDialogContextHelpId() {
		return IContextHelpIds.CHANNEL_COMMAND_HANDLER_STATUS_DIALOG;
	}

	/**
	 * Returns the don't ask again key to be used for the status dialog in
	 * case of an execution failure.
	 *
	 * @return The don't ask again key or <code>null</code>.
	 */
	protected String getStatusDialogDontAskAgainKey() {
		return null;
	}
}
