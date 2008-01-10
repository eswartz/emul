/*******************************************************************************
 * Copyright (c) 2007 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package com.windriver.tcf.api.protocol;

/**
 * Base interface for all service interfaces. A client can get list of available services
 * by calling Peer.getServices()
 */

public interface IService {

    /**
     * Get unique name of this service.
     * @return service name.
     */
    String getName();
}
