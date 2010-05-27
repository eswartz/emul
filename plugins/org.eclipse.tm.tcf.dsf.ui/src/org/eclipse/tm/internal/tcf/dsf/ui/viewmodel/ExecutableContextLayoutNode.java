/*******************************************************************************
 * Copyright (c) 2006, 2010 Ericsson and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Ericsson                 - Initial API and implementation
 *     Wind River Systems       - reused for TCF connection type
 *******************************************************************************/

package org.eclipse.tm.internal.tcf.dsf.ui.viewmodel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RejectedExecutionException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.IDsfStatusConstants;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IElementLabelProvider;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IViewerUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.tm.internal.tcf.debug.model.TCFContextState;
import org.eclipse.tm.internal.tcf.dsf.ui.Activator;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFExecutionDMC;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFRunControl;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFStack;
import org.eclipse.tm.tcf.protocol.Protocol;
import org.eclipse.tm.tcf.util.TCFDataCache;
import org.eclipse.ui.PlatformUI;


@SuppressWarnings("restriction")
public class ExecutableContextLayoutNode extends AbstractDMVMNode implements IElementLabelProvider {

    public ExecutableContextLayoutNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, IRunControl.IExecutionDMContext.class);
    }

    private void doneViewerUpdate(final IViewerUpdate req) {
        PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
            public void run() {
                req.done();
            }
        });
    }

    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        TCFDSFRunControl service = getServicesTracker().getService(TCFDSFRunControl.class);
        if (service == null) {
            update.setStatus(new Status(IStatus.ERROR,
                    Activator.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE,
                    "Run Control service not available.", null)); //$NON-NLS-1$
            handleFailedUpdate(update);
            return;
        }

        final TCFDSFExecutionDMC dmc = findDmcInPath(update.getViewerInput(),
                update.getElementPath(), TCFDSFExecutionDMC.class);

        service.getAllContexts(dmc,
                new DataRequestMonitor<IExecutionDMContext[]>(getSession().getExecutor(), null) {
                    @Override
                    public void handleCompleted() {
                        if (!getStatus().isOK()) {
                            handleFailedUpdate(update);
                        }
                        else {
                            fillUpdateWithVMCs(update, getData());
                            doneViewerUpdate(update);
                        }
                    }
                }
        );
    }

    public void update(final ILabelUpdate[] updates) {
        try {
            getSession().getExecutor().execute(new DsfRunnable() {
                public void run() {
                    updateLabelInSessionThread(updates);
                }
            });
        }
        catch (RejectedExecutionException e) {
            for (ILabelUpdate update : updates) {
                update.setStatus(new Status(IStatus.ERROR,
                        Activator.PLUGIN_ID, IDsfStatusConstants.INTERNAL_ERROR,
                        "Cannot execute update request.", e)); //$NON-NLS-1$
                handleFailedUpdate(update);
            }
        }
    }

    private void updateLabelInSessionThread(final ILabelUpdate[] updates) {
        TCFDSFRunControl service = getServicesTracker().getService(TCFDSFRunControl.class);
        if (service == null) {
            for (final ILabelUpdate update : updates) {
                update.setStatus(new Status(IStatus.ERROR,
                        Activator.PLUGIN_ID, IDsfStatusConstants.INVALID_STATE,
                        "Run Control service not available.", null)); //$NON-NLS-1$
                handleFailedUpdate(update);
            }
            return;
        }
        TCFDataCache<?> pending = null;
        for (final ILabelUpdate update : updates) {
            TCFDSFExecutionDMC dmc = (TCFDSFExecutionDMC)findDmcInPath(update.getViewerInput(),
                    update.getElementPath(), IContainerDMContext.class);
            if (!dmc.run_control_context_cache.validate()) pending = dmc.run_control_context_cache;
            else if (!dmc.run_control_state_cache.validate()) pending = dmc.run_control_state_cache;
        }
        if (pending != null) {
            pending.wait(new Runnable() {
                public void run() {
                    updateLabelInSessionThread(updates);
                }
            });
            return;
        }

        for (final ILabelUpdate update : updates) {
            TCFDSFExecutionDMC dmc = (TCFDSFExecutionDMC)findDmcInPath(update.getViewerInput(),
                    update.getElementPath(), IContainerDMContext.class);

            org.eclipse.tm.tcf.services.IRunControl.RunControlContext rc = dmc.run_control_context_cache.getData();
            String image = null;
            if (rc == null) {
                image = IDebugUIConstants.IMG_ACT_DEBUG;
            }
            else if (!rc.hasState()) {
                image = IDebugUIConstants.IMG_OBJS_DEBUG_TARGET;
            }
            else {
                TCFContextState state = dmc.run_control_state_cache.getData();
                if (state != null && state.is_suspended) {
                    image = IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
                }
                else {
                    image = IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
                }
            }
            update.setImageDescriptor(DebugUITools.getImageDescriptor(image), 0);

            update.setLabel(dmc.getTcfContextId(), 0);
            doneViewerUpdate(update);
        }
    }

    public int getDeltaFlags(Object e) {
        if (e instanceof IStartedDMEvent || e instanceof IExitedDMEvent) {
            return IModelDelta.CONTENT;
        }
        if (e instanceof IRunControl.IResumedDMEvent || e instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.STATE;
        }
        return IModelDelta.NO_CHANGE;
    }

    private List<TCFDSFExecutionDMC> getPath(TCFDSFExecutionDMC dmc) {
        List<TCFDSFExecutionDMC> list = new ArrayList<TCFDSFExecutionDMC>();
        while (dmc != null) {
            list.add(dmc);
            IDMContext[] up = dmc.getParents();
            dmc = null;
            for (IDMContext c: up) {
                if (c instanceof TCFDSFExecutionDMC) {
                    dmc = (TCFDSFExecutionDMC)c;
                    if (dmc.getTcfContextId() == null) dmc = null;
                    break;
                }
            }
        }
        return list;
    }

    public void buildDelta(final Object e, final VMDelta parentDelta, int nodeOffset, final RequestMonitor rm) {
        if (e instanceof IRunControl.IResumedDMEvent || e instanceof IRunControl.ISuspendedDMEvent) {
            Protocol.invokeLater(new Runnable() {
                public void run() {
                    TCFDSFStack.TCFFrameDMC frame = null;
                    TCFDSFExecutionDMC dmc = (TCFDSFExecutionDMC)((IDMEvent<?>)e).getDMContext();
                    /*
                    if (e instanceof IRunControl.ISuspendedDMEvent) {
                        TCFDSFStack service = getServicesTracker().getService(TCFDSFStack.class);
                        if (service != null) {
                            TCFDataCache<?> cache = service.getFramesCache(dmc, null);
                            if (!cache.validate()) {
                                cache.wait(this);
                                return;
                            }
                            if (cache.getError() == null) {
                                frame = service.getTopFrame(dmc);
                            }
                        }
                    }
                    */
                    ModelDelta delta = parentDelta;
                    List<TCFDSFExecutionDMC> list = getPath(dmc);
                    for (int i = list.size() - 1; i >= 0; i--) {
                        delta = delta.addNode(createVMContext(list.get(i)),
                                i == 0 ? IModelDelta.CONTENT | IModelDelta.STATE : 0);
                    }
                    if (frame != null) {
                        delta = delta.addNode(createVMContext(frame),
                                IModelDelta.EXPAND | IModelDelta.SELECT);
                    }
                    rm.done();
                }
            });
            return;
        }
        else if (e instanceof IStartedDMEvent || e instanceof IExitedDMEvent) {
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
        }
        rm.done();
    }
}
