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

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.tm.internal.tcf.debug.ui.Activator;

public class TCFPreferences {

    public static final String
        PREF_STACK_FRAME_LIMIT_ENABLED = "StackFrameLimitEnabled",
        PREF_STACK_FRAME_LIMIT_VALUE = "StackFrameLimitValue",
        PREF_STACK_FRAME_ARG_NAMES = "StackFrameArgNames",
        PREF_STACK_FRAME_ARG_VALUES = "StackFrameArgValues",
        PREF_WAIT_FOR_PC_UPDATE_AFTER_STEP = "WaitForPCUpdateAfterStep",
        PREF_WAIT_FOR_VIEWS_UPDATE_AFTER_STEP = "WaitForViewsUpdateAfterStep",
        PREF_DELAY_STACK_UPDATE_UNTIL_LAST_STEP = "DelayStackUpdateUntilLastStep",
        PREF_MIN_STEP_INTERVAL = "MinStepInterval",
        PREF_MIN_UPDATE_INTERVAL = "MinUpdateInterval",
        PREF_VIEW_UPDATES_THROTTLE = "ViewUpdatesThrottle",
        PREF_TARGET_TRAFFIC_THROTTLE = "TargetTrafficThrottle";

    public static IPreferenceStore getPreferenceStore() {
        return Activator.getDefault().getPreferenceStore();
    }
}
