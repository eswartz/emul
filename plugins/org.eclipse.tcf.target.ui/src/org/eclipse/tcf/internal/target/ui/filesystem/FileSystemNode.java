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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.internal.target.ui.Activator;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;

public class FileSystemNode {

	private static Map<ITarget, FileSystemNode> nodes = new HashMap<ITarget, FileSystemNode>();
	
	private final ITarget target;
	private IFileSystem.DirEntry[] roots;
	private boolean fetching = false;
	
	public static FileSystemNode createFor(ITarget target) {
		synchronized (nodes) {
			FileSystemNode node = nodes.get(target);
			if (node == null) {
				node = new FileSystemNode(target);
				nodes.put(target, node);
			}
			return node;
		}
	}
	
	public FileSystemNode(ITarget target) {
		this.target = target;
	}

	public ITarget getTarget() {
		return target;
	}

	private synchronized void setRoots(DirEntry[] roots) {
		this.roots = roots;
		fetching = false;
	}
	
	private void refresh(final TreeViewer viewer, final Object parentElement) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh(parentElement);
			}
		});
	}
	
	public synchronized Object[] getRoots(final TreeViewer viewer, final Object parentElement) {
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
								public void doneRoots(IToken token, FileSystemException error, DirEntry[] entries) {
									setRoots(entries);
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
}
