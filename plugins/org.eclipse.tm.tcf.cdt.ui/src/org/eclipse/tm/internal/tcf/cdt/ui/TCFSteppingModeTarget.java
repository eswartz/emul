/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui;

import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.debug.core.model.ITargetProperties;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.sourcelookup.ISourceDisplay;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

/**
 * Integrates the TCF model with the "Instruction Stepping Mode" button from CDT.
 */
@SuppressWarnings("deprecation")
public class TCFSteppingModeTarget implements ISteppingModeTarget, ITargetProperties {

    private final Preferences fPreferences;
    private final TCFModel fModel;

    public TCFSteppingModeTarget(TCFModel model) {
        fPreferences= new Preferences();
        fPreferences.setDefault(PREF_INSTRUCTION_STEPPING_MODE, model.isInstructionSteppingEnabled());
        fModel = model;
    }

    public void addPropertyChangeListener(IPropertyChangeListener listener) {
        fPreferences.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(IPropertyChangeListener listener) {
        fPreferences.removePropertyChangeListener(listener);
    }

    public boolean supportsInstructionStepping() {
        return true;
    }

    public void enableInstructionStepping(boolean enabled) {
        fPreferences.setValue(PREF_INSTRUCTION_STEPPING_MODE, enabled);
        fModel.setInstructionSteppingEnabled(enabled);
        // switch to disassembly or source
        forceSourceDisplay(DebugUITools.getDebugContext());
    }

    private void forceSourceDisplay(IAdaptable debugContext) {
        ISourceDisplay sourceDisplay = (ISourceDisplay) debugContext.getAdapter(ISourceDisplay.class);
        if (sourceDisplay != null) {
            IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
            if (window != null) {
                IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                    sourceDisplay.displaySource(debugContext, page, true);
                }
            }
        }
    }

    public boolean isInstructionSteppingEnabled() {
        return fPreferences.getBoolean(PREF_INSTRUCTION_STEPPING_MODE);
    }

}
