/*******************************************************************************
 * Copyright (c) 2007, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.ui.commands;

import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IDisconnectHandler;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFModel;
import org.eclipse.tm.internal.tcf.debug.ui.model.TCFRunnable;


public class DisconnectCommand implements IDisconnectHandler {

    private final TCFModel model;

    public DisconnectCommand(TCFModel model) {
        this.model = model;
    }

    public void canExecute(final IEnabledStateRequest monitor) {
        new TCFRunnable(monitor) {
            public void run() {
                monitor.setEnabled(model.getLaunch().canDisconnect());
                monitor.setStatus(Status.OK_STATUS);
                done();
            }
        };
    }

    public boolean execute(final IDebugCommandRequest monitor) {
        new TCFRunnable(monitor) {
            public void run() {
                try {
                    model.getLaunch().disconnect();
                    monitor.setStatus(Status.OK_STATUS);
                }
                catch (DebugException x) {
                    monitor.setStatus(x.getStatus());
                }
                done();
            }
        };
        return false;
    }
}
