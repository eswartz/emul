/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others. All rights reserved.
 * This program and the accompanying materials are made available under the terms
 * of the Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Uwe Stieber (Wind River) - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.te.tcf.filesystem.controls;

import java.util.Iterator;

import org.eclipse.core.runtime.Assert;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.tcf.services.IFileSystem.IFileHandle;
import org.eclipse.tm.te.tcf.core.Tcf;
import org.eclipse.tm.te.tcf.core.interfaces.IChannelManager;
import org.eclipse.tm.te.tcf.filesystem.model.FSModel;
import org.eclipse.tm.te.tcf.filesystem.model.FSTreeNode;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tm.te.ui.nls.Messages;
import org.eclipse.ui.PlatformUI;


/**
 * Target Explorer: File system tree content provider implementation.
 */
public class FSTreeContentProvider implements ITreeContentProvider {
	/**
	 * Static reference to the return value representing no elements.
	 */
	protected final static Object[] NO_ELEMENTS = new Object[0];

	/**
	 * The file system model instance associated with this file system
	 * tree content provider instance. Each content provider has it's own
	 * file system mode instance.
	 */
	/* default*/ final FSModel model = new FSModel();

	/* default */ Viewer viewer = null;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		this.viewer = viewer;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		model.dispose();
	}

	/**
	 * Close the open communication channel.
	 */
	protected void closeOpenChannel(final IChannel channel) {
		if (channel != null) {
			if (Protocol.isDispatchThread()) {
				channel.close();
			} else {
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						channel.close();
					}
				});
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getElements(java.lang.Object)
	 */
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getParent(java.lang.Object)
	 */
	public Object getParent(Object element) {
		if (element instanceof FSTreeNode) {
			FSTreeNode parent = ((FSTreeNode)element).parent;
			// If the parent is a root node, return the associated peer node
			if (parent != null && parent.type != null && parent.type.endsWith("RootNode")) { //$NON-NLS-1$
				return parent.peerNode;
			}
			return parent;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		Assert.isNotNull(parentElement);

		Object[] children = NO_ELEMENTS;

		// For the file system, we need the peer node
		if (parentElement instanceof IPeerModel) {
			final IPeerModel peerNode = (IPeerModel)parentElement;
			final String peerId = peerNode.getPeer().getID();

			// Get the file system model root node, if already stored
			final FSTreeNode[] root = new FSTreeNode[1];
			if (Protocol.isDispatchThread()) {
				root[0] = model.getRoot(peerId);
			} else {
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						root[0] = model.getRoot(peerId);
					}
				});
			}

			// If the file system model root node hasn't been created, create
			// and initialize the root node now.
			if (root[0] == null) {
				IPeer peer = peerNode.getPeer();
				final int[] state = new int[1];
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						state[0] = peerNode.getIntProperty(IPeerModelProperties.PROP_STATE);
					}
				});
				if (peer != null && IPeerModelProperties.STATE_ERROR != state[0] && IPeerModelProperties.STATE_NOT_REACHABLE != state[0]) {
					FSTreeNode rootNode = new FSTreeNode();
					rootNode.type = "FSRootNode"; //$NON-NLS-1$
					rootNode.peerNode = peerNode;
					rootNode.childrenQueried = false;
					rootNode.childrenQueryRunning = true;
					model.putRoot(peerId, rootNode);

					// Add a special "Pending..." node
					FSTreeNode pendingNode = new FSTreeNode();
					pendingNode.name = Messages.PendingOperation_label;
					pendingNode.type ="FSPendingNode"; //$NON-NLS-1$
					pendingNode.parent = rootNode;
					pendingNode.peerNode = rootNode.peerNode;
					rootNode.getChildren().add(pendingNode);

					children = rootNode.getChildren().toArray();

					Tcf.getChannelManager().openChannel(peer, new IChannelManager.DoneOpenChannel() {
						public void doneOpenChannel(final Throwable error, final IChannel channel) {
							Assert.isTrue(Protocol.isDispatchThread());

							if (channel != null) {
								final IFileSystem service = channel.getRemoteService(IFileSystem.class);
								if (service != null) {

									Protocol.invokeLater(new Runnable() {
										public void run() {
											service.roots(new IFileSystem.DoneRoots() {
												public void doneRoots(IToken token, FileSystemException error, DirEntry[] entries) {
													// Close the channel, not needed anymore
													closeOpenChannel(channel);

													FSTreeNode rootNode = model.getRoot(peerId);
													if (rootNode != null && error == null) {

														for (DirEntry entry : entries) {
															FSTreeNode node = createNodeFromDirEntry(entry, true);
															if (node != null) {
																node.parent = rootNode;
																node.peerNode = rootNode.peerNode;
																rootNode.getChildren().add(node);
															}
														}

														// Find the pending node and remove it from the child list
														Iterator<FSTreeNode> iterator = rootNode.getChildren().iterator();
														while (iterator.hasNext()) {
															FSTreeNode candidate = iterator.next();
															if (Messages.PendingOperation_label.equals(candidate.name)) {
																iterator.remove();
																break;
															}
														}

														// Reset the children query markers
														rootNode.childrenQueryRunning = false;
														rootNode.childrenQueried = true;
													}

													PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
														public void run() {
															if (viewer != null) viewer.refresh();
														}
													});
												}
											});
										}
									});

									PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
										public void run() {
											if (viewer != null) viewer.refresh();
										}
									});
								} else {
									// The file system service is not available for this peer.
									// --> Close the just opened channel
									closeOpenChannel(channel);
								}
							}
						}
					});
				}
			} else {
				children = root[0].getChildren().toArray();
			}
		} else if (parentElement instanceof FSTreeNode) {
			final FSTreeNode node = (FSTreeNode)parentElement;
			// Get possible children
			children = node.getChildren().toArray();
			// No children -> check for "childrenQueried" property. If false, trigger the query.
			if (children.length == 0 && !node.childrenQueried && node.type.endsWith("DirNode")) { //$NON-NLS-1$
				// Add a special "Pending..." node
				FSTreeNode pendingNode = new FSTreeNode();
				pendingNode.name = Messages.PendingOperation_label;
				pendingNode.type ="FSPendingNode"; //$NON-NLS-1$
				pendingNode.parent = node;
				pendingNode.peerNode = node.peerNode;
				node.getChildren().add(pendingNode);

				children = node.getChildren().toArray();

				if (!node.childrenQueryRunning && node.peerNode != null) {
					node.childrenQueryRunning = true;
					final String absName = getEntryAbsoluteName(node);

					if (absName != null) {
						// Open a channel to the peer and query the children
						Tcf.getChannelManager().openChannel(node.peerNode.getPeer(), new IChannelManager.DoneOpenChannel() {
							public void doneOpenChannel(final Throwable error, final IChannel channel) {
								Assert.isTrue(Protocol.isDispatchThread());

								if (channel != null && channel.getState() == IChannel.STATE_OPEN) {
									final IFileSystem service = channel.getRemoteService(IFileSystem.class);
									if (service != null) {

										Protocol.invokeLater(new Runnable() {
											public void run() {
												service.opendir(absName, new IFileSystem.DoneOpen() {
													public void doneOpen(IToken token, FileSystemException error, final IFileHandle handle) {
														if (error == null) {
															// Read the directory content until finished
															readdir(channel, service, handle, node);
														} else {
															// In case of an error, we are done here
															node.childrenQueryRunning = false;
															node.childrenQueried = true;
														}
													}
												});
											}
										});
									} else {
										// No file system service available
										node.childrenQueryRunning = false;
										node.childrenQueried = true;
									}
								} else {
									// Channel failed to open
									node.childrenQueryRunning = false;
									node.childrenQueried = true;
								}
							}
						});
					} else {
						// No absolute name
						node.childrenQueryRunning = false;
						node.childrenQueried = true;
					}
				}
			}
		}

		return children;
	}

	/**
	 * Reads the content of a directory until the file system service signals EOF.
	 *
	 * @param channel The open channel. Must not be <code>null</code>.
	 * @param service The file system service. Must not be <code>null</code>.
	 * @param handle The directory handle. Must not be <code>null</code>.
	 * @param parentNode The parent node receiving the entries. Must not be <code>null</code>.
	 * @param mode The notification mode to set to the parent node once done.
	 */
	protected void readdir(final IChannel channel, final IFileSystem service, final IFileHandle handle, final FSTreeNode parentNode) {
		Assert.isNotNull(channel);
		Assert.isNotNull(service);
		Assert.isNotNull(handle);
		Assert.isNotNull(parentNode);

		Protocol.invokeLater(new Runnable() {
			public void run() {
				service.readdir(handle, new IFileSystem.DoneReadDir() {

					public void doneReadDir(IToken token, FileSystemException error, DirEntry[] entries, boolean eof) {
						// Close the handle and channel if EOF is signaled or an error occurred.
						if (eof) {
							service.close(handle, new IFileSystem.DoneClose() {
								public void doneClose(IToken token, FileSystemException error) {
									closeOpenChannel(channel);
								}
							});
						}

						// Process the returned data
						if (error == null && entries != null && entries.length > 0) {
							for (DirEntry entry : entries) {
								FSTreeNode node = createNodeFromDirEntry(entry, false);
								if (node != null) {
									node.parent = parentNode;
									node.peerNode = parentNode.peerNode;
									parentNode.getChildren().add(node);
								}
							}
						}

						if (eof) {
							// Find the pending node and remove it from the child list
							Iterator<FSTreeNode> iterator = parentNode.getChildren().iterator();
							while (iterator.hasNext()) {
								FSTreeNode candidate = iterator.next();
								if (Messages.PendingOperation_label.equals(candidate.name)) {
									iterator.remove();
									break;
								}
							}

							// Reset the children query markers
							parentNode.childrenQueryRunning = false;
							parentNode.childrenQueried = true;
						} else {
							// And invoke ourself again
							readdir(channel, service, handle, parentNode);
						}

						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								if (viewer instanceof StructuredViewer) ((StructuredViewer)viewer).refresh(parentNode);
							}
						});
					}
				});
			}
		});
	}


	/**
	 * Creates a tree node from the given directory entry.
	 *
	 * @param entry The directory entry. Must not be <code>null</code>.
	 *
	 * @return The tree node.
	 */
	protected FSTreeNode createNodeFromDirEntry(DirEntry entry, boolean entryIsRootNode) {
		Assert.isNotNull(entry);

		FSTreeNode node = null;

		IFileSystem.FileAttrs attrs = entry.attrs;

		if (attrs == null || attrs != null && attrs.isDirectory()) {
			node = new FSTreeNode();
			node.childrenQueried = false;
			node.childrenQueryRunning = false;
			node.attr = attrs;
			node.name = entry.filename;
			node.type = entryIsRootNode ? "FSRootDirNode" : "FSDirNode"; //$NON-NLS-1$ //$NON-NLS-2$
		} else if (attrs.isFile()) {
			node = new FSTreeNode();
			node.childrenQueried = false;
			node.childrenQueryRunning = false;
			node.attr = attrs;
			node.name = entry.filename;
			node.type = "FSFileNode"; //$NON-NLS-1$
		}

		return node;
	}

	/**
	 * Returns the absolute name for the given node.
	 *
	 * @param node The node. Must not be <code>null</code>.
	 * @return The absolute name.
	 */
	public static String getEntryAbsoluteName(FSTreeNode node) {
		Assert.isNotNull(node);

		StringBuilder path = new StringBuilder();

		// We have to walk upwards the hierarchy until the root node is found
		FSTreeNode parent = node.parent;
		while (parent != null && parent.type != null && parent.type.startsWith("FS")) { //$NON-NLS-1$
			if ("FSRootNode".equals(parent.type)) { //$NON-NLS-1$
				// We are done if reaching the root node
				break;
			}

			if (path.length() == 0) path.append(parent.name.replaceAll("\\\\", "/")); //$NON-NLS-1$ //$NON-NLS-2$
			else {
				String name = parent.name.replaceAll("\\\\", "/"); //$NON-NLS-1$ //$NON-NLS-2$
				if (!name.endsWith("/")) name = name + "/"; //$NON-NLS-1$ //$NON-NLS-2$
				path.insert(0, name);
			}

			parent = parent.parent;
		}

		if (path.length() > 0 && path.charAt(path.length() - 1) != '/') {
			path.append("/"); //$NON-NLS-1$
		}
		path.append(node.name);

		return path.toString();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#hasChildren(java.lang.Object)
	 */
	public boolean hasChildren(final Object element) {
		Assert.isNotNull(element);

		boolean hasChildren = false;

		if (element instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode)element;
			if (node.type != null && (node.type.endsWith("DirNode") || node.type.endsWith("RootNode"))) { //$NON-NLS-1$ //$NON-NLS-2$
				if (!node.childrenQueried || node.childrenQueryRunning) {
					hasChildren = true;
				} else if (node.childrenQueried) {
					hasChildren = node.getChildren().size() > 0;
				}
			}
		}
		else if (element instanceof IPeerModel) {
			final String[] peerId = new String[1];
			if (Protocol.isDispatchThread()) {
				peerId[0] = ((IPeerModel)element).getPeer().getID();
			} else {
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						peerId[0] = ((IPeerModel)element).getPeer().getID();
					}
				});
			}

			// Get the root node for this peer model object.
			// If null, true is returned as it means that the file system
			// model hasn't been created yet and have to treat is as children
			// not queried yet.
			FSTreeNode root = peerId[0] != null ? model.getRoot(peerId[0]): null;
			hasChildren = root != null ? hasChildren(root) : true;
		}

		return hasChildren;
	}
}
