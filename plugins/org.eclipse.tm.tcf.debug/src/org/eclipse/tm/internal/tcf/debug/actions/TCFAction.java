/*******************************************************************************
 * Copyright (c) 2008, 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.debug.actions;

import org.eclipse.tm.internal.tcf.debug.model.TCFLaunch;
import org.eclipse.tm.tcf.protocol.Protocol;

/**
 * TCFAction class represents user request to perform some action(s) on
 * a remote context, for example, step over line command.
 * Such action might require multiple data exchanges with remote target.
 * Actions for a particular context should be executed sequentially -
 * it does not make sense to execute two step commands concurrently.
 * If user requests actions faster then they are executed,
 * actions are placed into a FIFO queue.
 *
 * Clients are expected to implement run() method to perform the action job.
 * When the job is done, client code should call done() method.
 */
public abstract class TCFAction implements Runnable {

    protected final TCFLaunch launch;
    protected final String ctx_id;

    public TCFAction(TCFLaunch launch, String ctx_id) {
        assert Protocol.isDispatchThread();
        this.launch = launch;
        this.ctx_id = ctx_id;
        launch.addContextAction(this);
    }

    public String getContextID() {
        return ctx_id;
    }

    public void setActionResult(String id, String result) {
        launch.setContextActionResult(id, result);
    }

    public void done() {
        assert Protocol.isDispatchThread();
        launch.removeContextAction(this);
    }
}
