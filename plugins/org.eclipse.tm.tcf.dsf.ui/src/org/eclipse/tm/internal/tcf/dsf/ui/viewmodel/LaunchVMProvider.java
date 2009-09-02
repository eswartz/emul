/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.dsf.ui.viewmodel;

import java.util.concurrent.RejectedExecutionException;

import org.eclipse.dd.dsf.concurrent.ThreadSafe;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.LaunchRootVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.StackFramesVMNode;
import org.eclipse.dd.dsf.debug.internal.provisional.ui.viewmodel.launch.LaunchRootVMNode.LaunchesEvent;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.dd.dsf.ui.viewmodel.AbstractVMAdapter;
import org.eclipse.dd.dsf.ui.viewmodel.IRootVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.IVMNode;
import org.eclipse.dd.dsf.ui.viewmodel.datamodel.AbstractDMVMProvider;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchesListener2;
import org.eclipse.debug.internal.ui.viewers.model.provisional.IPresentationContext;


@SuppressWarnings("restriction")
public class LaunchVMProvider extends AbstractDMVMProvider
implements IDebugEventSetListener, ILaunchesListener2 {

    @ThreadSafe
    public LaunchVMProvider(AbstractVMAdapter adapter,
            IPresentationContext presentationContext,
            DsfSession session, ILaunch launch) {
        super(adapter, presentationContext, session);

        IRootVMNode launch_node = new LaunchRootVMNode(this);
        setRootNode(launch_node);

        IVMNode threads_node = new ExecutableContextLayoutNode(this, getSession());
        addChildNodes(launch_node, new IVMNode[] { threads_node });

        IVMNode stack_frames_node = new StackFramesVMNode(this, getSession());
        addChildNodes(threads_node, new IVMNode[] { stack_frames_node, threads_node });

        DebugPlugin.getDefault().addDebugEventListener(this);
        DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
    }


    public void handleDebugEvents(final DebugEvent[] events) {
        if (isDisposed()) return;

        // We're in session's executor thread.  Re-dispatch to VM Adapter
        // executor thread and then call root layout node.
        try {
            getExecutor().execute(new Runnable() {
                public void run() {
                    if (isDisposed()) return;

                    for (final DebugEvent event : events) {
                        handleEvent(event);
                    }
                }
            });
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

                    IRootVMNode rootLayoutNode = getRootVMNode();
                    if (rootLayoutNode != null && rootLayoutNode.getDeltaFlags(event) != 0) {
                        handleEvent(event);
                    }
                }});
        }
        catch (RejectedExecutionException e) {
            // Ignore.  This exception could be thrown if the provider is being
            // shut down.
        }
    }
}
