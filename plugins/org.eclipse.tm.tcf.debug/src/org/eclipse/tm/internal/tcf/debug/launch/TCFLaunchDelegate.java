/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
import org.eclipse.tm.tcf.services.IPathMap;
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
        ATTR_USE_LOCAL_AGENT = ITCFConstants.ID_TCF_DEBUG_MODEL + ".UseLocalAgent",
        ATTR_SIGNALS_DONT_STOP = ITCFConstants.ID_TCF_DEBUG_MODEL + ".SignalsDontStop",
        ATTR_SIGNALS_DONT_PASS = ITCFConstants.ID_TCF_DEBUG_MODEL + ".SignalsDontPath",
        ATTR_PATH_MAP = ITCFConstants.ID_TCF_DEBUG_MODEL + ".PathMap";

    public static class PathMapRule implements IPathMap.PathMapRule {

        private final Map<String,Object> props;

        public PathMapRule(Map<String,Object> props) {
            this.props = props;
        }

        public Map<String,Object> getProperties() {
            return props;
        }

        public String getID() {
            return (String)props.get(IPathMap.PROP_ID);
        }

        public String getSource() {
            return (String)props.get(IPathMap.PROP_SOURCE);
        }

        public String getDestination() {
            return (String)props.get(IPathMap.PROP_DESTINATION);
        }

        public String getHost() {
            return (String)props.get(IPathMap.PROP_HOST);
        }

        public String getProtocol() {
            return (String)props.get(IPathMap.PROP_PROTOCOL);
        }

        public String toString() {
            StringBuffer bf = new StringBuffer();
            for (String nm : props.keySet()) {
                Object o = props.get(nm);
                if (o != null) {
                    bf.append(nm);
                    bf.append('=');
                    String s = o.toString();
                    for (int i = 0; i < s.length(); i++) {
                        char ch = s.charAt(i);
                        if (ch >= ' ' && ch != '|' && ch != '\\') {
                            bf.append(ch);
                        }
                        else {
                            bf.append('\\');
                            bf.append((int)ch);
                            bf.append(';');
                        }
                    }
                    bf.append('|');
                }
            }
            bf.append('|');
            return bf.toString();
        }
    }

    /**
     * Given value of ATTR_PATH_MAP, return array of PathMapRule objects.
     * @param s - value of ATTR_PATH_MAP.
     * @return array of PathMapRule objects.
     */
    public static ArrayList<PathMapRule> parsePathMapAttribute(String s) {
        ArrayList<PathMapRule> map = new ArrayList<PathMapRule>();
        StringBuffer bf = new StringBuffer();
        int i = 0;
        while (i < s.length()) {
            PathMapRule e = new PathMapRule(new HashMap<String,Object>());
            while (i < s.length()) {
                char ch = s.charAt(i++);
                if (ch == '|') {
                    map.add(e);
                    break;
                }
                bf.setLength(0);
                bf.append(ch);
                while (i < s.length()) {
                    ch = s.charAt(i++);
                    if (ch == '=') break;
                    bf.append(ch);
                }
                String nm = bf.toString();
                bf.setLength(0);
                while (i < s.length()) {
                    ch = s.charAt(i++);
                    if (ch == '|') {
                        if (bf.length() > 0) e.props.put(nm, bf.toString());
                        break;
                    }
                    else if (ch == '\\') {
                        int n = 0;
                        while (i < s.length()) {
                            char d = s.charAt(i++);
                            if (d == ';') break;
                            n = n * 10 + (d - '0');
                        }
                        bf.append((char)n);
                    }
                    else {
                        bf.append(ch);
                    }
                }
            }
        }
        return map;
    }

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
