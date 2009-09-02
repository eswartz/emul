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

import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.tm.internal.tcf.debug.model.ITCFConstants;
import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.tcf.protocol.IChannel;


public class TCFDSFLaunch extends TCFLaunch {

    private final TCFDSFExecuter executor;
    private final DsfSession session;

    public TCFDSFLaunch(ILaunchConfiguration launchConfiguration, String mode) {
        super(launchConfiguration, mode);
        executor = new TCFDSFExecuter();
        session = DsfSession.startSession(executor, ITCFConstants.ID_TCF_DEBUG_MODEL);
    }

    @Override
    protected void runLaunchSequence(final Runnable done) {
        super.runLaunchSequence(new Runnable() {
            public void run() {
                IChannel channel = getChannel();
                if (channel != null) {
                    RequestMonitor monitor = new RequestMonitor(executor, null) {
                        @Override
                        protected void handleSuccess() {
                            done.run();
                        }
                    };
                    executor.execute(new TCFDSFLaunchSequence(session, TCFDSFLaunch.this, monitor));
                }
                else {
                    done.run();
                }
            }
        });
    }

    @Override
    protected void runShutdownSequence(final Runnable done) {
        RequestMonitor monitor = new RequestMonitor(executor, null) {
            @Override
            protected void handleSuccess() {
                TCFDSFLaunch.super.runShutdownSequence(done);
            }
        };
        executor.execute(new TCFDSFShutdownSequence(session, TCFDSFLaunch.this, monitor));
    }

    public DsfExecutor getDsfExecutor() {
        return executor;
    }

    public DsfSession getSession() {
        return session;
    }
}
