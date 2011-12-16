/*******************************************************************************
 * Copyright (c) 2004, 2010 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *     Ericsson             - DSF-GDB version
 *     Nokia                - Made generic to DSF
 *     Wind River Systems   - Adapted to TCF Debug
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.cdt.ui;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.debug.ui.CDebugUIPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNode;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeExecContext;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFNodeStackFrame;
import org.eclipse.ui.IPageListener;
import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWindowListener;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.progress.WorkbenchJob;

/**
 * Manages the current evaluation context (stack frame) for evaluation actions.
 * In each page, the selection is tracked in each debug view (if any). When a
 * debug target selection exists, the "debuggerActive" System property is set to
 * true. This property is used to make the "Run To Line", "Resume At Line",
 * "Move To Line" and "Add Watch Expression" actions visible in editors only if
 * there is a running debug session.
 */
public class EvaluationContextManager implements IWindowListener,
        IPageListener, ISelectionListener, IPartListener2 {

    // Must use the same ID as the base CDT since we want to enable
    // actions that are registered by base CDT.
    private final static String DEBUGGER_ACTIVE = CDebugUIPlugin.PLUGIN_ID + ".debuggerActive"; //$NON-NLS-1$

    protected static EvaluationContextManager fgManager;

    private Map<IWorkbenchPage, TCFNode> fContextsByPage = null;

    protected EvaluationContextManager() {
    }

    public static void startup() {
        WorkbenchJob job = new WorkbenchJob("") {
            @Override
            public IStatus runInUIThread(IProgressMonitor monitor) {
                if (fgManager == null) {
                    fgManager = new EvaluationContextManager();
                    IWorkbench workbench = PlatformUI.getWorkbench();
                    IWorkbenchWindow[] windows = workbench.getWorkbenchWindows();
                    for (int i = 0; i < windows.length; i++) {
                        fgManager.windowOpened(windows[i]);
                    }
                    workbench.addWindowListener(fgManager);
                }
                return Status.OK_STATUS;
            }
        };
        job.setPriority(Job.SHORT);
        job.setSystem(true);
        job.schedule();
    }

    public void windowActivated(IWorkbenchWindow window) {
        windowOpened(window);
    }

    public void windowDeactivated(IWorkbenchWindow window) {
    }

    public void windowClosed(IWorkbenchWindow window) {
        window.removePageListener(this);
    }

    public void windowOpened(IWorkbenchWindow window) {
        IWorkbenchPage[] pages = window.getPages();
        for (int i = 0; i < pages.length; i++) {
            window.addPageListener(this);
            pageOpened(pages[i]);
        }
    }

    public void pageActivated(IWorkbenchPage page) {
        pageOpened(page);
    }

    public void pageClosed(IWorkbenchPage page) {
        page.removeSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
        page.removePartListener(this);
    }

    public void pageOpened(IWorkbenchPage page) {
        page.addSelectionListener(IDebugUIConstants.ID_DEBUG_VIEW, this);
        page.addPartListener(this);
        IWorkbenchPartReference ref = page.getActivePartReference();
        if (ref != null) {
            partActivated(ref);
        }
    }

    public void selectionChanged(IWorkbenchPart part, ISelection selection) {
        IWorkbenchPage page = part.getSite().getPage();
        if (selection instanceof IStructuredSelection) {
            IStructuredSelection ss = (IStructuredSelection) selection;
            if (ss.size() == 1) {
                Object element = ss.getFirstElement();
                if (element instanceof TCFNodeExecContext
                        || element instanceof TCFNodeStackFrame) {
                    setContext(page, (TCFNode) element);
                    return;
                }
            }
        }
        // no context in the given view
        removeContext(page);
    }

    public void partActivated(IWorkbenchPartReference partRef) {
    }

    public void partBroughtToTop(IWorkbenchPartReference partRef) {
    }

    public void partClosed(IWorkbenchPartReference partRef) {
        if (IDebugUIConstants.ID_DEBUG_VIEW.equals(partRef.getId())) {
            removeContext(partRef.getPage());
        }
    }

    public void partDeactivated(IWorkbenchPartReference partRef) {
    }

    public void partOpened(IWorkbenchPartReference partRef) {
    }

    public void partHidden(IWorkbenchPartReference partRef) {
    }

    public void partVisible(IWorkbenchPartReference partRef) {
    }

    public void partInputChanged(IWorkbenchPartReference partRef) {
    }

    /**
     * Sets the evaluation context for the given page, and notes that a valid
     * execution context exists.
     *
     * @param page
     * @param target
     */
    private void setContext(IWorkbenchPage page, TCFNode target) {
        if (fContextsByPage == null) {
            fContextsByPage = new HashMap<IWorkbenchPage, TCFNode>();
        }
        fContextsByPage.put(page, target);
        System.setProperty(DEBUGGER_ACTIVE, Boolean.TRUE.toString());
    }

    /**
     * Removes an evaluation context for the given page, and determines if any
     * valid execution context remain.
     *
     * @param page
     */
    private void removeContext(IWorkbenchPage page) {
        if (fContextsByPage != null) {
            fContextsByPage.remove(page);
            if (fContextsByPage.isEmpty()) {
                System.setProperty(DEBUGGER_ACTIVE, Boolean.FALSE.toString());
                fContextsByPage = null;
            }
        }
    }
}
