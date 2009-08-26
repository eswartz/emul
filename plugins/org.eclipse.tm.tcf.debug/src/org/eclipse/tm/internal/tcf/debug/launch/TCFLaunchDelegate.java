/*******************************************************************************
 * Copyright (c) 2007, 2009 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.tm.internal.tcf.debug.model.ITCFConstants;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.util.TCFTask;


public class TCFLaunchDelegate extends LaunchConfigurationDelegate {

    public static final String
        ATTR_PEER_ID = ITCFConstants.ID_TCF_DEBUG_MODEL + ".PeerID",
        ATTR_PROJECT_NAME = ITCFConstants.ID_TCF_DEBUG_MODEL + ".ProjectName",
        ATTR_LOCAL_PROGRAM_FILE = ITCFConstants.ID_TCF_DEBUG_MODEL + ".LocalProgramFile",
        ATTR_REMOTE_PROGRAM_FILE = ITCFConstants.ID_TCF_DEBUG_MODEL + ".ProgramFile",
        ATTR_COPY_TO_REMOTE_FILE = ITCFConstants.ID_TCF_DEBUG_MODEL + ".CopyToRemote",
        ATTR_PROGRAM_ARGUMENTS = ITCFConstants.ID_TCF_DEBUG_MODEL + ".ProgramArguments",
        ATTR_WORKING_DIRECTORY = ITCFConstants.ID_TCF_DEBUG_MODEL + ".WorkingDirectory",
        ATTR_USE_TERMINAL = ITCFConstants.ID_TCF_DEBUG_MODEL + ".UseTerminal",
        ATTR_RUN_LOCAL_AGENT = ITCFConstants.ID_TCF_DEBUG_MODEL + ".RunLocalAgent",
        ATTR_USE_LOCAL_AGENT = ITCFConstants.ID_TCF_DEBUG_MODEL + ".UseLocalAgent";

    public ILaunch getLaunch(final ILaunchConfiguration configuration, final String mode) throws CoreException {
        return new TCFTask<ILaunch>() {
            int cnt;
            public void run() {
                // Need to delay at least one dispatch cycle to work around
                // a possible racing between thread that calls getLaunch() and 
                // the process of activation of other TCF plug-ins. 
                if (cnt++ < 2) Protocol.invokeLater(this);
                else done(new TCFLaunch(configuration, mode));
            }
        }.getE();
    }
    
    public void launch(final ILaunchConfiguration configuration, final String mode,
            final ILaunch launch, final IProgressMonitor monitor) throws CoreException {
        String local_id = null;
        int task_cnt = 1;
        if (configuration.getAttribute(TCFLaunchDelegate.ATTR_RUN_LOCAL_AGENT, true)) {
            task_cnt++;
            if (monitor != null) monitor.beginTask("Starting TCF Agent", task_cnt); //$NON-NLS-1$
            local_id = TCFLocalAgent.runLocalAgent();
        }
        else if (configuration.getAttribute(TCFLaunchDelegate.ATTR_USE_LOCAL_AGENT, true)) {
            task_cnt++;
            if (monitor != null) monitor.beginTask("Searching TCF Agent", task_cnt); //$NON-NLS-1$
            local_id = TCFLocalAgent.getLocalAgentID();
        }
        if (monitor != null) monitor.beginTask("Launching TCF debugger session", task_cnt); //$NON-NLS-1$
        final String id =
            configuration.getAttribute(TCFLaunchDelegate.ATTR_USE_LOCAL_AGENT, true) ?
                    local_id : configuration.getAttribute(ATTR_PEER_ID, "");
        Protocol.invokeLater(new Runnable() {
            public void run() {
                ((TCFLaunch)launch).launchTCF(mode, id);
                if (monitor != null) monitor.done();
            }
        });
    }
}
