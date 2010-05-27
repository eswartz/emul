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

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;
import org.eclipse.ui.dialogs.ElementListSelectionDialog;

public class TCFMainTab extends AbstractLaunchConfigurationTab {

    private Text project_text;
    private Text local_program_text;
    private Text remote_program_text;
    private Text working_dir_text;
    private Button default_dir_button;
    private Button terminal_button;
    private Exception init_error;

    public void createControl(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        setControl(comp);

        GridLayout topLayout = new GridLayout();
        comp.setLayout(topLayout);

        createVerticalSpacer(comp, 1);
        createProjectGroup(comp);
        createApplicationGroup(comp);
        createWorkingDirGroup(comp);
        createVerticalSpacer(comp, 1);
        createTerminalOption(comp, 1);
    }

    private void createProjectGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText("Project");

        Label label = new Label(group, SWT.NONE);
        label.setText("Project Name:");
        GridData gd = new GridData();
        gd.horizontalSpan = 2;
        label.setLayoutData(gd);

        project_text = new Text(group, SWT.SINGLE | SWT.BORDER);
        gd = new GridData(GridData.FILL_HORIZONTAL);
        project_text.setLayoutData(gd);
        project_text.addModifyListener(new ModifyListener() {

            public void modifyText(ModifyEvent evt) {
                updateLaunchConfigurationDialog();
            }
        });

