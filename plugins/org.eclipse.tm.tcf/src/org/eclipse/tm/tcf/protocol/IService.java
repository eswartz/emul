/*******************************************************************************
 * Copyright (c) 2007, 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.tcf.protocol;

/**
 * Base interface for all service interfaces. A client can get list of available services
 * by calling IChannel.getLocalServices() and IChannel.getRemoteServices().
 * 
 * Remote services are represented by a proxy objects that implement service interfaces by
 * translating method calls to TCF messages and sending them to a remote peer.
 * When communication channel is open, TCF automatically creates proxies for standard services.
 * TCF clients can provides addition proxies for non-standard services by calling IChannel.setServiceProxy(). 
 */

public interface IService {

    /**
     * Get unique name of this service.
     * @return service name.
     */
    String getName();
}
