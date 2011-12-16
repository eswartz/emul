/*******************************************************************************
 *  Copyright (c) 2006, 2010 IBM Corporation and others.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Initial Contributors:
 *  The following IBM employees contributed to the Remote System Explorer
 *  component that contains this file: David McKnight, Kushal Munir,
 *  Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson,
 *  Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 *
 *  Contributors:
 *  Martin Oberhuber (Wind River) - Adapted template for ssh service.
 *  Anna Dushistova  (MontaVista) - [259414][api] refactor the "SSH Shell" to use the generic Terminal->IHostShell converter
 *  Liping Ke        (Intel Corp.)- Adapted from org.eclipse.rse.subsystems.shells.ssh.SshShellSubSystemConfiguration
 *  Liping Ke        (Intel Corp.)- [246987] Implement TCF Shell/terminal services
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.shells;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.shells.IHostShell;
import org.eclipse.rse.services.shells.IShellService;
import org.eclipse.rse.subsystems.shells.core.subsystems.IRemoteCmdSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.IServiceCommandShell;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystem;
import org.eclipse.rse.subsystems.shells.core.subsystems.servicesubsystem.ShellServiceSubSystemConfiguration;
import org.eclipse.tm.internal.tcf.rse.*;
import org.eclipse.tm.internal.tcf.rse.terminals.TCFTerminalService;


public class TCFShellSubSystemConfiguration extends
        ShellServiceSubSystemConfiguration {

    public TCFShellSubSystemConfiguration() {
        super();
    }

    /**
     * Instantiate and return an instance of OUR subsystem.
     * Do not populate it yet though!
     * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(IHost)
     */
    public ISubSystem createSubSystemInternal(IHost host)
    {
            TCFConnectorService connectorService = (TCFConnectorService)getConnectorService(host);
            ISubSystem subsys = new ShellServiceSubSystem(host, connectorService, createShellService(host));
            return subsys;
    }

    public IConnectorService getConnectorService(IHost host) {
        return TCFConnectorServiceManager.getInstance().getConnectorService(host, getServiceImplType());
}

    public void setConnectorService(IHost host,
                IConnectorService connectorService) {
        TCFConnectorServiceManager.getInstance().setConnectorService(host, getServiceImplType(), connectorService);
    }

    public Class<ITCFService> getServiceImplType()
    {
            return ITCFService.class;
    }

    public IServiceCommandShell createRemoteCommandShell(IRemoteCmdSubSystem cmdSS, IHostShell hostShell) {
        return new TCFServiceCommandShell(cmdSS, hostShell);
    }

    public IShellService createShellService(IHost host) {
        TCFConnectorService cserv = (TCFConnectorService)getConnectorService(host);

        return (IShellService) (new TCFTerminalService(cserv)).getAdapter(IShellService.class);
    }


}
