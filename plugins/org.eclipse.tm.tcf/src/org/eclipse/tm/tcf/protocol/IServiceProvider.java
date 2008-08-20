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
package org.eclipse.tm.tcf.protocol;

/**
 * Clients can implement this interface if they want to provide implementation of a local service or
 * remote service proxy.  
 */
public interface IServiceProvider {
    
    public IService[] getLocalService(IChannel channel);
    
    public IService getServiceProxy(IChannel channel, String service_name);
}
