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

package com.windriver.tcf.dsf.ui;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.debug.service.INativeProcesses;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.INativeProcesses.IProcessDMData;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMProvider;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMLayoutNode;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IChildrenUpdate;
import org.eclipse.debug.internal.ui.viewers.model.provisional.ILabelUpdate;
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.debug.ui.IDebugUIConstants;

import com.windriver.tcf.dsf.core.services.TCFDSFRunControl;

@SuppressWarnings("restriction")
public class ContainerLayoutNode extends AbstractDMVMLayoutNode{

    public ContainerLayoutNode(AbstractVMProvider provider, DsfSession session) {
        super(provider, session, IRunControl.IExecutionDMContext.class);
    }

    @Override
    protected void updateElementsInSessionThread(IChildrenUpdate update) {
        if (!checkService(IRunControl.class, null, update)) return;

        IContainerDMContext containerCtx = getServicesTracker().getService(TCFDSFRunControl.class).getContainerDMC();
        update.setChild(new DMVMContext(containerCtx), 0); 
        update.done();
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
            } else {
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
}
