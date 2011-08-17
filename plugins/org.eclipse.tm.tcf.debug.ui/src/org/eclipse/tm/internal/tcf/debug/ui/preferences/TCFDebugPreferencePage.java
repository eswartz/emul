/*******************************************************************************
 * Copyright (c) 2011 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.preferences;

import org.eclipse.jface.preference.BooleanFieldEditor;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class TCFDebugPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public TCFDebugPreferencePage() {
        super(FLAT);
        setPreferenceStore(TCFPreferences.getPreferenceStore());
        setDescription("General settings for debuggers using Target Communication Framework (TCF)");
    }

    public void init(IWorkbench workbench) {
    }

    @Override
    protected void createFieldEditors() {
        Composite parent = getFieldEditorParent();
        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        parent.setLayout(layout);

        createPerformanceGroup(parent);
        createStackTraceGroup(parent);
    }

    private void createPerformanceGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Performance");
        GridLayout layout = new GridLayout(3, false);
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        BooleanFieldEditor autoThreadListUpdates = new BooleanFieldEditor(
                TCFPreferences.PREF_DELAY_CHILDREN_LIST_UPDATES,
                "Delay children list updates in the Debug View until a child context is suspended",
                group);

        autoThreadListUpdates.fillIntoGrid(group, 3);
        addField(autoThreadListUpdates);

        BooleanFieldEditor syncSteppingEditor = new BooleanFieldEditor(
                TCFPreferences.PREF_WAIT_FOR_PC_UPDATE_AFTER_STEP,
                "Wait for editor marker to update after every step",
                group);

        syncSteppingEditor.fillIntoGrid(group, 3);
        addField(syncSteppingEditor);

        BooleanFieldEditor syncViewsEditor = new BooleanFieldEditor(
                TCFPreferences.PREF_WAIT_FOR_VIEWS_UPDATE_AFTER_STEP,
                "Wait for views to update after every step",
                group);

        syncViewsEditor.fillIntoGrid(group, 3);
        addField(syncViewsEditor);

        BooleanFieldEditor delayStackEditor = new BooleanFieldEditor(
                TCFPreferences.PREF_DELAY_STACK_UPDATE_UNTIL_LAST_STEP,
                "Delay stack trace update util last step",
                group);

        delayStackEditor.fillIntoGrid(group, 3);
        addField(delayStackEditor);

        IntegerFieldEditor minStepIntervalEditor = new DecoratingIntegerFieldEditor(
                TCFPreferences.PREF_MIN_STEP_INTERVAL,
                "Minimum interval between steps (in milliseconds)",
                group);

        minStepIntervalEditor.setValidRange(0, 10000);
        minStepIntervalEditor.fillIntoGrid(group, 3);
        addField(minStepIntervalEditor);

        IntegerFieldEditor minUpdateIntervalEditor = new DecoratingIntegerFieldEditor(
                TCFPreferences.PREF_MIN_UPDATE_INTERVAL,
                "Minimum interval between view updates (in milliseconds)",
                group);

        minUpdateIntervalEditor.setValidRange(0, 10000);
        minUpdateIntervalEditor.fillIntoGrid(group, 3);
        addField(minUpdateIntervalEditor);

        BooleanFieldEditor updatesThrottleEditor = new BooleanFieldEditor(
                TCFPreferences.PREF_VIEW_UPDATES_THROTTLE,
                "Reduce views updates frequency during UI jobs congestion",
                group);

        updatesThrottleEditor.fillIntoGrid(group, 3);
        addField(updatesThrottleEditor);

        BooleanFieldEditor trafficThrottleEditor = new BooleanFieldEditor(
                TCFPreferences.PREF_TARGET_TRAFFIC_THROTTLE,
                "Reduce data requests frequency during target traffic congestion",
                group);

        trafficThrottleEditor.fillIntoGrid(group, 3);
        addField(trafficThrottleEditor);

        group.setLayout(layout);
    }

    private void createStackTraceGroup(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setText("Stack trace");
        GridLayout layout = new GridLayout(3, false);
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        BooleanFieldEditor showArgNamesEditor = new BooleanFieldEditor(
                TCFPreferences.PREF_STACK_FRAME_ARG_NAMES,
                "Show function argument names in stack frames",
                group);

        showArgNamesEditor.fillIntoGrid(group, 3);
        addField(showArgNamesEditor);

        BooleanFieldEditor showArgValuesEditor = new BooleanFieldEditor(
                TCFPreferences.PREF_STACK_FRAME_ARG_VALUES,
                "Show function argument values in stack frames",
                group);

        showArgValuesEditor.fillIntoGrid(group, 3);
        addField(showArgValuesEditor);

        IntegerFieldEditor limitEditor = new IntegerWithBooleanFieldEditor(
                TCFPreferences.PREF_STACK_FRAME_LIMIT_ENABLED,
                TCFPreferences.PREF_STACK_FRAME_LIMIT_VALUE,
                "Limit number of stack frames to",
                group);

        limitEditor.setValidRange(1, Integer.MAX_VALUE);
        limitEditor.setValidateStrategy(IntegerWithBooleanFieldEditor.VALIDATE_ON_FOCUS_LOST);
        limitEditor.fillIntoGrid(group, 3);
        addField(limitEditor);

        group.setLayout(layout);
    }
}
