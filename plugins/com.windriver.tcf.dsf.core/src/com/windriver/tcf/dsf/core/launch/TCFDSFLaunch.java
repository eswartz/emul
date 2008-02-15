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

import org.eclipse.dd.dsf.concurrent.DsfExecutor;
import org.eclipse.dd.dsf.concurrent.RequestMonitor;
import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.debug.core.ILaunchConfiguration;

import com.windriver.debug.tcf.core.model.ITCFConstants;
import com.windriver.debug.tcf.core.model.TCFLaunch;
import com.windriver.tcf.api.protocol.IChannel;

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
                        protected void handleOK() {
                            done.run();
                        }
                    };
                    TCFDSFLaunchSequence seq = new TCFDSFLaunchSequence(session, TCFDSFLaunch.this, monitor);
                    executor.execute(seq);
                }
                else {
                    done.run();
                }
            }
        });
    }

    public DsfExecutor getDsfExecutor() {
        return executor;
    }

    public DsfSession getSession() {
        return session;
    }
}
