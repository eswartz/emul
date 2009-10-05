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

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
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

    /**
     * Given project name and program name returns absolute path of the program.
     * @param project_name - workspace project name.
     * @param program_name - launch program name.
     * @return program path or null if both project name and program name are null.
     */
    public static String getProgramPath(String project_name, String program_name) {
        if (program_name == null || program_name.length() == 0) return null;
        if (project_name == null || project_name.length() == 0) {
            File file = new File(program_name);
            if (!file.isAbsolute()) {
                File ws = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
                file = new File(ws, program_name);
            }
            return file.getAbsolutePath();
        }
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(project_name);
        IPath program_path = new Path(program_name);
        if (!program_path.isAbsolute()) {
            if (project == null || !project.getFile(program_name).exists()) return null;
            program_path = project.getFile(program_name).getLocation();
        }
        return program_path.toOSString();
    }

    /**
     * Create new TCF launch object.
     * @return new TCFLaunch object
     */
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

    /**
     * Launch TCF session.
     */
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
