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
package com.windriver.tcf.dsf.core.services;

import org.eclipse.dd.dsf.datamodel.AbstractDMContext;
import org.eclipse.dd.dsf.datamodel.IDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IContainerDMContext;
import org.eclipse.dd.dsf.debug.service.IRunControl.IExecutionDMContext;
import org.eclipse.dd.dsf.service.IDsfService;

public abstract class TCFDSFExecutionDMC extends AbstractDMContext implements IExecutionDMContext, IContainerDMContext {
    
    interface DataCache {
    }
    
    DataCache stack_frames_cache;
    DataCache memory_cache;
    DataCache registers_cache;
    
    TCFDSFExecutionDMC(IDsfService service, IDMContext[] parents) {
        super(service, parents);
    }
    
    /**
     * Get TCF ID of execution context.
     * @return TCF ID.
     */
    public abstract String getTcfContextId();
    
    /**
     * Check if this context object is disposed, because, for example, a thread has exited.
     * @return true if context object is disposed.
     */
    public abstract boolean isDisposed();
    
    /**
     * Validate execution state data.
     * @return true if state is valid, false if data retrieval is started.
     */
    public abstract boolean validateState();
    
    /**
     * Add a listener to be activated when state data retrieval is done. 
     * @param req - listener object.
     */
    public abstract void addStateWaitingRequest(IDataRequest req);
        
    /**
     * Get current program counter. This method must be called only when
     * execution state data is valid - when validateState() return true.
     * @return current program counter address.
     */
    public abstract TCFAddress getPC();
}
