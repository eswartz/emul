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
	private ITarget target;
	private FileSystemNode[] roots;
	private boolean showRootNode;
	private boolean fetching;

	final static String rootNode = "File System";
	
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
		if (newInput instanceof ITarget) {
			target = (ITarget)newInput;
			roots = null;
		}
		
		if (viewer instanceof TreeViewer)
			this.viewer = (TreeViewer)viewer;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		return getChildren(inputElement);
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement == target) {
			if (showRootNode)
				return new Object[] { rootNode };
			else
				return getRoots(parentElement);
		} else if (parentElement == rootNode) {
			return getRoots(parentElement);
		} else if (parentElement instanceof FileSystemNode) {
			return ((FileSystemNode)parentElement).getChildren(viewer, target);
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

	private Object[] getRoots(final Object parentElement) {
		if (roots != null)
			return roots;
		
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
									roots = new FileSystemNode[entries.length];
									for (int i = 0; i < entries.length; ++i)
										roots[i] = new FileSystemNode(entries[i]);
									fetching = false;
									refresh(viewer, parentElement);
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
		
		return Activator.PENDING_NODES;
	}
	
	@Override
	public Object getParent(Object element) {
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ITarget)
			return true;
		if (element instanceof FileSystemNode)
			return true;
		return false;
	}

}
