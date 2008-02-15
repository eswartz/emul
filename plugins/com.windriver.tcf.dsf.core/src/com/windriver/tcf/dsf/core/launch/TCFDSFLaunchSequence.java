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
package com.windriver.tcf.dsf.core.launch;

import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.concurrent.Sequence;
import org.eclipse.dd.dsf.service.DsfSession;

import com.windriver.tcf.api.protocol.IChannel;
import com.windriver.tcf.dsf.core.services.TCFDSFBreakpoints;
import com.windriver.tcf.dsf.core.services.TCFDSFMemory;
import com.windriver.tcf.dsf.core.services.TCFDSFNativeProcesses;
import com.windriver.tcf.dsf.core.services.TCFDSFRegisters;
import com.windriver.tcf.dsf.core.services.TCFDSFRunControl;
import com.windriver.tcf.dsf.core.services.TCFDSFStack;
import com.windriver.tcf.dsf.core.services.TCFDSFStepQueueManager;

class TCFDSFLaunchSequence extends Sequence {

    private final Step[] steps;

    TCFDSFLaunchSequence(final DsfSession session, final TCFDSFLaunch launch, RequestMonitor monitor) {
        super(session.getExecutor(), monitor);
        final IChannel channel = launch.getChannel();
        steps = new Step[] {
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        new TCFDSFNativeProcesses(session, channel, monitor);
                    }
                },
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        new TCFDSFRunControl(session, channel, monitor);
                    }
                },
                new Step() {
                    @Override
                    public void execute(RequestMonitor monitor) {
                        new TCFDSFStepQueueManager(session).initialize(monitor);
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
