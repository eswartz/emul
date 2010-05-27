/*******************************************************************************
 * Copyright (c) 2010 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.dsf.ui.actions;

import org.eclipse.dd.dsf.service.DsfSession;
import org.eclipse.debug.core.commands.IDebugCommandRequest;
import org.eclipse.debug.core.commands.IEnabledStateRequest;
import org.eclipse.debug.core.commands.ITerminateHandler;

public class TcfTerminateCommand implements ITerminateHandler {

    public TcfTerminateCommand(DsfSession session) {

    }

    public void dispose() {

    }

    public void canExecute(IEnabledStateRequest request) {
        // TODO Auto-generated method stub

    }

    public boolean execute(IDebugCommandRequest request) {
        // TODO Auto-generated method stub
        return false;
    }
}
