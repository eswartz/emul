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
package org.eclipse.tm.internal.tcf.debug.ui.launch;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.debug.ui.StringVariableSelectionDialog;
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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.tm.internal.tcf.debug.launch.TCFLaunchDelegate;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;
import org.eclipse.tm.internal.tcf.debug.ui.ImageCache;


public class TCFArgumentsTab extends AbstractLaunchConfigurationTab {

    private Text text_arguments;
    private Button button_variables;
    private Exception init_error;

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
    }

    private void createArgumentsGroup(Composite parent) {
        Composite comp = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.marginHeight = 0;
        layout.marginWidth = 0;
        comp.setLayout(layout);
        GridData gd = new GridData(GridData.FILL_BOTH);
        comp.setLayoutData(gd);

        Label label = new Label(comp, SWT.NONE);
        label.setText("Program Arguments:");
        gd = new GridData();
        gd.horizontalSpan = 2;
        label.setLayoutData(gd);

        text_arguments = new Text(comp, SWT.MULTI | SWT.WRAP | SWT.BORDER | SWT.V_SCROLL);
        gd = new GridData(GridData.FILL_BOTH);
        gd.heightHint = 40;
        gd.widthHint = 100;
        text_arguments.setLayoutData(gd);
        text_arguments.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent evt) {
                updateLaunchConfigurationDialog();
            }
        });
        button_variables = createPushButton(comp, "Variables", null);
        gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
        button_variables.setLayoutData(gd);
        button_variables.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent arg0) {
                handleVariablesButtonSelected(text_arguments);
            }
        });
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

    public void setDefaults(ILaunchConfigurationWorkingCopy config) {
        config.setAttribute(TCFLaunchDelegate.ATTR_PROGRAM_ARGUMENTS, (String)null);
    }

    public void initializeFrom(ILaunchConfiguration configuration) {
        setErrorMessage(null);
        setMessage(null);
        try {
            text_arguments.setText(configuration.getAttribute(TCFLaunchDelegate.ATTR_PROGRAM_ARGUMENTS, "")); //$NON-NLS-1$
        }
        catch (CoreException e) {
            init_error = e;
            setErrorMessage("Cannot read launch configuration: " + e);
            Activator.log(e);
        }
    }

    public void performApply(ILaunchConfigurationWorkingCopy configuration) {
        configuration.setAttribute(
                TCFLaunchDelegate.ATTR_PROGRAM_ARGUMENTS,
                getAttributeValueFrom(text_arguments));
    }

    @Override
    public boolean isValid(ILaunchConfiguration config) {
        setErrorMessage(null);
        setMessage(null);

        if (init_error != null) {
            setErrorMessage("Cannot read launch configuration: " + init_error);
            return false;
        }

        return true;
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
        return ImageCache.getImageDescriptor(ImageCache.IMG_ARGUMENTS_TAB).createImage();
    }
}
