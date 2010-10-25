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
package org.eclipse.tcf.internal.target.core;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.tcf.target.core.AbstractTarget;
import org.eclipse.tcf.target.core.ITarget;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.osgi.framework.Bundle;

public class LocalTarget extends AbstractTarget implements ITarget {

	// TODO provide API and UI to change these
	private String host = "127.0.0.1";
	private String port = "1534";
	
	private Process process;

	@Override
	public String getShortName() {
		return "Local";
	}
	
	@Override
	protected void launch() {
		try {
			// Find the path to the agent executable
			File agentFile = getAgentFile();
			if (!Platform.getOS().equals(Platform.OS_WIN32)) {
				Runtime.getRuntime().exec(new String[] { "chmod", "+x", agentFile.getAbsolutePath() }).waitFor();
			}
			
			// Start the agent
			process = Runtime.getRuntime().exec(new String[] {
				agentFile.getAbsolutePath(),
				"-s",
				"TCP:" + host + ":" + port + ";IsLocal=true" });

			// Start monitor process
			Thread t = new Thread() {
				public void run() {
					try {
						final int n = process.waitFor();
						if (n != 0) {
							// Assume bad things happened and peers never reported
							Protocol.invokeLater(new Runnable() {
								@Override
								public void run() {
									for (ITargetRequest request : outstandingRequests)
										request.channelUnavailable(Activator.createStatus(IStatus.ERROR, new Error("Local TCF Agent exited with code: " + n)));
									outstandingRequests.clear();
								}
							});
						}
					} catch (InterruptedException e) {
						Activator.log(IStatus.WARNING, e);
					}
				}
			};
			t.setDaemon(true);
			t.setName("Local TCF Agent Monitor");
			t.start();
				
		} catch (IOException e) {
			Activator.log(IStatus.ERROR, e);
		} catch (URISyntaxException e) {
			Activator.log(IStatus.ERROR, e);
		} catch (InterruptedException e) {
			Activator.log(IStatus.ERROR, e);
		}
	}
	
	private boolean isLocalAgent(IPeer peer) {
        String h = peer.getAttributes().get(IPeer.ATTR_IP_HOST);
        String p = peer.getAttributes().get(IPeer.ATTR_IP_PORT);
        String l = peer.getAttributes().get("IsLocal");
        return host.equals(h) && port.equals(p) && "true".equals(l);
	}
	
	private static File getAgentFile() throws URISyntaxException, IOException {
		String localAgent = System.getProperty("tcf.localAgent");
		if (localAgent != null) {
			return new File(localAgent);
		} else {
	        String os = System.getProperty("os.name");
	        String arch = System.getProperty("os.arch");
	        String fnm = "agent";
	        if (arch.equals("x86")) arch = "i386";
	        if (arch.equals("i686")) arch = "i386";
	        if (os.startsWith("Windows")) {
	            os = "Windows";
	            fnm = "agent.exe";
	        }
	        if (os.equals("Linux")) os = "GNU/Linux";
			IPath agentPath = new Path("agent/" + os + "/" + arch + "/" + fnm);
			Bundle debugBundle = Platform.getBundle("org.eclipse.tm.tcf.debug");
			URL agentURL = FileLocator.find(debugBundle, agentPath, null);
			return new File(FileLocator.toFileURL(agentURL).toURI());
		}
	}
	
	@Override
	public boolean handleNewPeer(IPeer peer) {
		// If it's the first one in, make sure it's a local agent
		if (peers.isEmpty() && !isLocalAgent(peer))
			return false;
		
		return super.handleNewPeer(peer);
	}

	public void dispose() {
		if (process != null)
			process.destroy();
	}
}
