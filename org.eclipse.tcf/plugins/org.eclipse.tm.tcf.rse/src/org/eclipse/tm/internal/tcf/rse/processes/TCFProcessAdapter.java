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
package org.eclipse.tm.internal.tcf.rse.processes;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.rse.services.clientserver.processes.IHostProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IHostProcessToRemoteProcessAdapter;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcess;
import org.eclipse.rse.subsystems.processes.core.subsystem.IRemoteProcessContext;

public class TCFProcessAdapter implements IHostProcessToRemoteProcessAdapter {

    public IRemoteProcess convertToRemoteProcess(IRemoteProcessContext context,
            IRemoteProcess parent, IHostProcess node) {
        IHostProcess[] nodes = new IHostProcess[]{ node };
        IRemoteProcess[] processes = convertToRemoteProcesses(context, parent, nodes);
        if (processes != null && processes.length > 0) return processes[0];
        return null;
    }

    public IRemoteProcess[] convertToRemoteProcesses(
            IRemoteProcessContext context, IRemoteProcess parent,
            IHostProcess[] nodes) {

        if (nodes == null)return null;
        List<IRemoteProcess> list = new ArrayList<IRemoteProcess>(nodes.length);
        for (int idx = 0; idx < nodes.length; idx++) {
            TCFProcessResource node = (TCFProcessResource)nodes[idx];
            if (node != null) list.add(new TCFRemoteProcess(context, node));
        }
        return list.toArray(new IRemoteProcess[list.size()]);
    }
}
