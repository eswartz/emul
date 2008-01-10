/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.debug.tcf.ui.launch;

import java.net.URL;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Text;

import com.windriver.debug.tcf.core.launch.TCFLaunchDelegate;
import com.windriver.debug.tcf.ui.TCFUI;

public class TCFArgumentsTab extends AbstractLaunchConfigurationTab {

    private Text text_arguments;
    private Button button_variables;
    private Text text_working_dir;
    private Button button_default_dir;
    private Image image;

    public void createControl(Composite parent) {
        Font font = parent.getFont();
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout(1, true);
        comp.setLayout(layout);
        comp.setFont(font);

        GridData gd = new GridData(GridData.FILL_BOTH);
        comp.setLayoutData(gd);
        setControl(comp);

        createArgumentsGroup(comp);
        createWorkingDirGroup(comp);
        
        URL url = FileLocator.find(TCFUI.getDefault().getBundle(),
                new Path("icons/arguments_tab.gif"), null);
        ImageDescriptor descriptor = null;
        if (url == null) {
            descriptor = ImageDescriptor.getMissingImageDescriptor();
        }
        else {
            descriptor = ImageDescriptor.createFromURL(url);
        }
        image = descriptor.createImage(parent.getDisplay());
    }

    private void createArgumentsGroup(Composite comp) {
        Font font = comp.getFont();

        Group group = new Group(comp, SWT.NONE);
        group.setFont(font);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(GridData.FILL_BOTH));
        group.setText("Program Arguments");
        
        text_arguments = new Text(group, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
        GridData gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 40;
        gd.widthHint = 100;
        text_arguments.setLayoutData(gd);
        text_arguments.setFont(font);
        text_arguments.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent evt) {
                updateLaunchConfigurationDialog();
            }
        });
        button_variables= createPushButton(group, "Variables", null);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
        button_variables.setLayoutData(gd);
        button_variables.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                handleVariablesButtonSelected(text_arguments);
            }
        });
    }
    
    private void createWorkingDirGroup(Composite comp) {
        Font font = comp.getFont();
        
        Group group = new Group(comp, SWT.NONE);
        GridLayout workingDirLayout = new GridLayout();
        workingDirLayout.makeColumnsEqualWidth = false;
        group.setLayout(workingDirLayout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setFont(font);
        group.setText("Working directory");

        text_working_dir = new Text(group, SWT.SINGLE | SWT.BORDER);
        text_working_dir.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        text_working_dir.setFont(font);

        button_default_dir = new Button(group, SWT.CHECK);
        button_default_dir.setText("Use default");
        button_default_dir.setLayoutData(new GridData(GridData.FILL, GridData.BEGINNING, true, false));
        button_default_dir.setFont(font);
    }
    
    @Override
    public void dispose() {
        if (image != null) {
            image.dispose();
            image = null;
        }
        super.dispose();
    }

    /**
     * A variable entry button has been pressed for the given text
     * field. Prompt the user for a variable and enter the result
     * in the given field.
     */
    private void handleVariablesButtonSelected(Text textField) {
        String variable = getVariable();
        if (variable != null) textField.append(variable);
    }

    /**
     * Prompts the user to choose and configure a variable and returns
     * the resulting string, suitable to be used as an attribute.
     */
    private String getVariable() {
        StringVariableSelectionDialog dialog = new StringVariableSelectionDialog(getShell());
        dialog.open();
        return dialog.getVariableExpression();
    }

    public boolean isValid(ILaunchConfiguration config) {
        return true;
    }

    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
        config.setAttribute(TCFLaunchDelegate.ATTR_PROGRAM_ARGUMENTS, (String)null);
        config.setAttribute(TCFLaunchDelegate.ATTR_WORKING_DIRECTORY, (String)null);
    }

    public void initializeFrom(ILaunchConfiguration configuration) {
        try {
            text_arguments.setText(configuration.getAttribute(TCFLaunchDelegate.ATTR_PROGRAM_ARGUMENTS, "")); //$NON-NLS-1$
            text_working_dir.setText(configuration.getAttribute(TCFLaunchDelegate.ATTR_WORKING_DIRECTORY, "")); //$NON-NLS-1$
        }
        catch (CoreException e) {
            setErrorMessage("Cannot read launch configuration: " + e);
        }
    }

    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(
                TCFLaunchDelegate.ATTR_PROGRAM_ARGUMENTS,
                getAttributeValueFrom(text_arguments));
        configuration.setAttribute(
                TCFLaunchDelegate.ATTR_WORKING_DIRECTORY,
                getAttributeValueFrom(text_working_dir));
    }

    protected String getAttributeValueFrom(Text text) {
        String content = text.getText().trim();
        content = content.replaceAll("\r\n", "\n");  // eliminate Windows \r line delimiter
        if (content.length() > 0) return content;
        return null;
    }

    public String getName() {
        return "Arguments";
    }
    
    @Override
    public Image getImage() {
        return image;
    }
}
