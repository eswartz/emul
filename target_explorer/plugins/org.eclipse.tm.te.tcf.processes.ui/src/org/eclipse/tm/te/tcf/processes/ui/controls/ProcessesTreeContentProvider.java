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

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.ISysMonitor;
import org.eclipse.tm.te.tcf.core.Tcf;
import org.eclipse.tm.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tm.te.ui.nls.Messages;
import org.eclipse.ui.PlatformUI;

/**
 * Processes tree control content provider implementation.
 */
public class ProcessesTreeContentProvider implements ITreeContentProvider {
	/**
	 * Static reference to the return value representing no elements.
	 */
	protected final static Object[] NO_ELEMENTS = new Object[0];

	/* default */ IPeerModel peerNode = null;
	private ProcessesTreeNode rootNode = null;

	private IChannel channel = null;
	private ISysMonitor service = null;

	/* default */ Viewer viewer = null;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	@Override
    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
		if (oldInput != null && newInput == null) {
			closeOpenChannel();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	@Override
    public void dispose() {
		closeOpenChannel();
	}

	/**
	 * Close the open communication channel and set back the node references.
	 */
	protected void closeOpenChannel() {
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
			channel = null;
			service = null;
		}

		peerNode = null;
		rootNode = null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	@Override
    public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	@Override
    public Object getParent(Object element) {
		if (element instanceof ProcessesTreeNode) {
			return ((ProcessesTreeNode)element).parent;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	@Override
    public Object[] getChildren(Object parentElement) {
		Assert.isNotNull(parentElement);

		Object[] children = NO_ELEMENTS;

		// For the file system, we need the peer node
		if (parentElement instanceof IPeerModel) {
			// Is it the same peer node we have seen before?
			if (peerNode == null || peerNode != null && !peerNode.equals(parentElement)) {
				// Remember the peer node
				peerNode = (IPeerModel)parentElement;

				// If we still have a channel open, for now, we just close the old channel
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
					channel = null;
				}

				IPeer peer = peerNode.getPeer();
				final int[] state = new int[1];
				Protocol.invokeAndWait(new Runnable() {
					@Override
                    public void run() {
						state[0] = peerNode.getIntProperty(IPeerModelProperties.PROP_STATE);
					}
				});
				if (peer != null && IPeerModelProperties.STATE_ERROR != state[0] && IPeerModelProperties.STATE_NOT_REACHABLE != state[0]) {
					ProcessesTreeNode pendingNode = new ProcessesTreeNode();
					pendingNode.name = Messages.PendingOperation_label;
					pendingNode.type ="ProcPendingNode"; //$NON-NLS-1$

					children = new Object[] { pendingNode };

					Tcf.getChannelManager().openChannel(peer, new IChannelManager.DoneOpenChannel() {
						@Override
                        @SuppressWarnings("synthetic-access")
						public void doneOpenChannel(Throwable error, IChannel channel) {
							Assert.isTrue(Protocol.isDispatchThread());

							if (channel != null) {
								ProcessesTreeContentProvider.this.channel = channel;

								service = channel.getRemoteService(ISysMonitor.class);
								if (service != null) {
									rootNode = new ProcessesTreeNode();
									rootNode.type = "ProcRootNode"; //$NON-NLS-1$
									rootNode.childrenQueried = false;
									rootNode.childrenQueryRunning = true;

									Protocol.invokeLater(new Runnable() {
										@Override
                                        public void run() {
											service.getChildren(null, new ISysMonitor.DoneGetChildren() {
												/* (non-Javadoc)
												 * @see org.eclipse.tm.tcf.services.ISysMonitor.DoneGetChildren#doneGetChildren(org.eclipse.tm.tcf.protocol.IToken, java.lang.Exception, java.lang.String[])
												 */
												@Override
                                                public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
													if (rootNode != null) {
														if (error == null && context_ids != null && context_ids.length > 0) {
															for (String contextId : context_ids) {
																service.getContext(contextId, new ISysMonitor.DoneGetContext() {
																	/* (non-Javadoc)
																	 * @see org.eclipse.tm.tcf.services.ISysMonitor.DoneGetContext#doneGetContext(org.eclipse.tm.tcf.protocol.IToken, java.lang.Exception, org.eclipse.tm.tcf.services.ISysMonitor.SysMonitorContext)
																	 */
																	@Override
                                                                    public void doneGetContext(IToken token, Exception error, ISysMonitor.SysMonitorContext context) {
																		if (error == null &&  context != null) {
																			ProcessesTreeNode node = createNodeFromSysMonitorContext(context);
																			if (node != null) {
																				node.parent = rootNode;
																				rootNode.children.add(node);
																			}
																		}
																	}
																});
															}
														}

														Protocol.invokeLater(new Runnable() {
															@Override
                                                            public void run() {
																// Reset the children query marker
																rootNode.childrenQueryRunning = false;
																rootNode.childrenQueried = true;

																PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
																	@Override
                                                                    public void run() {
																		if (viewer != null) viewer.refresh();
																	}
																});
															}
														});
													}
												}
											});
										}
									});
								} else {
									// TCF file system service is not available, close the just opened channel
									closeOpenChannel();
								}
							}
						}
					});
				} else {
					dispose();
				}
			} else if (rootNode != null && rootNode.childrenQueried) {
				children = rootNode.children.toArray();
			}
		} else if (parentElement instanceof ProcessesTreeNode) {
			ProcessesTreeNode node = (ProcessesTreeNode)parentElement;
			// Get possible children
			children = node.children.toArray();
			// No children -> check for "childrenQueried" property. If false, trigger the query.
			if (children.length == 0 && !node.childrenQueried) {
				ProcessesTreeNode pendingNode = new ProcessesTreeNode();
				pendingNode.name = Messages.PendingOperation_label;
				pendingNode.type ="ProcPendingNode"; //$NON-NLS-1$

				children = new Object[] { pendingNode };
				doGetChildrenForProcessContext(node);
			}
		}

