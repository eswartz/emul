/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.launch;

import java.io.IOException;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.tm.internal.tcf.debug.model.ITCFConstants;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.tcf.protocol.IPeer;
import org.eclipse.tm.tcf.protocol.Protocol;


public class TCFLaunchDelegate extends LaunchConfigurationDelegate {

    public static final String
        ATTR_PEER_ID = ITCFConstants.ID_TCF_DEBUG_MODEL + ".PeerID",
        ATTR_PROGRAM_FILE = ITCFConstants.ID_TCF_DEBUG_MODEL + ".ProgramFile",
        ATTR_PROGRAM_ARGUMENTS = ITCFConstants.ID_TCF_DEBUG_MODEL + ".ProgramArguments",
        ATTR_WORKING_DIRECTORY = ITCFConstants.ID_TCF_DEBUG_MODEL + ".WorkingDirectory";

    public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
        return new TCFLaunch(configuration, mode);
    }

    public void launch(final ILaunchConfiguration configuration, final String mode,
            final ILaunch launch, IProgressMonitor monitor) throws CoreException {
        if (monitor == null) monitor = new NullProgressMonitor();
        monitor.beginTask("Launching debugger session", 1); //$NON-NLS-1$
        Protocol.invokeAndWait(new Runnable() {
            public void run() {
                try {
                    String id = configuration.getAttribute(TCFLaunchDelegate.ATTR_PEER_ID, "");
                    IPeer peer = Protocol.getLocator().getPeers().get(id);
                    if (peer == null) throw new IOException("Cannot locate peer " + id);
                    TCFLaunch.TerminateListener term = null;
                    if (peer instanceof TCFLaunch.TerminateListener) term = (TCFLaunch.TerminateListener)peer;
                    ((TCFLaunch)launch).launchTCF(mode, peer, term);
                }
                catch (Throwable e) {
                    ((TCFLaunch)launch).setError(e);
                }
            }
        });
        monitor.done();
    }
}
