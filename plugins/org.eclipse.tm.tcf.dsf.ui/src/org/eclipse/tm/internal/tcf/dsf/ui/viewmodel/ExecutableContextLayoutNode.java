/*******************************************************************************
 * Copyright (c) 2006 Ericsson and others.
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

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFExecutionDMC;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFRunControl;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFRunControlState;
import org.eclipse.tm.tcf.util.TCFDataCache;


@SuppressWarnings("restriction")
public class ExecutableContextLayoutNode extends AbstractDMVMNode implements IElementLabelProvider {

    public ExecutableContextLayoutNode(AbstractDMVMProvider provider, DsfSession session) {
        super(provider, session, IRunControl.IExecutionDMContext.class);
    }
    
    private void doneViewerUpdate(final IViewerUpdate req) {
        Display.getDefault().asyncExec(new Runnable() {
            public void run() {
                req.done();
            }
        });
    }

    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        if (!checkService(IRunControl.class, null, update)) return;

        final TCFDSFExecutionDMC dmc = findDmcInPath(update.getViewerInput(),
                update.getElementPath(), TCFDSFExecutionDMC.class);
        
        getServicesTracker().getService(TCFDSFRunControl.class).getAllContexts(dmc,
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
                handleFailedUpdate(update);
            }
        }
    }
        
    private void updateLabelInSessionThread(final ILabelUpdate[] updates) {
        TCFDataCache<?> pending = null;
        for (final ILabelUpdate update : updates) {
            if (!checkService(TCFDSFRunControl.class, null, update)) continue;
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
            if (!checkService(TCFDSFRunControl.class, null, update)) continue;
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
                TCFDSFRunControlState state = dmc.run_control_state_cache.getData();
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

    public void buildDelta(Object e, VMDelta parentDelta, int nodeOffset, RequestMonitor requestMonitor) {
        if (e instanceof IRunControl.IResumedDMEvent || e instanceof IRunControl.ISuspendedDMEvent) {
            parentDelta.addNode(new DMVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.STATE);
        }
        else if (e instanceof IStartedDMEvent || e instanceof IExitedDMEvent) {
            parentDelta.setFlags(parentDelta.getFlags() | IModelDelta.CONTENT);
            //parentDelta.addNode(createVMContext(((IDMEvent<?>)e).getDMContext()), IModelDelta.CONTENT);
        }            
        requestMonitor.done();
    }
}
