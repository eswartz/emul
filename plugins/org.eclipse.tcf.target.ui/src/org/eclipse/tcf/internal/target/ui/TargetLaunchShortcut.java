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
package org.eclipse.tcf.internal.target.ui;

import java.io.IOException;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.ILaunchShortcut2;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tcf.target.ui.ITargetEditorInput;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.util.TCFTask;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;

public class TargetLaunchShortcut implements ILaunchShortcut2 {

	@Override
	public void launch(ISelection selection, String mode) {
		if (selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection)selection).getFirstElement();
			if (obj instanceof ITarget)
				launch((ITarget)obj, mode);
		}
	}

	@Override
	public void launch(IEditorPart editor, String mode) {
		IEditorInput input = editor.getEditorInput();
		if (input instanceof ITargetEditorInput) {
			launch(((ITargetEditorInput)input).getTarget(), mode);
		}
	}

	private void launch(ITarget target, String mode) {
		ILaunchConfiguration[] configs = getLaunchConfigurations(target);
		if (configs.length == 0)
			return;
		
		ILaunchConfiguration config = configs[0]; // there should only be one
		DebugUITools.launch(config, mode);
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(ISelection selection) {
		if (selection instanceof IStructuredSelection) {
			Object obj = ((IStructuredSelection)selection).getFirstElement();
			if (obj instanceof ITarget)
				return getLaunchConfigurations((ITarget)obj);
		}
		return null;
	}

	@Override
	public ILaunchConfiguration[] getLaunchConfigurations(IEditorPart editorpart) {
		IEditorInput input = editorpart.getEditorInput();
		if (input instanceof ITargetEditorInput) {
			return getLaunchConfigurations(((ITargetEditorInput)input).getTarget());
		}
		return null;
	}

	private ILaunchConfiguration findLaunchConfiguration(final ITarget target) throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		ILaunchConfigurationType type = manager.getLaunchConfigurationType("org.eclipse.tm.tcf.debug.LaunchConfigurationType");
		final ILaunchConfiguration[] configs = manager.getLaunchConfigurations(type);
		
		// Find the config that matches our target if there is one
		try {
			return new TCFTask<ILaunchConfiguration>() {
				public void run() {
					target.handleTargetRequest(new ITarget.ITargetRequest() {
						@Override
						public void execute(IChannel channel) {
							IPeer peer = channel.getRemotePeer();
							String peerId = peer.getID();
							String agentId = peer.getAgentID();
							
							for (ILaunchConfiguration config : configs) {
								try {
									String cpeerId = config.getAttribute(TCFLaunchDelegate.ATTR_PEER_ID, (String)null);
									if (cpeerId == null)
										continue;
									
									if (cpeerId.equals(peerId)) {
										done(config);
										return;
									} else {
										IPeer cpeer = Protocol.getLocator().getPeers().get(cpeerId);
										if (cpeer != null) {
											String cagentId = cpeer.getAgentID();
											if (cagentId != null && cagentId.equals(agentId)) {
												done(config);
												return;
											}
										}
									}
								} catch (CoreException e) {
								}
							}
							
							done(null);
						}
						
						@Override
						public void channelUnavailable(IStatus error) {
							done(null);
						}
					});
				};
			}.getIO();
		} catch (IOException e) {
			throw new CoreException(Activator.createStatus(IStatus.ERROR, e));
		}
	}
	
	private ILaunchConfiguration createLaunchConfiguration(final ITarget target) throws CoreException {
		ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
		final ILaunchConfigurationType type = manager.getLaunchConfigurationType("org.eclipse.tm.tcf.debug.LaunchConfigurationType");
		ILaunchConfigurationWorkingCopy wc = new TCFTask<ILaunchConfigurationWorkingCopy>() {
			@Override
			public void run() {
				target.handleTargetRequest(new ITarget.ITargetRequest() {
					@Override
					public void execute(IChannel channel) {
						try {
							ILaunchConfigurationWorkingCopy wc = type.newInstance(null, "Attach " + target.getName());
							IPeer peer = channel.getRemotePeer();
							wc.setAttribute(TCFLaunchDelegate.ATTR_PEER_ID, peer.getID());
							wc.setAttribute(TCFLaunchDelegate.ATTR_USE_LOCAL_AGENT, false);
							wc.setAttribute(TCFLaunchDelegate.ATTR_USE_LOCAL_AGENT, false);
							done(wc);
						} catch (CoreException e) {
							error(e);
						}
					}
					
					@Override
					public void channelUnavailable(IStatus error) {
						error(new CoreException(error));
					}
				});
			}
		}.getE();
		return wc.doSave();
	}
	
	private ILaunchConfiguration[] getLaunchConfigurations(ITarget target) {
		try {
			ILaunchConfiguration config = findLaunchConfiguration(target);
			if (config == null)
				config = createLaunchConfiguration(target);
			
			if (config != null)
				return new ILaunchConfiguration[] { config };
		} catch (CoreException e) {
			Activator.log(e.getStatus());
		}
		return new ILaunchConfiguration[0];
	}
	
	@Override
	public IResource getLaunchableResource(ISelection selection) {
		// We're not associated with a resource
		return null;
	}

	@Override
	public IResource getLaunchableResource(IEditorPart editorpart) {
		// We're not associated with a resource
		return null;
	}
}
