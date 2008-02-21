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

package org.eclipse.tm.internal.tcf.dsf.ui;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.IDMEvent;
import org.eclipse.dd.dsf.debug.service.INativeProcesses;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.INativeProcesses.IProcessDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExitedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.IStartedDMEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.VMDelta;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFRunControl;


@SuppressWarnings("restriction")
public class ContainerLayoutNode extends AbstractDMVMLayoutNode{

    public ContainerLayoutNode(AbstractVMProvider provider, DsfSession session) {
        super(provider, session, IRunControl.IExecutionDMContext.class);
    }

    @Override
    protected void updateElementsInSessionThread(final IChildrenUpdate update) {
        if (!checkService(IRunControl.class, null, update)) return;
        final IContainerDMContext contDmc = findDmcInPath(
                update.getElementPath().getParentPath(), IContainerDMContext.class);

        getServicesTracker().getService(TCFDSFRunControl.class).getContainerContexts(contDmc,
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
    // Labels are only updated for elements that are visible.
    protected void updateLabelInSessionThread(ILabelUpdate[] updates) {
        for (final ILabelUpdate update : updates) {
            if (!checkService(IRunControl.class, null, update)) continue;
            if (!checkService(INativeProcesses.class, null, update)) continue;

            final IContainerDMContext dmc = findDmcInPath(update.getElementPath(), IContainerDMContext.class);

            INativeProcesses processes = getServicesTracker().getService(INativeProcesses.class);

            String imageKey = null;

            if (getServicesTracker().getService(IRunControl.class).isSuspended(dmc)) {
                imageKey = IDebugUIConstants.IMG_OBJS_THREAD_SUSPENDED;
            }
            else {
                imageKey = IDebugUIConstants.IMG_OBJS_THREAD_RUNNING;
            }
            update.setImageDescriptor(DebugUITools.getImageDescriptor(imageKey), 0);

            processes.getProcessData(
                    processes.getProcessForDebugContext(dmc), 
                    new DataRequestMonitor<IProcessDMData>(getSession().getExecutor(), null) { 
                        @SuppressWarnings("restriction")
                        @Override
                        public void handleCompleted() {
                            if (!getStatus().isOK()) {
                                update.done();
                                return;
                            }
                            update.setLabel(getData().getName(), 0);
                            update.done();
                        }
                    });
        }
    }

    @Override
    protected int getNodeDeltaFlagsForDMEvent(IDMEvent<?> e) {
        if (e instanceof IStartedDMEvent || e instanceof IExitedDMEvent) {
            return IModelDelta.CONTENT;
        }
        if (e instanceof IRunControl.IContainerResumedDMEvent || e instanceof IRunControl.IContainerSuspendedDMEvent) {
            return IModelDelta.STATE;
        } 
        return IModelDelta.NO_CHANGE;
    }

    @Override
    protected void buildDeltaForDMEvent(final IDMEvent<?> e, final VMDelta parentDelta, final int nodeOffset, final RequestMonitor requestMonitor) {

        if (e instanceof IRunControl.IContainerResumedDMEvent || e instanceof IRunControl.IContainerSuspendedDMEvent) {
            parentDelta.addNode(new DMVMContext(e.getDMContext()), IModelDelta.STATE);
        }
        if (e instanceof IStartedDMEvent || e instanceof IExitedDMEvent) {
            parentDelta.addNode(new DMVMContext(e.getDMContext()), IModelDelta.CONTENT);
        }            
        super.buildDeltaForDMEvent(e, parentDelta, nodeOffset, requestMonitor);
    }
}
