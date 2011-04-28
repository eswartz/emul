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
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModel;
import org.eclipse.tm.te.tcf.locator.interfaces.nodes.IPeerModelProperties;
import org.eclipse.tm.te.ui.nodes.PendingOperation;
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
	 * Static reference to the return value representing a pending child query.
	 */
	protected final static Object[] PENDING = new Object[] { new PendingOperation() };


	/* default */ IPeerModel fPeerNode = null;
	private FSTreeNode fRootNode = null;

	private IChannel fChannel = null;
	private IFileSystem fService = null;

	/* default */ Viewer fViewer = null;

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
	 */
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		fViewer = viewer;
		if (oldInput != null && newInput == null) {
			closeOpenChannel();
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
	 */
	public void dispose() {
		closeOpenChannel();
	}

	/**
	 * Close the open communication channel and set back the node references.
	 */
	protected void closeOpenChannel() {
		if (fChannel != null) {
			final IChannel finChannel = fChannel;
			if (Protocol.isDispatchThread()) {
				finChannel.close();
			} else {
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						finChannel.close();
					}
				});
			}
			fChannel = null;
			fService = null;
		}

		fPeerNode = null;
		fRootNode = null;
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
			return ((FSTreeNode)element).parent;
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.viewers.ITreeContentProvider#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object parentElement) {
		assert parentElement != null;

		Object[] children = NO_ELEMENTS;

		// For the file system, we need the peer node
		if (parentElement instanceof IPeerModel) {
			// Is it the same peer node we have seen before?
			if (fPeerNode == null || fPeerNode != null && !fPeerNode.equals(parentElement)) {
				// Remember the peer node
				fPeerNode = (IPeerModel)parentElement;

				// If we still have a channel open, for now, we just close the old channel
				if (fChannel != null) {
					final IChannel finChannel = fChannel;
					if (Protocol.isDispatchThread()) {
						finChannel.close();
					} else {
						Protocol.invokeAndWait(new Runnable() {
							public void run() {
								finChannel.close();
							}
						});
					}
					fChannel = null;
				}

				IPeer peer = fPeerNode.getPeer();
				final int[] state = new int[1];
				Protocol.invokeAndWait(new Runnable() {
					public void run() {
						state[0] = fPeerNode.getIntProperty(IPeerModelProperties.PROP_STATE);
					}
				});
				if (peer != null && IPeerModelProperties.STATE_ERROR != state[0] && IPeerModelProperties.STATE_NOT_REACHABLE != state[0]) {
					children = PENDING;

					Tcf.getChannelManager().openChannel(peer, new IChannelManager.DoneOpenChannel() {
						@SuppressWarnings("synthetic-access")
						public void doneOpenChannel(Throwable error, IChannel channel) {
							assert Protocol.isDispatchThread();

							if (channel != null) {
								fChannel = channel;

								fService = fChannel.getRemoteService(IFileSystem.class);
								if (fService != null) {
									fRootNode = new FSTreeNode();
									fRootNode.type = "FSRootNode"; //$NON-NLS-1$
									fRootNode.childrenQueried = false;
									fRootNode.childrenQueryRunning = true;

									Protocol.invokeLater(new Runnable() {
										public void run() {
											fService.roots(new IFileSystem.DoneRoots() {
												public void doneRoots(IToken token, FileSystemException error, DirEntry[] entries) {

													if (fRootNode != null && error == null) {

														for (DirEntry entry : entries) {
															FSTreeNode node = createNodeFromDirEntry(entry, true);
															if (node != null) {
																node.parent = fRootNode;
																fRootNode.children.add(node);
															}
														}

														// Reset the children query markers
														fRootNode.childrenQueryRunning = false;
														fRootNode.childrenQueried = true;
													}

													PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
														public void run() {
															if (fViewer != null) fViewer.refresh();
														}
													});
												}
											});
										}
									});

									PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
										public void run() {
											if (fViewer != null) fViewer.refresh();
										}
									});
								} else {
									// TCF file system service is not available, close the just opened channel
									closeOpenChannel();
								}

							}
						}
					});
				}
			} else if (fRootNode != null && fRootNode.childrenQueried) {
				children = fRootNode.children.toArray();
			}
		} else if (parentElement instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode)parentElement;
			// Get possible children
			children = node.children.toArray();
			// No children -> check for "childrenQueried" property. If false, trigger the query.
			if (children.length == 0 && !node.childrenQueried && node.type.endsWith("DirNode")) { //$NON-NLS-1$
				children = PENDING;

				if (!node.childrenQueryRunning) {
					final FSTreeNode parentNode = node;

					final String absName = getEntryAbsoluteName(node);
					if (absName != null && fService != null) {
						parentNode.childrenQueryRunning = true;

						Protocol.invokeLater(new Runnable() {
							@SuppressWarnings("synthetic-access")
							public void run() {
								fService.opendir(absName, new IFileSystem.DoneOpen() {

									public void doneOpen(IToken token, FileSystemException error, final IFileHandle handle) {

										if (error == null && fService != null) {
											// Read the directory content until finished
											readdir(fService, handle, parentNode);
										} else {
											// In case of an error, we are done here
											parentNode.childrenQueryRunning = false;
											parentNode.childrenQueried = true;
										}
									}
								});
							}
						});
					}
				}

				PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
					public void run() {
						if (fViewer != null) fViewer.refresh();
					}
				});
			}
		}

		return children;
	}

	/**
	 * Reads the content of a directory until the file system service signals EOF.
	 *
	 * @param service The file system service. Must not be <code>null</code>.
	 * @param handle The directory handle. Must not be <code>null</code>.
	 * @param parentNode The parent node receiving the entries. Must not be <code>null</code>.
	 * @param mode The notification mode to set to the parent node once done.
	 */
	protected void readdir(final IFileSystem service, final IFileHandle handle, final FSTreeNode parentNode) {
		assert service != null && handle != null && parentNode != null;

		Protocol.invokeLater(new Runnable() {
			@SuppressWarnings("synthetic-access")
			public void run() {
				fService.readdir(handle, new IFileSystem.DoneReadDir() {

					public void doneReadDir(IToken token, FileSystemException error, DirEntry[] entries, boolean eof) {
						if (fService != null) {
							// Close the handle if EOF is signaled or an error occurred.
							if (eof) {
								fService.close(handle, new IFileSystem.DoneClose() {
									public void doneClose(IToken token, FileSystemException error) {
									}
								});
							}

							// Process the returned data
							if (error == null && entries != null && entries.length > 0) {
								for (DirEntry entry : entries) {
									FSTreeNode node = createNodeFromDirEntry(entry, false);
									if (node != null) {
										node.parent = parentNode;
										parentNode.children.add(node);
									}
								}
							}

							if (eof) {
								// Reset the children query markers
								parentNode.childrenQueryRunning = false;
								parentNode.childrenQueried = true;
							} else {
								// And invoke ourself again
								readdir(service, handle, parentNode);
							}
						}

						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								if (fViewer instanceof StructuredViewer) ((StructuredViewer)fViewer).refresh(parentNode);
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
		assert entry != null;

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
		assert node != null;

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
	public boolean hasChildren(Object element) {
		assert element != null;

		boolean hasChildren = false;

		if (element instanceof FSTreeNode) {
			FSTreeNode node = (FSTreeNode)element;
			if (node.type != null && node.type.endsWith("DirNode")) { //$NON-NLS-1$
				if (!node.childrenQueried || node.childrenQueryRunning) {
					hasChildren = true;
				} else if (node.childrenQueried) {
					hasChildren = node.children.size() > 0;
				}
			}
		}

		return hasChildren;
	}
}