        Button project_button = createPushButton(group, "Browse...", null);
        project_button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent evt) {
                handleProjectButtonSelected();
                updateLaunchConfigurationDialog();
            }
        });
    }

    private void createApplicationGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText("Application");

        createLocalExeFileGroup(group);
        createRemoteExeFileGroup(group);
    }

    private void createLocalExeFileGroup(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 3;
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        comp.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        comp.setLayoutData(gd);

        Label program_label = new Label(comp, SWT.NONE);
        program_label.setText("Local File Path:");
        gd = new GridData();
        gd.horizontalSpan = 3;
        program_label.setLayoutData(gd);

        local_program_text = new Text(comp, SWT.SINGLE | SWT.BORDER);
        local_program_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        local_program_text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent evt) {
                updateLaunchConfigurationDialog();
            }
        });

        Button search_button = createPushButton(comp, "Search...", null);
        search_button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent evt) {
                handleSearchButtonSelected();
                updateLaunchConfigurationDialog();
            }
        });

        Button browse_button = createPushButton(comp, "Browse...", null);
        browse_button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent evt) {
                handleBinaryBrowseButtonSelected();
                updateLaunchConfigurationDialog();
            }
        });
    }

    private void createRemoteExeFileGroup(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        comp.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        comp.setLayoutData(gd);

        Label program_label = new Label(comp, SWT.NONE);
        program_label.setText("Remote File Path:");
        gd = new GridData();
        gd.horizontalSpan = 3;
        program_label.setLayoutData(gd);

        remote_program_text = new Text(comp, SWT.SINGLE | SWT.BORDER);
        remote_program_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        remote_program_text.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent evt) {
                updateLaunchConfigurationDialog();
            }
        });
    }

    private void createWorkingDirGroup(Composite comp) {
        Group group = new Group(comp, SWT.NONE);
        GridLayout layout = new GridLayout();
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText("Working directory");

        working_dir_text = new Text(group, SWT.SINGLE | SWT.BORDER);
        working_dir_text.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        default_dir_button = new Button(group, SWT.CHECK);
        default_dir_button.setText("Use default");
        default_dir_button.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        default_dir_button.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent evt) {
                updateLaunchConfigurationDialog();
            }
        });
    }

    @Override
    protected void updateLaunchConfigurationDialog() {
        super.updateLaunchConfigurationDialog();
        working_dir_text.setEnabled(!default_dir_button.getSelection());
    }

    private void createTerminalOption(Composite parent, int colSpan) {
        Composite terminal_comp = new Composite(parent, SWT.NONE);
        GridLayout terminal_layout = new GridLayout();
        terminal_layout.numColumns = 1;
        terminal_layout.marginHeight = 0;
        terminal_layout.marginWidth = 0;
        terminal_comp.setLayout(terminal_layout);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = colSpan;
        terminal_comp.setLayoutData(gd);

        terminal_button = createCheckButton(terminal_comp, "Use Terminal");
        terminal_button.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent evt) {
                updateLaunchConfigurationDialog();
            }
        });
        terminal_button.setEnabled(true);
    }

    public void initializeFrom(ILaunchConfiguration config) {
        setErrorMessage(null);
        setMessage(null);
        try {
            project_text.setText(config.getAttribute(TCFLaunchDelegate.ATTR_PROJECT_NAME, ""));
            local_program_text.setText(config.getAttribute(TCFLaunchDelegate.ATTR_LOCAL_PROGRAM_FILE, ""));
            remote_program_text.setText(config.getAttribute(TCFLaunchDelegate.ATTR_REMOTE_PROGRAM_FILE, ""));
            working_dir_text.setText(config.getAttribute(TCFLaunchDelegate.ATTR_WORKING_DIRECTORY, ""));
            default_dir_button.setSelection(!config.hasAttribute(TCFLaunchDelegate.ATTR_WORKING_DIRECTORY));
            terminal_button.setSelection(config.getAttribute(TCFLaunchDelegate.ATTR_USE_TERMINAL, true));
            working_dir_text.setEnabled(!default_dir_button.getSelection());
        }
        catch (Exception e) {
            init_error = e;
            setErrorMessage("Cannot read launch configuration: " + e);
            Activator.log(e);
        }
    }

    private IProject getProject() {
        String name = project_text.getText().trim();
        if (name.length() == 0) return null;
        return ResourcesPlugin.getWorkspace().getRoot().getProject(name);
    }

    public void performApply(ILaunchConfigurationWorkingCopy config) {
        config.setAttribute(TCFLaunchDelegate.ATTR_PROJECT_NAME, project_text.getText());
        config.setAttribute(TCFLaunchDelegate.ATTR_LOCAL_PROGRAM_FILE, local_program_text.getText());
        config.setAttribute(TCFLaunchDelegate.ATTR_REMOTE_PROGRAM_FILE, remote_program_text.getText());
        if (default_dir_button.getSelection()) {
            config.removeAttribute(TCFLaunchDelegate.ATTR_WORKING_DIRECTORY);
        }
        else {
            config.setAttribute(TCFLaunchDelegate.ATTR_WORKING_DIRECTORY, working_dir_text.getText());
        }
        config.setAttribute(TCFLaunchDelegate.ATTR_USE_TERMINAL, terminal_button.getSelection());
    }

    /**
     * Show a dialog that lists all executable files in currently selected project.
     */
    private void handleSearchButtonSelected() {
        IProject project = getProject();
        if (project == null) {
            MessageDialog.openInformation(getShell(),
                    "Project required",
                    "Enter project before searching for program");
            return;
        }
        ITCFLaunchContext launch_context = TCFLaunchContext.getLaunchContext(project);
        if (launch_context == null) return;
        String path = launch_context.chooseBinary(getShell(), project);
        if (path != null) local_program_text.setText(path);
    }

    /**
     * Show a dialog that lets the user select a project. This in turn provides context for the main
     * type, allowing the user to key a main type name, or constraining the search for main types to
     * the specified project.
     */
    private void handleBinaryBrowseButtonSelected() {
        FileDialog file_dialog = new FileDialog(getShell(), SWT.NONE);
        file_dialog.setFileName(local_program_text.getText());
        String path = file_dialog.open();
        if (path != null) local_program_text.setText(path);
    }

    /**
     * Show a dialog that lets the user select a project. This in turn provides context for the main
     * type, allowing the user to key a main type name, or constraining the search for main types to
     * the specified project.
     */
    private void handleProjectButtonSelected() {
        try {
            IProject project = chooseProject();
            if (project == null) return;
            project_text.setText(project.getName());
        }
        catch (Exception e) {
            Activator.log("Cannot get project description", e);
        }
    }

    /**
     * Show project list dialog and return the first selected project, or null.
     */
    private IProject chooseProject() {
        try {
            IProject[] projects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
            ILabelProvider label_provider = new LabelProvider() {

                @Override
                public String getText(Object element) {
                    if (element == null) return "";
                    return ((IProject)element).getName();
                }
            };
            ElementListSelectionDialog dialog = new ElementListSelectionDialog(getShell(), label_provider);
            dialog.setTitle("Project Selection");
            dialog.setMessage("Choose project to constrain search for program");
            dialog.setElements(projects);

            IProject cProject = getProject();
            if (cProject != null) dialog.setInitialSelections(new Object[]{cProject});
            if (dialog.open() == Window.OK) return (IProject)dialog.getFirstResult();
        }
        catch (Exception e) {
            Activator.log("Cannot show project list dialog", e);
        }
        return null;
    }

    @Override
    public boolean isValid(ILaunchConfiguration config) {
        setErrorMessage(null);
        setMessage(null);

        if (init_error != null) {
            setErrorMessage("Cannot read launch configuration: " + init_error);
            return false;
        }

        String project_name = project_text.getText().trim();
        if (project_name.length() != 0) {
            IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(project_name);
            if (!project.exists()) {
                setErrorMessage("Project does not exist");
                return false;
            }
            if (!project.isOpen()) {
                setErrorMessage("Project must be opened");
                return false;
            }
        }
        String local_name = local_program_text.getText().trim();
        if (local_name.equals(".") || local_name.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
            setErrorMessage("Invalid local program name");
            return false;
        }
        String remote_name = remote_program_text.getText().trim();
        if (remote_name.equals(".") || remote_name.equals("..")) { //$NON-NLS-1$ //$NON-NLS-2$
            setErrorMessage("Invalid remote program name");
            return false;
        }
        if (local_name.length() > 0) {
            IProject project = getProject();
            IPath program_path = new Path(local_name);
            if (!program_path.isAbsolute()) {
                if (project == null) {
                    File ws = ResourcesPlugin.getWorkspace().getRoot().getLocation().toFile();
                    File file = new File(ws, local_name);
                    if (!file.exists()) {
                        setErrorMessage("File not found: " + file);
                        return false;
                    }
                    if (file.isDirectory()) {
                        setErrorMessage("Program path is directory name: " + file);
                        return false;
                    }
                    program_path = new Path(file.getAbsolutePath());
                }
                else if (!project.getFile(local_name).exists()) {
                    setErrorMessage("Program does not exist");
                    return false;
                }
                else {
                    program_path = project.getFile(local_name).getLocation();
                }
            }
            else {
                File file = program_path.toFile();
                if (!file.exists()) {
                    setErrorMessage("Program file does not exist");
                    return false;
                }
                if (file.isDirectory()) {
                    setErrorMessage("Program path is directory name");
                    return false;
                }
            }
            if (project != null) {
                try {
                    ITCFLaunchContext launch_context = TCFLaunchContext.getLaunchContext(project);
                    if (launch_context != null && !launch_context.isBinary(project, program_path)) {
                        setErrorMessage("Program is not a recongnized executable");
                        return false;
                    }
                }
                catch (CoreException e) {
                    Activator.log(e);
                    setErrorMessage(e.getLocalizedMessage());
                    return false;
                }
            }
        }
        return true;
    }

    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
        config.setAttribute(TCFLaunchDelegate.ATTR_PROJECT_NAME, "");
        config.setAttribute(TCFLaunchDelegate.ATTR_USE_TERMINAL, true);
        config.setAttribute(TCFLaunchDelegate.ATTR_WORKING_DIRECTORY, (String)null);
        ITCFLaunchContext launch_context = TCFLaunchContext.getLaunchContext(null);
        if (launch_context != null) launch_context.setDefaults(getLaunchConfigurationDialog(), config);
    }

    public String getName() {
        return "Main";
    }

    @Override
    public Image getImage() {
        return ImageCache.getImage(ImageCache.IMG_TCF);
    }
}
