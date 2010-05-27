/*******************************************************************************
 * Copyright (c) 2009, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationType;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugModelPresentation;
import org.eclipse.debug.ui.ILaunchShortcut;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

/**
 * This class implements extension point that provides support for selection sensitive launching using TCF.
 * Extensions register a shortcut which appears in the run and/or debug cascade menus to launch
 * the workbench selection or active editor.
 */
public class TCFLaunchShortcut implements ILaunchShortcut {

    private static final String LAUNCH_CONFIGURATION_TYPE_ID = "org.eclipse.tm.tcf.debug.LaunchConfigurationType"; //$NON-NLS-1$

    public void launch(ISelection selection, String mode) {
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection)selection;
            Object obj = ss.getFirstElement();
            ITCFLaunchContext context = TCFLaunchContext.getLaunchContext(obj);
            IProject project = context.getProject(obj);
            IPath path = context.getPath(obj);
            ILaunchConfiguration config = null;
            List<ILaunchConfiguration> list = searchConfigurations(project, path);
            if (list != null) {
                int count = list.size();
                if (count == 0) {
                    config = createConfiguration(project, path);
                }
                else if (count == 1) {
                    config = list.get(0);
                }
                else {
                    config = chooseConfiguration(list);
                }
                if (config != null) DebugUITools.launch(config, mode);
            }
        }
    }

    public void launch(IEditorPart editor, String mode) {
    }

    private List<ILaunchConfiguration> searchConfigurations(IProject project, IPath path) {
        try {
            List<ILaunchConfiguration> list = new ArrayList<ILaunchConfiguration>();
            ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
            ILaunchConfigurationType type = manager.getLaunchConfigurationType(LAUNCH_CONFIGURATION_TYPE_ID);
            ILaunchConfiguration[] configs = DebugPlugin.getDefault().getLaunchManager().getLaunchConfigurations(type);
            for (ILaunchConfiguration config : configs) {
                if (config.getAttribute(TCFLaunchDelegate.ATTR_LOCAL_PROGRAM_FILE, "").equals(path.toOSString()) && //$NON-NLS-1$
                        config.getAttribute(TCFLaunchDelegate.ATTR_PROJECT_NAME, "").equals(project.getName())) { //$NON-NLS-1$
                    list.add(config);
                }
            }
            return list;
        }
        catch (CoreException x) {
            MessageDialog.openError(getShell(), "Error searching available launch configurations", x.getStatus().getMessage());
            return null;
        }
    }

    private ILaunchConfiguration chooseConfiguration(List<ILaunchConfiguration> list) {
        IDebugModelPresentation labelProvider = DebugUITools.newDebugModelPresentation();
        ElementListSelectionDialog dialog= new ElementListSelectionDialog(getShell(), labelProvider);
        dialog.setElements(list.toArray());
        dialog.setTitle("TCF Launch Configuration");
        dialog.setMessage("&Select existing configuration:");
        dialog.setMultipleSelection(false);
        int result = dialog.open();
        labelProvider.dispose();
        if (result == Window.OK) return (ILaunchConfiguration) dialog.getFirstResult();
        return null;
    }

    private ILaunchConfiguration createConfiguration(IProject project, IPath path) {
        ILaunchConfiguration config = null;
        ILaunchConfigurationWorkingCopy wc = null;
        try {
            ILaunchManager manager = DebugPlugin.getDefault().getLaunchManager();
            ILaunchConfigurationType type = manager.getLaunchConfigurationType(LAUNCH_CONFIGURATION_TYPE_ID);
            wc = type.newInstance(null, manager.generateUniqueLaunchConfigurationNameFrom("TCF Local Host " + path.lastSegment()));
            wc.setAttribute(TCFLaunchDelegate.ATTR_LOCAL_PROGRAM_FILE, path.toOSString());
            wc.setAttribute(TCFLaunchDelegate.ATTR_PROJECT_NAME, project.getName());
            // wc.setMappedResources(new IResource[] { });
            config = wc.doSave();
        }
        catch (CoreException x) {
            MessageDialog.openError(getShell(), "Cannot create launch configuration", x.getStatus().getMessage());
        }
        return config;
    }

    private Shell getShell() {
        Shell shell = null;
        IWorkbenchWindow window = Activator.getDefault().getWorkbench().getActiveWorkbenchWindow();
        if (window != null) shell = window.getShell();
        return shell;
    }
}
