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
package org.eclipse.tm.internal.tcf.cdt.ui.sourcelookup;

import org.eclipse.cdt.debug.core.model.ISteppingModeTarget;
import org.eclipse.cdt.debug.internal.ui.sourcelookup.CSourceNotFoundEditor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.tm.internal.tcf.cdt.ui.sourcelookup.TCFSourceNotFoundPresentation.TCFCSourceNotFoundEditorInput;
import org.eclipse.tm.internal.tcf.cdt.ui.sourcelookup.TCFSourceNotFoundPresentation.TCFCSourceNotFoundElement;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;

/**
 * Customized source-not-found editor
 */
@SuppressWarnings("restriction")
public class TCFCSourceNotFoundEditor extends CSourceNotFoundEditor {

    static final String ID = "org.eclipse.tm.tcf.cdt.ui.source_not_found_editor";

    private class LaunchListener implements ILaunchesListener2 {
        public void launchesRemoved(ILaunch[] launches) {
        }
        public void launchesAdded(ILaunch[] launches) {
        }
        public void launchesChanged(ILaunch[] launches) {
        }
        public void launchesTerminated(ILaunch[] launches) {
            for (ILaunch launch : launches) {
                if (launch == fLaunch) {
                    closeEditor();
                    break;
                }
            }
        }
    }

    private ILaunch fLaunch;
    private LaunchListener fLaunchListener;

    @Override
    public void setInput(IEditorInput input) {
        if (input instanceof TCFCSourceNotFoundEditorInput) {
            TCFCSourceNotFoundElement element = (TCFCSourceNotFoundElement) ((TCFCSourceNotFoundEditorInput) input).getArtifact();
            TCFModel model = element.getModel();
            if (model != null) {
                fLaunch = model.getLaunch();
                DebugPlugin.getDefault().getLaunchManager().addLaunchListener(fLaunchListener = new LaunchListener());
            }
        }
        super.setInput(input);
    }

    @Override
    protected void viewDisassembly() {
        Object artifact = getArtifact();
        ISteppingModeTarget target = (ISteppingModeTarget) DebugPlugin.getAdapter(artifact, ISteppingModeTarget.class);
        if (target != null) {
            target.enableInstructionStepping(true);
            closeEditor();
        }
    }

    @Override
    public void dispose() {
        if (fLaunchListener != null) {
            DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(fLaunchListener);
        }
        super.dispose();
    }

    protected void closeEditor() {
        final IEditorPart editor = this;
        editor.getSite().getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                IWorkbenchPage page = editor.getSite().getPage();
                if (page != null) page.closeEditor(editor, false);
            }
        });
    }
}
