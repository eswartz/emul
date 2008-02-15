/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.tcf.dsf.ui;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.DataRequestMonitor;
import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.debug.ui.viewmodel.launch.StackFramesLayoutNode;
import org.eclipse.dd.dsf.debug.ui.viewmodel.launch.StandardLaunchRootLayoutNode;
import org.eclipse.dd.dsf.debug.ui.viewmodel.launch.StandardProcessLayoutNode;
import org.eclipse.dd.dsf.debug.ui.viewmodel.launch.StandardLaunchRootLayoutNode.LaunchesEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IVMLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMRootLayoutNode;
import org.eclipse.dd.dsf.ui.viewmodel.dm.AbstractDMVMProvider;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IModelDelta;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;


@SuppressWarnings("restriction")
public class LaunchVMProvider extends AbstractDMVMProvider 
implements IDebugEventSetListener, ILaunchesListener2 {

    @ThreadSafe
    public LaunchVMProvider(AbstractVMAdapter adapter, IPresentationContext presentationContext, 
            DsfSession session, ILaunch launch) 
    {
        super(adapter, presentationContext, session);

        IVMRootLayoutNode launchNode = new StandardLaunchRootLayoutNode(this, launch);
        // Container node to contain all processes and threads
        IVMLayoutNode containerNode = new ContainerLayoutNode(this, getSession());
        IVMLayoutNode processesNode = new StandardProcessLayoutNode(this);
        launchNode.setChildNodes(new IVMLayoutNode[] { containerNode, processesNode});

        IVMLayoutNode threadsNode = new ThreadLayoutNode(this, getSession());
        containerNode.setChildNodes(new IVMLayoutNode[] { threadsNode });

        IVMLayoutNode stackFramesNode = new StackFramesLayoutNode(this, getSession());
        threadsNode.setChildNodes(new IVMLayoutNode[] { stackFramesNode });

        setRootLayoutNode(launchNode);

        DebugPlugin.getDefault().addDebugEventListener(this);
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
    }


    public void handleDebugEvents(final DebugEvent[] events) {
        if (isDisposed()) return;

        // We're in session's executor thread.  Re-dispach to VM Adapter 
        // executor thread and then call root layout node.
        try {
            getExecutor().execute(new Runnable() {
                public void run() {
                    if (isDisposed()) return;

                    for (final DebugEvent event : events) {
                        IVMRootLayoutNode rootLayoutNode = getRootLayoutNode();
                        if (rootLayoutNode != null && rootLayoutNode.getDeltaFlags(event) != 0) {
                            rootLayoutNode.createDelta(
                                    event, 
                                    new DataRequestMonitor<IModelDelta>(getExecutor(), null) {
                                        @Override
                                        public void handleCompleted() {
                                            if (getStatus().isOK()) {
                                                getModelProxy().fireModelChangedNonDispatch(getData());
                                            }
                                        }
                                        @Override
                                        public String toString() {
                                            return "Result of a delta for debug event: '" + event.toString() +
                                            "' in VMP: '" + LaunchVMProvider.this + "'" +
                                            "\n" + getData();
                                        }
                                    });
                        }
                    }
                }});
        }
        catch (RejectedExecutionException e) {
            // Ignore.  This exception could be thrown if the provider is being 
            // shut down.  
        }
    }

    @Override
    public void dispose() {
        DebugPlugin.getDefault().removeDebugEventListener(this);
        DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
        super.dispose();
    }

    public void launchesAdded(ILaunch[] launches) {
        handleLaunchesEvent(new LaunchesEvent(launches, LaunchesEvent.Type.ADDED)); 
    }

    public void launchesRemoved(ILaunch[] launches) {
        handleLaunchesEvent(new LaunchesEvent(launches, LaunchesEvent.Type.REMOVED)); 
    }

    public void launchesChanged(ILaunch[] launches) {
        handleLaunchesEvent(new LaunchesEvent(launches, LaunchesEvent.Type.CHANGED)); 
    }

    public void launchesTerminated(ILaunch[] launches) {
        handleLaunchesEvent(new LaunchesEvent(launches, LaunchesEvent.Type.TERMINATED)); 
    }

    private void handleLaunchesEvent(final LaunchesEvent event) {
        if (isDisposed()) return;

        // We're in session's executor thread.  Re-dispach to VM Adapter 
        // executor thread and then call root layout node.
        try {
            getExecutor().execute(new Runnable() {
                public void run() {
                    if (isDisposed()) return;

                    IVMRootLayoutNode rootLayoutNode = getRootLayoutNode();
                    if (rootLayoutNode != null && rootLayoutNode.getDeltaFlags(event) != 0) {
                        rootLayoutNode.createDelta(
                                event, 
                                new DataRequestMonitor<IModelDelta>(getExecutor(), null) {
                                    @Override
                                    public void handleCompleted() {
                                        if (getStatus().isOK()) {
                                            getModelProxy().fireModelChangedNonDispatch(getData());
                                        }
                                    }
                                    @Override
                                    public String toString() {
                                        return "Result of a delta for launch event: '" + event.toString() +
                                        "' in VMP: '" + LaunchVMProvider.this + "'" +
                                        "\n" + getData();
                                    }
                                });
                    }
                }});
        }
        catch (RejectedExecutionException e) {
            // Ignore.  This exception could be thrown if the provider is being 
            // shut down.  
        }
    }
}
