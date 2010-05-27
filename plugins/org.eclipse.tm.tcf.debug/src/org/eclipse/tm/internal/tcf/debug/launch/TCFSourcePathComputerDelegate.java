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

import java.util.ArrayList;
import java.util.HashSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourcePathComputerDelegate;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.WorkspaceSourceContainer;

/**
 * Computes the default source lookup path for a TCF launch configuration. The
 * default source lookup path is the project containing the TCF
 * program being launched. If the program is not specified, the workspace is
 * searched by default.
 */
public class TCFSourcePathComputerDelegate implements ISourcePathComputerDelegate {

    public ISourceContainer[] computeSourceContainers(
            ILaunchConfiguration configuration, IProgressMonitor monitor)
            throws CoreException {
        ArrayList<ISourceContainer> res = new ArrayList<ISourceContainer>();
        String project_name = configuration.getAttribute(TCFLaunchDelegate.ATTR_PROJECT_NAME, (String)null);
        String program_name = configuration.getAttribute(TCFLaunchDelegate.ATTR_LOCAL_PROGRAM_FILE, (String)null);
        String path = TCFLaunchDelegate.getProgramPath(project_name, program_name);
        if (path != null) {
            IFile[] files = ResourcesPlugin.getWorkspace().getRoot().findFilesForLocation(new Path(path));
            if (files != null && files.length > 0) {
                HashSet<IProject> projects = new HashSet<IProject>();
                for (IFile file : files) projects.add(file.getProject());
                for (IProject project : projects) res.add(new ProjectSourceContainer(project, true));
            }
        }
        if (res.size() == 0 && project_name != null && project_name.length() > 0) {
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(project_name);
            if (project != null) res.add(new ProjectSourceContainer(project, true));
        }
        if (res.size() == 0) res.add(new WorkspaceSourceContainer());
        return res.toArray(new ISourceContainer[res.size()]);
    }
}
