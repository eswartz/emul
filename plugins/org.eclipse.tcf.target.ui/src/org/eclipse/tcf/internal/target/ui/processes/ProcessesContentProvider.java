/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Wind River Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.tcf.internal.target.ui.processes;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tcf.internal.target.core.Activator;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tcf.target.core.TargetRequestSequence;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.services.IProcesses;

/**
 * @author dschaefer
 *
 */
public class ProcessesContentProvider implements ITreeContentProvider {

	private TreeViewer viewer;
	private boolean showRootNode;
	private boolean fetching;

	static final String pending = "pending...";
	
	static class RootNode {
		private static final String propertyName = "processesRoot";
		private final ITarget target;
		private ProcessesNode[] processes;
		
		public RootNode(ITarget target) {
			this.target = target;
		}
	}

	public ProcessesContentProvider() {
		this(true);
	}
	
	public ProcessesContentProvider(boolean showRootNode) {
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
			RootNode root = (RootNode)target.getLocalProperties().get(RootNode.propertyName);
			if (root == null) {
				root = new RootNode(target);
				target.getLocalProperties().put(RootNode.propertyName, root);
			}
			if (showRootNode) {
				return new Object[] { root };
			} else
				return getProcesses((ITarget)parentElement);
		} else if (parentElement instanceof RootNode) {
			return getProcesses(((RootNode)parentElement).target);
		} else if (parentElement instanceof ProcessesNode) {
			return ((ProcessesNode)parentElement).getChildren(viewer);
		} else
			return new Object[0];
	}

	private void refresh(final TreeViewer viewer, final Object parentElement) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				viewer.refresh(parentElement);
			}
		});
	}

	private Object[] getProcesses(final ITarget target) {
		final RootNode root = (RootNode)target.getLocalProperties().get(RootNode.propertyName);
		if (root == null)
			return new Object[0];
		
		if (root.processes != null)
			return root.processes;
		
		if (!fetching) {
			fetching = true;
			Protocol.invokeLater(new Runnable() {
				@Override
				public void run() {
					target.handleTargetRequest(new TargetRequestSequence() {
						IProcesses processes;
						int i = 0;
						
						@Override
						public Step[] getSteps() {
							return new Step[] {
								new Step() {
									@Override
									public void run(IChannel channel) {
										processes = channel.getRemoteService(IProcesses.class);
										processes.getChildren(null, false, new IProcesses.DoneGetChildren() {
											@Override
											public void doneGetChildren(IToken token, Exception error, String[] context_ids) {
												root.processes = new ProcessesNode[context_ids.length];
												for (int i = 0; i < context_ids.length; ++i)
													root.processes[i] = new ProcessesNode(target, context_ids[i]);
												refresh(viewer, target);
												nextStep();
											}
										});										
									}
								},
								new Step() {
									@Override
									public void run(IChannel channel) {
										processes.getContext(root.processes[i].getContextId(), new IProcesses.DoneGetContext() {
											@Override
											public void doneGetContext(IToken token, Exception error, IProcesses.ProcessContext context) {
												if (error != null) {
													Activator.log(IStatus.ERROR, error);
												} else {
													root.processes[i].setContext(context);
													if (++i < root.processes.length)
														processes.getContext(root.processes[i].getContextId(), this);
													else {
														refresh(viewer, target);
														fetching = false;
														nextStep();
													}
												}
											}
										});
									}
								}
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
		
		return new Object[] { pending };
	}
	
	@Override
	public Object getParent(Object element) {
		if (element instanceof RootNode)
			return ((RootNode)element).target;
		else if (element instanceof ProcessesNode)
			return ((ProcessesNode)element).getParent();
		else
			return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ITarget)
			return true;
		else if (element instanceof RootNode)
			return true;
		else
			return false;
	}

}
