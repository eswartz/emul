/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.launch;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.swt.widgets.Shell;

/**
 * TCF clients can implement ITCFLaunchContext to provide information about
 * workspace projects to TCF Launch Configuration.
 *
 * The information includes default values for launch configuration attributes,
 * list of executable binary files, etc.
 *
 * Since each project type can have its own methods to retrieve relevant information,
 * there should be implementation of this interface for each project type that support TCF.
 *
 * Implementation should be able to examine current IDE state (like active editor input source,
 * project explorer selection, etc.) and figure out an "active project".
 */
public interface ITCFLaunchContext {

    /**
     * Check if this context is currently active.
     * @return true if active.
     */
    boolean isActive();

    /**
     * Check if this context recognizes type of a selection.
     * @param selection
     * @return true if the selection is supported by this context.
     */
    boolean isSupportedSelection(Object selection);

    /**
     * Get selection project.
     * @param selection
     * @return selection project or null if selection is not part of a project
     */
    IProject getProject(Object selection);

    /**
     * Get selection file path.
     * @param selection
     * @return selection file path or null if selection is not a file
     */
    IPath getPath(Object selection);

    /**
     * Set launch configuration attributes to default values best suited for current context.
     * @param dlg - currently open launch configuration dialog
     * @param config - currently open launch configuration
     */
    void setDefaults(ILaunchConfigurationDialog dlg, ILaunchConfigurationWorkingCopy config);

    /**
     * Get project build configuration ID.
     * @param project
     * @return build configuration ID.
     */
    String getBuildConfigID(IProject project);

    /**
     * Show a dialog box that allows user to select executable binary file from a list
     * of available file in this context.
     * @param project_name
     * @param shell
     * @return binary file path
     */
    String chooseBinary(Shell shell, IProject project);

    /**
     * Check if a path represents an executable binary file.
     * @param project
     * @param path - full path to a file in the project
     * @return
     * @throws CoreException
     */
    boolean isBinary(IProject project, IPath path) throws CoreException;
}
