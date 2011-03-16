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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.internal.target.ui.Activator;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tcf.target.core.TargetRequestSequence;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IFileSystem;
import org.eclipse.tm.tcf.services.IFileSystem.DirEntry;
import org.eclipse.tm.tcf.services.IFileSystem.FileSystemException;
import org.eclipse.tm.tcf.services.IFileSystem.IFileHandle;

public class FileSystemNode {

	private final FileSystemNode parent;
	private final ITarget target;
	private final IFileSystem.DirEntry dirEntry;
	
	private FileSystemNode[] children;
	private boolean fetching;

	public FileSystemNode(ITarget target, IFileSystem.DirEntry dirEntry) {
		this(target, null, dirEntry);
	}
	
	public FileSystemNode(ITarget target, FileSystemNode parent, IFileSystem.DirEntry dirEntry) {
		this.target = target;
		this.parent = parent;
		this.dirEntry = dirEntry;
	}

	public IFileSystem.DirEntry getDirEntry() {
		return dirEntry;
	}
	
	private char buildPath(StringBuffer path, boolean last) {
		String filename = dirEntry.filename;
		char separator;
		if (parent != null)
			separator = parent.buildPath(path, false);
		else
			separator = filename.charAt(filename.length() - 1);
		
		path.append(filename);
		if (parent != null && !last)
			// Only need the separator after non root nodes and not the last one
			path.append(separator);
		
		return separator;
	}
	
	public String getPath() {
		StringBuffer path = new StringBuffer();
		buildPath(path, true);
		return path.toString();
	}
	
	private void refresh(final TreeViewer viewer, final Object parentElement) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh(parentElement);
			}
		});
	}
	
	public boolean hasChildren() {
		// On Windows, if drive isn't mounted, it's listed but has no attributes.
		if (dirEntry.attrs == null)
			return false;
		return dirEntry.attrs.isDirectory();
	}
	
	public Object getParent() {
		return parent != null ? parent : target;
	}
	
	public Object[] getChildren(final TreeViewer viewer) {
		if (children != null)
			return children;
		
		if (!fetching) {
			fetching = true;
			Protocol.invokeLater(new Runnable() {
				@Override
				public void run() {
					// Get the roots
					target.handleTargetRequest(new TargetRequestSequence() {
						private IFileSystem fileSystem;
						private IFileSystem.IFileHandle _handle;
						private List<FileSystemNode> _children = new ArrayList<FileSystemNode>();
						
						@Override
						public Step[] getSteps() {
							return new TargetRequestSequence.Step[] {
								new TargetRequestSequence.Step() {
									@Override
									public void run(IChannel channel) {
										fileSystem = channel.getRemoteService(IFileSystem.class);
										fileSystem.opendir(getPath(), new IFileSystem.DoneOpen() {
											@Override
											public void doneOpen(IToken token, FileSystemException error, IFileHandle handle) {
												if (error != null) {
													Activator.log(IStatus.ERROR, error);
												} else {
													_handle = handle;
													nextStep();
												}
											}
										});
									}
								},
								new TargetRequestSequence.Step() {
									@Override
									public void run(IChannel channel) {
										fileSystem.readdir(_handle, new IFileSystem.DoneReadDir() {
											@Override
											public void doneReadDir(IToken token, FileSystemException error,
													DirEntry[] entries, boolean eof) {
												if (error != null) {
													Activator.log(IStatus.ERROR, error);
												} else if (entries != null) {
													for (IFileSystem.DirEntry entry : entries)
														_children.add(new FileSystemNode(target, FileSystemNode.this, entry));
													
													// Loop until we get an eof
													if (eof)
														nextStep();
													else
														fileSystem.readdir(_handle, this);
												}
											}
										});
									}
								},
								new TargetRequestSequence.Step() {
									@Override
									public void run(IChannel channel) {
										fileSystem.close(_handle, new IFileSystem.DoneClose() {
											@Override
											public void doneClose(IToken token, FileSystemException error) {
												if (error != null)
													Activator.log(IStatus.ERROR, error);
												else {
													children = _children.toArray(new FileSystemNode[_children.size()]);
													fetching = false;
													refresh(viewer, FileSystemNode.this);
													nextStep();
												}
											}
										});
									}
								},
							};
						}
					
						@Override
						public void channelUnavailable(IStatus error) {
							Activator.log(error);
						}
					});
				}
			});
		}
		
		return new Object[] { FileSystemContentProvider.pending };
	}
}
