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
package org.eclipse.tm.internal.tcf.cdt.ui;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.model.CModelException;
import org.eclipse.cdt.core.model.CoreModel;
import org.eclipse.cdt.core.model.ICElement;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.core.model.IBinary;
import org.eclipse.cdt.core.settings.model.ICProjectDescription;
import org.eclipse.cdt.ui.CElementLabelProvider;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate;
import org.eclipse.tm.internal.tcf.debug.ui.launch.ITCFLaunchContext;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.dialogs.TwoPaneElementSelector;

public class TCFLaunchContext implements ITCFLaunchContext {

    public boolean isActive() {
        return getContext(null) != null;
    }

    public boolean isSupportedSelection(Object selection) {
        if (selection instanceof IProject) {
            return CoreModel.getDefault().getCModel().getCProject(((IProject)selection).getName()) != null;
        }
        return selection instanceof ICElement;
    }

    public IPath getPath(Object selection) {
        if (selection instanceof IResource) {
            return ((IResource)selection).getLocation();
        }
        if (selection instanceof ICElement) {
            return ((ICElement)selection).getResource().getLocation();
        }
        return null;
    }

    public IProject getProject(Object selection) {
        if (selection instanceof IProject) return (IProject)selection;
        if (selection instanceof ICElement) return ((ICElement)selection).getCProject().getProject();
        return null;
    }

    public void setDefaults(ILaunchConfigurationDialog dlg, ILaunchConfigurationWorkingCopy config) {
        ICElement element = getContext(config);
        if (element != null) {
            initializeCProject(element, config);
            initializeProgramName(element, dlg, config);
        }
    }

