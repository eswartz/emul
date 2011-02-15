/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.target.ui.filesystem;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.internal.target.ui.Activator;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;

public class FileSystemContentProvider implements ITreeContentProvider {

	private TreeViewer viewer;
	private boolean showRootNode;
	private boolean fetching;
	
	static final String pending = "pending...";
	
	static class RootNode {
		private static final String propertyName = "fileSystemRoot";
		private final ITarget target;
		private FileSystemNode[] roots;
		
		public RootNode(ITarget target) {
			this.target = target;
		}
	}
	
	public FileSystemContentProvider() {
		this(true);
	}
	
	public FileSystemContentProvider(boolean showRootNode) {
		this.showRootNode = showRootNode;
	}
	
	@Override
	public void dispose() {
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		if (viewer instanceof TreeViewer)
			this.viewer = (TreeViewer)viewer;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ITarget) {
			ITarget target = (ITarget)parentElement;
			if (showRootNode) {
				RootNode root = (RootNode)target.getLocalProperties().get(RootNode.propertyName);
				if (root == null) {
					root = new RootNode(target);
					target.getLocalProperties().put(RootNode.propertyName, root);
				}
				return new Object[] { root };
			} else
				return getRoots((ITarget)parentElement);
		} else if (parentElement instanceof RootNode) {
			return getRoots(((RootNode)parentElement).target);
		} else if (parentElement instanceof FileSystemNode) {
			return ((FileSystemNode)parentElement).getChildren(viewer);
		} else {
			return new Object[0];
		}
	}
	
	private void refresh(final TreeViewer viewer, final Object parentElement) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh(parentElement);
			}
		});
	}

	private Object[] getRoots(final ITarget target) {
		final RootNode root = (RootNode)target.getLocalProperties().get(RootNode.propertyName);
		if (root == null)
			return new Object[0];
		
		if (root.roots != null)
			return root.roots;
		
		if (!fetching) {
			fetching = true;
			Protocol.invokeLater(new Runnable() {
				@Override
				public void run() {
					// Get the roots
					target.handleTargetRequest(new ITarget.ITargetRequest() {
						@Override
						public void execute(IChannel channel) {
							IFileSystem fileSystem = channel.getRemoteService(IFileSystem.class);
							fileSystem.roots(new IFileSystem.DoneRoots() {
								@Override
								public void doneRoots(IToken token, IFileSystem.FileSystemException error, IFileSystem.DirEntry[] entries) {
									root.roots = new FileSystemNode[entries.length];
									for (int i = 0; i < entries.length; ++i)
										root.roots[i] = new FileSystemNode(target, entries[i]);
									fetching = false;
									refresh(viewer, target);
								}
							});
						}
						
						@Override
						public void channelUnavailable(IStatus error) {
							Activator.log(error);
						}
					});
				}
			});
		}
		
		return new Object[] { pending };
	}
	
	@Override
	public Object getParent(Object element) {
		if (element instanceof RootNode)
			return ((RootNode)element).target;
		else if (element instanceof FileSystemNode)
			return ((FileSystemNode)element).getParent();
		else
			return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ITarget)
			return true;
		else if (element instanceof FileSystemNode)
			return ((FileSystemNode)element).hasChildren();
		else if (element instanceof RootNode)
			return true;
		else
			return false;
	}

}
