/*******************************************************************************
 * Copyright (c) 2008 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Anyware Technologies  - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.core;

import org.eclipse.tm.internal.tcf.services.local.DiagnosticsService;
import org.eclipse.tm.internal.tcf.services.remote.LocatorProxy;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IServiceProvider;
import org.eclipse.tm.tcf.protocol.Protocol;

public class StandardServiceProvider implements IServiceProvider {
    
    private final String package_name = LocatorProxy.class.getPackage().getName();
    
    public IService[] getLocalService(IChannel channel) {
        return new IService[]{ Protocol.getLocator(), new DiagnosticsService(channel) };
    }

    public IService getServiceProxy(IChannel channel, String service_name) {
        IService service = null;
        try {
            Class<?> cls = Class.forName(package_name + "." + service_name + "Proxy");
            service = (IService)cls.getConstructor(IChannel.class).newInstance(channel);
            assert service_name.equals(service.getName());
        }
        catch (Exception x) {
        }
        return service;
    }
}