		return children;
	}

	/**
	 * Query the children of the given process context.
	 *
	 * @param node The process context node. Must be not <code>null</code>.
	 */
	protected void doGetChildrenForProcessContext(ProcessesTreeNode node) {
		Assert.isNotNull(node);

		if (!node.childrenQueryRunning) {
			final ProcessesTreeNode parentNode = node;

			final String parentId = node.id;
			if (parentId != null && service != null) {
				parentNode.childrenQueryRunning = true;

				Protocol.invokeLater(new Runnable() {
					@Override
                    @SuppressWarnings("synthetic-access")
					public void run() {
						service.getChildren(parentId, new ISysMonitor.DoneGetChildren() {
							/* (non-Javadoc)
							 * @see org.eclipse.tm.tcf.services.IProcesses.DoneGetChildren#doneGetChildren(org.eclipse.tm.tcf.protocol.IToken, java.lang.Exception, java.lang.String[])
							 */
							@Override
                            public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
								if (error == null && context_ids != null && context_ids.length > 0) {
									for (String contextId : context_ids) {
										service.getContext(contextId, new ISysMonitor.DoneGetContext() {
											/* (non-Javadoc)
											 * @see org.eclipse.tm.tcf.services.ISysMonitor.DoneGetContext#doneGetContext(org.eclipse.tm.tcf.protocol.IToken, java.lang.Exception, org.eclipse.tm.tcf.services.ISysMonitor.SysMonitorContext)
											 */
											@Override
                                            public void doneGetContext(IToken token, Exception error, ISysMonitor.SysMonitorContext context) {
												if (error == null && context != null) {
													ProcessesTreeNode node = createNodeFromSysMonitorContext(context);
													if (node != null) {
														node.parent = parentNode;
														parentNode.children.add(node);
													}
												}
											}
										});
									}
								}

								Protocol.invokeLater(new Runnable() {
									@Override
                                    public void run() {
										// Reset the children query marker
										parentNode.childrenQueryRunning = false;
										parentNode.childrenQueried = true;

										PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
											@Override
                                            public void run() {
												if (viewer instanceof StructuredViewer) ((StructuredViewer)viewer).refresh(parentNode);
											}
										});
									}
								});
							}
						});
					}
				});
			}
		}

	}

	/**
	 * Creates a node from the given system monitor context.
	 *
	 * @param context The system monitor context. Must be not <code>null</code>.
	 *
	 * @return The node.
	 */
	protected ProcessesTreeNode createNodeFromSysMonitorContext(ISysMonitor.SysMonitorContext context) {
		Assert.isTrue(Protocol.isDispatchThread());
		Assert.isNotNull(context);

		ProcessesTreeNode node = new ProcessesTreeNode();

		node.childrenQueried = false;
		node.childrenQueryRunning = false;
		node.context = context;
		node.name = context.getFile();
		node.type = "ProcNode"; //$NON-NLS-1$
		node.id = context.getID();
		node.pid = context.getPID();
		node.ppid = context.getPPID();
		node.parentId = context.getParentID();
		node.state = context.getState();
		node.username = context.getUserName();

		doGetChildrenForProcessContext(node);

		return node;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	@Override
    public boolean hasChildren(Object element) {
		Assert.isNotNull(element);

		boolean hasChildren = false;

		// No children yet and the element is a process node
		if (element instanceof ProcessesTreeNode) {
			ProcessesTreeNode node = (ProcessesTreeNode)element;
			if (!node.childrenQueried || node.childrenQueryRunning) {
				hasChildren = true;
			} else if (node.childrenQueried) {
				hasChildren = node.children.size() > 0;
			}
		}

		return hasChildren;
	}
}
