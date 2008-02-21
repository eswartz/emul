/*******************************************************************************
 * Copyright (c) 2008 Wind River Systems, Inc. and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *  
 * Contributors:
 *     Wind River Systems - initial API and implementation
 *******************************************************************************/
package org.eclipse.tm.internal.tcf.dsf.services;

import java.util.ArrayList;
import java.util.Collection;

import org.eclipse.tm.tcf.protocol.IChannel;
import org.eclipse.tm.tcf.protocol.IToken;
import org.eclipse.tm.tcf.protocol.Protocol;


public abstract class TCFDataCache<V> {

    protected Throwable error;
    protected IToken command;
    protected boolean valid;
    protected V data;
    
    protected final IChannel channel;
    protected final Collection<IDataRequest> waiting_list = new ArrayList<IDataRequest>();
    
    public TCFDataCache(IChannel channel) {
        assert channel != null;
        this.channel = channel;
    }
    
    public void cancel() {
        // Cancel current data retrieval command
        if (command != null) {
            command.cancel();
            command = null;
        }
        // Cancel waiting data requests
        if (!waiting_list.isEmpty()) {
            IDataRequest[] arr = waiting_list.toArray(new IDataRequest[waiting_list.size()]);
            waiting_list.clear();
            for (IDataRequest r : arr) r.cancel();
        }
    }
    
    public boolean validate() {
        assert Protocol.isDispatchThread();
        if (channel.getState() != IChannel.STATE_OPEN) {
            error = null;
            command = null;
            data = null;
            valid = true;
            return true;
        }
        if (command != null) {
            return false;
        }
        if (!valid && !startDataRetrieval()) return false;
        assert command == null;
        if (!waiting_list.isEmpty()) {
            IDataRequest[] arr = waiting_list.toArray(new IDataRequest[waiting_list.size()]);
            waiting_list.clear();
            for (IDataRequest r : arr) r.done();
        }
        return true;
    }
    
    public void addWaitingRequest(IDataRequest req) {
        assert !valid;
        waiting_list.add(req);
    }
    
    public void reset(V data) {
        cancel();
        this.data = data;
        error = null;
        valid = true;
    }
    
    public void reset() {
        cancel();
        error = null;
        data = null;
        valid = false;
    }
    
    public boolean isValid() {
        return valid;
    }
    
    public Throwable getError() {
        assert valid;
        return error;
    }
    
    public V getData() {
        assert valid;
        return data;
    }
    
    public abstract boolean startDataRetrieval();
}
