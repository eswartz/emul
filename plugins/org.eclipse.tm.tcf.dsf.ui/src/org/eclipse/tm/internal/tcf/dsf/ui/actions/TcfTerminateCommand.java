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
