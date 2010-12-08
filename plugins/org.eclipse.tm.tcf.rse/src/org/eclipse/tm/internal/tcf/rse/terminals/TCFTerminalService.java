/*******************************************************************************
 * Copyright (c) 2010 Intel Corporation. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Liping Ke(Intel Corp.) - initial API and implementation
 ******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.terminals;

import org.eclipse.core.runtime.IProgressMonitor;

import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.services.terminals.AbstractTerminalService;
import org.eclipse.rse.services.terminals.ITerminalShell;
import org.eclipse.tm.internal.tcf.rse.ITCFService;
import org.eclipse.tm.internal.tcf.rse.ITCFSessionProvider;
import org.eclipse.tm.internal.tcf.rse.Messages;
import org.eclipse.tm.internal.tcf.rse.shells.TCFTerminalShell;

public class TCFTerminalService extends AbstractTerminalService implements ITCFService{
    private final ITCFSessionProvider fSessionProvider;    

    /**
     * Return the TCF property set, and fill it with default values if it has
     * not been created yet. Extender may override in order to set different
     * default values.
     *
     * @return a property set holding properties understood by the TCF
     *         connector service.
     */
    public ITerminalShell launchTerminal(String ptyType, String encoding,
            String[] environment, String initialWorkingDirectory,
            String commandToRun, IProgressMonitor monitor)
            throws SystemMessageException {
        TCFTerminalShell hostShell = new TCFTerminalShell(fSessionProvider, ptyType, encoding, environment, initialWorkingDirectory, commandToRun);
        return hostShell;
    }


    public TCFTerminalService(ITCFSessionProvider sessionProvider) {
        fSessionProvider = sessionProvider;
    }

    public ITCFSessionProvider getSessionProvider() {
                return fSessionProvider;
        }
    @Override
    public String getName() {
        return Messages.TCFTerminalService_Name;
    }
    @Override
    public String getDescription() {
        return Messages.TCFTerminalService_Description;
    }
}