    /**
     * Returns the current C element context from which to initialize default
     * settings, or <code>null</code> if none. Note, if possible we will
     * return the IBinary based on configuration entry as this may be more useful then
     * just the project.
     *
     * @return C element context.
     */
    private ICElement getContext(ILaunchConfiguration config) {
        String projectName = null;
        String programName = null;
        if (config != null) {
            try {
                projectName = config.getAttribute(TCFLaunchDelegate.ATTR_PROJECT_NAME, (String)null);
                programName = config.getAttribute(TCFLaunchDelegate.ATTR_LOCAL_PROGRAM_FILE, (String)null);
            }
            catch (CoreException e) {
                Activator.log(e);
            }
        }
        Object obj = null;
        IWorkbenchPage page = Activator.getActivePage();
        if (projectName != null && !projectName.equals("")) { //$NON-NLS-1$
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
            ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
            if (cProject != null && cProject.exists()) obj = cProject;
        }
        else {
            if (page != null) {
                ISelection selection = page.getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection ss = (IStructuredSelection)selection;
                    if (!ss.isEmpty()) obj = ss.getFirstElement();
                }
            }
        }
        if (obj instanceof IResource) {
            ICElement ce = CoreModel.getDefault().create((IResource)obj);
            if (ce == null) {
                IProject pro = ((IResource)obj).getProject();
                ce = CoreModel.getDefault().create(pro);
            }
            obj = ce;
        }
        if (obj instanceof ICElement) {
            if (programName == null || programName.equals("")) { //$NON-NLS-1$
                return (ICElement)obj;
            }
            ICElement ce = (ICElement)obj;
            IProject project;
            project = (IProject)ce.getCProject().getResource();
            IPath programFile = project.getFile(programName).getLocation();
            ce = CCorePlugin.getDefault().getCoreModel().create(programFile);
            if (ce != null && ce.exists()) return ce;
            return (ICElement)obj;
        }
        IEditorPart part = page.getActiveEditor();
        if (part != null) {
            IEditorInput input = part.getEditorInput();
            return (ICElement)input.getAdapter(ICElement.class);
        }
        return null;
    }

    /**
     * Set the C project attribute based on the ICElement.
     */
    private void initializeCProject(ICElement cElement, ILaunchConfigurationWorkingCopy config) {
        ICProject cProject = cElement.getCProject();
        String name = null;
        if (cProject != null && cProject.exists()) {
            name = cProject.getElementName();
            config.setMappedResources(new IResource[] {cProject.getProject()});
        }
        config.setAttribute(TCFLaunchDelegate.ATTR_PROJECT_NAME, name);
    }

    /**
     * Set the program name attributes on the working copy based on the ICElement
     */
    private void initializeProgramName(ICElement cElement, ILaunchConfigurationDialog dlg, ILaunchConfigurationWorkingCopy config) {

        boolean renamed = false;

        if (!(cElement instanceof IBinary)) {
            cElement = cElement.getCProject();
        }

        if (cElement instanceof ICProject) {

            IProject project = cElement.getCProject().getProject();
            String name = project.getName();
            ICProjectDescription projDes = CCorePlugin.getDefault().getProjectDescription(project);
            if (projDes != null) {
                String buildConfigName = projDes.getActiveConfiguration().getName();
                name = name + " " + buildConfigName; //$NON-NLS-1$
            }
            name = dlg.generateName(name);
            config.rename(name);
            renamed = true;
        }

        IBinary binary = null;
        if (cElement instanceof ICProject) {
            IBinary[] bins = getBinaryFiles((ICProject)cElement);
            if (bins != null && bins.length == 1) {
                binary = bins[0];
            }
        }
        else if (cElement instanceof IBinary) {
            binary = (IBinary)cElement;
        }

        if (binary != null) {
            String path;
            path = binary.getResource().getProjectRelativePath().toOSString();
            config.setAttribute(TCFLaunchDelegate.ATTR_LOCAL_PROGRAM_FILE, path);
            if (!renamed)
            {
                String name = binary.getElementName();
                int index = name.lastIndexOf('.');
                if (index > 0) {
                    name = name.substring(0, index);
                }
                name = dlg.generateName(name);
                config.rename(name);
                renamed = true;
            }
        }

        if (!renamed) {
            String name = dlg.generateName(cElement.getCProject().getElementName());
            config.rename(name);
        }
    }

    public String getBuildConfigID(IProject project) {
        ICProjectDescription projDes = CCorePlugin.getDefault().getProjectDescription(project);
        if (projDes == null) return null;
        return projDes.getActiveConfiguration().getId();
    }

    /**
     * Iterate through and suck up all of the executable files that we can find.
     */
    private IBinary[] getBinaryFiles(final ICProject cproject) {
        if (cproject == null || !cproject.exists()) return null;
        final Display display = Display.getCurrent();
        final Object[] ret = new Object[1];
        BusyIndicator.showWhile(display, new Runnable() {

            public void run() {
                try {
                    ret[0] = cproject.getBinaryContainer().getBinaries();
                }
                catch (CModelException e) {
                    Activator.errorDialog("Launch UI internal error", e);
                }
            }
        });
        return (IBinary[])ret[0];
    }

    /**
     * Return true if given file path names a binary file.
     * @param project
     * @param path
     * @return true if binary file.
     * @throws CoreException
     */
    public boolean isBinary(IProject project, IPath path) throws CoreException {
        ICExtensionReference[] parserRef = CCorePlugin.getDefault().getBinaryParserExtensions(project);
        for (int i = 0; i < parserRef.length; i++) {
            try {
                IBinaryParser parser = (IBinaryParser)parserRef[i].createExtension();
                if (parser.getBinary(path) != null) return true;
            }
            catch (Exception e) {
            }
        }
        IBinaryParser parser = CCorePlugin.getDefault().getDefaultBinaryParser();
        try {
            return parser.getBinary(path) != null;
        }
        catch (Exception e) {
        }
        return false;
    }

    public String chooseBinary(Shell shell, IProject project) {
        ILabelProvider programLabelProvider = new CElementLabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof IBinary) {
                    IBinary bin = (IBinary)element;
                    StringBuffer name = new StringBuffer();
                    name.append(bin.getPath().lastSegment());
                    return name.toString();
                }
                return super.getText(element);
            }

            @Override
            public Image getImage(Object element) {
                if (! (element instanceof ICElement)) {
                    return super.getImage(element);
                }
                ICElement celement = (ICElement)element;

                if (celement.getElementType() == ICElement.C_BINARY) {
                    IBinary belement = (IBinary)celement;
                    if (belement.isExecutable()) {
                        return DebugUITools.getImage(IDebugUIConstants.IMG_ACT_RUN);
                    }
                }

                return super.getImage(element);
            }
        };

        ILabelProvider qualifierLabelProvider = new CElementLabelProvider() {

            @Override
            public String getText(Object element) {
                if (element instanceof IBinary) {
                    IBinary bin = (IBinary)element;
                    StringBuffer name = new StringBuffer();
                    name.append(bin.getCPU() + (bin.isLittleEndian() ? "le" : "be")); //$NON-NLS-1$ //$NON-NLS-2$
                    name.append(" - "); //$NON-NLS-1$
                    name.append(bin.getPath().toString());
                    return name.toString();
                }
                return super.getText(element);
            }
        };

        TwoPaneElementSelector dialog = new TwoPaneElementSelector(shell, programLabelProvider, qualifierLabelProvider);
        dialog.setElements(getBinaryFiles(getCProject(project.getName())));
        dialog.setMessage("Choose program to run");
        dialog.setTitle("Program Selection");
        dialog.setUpperListLabel("Binaries:");
        dialog.setLowerListLabel("Qualifier:");
        dialog.setMultipleSelection(false);
        // dialog.set
        if (dialog.open() != Window.OK) return null;
        IBinary binary = (IBinary)dialog.getFirstResult();
        return binary.getResource().getProjectRelativePath().toString();
    }

    /**
     * Return the ICProject corresponding to the project name in the project name text field, or
     * null if the text does not match a project name.
     */
    private ICProject getCProject(String name) {
        String projectName = name.trim();
        if (projectName.length() < 1) return null;
        return CoreModel.getDefault().getCModel().getCProject(projectName);
    }
}
