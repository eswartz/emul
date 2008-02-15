/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.tcf.dsf.core.services;

import java.util.HashMap;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.eclipse.dd.dsf.concurrent.DsfRunnable;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.datamodel.DMContexts;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl;
import org.eclipse.dd.dsf.debug.service.IStepQueueManager;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IResumedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.ISuspendedDMEvent;
import org.eclipse.dd.dsf.debug.service.IRunControl.StateChangeReason;
import org.eclipse.dd.dsf.debug.service.IRunControl.StepType;
import org.eclipse.dd.dsf.service.AbstractDsfService;
import org.eclipse.dd.dsf.service.DsfServiceEventHandler;
import org.eclipse.dd.dsf.service.DsfSession;
import org.osgi.framework.BundleContext;

import com.windriver.tcf.dsf.core.Activator;

public class TCFDSFStepQueueManager extends AbstractDsfService
implements IStepQueueManager{

    private static class StepRequest {
        StepType fStepType;
        boolean fIsInstructionStep;
        StepRequest(StepType type, boolean instruction) {
            fStepType = type;
            fIsInstructionStep = instruction;
        }
    }

    private IRunControl fRunControl;
    private int fQueueDepth = 3;
    private Map<IExecutionDMContext,List<StepRequest>> fStepQueues = new HashMap<IExecutionDMContext,List<StepRequest>>();
    private Map<IExecutionDMContext,Boolean> fTimedOutFlags = new HashMap<IExecutionDMContext,Boolean>();
    private Map<IExecutionDMContext,ScheduledFuture<?>> fTimedOutFutures = new HashMap<IExecutionDMContext,ScheduledFuture<?>>();

    public TCFDSFStepQueueManager(DsfSession session) {
        super(session);
    }

    ///////////////////////////////////////////////////////////////////////////
    // IDsfService
    @Override
    public void initialize(final RequestMonitor requestMonitor) {
        super.initialize(
                new RequestMonitor(getExecutor(), requestMonitor) { 
                    @Override
                    protected void handleOK() {
                        doInitialize(requestMonitor);
                    }});
    }

    private void doInitialize(final RequestMonitor requestMonitor) {
        fRunControl = getServicesTracker().getService(IRunControl.class);

        getSession().addServiceEventListener(this, null);
        register(new String[]{IStepQueueManager.class.getName()}, new Hashtable<String,String>());
        requestMonitor.done();
    }

    @Override
    public void shutdown(final RequestMonitor requestMonitor) {
        unregister();
        getSession().removeServiceEventListener(this);
        super.shutdown(requestMonitor);
    }

    ///////////////////////////////////////////////////////////////////////////
    // AbstractService    
    @Override
    protected BundleContext getBundleContext() {
        return Activator.getBundleContext();
    }

    ///////////////////////////////////////////////////////////////////////////
    // IStepQueueManager
    public boolean canEnqueueStep(IDMContext ctx) {
        IExecutionDMContext execCtx = DMContexts.getAncestorOfType(ctx, IExecutionDMContext.class);
        return execCtx != null && 
        ( (fRunControl.isSuspended(execCtx) && fRunControl.canStep(execCtx)) || 
                (fRunControl.isStepping(execCtx) && !isSteppingTimedOut(execCtx)) );
    }

    // IStepQueueManager
    public boolean canEnqueueInstructionStep(IDMContext ctx) {
        IExecutionDMContext execCtx = DMContexts.getAncestorOfType(ctx, IExecutionDMContext.class);
        return execCtx != null && 
        ( (fRunControl.isSuspended(execCtx) && fRunControl.canInstructionStep(execCtx)) || 
                (fRunControl.isStepping(execCtx) && !isSteppingTimedOut(execCtx)) );
    }

    public int getPendingStepCount(IDMContext execCtx) {
        List<StepRequest> stepQueue = fStepQueues.get(execCtx);
        if (stepQueue == null) return 0;
        return stepQueue.size();
    }

    public void enqueueStep(IDMContext ctx, StepType stepType) {
        IExecutionDMContext execCtx = DMContexts.getAncestorOfType(ctx, IExecutionDMContext.class);
        if (execCtx != null) {
            if (fRunControl.canStep(execCtx)) {
                fRunControl.step(execCtx, stepType, new RequestMonitor(getExecutor(), null)); 
            } else if (canEnqueueStep(execCtx)) {
                List<StepRequest> stepQueue = fStepQueues.get(execCtx);
                if (stepQueue == null) {
                    stepQueue = new LinkedList<StepRequest>();
                    fStepQueues.put(execCtx, stepQueue);
                }
                if (stepQueue.size() < fQueueDepth) {
                    stepQueue.add(new StepRequest(stepType, false));
                }
            }
        }
    }

    public void enqueueInstructionStep(IDMContext ctx, StepType stepType) {
        IExecutionDMContext execCtx = DMContexts.getAncestorOfType(ctx, IExecutionDMContext.class);
        if (execCtx != null) {
            if (fRunControl.canInstructionStep(execCtx)) {
                fRunControl.instructionStep(execCtx, stepType, new RequestMonitor(getExecutor(), null)); 
            }
            else if (canEnqueueInstructionStep(execCtx)) {
                List<StepRequest> stepQueue = fStepQueues.get(execCtx);
                if (stepQueue == null) {
                    stepQueue = new LinkedList<StepRequest>();
                    fStepQueues.put(execCtx, stepQueue);
                }
                if (stepQueue.size() < fQueueDepth) {
                    stepQueue.add(new StepRequest(stepType, true));
                }
            }
        }
    }

    public boolean isSteppingTimedOut(IDMContext context) {
        IExecutionDMContext execCtx = DMContexts.getAncestorOfType(context, IExecutionDMContext.class);
        if (execCtx != null) {
            return fTimedOutFlags.containsKey(execCtx) ? fTimedOutFlags.get(execCtx) : false;
        } 
        return false;
    }


    public int getStepQueueDepth() { return fQueueDepth; }
    public void setStepQueueDepth(int depth) { fQueueDepth = depth; }

    ///////////////////////////////////////////////////////////////////////////

    @DsfServiceEventHandler 
    public void eventDispatched(ISuspendedDMEvent e) {
        // Take care of the stepping time out
        fTimedOutFlags.remove(e.getDMContext());
        ScheduledFuture<?> future = fTimedOutFutures.remove(e.getDMContext()); 
        if (future != null) future.cancel(false);

        // Check if there's a step pending, if so execute it
        if (fStepQueues.containsKey(e.getDMContext())) {
            List<StepRequest> queue = fStepQueues.get(e.getDMContext());
            StepRequest request = queue.remove(queue.size() - 1);
            if (queue.isEmpty()) fStepQueues.remove(e.getDMContext());
            if (request.fIsInstructionStep) {
                if (fRunControl.canInstructionStep(e.getDMContext())) {
                    fRunControl.instructionStep(
                            e.getDMContext(), request.fStepType, new RequestMonitor(getExecutor(), null));
                } else {
                    // For whatever reason we can't step anymore, so clear out
                    // the step queue.
                    fStepQueues.remove(e.getDMContext());
                }
            } else {
                if (fRunControl.canStep(e.getDMContext())) {
                    fRunControl.step(e.getDMContext(), request.fStepType,new RequestMonitor(getExecutor(), null));
                } else {
                    // For whatever reason we can't step anymore, so clear out
                    // the step queue.
                    fStepQueues.remove(e.getDMContext());
                }
            }
        }
    }

    @DsfServiceEventHandler 
    public void eventDispatched(final IResumedDMEvent e) {
        if (e.getReason().equals(StateChangeReason.STEP)) {
            fTimedOutFlags.put(e.getDMContext(), Boolean.FALSE);
            // We shouldn't have a stepping timeout running unless we get two 
            // stepping events in a row without a suspended, which would be a 
            // protocol error.
            assert !fTimedOutFutures.containsKey(e.getDMContext());
            fTimedOutFutures.put(
                    e.getDMContext(), 
                    getExecutor().schedule(
                            new DsfRunnable() { public void run() {
                                fTimedOutFutures.remove(e.getDMContext());

                                // Issue the stepping time-out event.
                                getSession().dispatchEvent(
                                        new ISteppingTimedOutEvent() { 
                                            public IExecutionDMContext getDMContext() { return e.getDMContext(); }
                                        }, 
                                        getProperties());
                            }},
                            STEPPING_TIMEOUT, TimeUnit.MILLISECONDS)
            );

        } 
    }    

    @DsfServiceEventHandler 
    public void eventDispatched(ISteppingTimedOutEvent e) {
        fTimedOutFlags.put(e.getDMContext(), Boolean.TRUE);
    }
}
