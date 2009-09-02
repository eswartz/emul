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
package org.eclipse.tm.internal.tcf.dsf.launch;

import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.debug.service.StepQueueManager;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFBreakpoints;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFMemory;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFRegisters;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFRunControl;
import org.eclipse.tm.internal.tcf.dsf.services.TCFDSFStack;
import org.eclipse.tm.tcf.protocol.IChannel;


class TCFDSFLaunchSequence extends Sequence {

    private final Step[] steps;

    TCFDSFLaunchSequence(final DsfSession session, final TCFDSFLaunch launch, RequestMonitor monitor) {
        super(session.getExecutor(), monitor);
        final IChannel channel = launch.getChannel();
        steps = new Step[] {
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        new TCFDSFRunControl(launch.getLaunchConfiguration(), launch, session, channel, monitor);
                    }
                },
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        new StepQueueManager(session).initialize(monitor);
                    }
                },
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        new TCFDSFStack(session, channel, monitor);
                    }
                },
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        new TCFDSFMemory(session, channel, monitor);
                    }
                },
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        new TCFDSFRegisters(session, channel, monitor);
                    }
                },
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        new TCFDSFBreakpoints(session, launch, monitor);
                    }
                },
        };
    }

    @Override
    public Step[] getSteps() {
        return steps;
    }
}
