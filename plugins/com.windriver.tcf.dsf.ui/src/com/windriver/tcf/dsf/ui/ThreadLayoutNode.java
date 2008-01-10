/*******************************************************************************
 * Copyright (c) 2006 Wind River Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *     Ericsson 	  - Modified for multi threaded functionality	
 *******************************************************************************/
package com.windriver.tcf.dsf.ui;

import java.util.List;
import java.util.Map;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.INativeProcesses;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.INativeProcesses.IThreadDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.IVMContext;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;

import com.windriver.tcf.dsf.core.services.TCFDSFExecutionDMC;

@SuppressWarnings("restriction")
public class ThreadLayoutNode extends AbstractDMVMLayoutNode {

    public ThreadLayoutNode(AbstractVMProvider provider, DsfSession session) {
        super(provider, session, IRunControl.IExecutionDMContext.class);
    }

    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        if (!checkService(IRunControl.class, null, update)) return;
        final IContainerDMContext contDmc = findDmcInPath(update.getElementPath(), IContainerDMContext.class);

        if (contDmc == null) {
            handleFailedUpdate(update);
            return;
        } 

        getServicesTracker().getService(IRunControl.class).getExecutionContexts(contDmc,
                new DataRequestMonitor<IExecutionDMContext[]>(getSession().getExecutor(), null){
                    @Override
                    public void handleCompleted() {
                        if (!getStatus().isOK()) {
                            handleFailedUpdate(update);
                            return;
                        }
                        fillUpdateWithVMCs(update, getData());
                        update.done();
                    }
                });
    }

    @Override
    protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
            if (!checkService(IRunControl.class, null, update)) continue;
            if (!checkService(INativeProcesses.class, null, update)) continue;

            final IExecutionDMContext dmc = findDmcInPath(update.getElementPath(), IExecutionDMContext.class);

            INativeProcesses processes = getServicesTracker().getService(INativeProcesses.class);

            String imageKey = null;
            if (getServicesTracker().getService(IRunControl.class).isSuspended(dmc)) {
                imageKey = IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
            }
            else {
                imageKey = IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
            }
            update.setImageDescriptor(DebugUITools.getImageDescriptor(imageKey), 0);

            // Find the Reason for the State
            final StringBuilder reason = new StringBuilder();
            getServicesTracker().getService(IRunControl.class).getExecutionData(dmc, 
                    new DataRequestMonitor<IExecutionDMData>(getSession().getExecutor(), null) { 
                @Override
                public void handleCompleted(){
                    if (!getStatus().isOK()) {
                        update.done();
                        return;
                    }
                    if(getData().getStateChangeReason() != null){
                        reason.append(": " + getData().getStateChangeReason() ); //$NON-NLS-1$
                    }    
                }
            });

            getServicesTracker().getService(INativeProcesses.class).getThreadData(
                    processes.getThreadForDebugContext(dmc), 
                    new DataRequestMonitor<IThreadDMData>(getSession().getExecutor(), null) { 
                        @Override
                        public void handleCompleted() {
                            if (!getStatus().isOK()) {
                                update.done();
                                return;
                            }
                            final StringBuilder builder = new StringBuilder("Thread["); //$NON-NLS-1$
                            builder.append(((TCFDSFExecutionDMC)dmc).getTcfContextId());
                            builder.append("] "); //$NON-NLS-1$
                            builder.append(getData().getId());
                            builder.append(getData().getName());
                            if(getServicesTracker().getService(IRunControl.class).isSuspended(dmc))
                                builder.append(" (Suspended"); //$NON-NLS-1$
                            else
                                builder.append(" (Running"); //$NON-NLS-1$
                            // Reason will be null before ContainerSuspendEvent is fired
                            if(reason.length() > 0 )
                                builder.append(reason);
                            builder.append(")"); //$NON-NLS-1$
                            update.setLabel(builder.toString(), 0);
                            update.done();
                        }
                    });
        }
    }

    @Override
    protected int getNodeDeltaFlagsForDMEvent(IDMEvent<?> e) {
        if(e instanceof IRunControl.IContainerResumedDMEvent || e instanceof IRunControl.IContainerSuspendedDMEvent || e instanceof IStartedDMEvent || e instanceof IExitedDMEvent) {
            return IModelDelta.CONTENT;
        }
        if(e instanceof IRunControl.IResumedDMEvent || e instanceof IRunControl.ISuspendedDMEvent) {
            return IModelDelta.STATE;
        } 
        return IModelDelta.NO_CHANGE;
    }

    @Override
    protected void buildDeltaForDMEvent(final IDMEvent<?> e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {
        if(e instanceof IRunControl.IContainerResumedDMEvent || e instanceof IRunControl.IContainerSuspendedDMEvent) {
            // Since IContainerDMContext sub-classes IExecutionDMContext, container 
            // events require special processing:  
            // Retrieve all the thread elements and mark their state as changed.  
            // Then pass these elements to the child layout nodes for processing
            final Map<IVMLayoutNode,Integer> childNodeDeltas = getChildNodesWithDeltaFlags(e);
            if (childNodeDeltas.size() == 0) {
                // There are no child nodes with deltas, just return to parent.
                requestMonitor.done();
                return;
            }            

            // Calculate the index of this node by retrieving all the 
            // elements and then finding the DMC that the event is for.  
            updateElements(new ElementsUpdate(
                    new DataRequestMonitor<List<Object>>(getExecutor(), null) {
                        @Override
                        protected void handleCompleted() {
                            if (isDisposed()) return;

                            // Check for an empty list of elements.  If it's empty then we 
                            // don't have to call the children nodes, so return here.
                            // No need to propagate error, there's no means or need to display it.
                            if (!getStatus().isOK() || getData().isEmpty()) {
                                requestMonitor.done();
                                return;
                            }

                            for (int i = 0; i < getData().size(); i++) {
                                IVMContext vmc = (IVMContext)getData().get(i);
                                VMDelta delta = parentDelta.addNode(vmc, nodeOffset + i, IModelDelta.STATE);
                                callChildNodesToBuildDelta(childNodeDeltas, delta, e, requestMonitor);
                                if (vmc.equals(getData().get(i))) break;
                            }                            
                        }
                    }, 
                    parentDelta));
            return;
        }
        else if (e instanceof IRunControl.IResumedDMEvent || e instanceof IRunControl.ISuspendedDMEvent) {
            parentDelta.addNode(new DMVMContext(e.getDMContext()), IModelDelta.STATE);
            super.buildDeltaForDMEvent(e, parentDelta, nodeOffset, requestMonitor);
        }
        else {
            super.buildDeltaForDMEvent(e, parentDelta, nodeOffset, requestMonitor);
        }            
    }
}
