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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import org.eclipse.tm.internal.tcf.services.remote.GenericProxy;
import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IService;
import org.eclipse.tm.tcf.protocol.IServiceProvider;

public class ServiceManager {

    private static final Collection<IServiceProvider> providers = new ArrayList<IServiceProvider>();

    static {
        addServiceProvider(new StandardServiceProvider());
    }

    public static void addServiceProvider(IServiceProvider provider) {
        providers.add(provider);
    }

    public static void removeServiceProvider(IServiceProvider provider) {
        providers.remove(provider);
    }
    
    public static void onChannelCreated(IChannel channel, Map<String,IService> services) {
        for (IServiceProvider provider : providers) {
            IService[] arr = provider.getLocalService(channel);
            if (arr == null) continue;
            for (IService service : arr) {
                if (services.containsKey(service.getName())) continue;
                services.put(service.getName(), service);
            }
        }
    }
    
    public static void onChannelOpened(IChannel channel, Collection<String> service_names, Map<String,IService> services) {
        for (String name : service_names) {
            for (IServiceProvider provider : providers) {
                IService service = provider.getServiceProxy(channel, name);
                if (service == null) continue;
                services.put(name, service);
                break;
            }
            if (!services.containsKey(name)) {
                services.put(name, new GenericProxy(channel, name));
            }
        }
    }
}
