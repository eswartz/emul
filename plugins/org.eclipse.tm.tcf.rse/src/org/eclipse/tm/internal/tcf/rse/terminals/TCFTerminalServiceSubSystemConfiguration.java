/*******************************************************************************
 * Copyright (c) 2008, 2010 MontaVista Software, Inc. and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * Yu-Fen Kuo      (MontaVista) - initial API and implementation
 * Anna Dushistova (MontaVista) - [240530][rseterminal][apidoc] Add terminals.rse Javadoc into org.eclipse.rse.doc.isv
 * Liping Ke      (Intel Corp.) - Adapted from TerminalServiceSubSystemConfiguration
 * Liping Ke      (Intel Corp.) - TCF terminal services subsystem implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.rse.terminals;

import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.IConnectorService;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.services.terminals.ITerminalService;
import org.eclipse.rse.subsystems.terminals.core.TerminalServiceSubSystem;
import org.eclipse.rse.subsystems.terminals.core.TerminalServiceSubSystemConfiguration;
import org.eclipse.tm.internal.tcf.rse.ITCFService;
import org.eclipse.tm.internal.tcf.rse.TCFConnectorService;
import org.eclipse.tm.internal.tcf.rse.TCFConnectorServiceManager;

public class TCFTerminalServiceSubSystemConfiguration extends
        TerminalServiceSubSystemConfiguration {


   /**
    * Instantiate and return an instance of OUR subsystem. Do not populate it
    * yet though!
    *
    * @see org.eclipse.rse.core.subsystems.SubSystemConfiguration#createSubSystemInternal(IHost)
    */
   public ISubSystem createSubSystemInternal(IHost host) {
       TCFConnectorService connectorService = (TCFConnectorService) getConnectorService(host);
       ISubSystem subsys = new TerminalServiceSubSystem(host,
               connectorService, createTerminalService(host));
       return subsys;
   }

       /**
        * @inheritDoc
        * @since 1.0
        */
   public ITerminalService createTerminalService(IHost host) {
       TCFConnectorService cserv = (TCFConnectorService) getConnectorService(host);
               return new TCFTerminalService(cserv);
   }

   public IConnectorService getConnectorService(IHost host) {
       return TCFConnectorServiceManager.getInstance().getConnectorService(
               host, getServiceImplType());
   }

   public void setConnectorService(IHost host,
           IConnectorService connectorService) {
       TCFConnectorServiceManager.getInstance().setConnectorService(host,
               getServiceImplType(), connectorService);
   }

   public Class<ITCFService> getServiceImplType() {
       return ITCFService.class;
   }

}
